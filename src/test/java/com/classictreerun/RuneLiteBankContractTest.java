package com.classictreerun;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.banktags.BankTag;
import net.runelite.client.plugins.banktags.TagManager;
import net.runelite.client.plugins.banktags.tabs.Layout;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exercises RuneLite's actual item canonicalizer and custom-tag registry without a game client. */
public class RuneLiteBankContractTest
{
    private static final int NOTE=999_998,PLACEHOLDER=999_997;

    private static ItemManager itemManager() throws Exception
    {
        Client client=(Client)Proxy.newProxyInstance(Client.class.getClassLoader(),new Class<?>[]{Client.class},(p,m,a)->{
            if("getItemDefinition".equals(m.getName()))return definition((Integer)a[0]);
            throw new AssertionError("Unexpected client access: "+m.getName());
        });
        // The constructor registers price jobs. Discard them: no threads, prices or HTTP calls run.
        ScheduledExecutorService executor=(ScheduledExecutorService)Proxy.newProxyInstance(
            ScheduledExecutorService.class.getClassLoader(),new Class<?>[]{ScheduledExecutorService.class},(p,m,a)->{
                if("scheduleWithFixedDelay".equals(m.getName()) || "submit".equals(m.getName()))return null;
                throw new AssertionError("Unexpected background task: "+m.getName());
            });
        Constructor<?> constructor=ItemManager.class.getDeclaredConstructors()[0];constructor.setAccessible(true);
        Object[] args=new Object[constructor.getParameterCount()];
        Class<?>[] types=constructor.getParameterTypes();
        for(int i=0;i<types.length;i++)
        {
            if(types[i]==Client.class)args[i]=client;
            else if(types[i]==ScheduledExecutorService.class)args[i]=executor;
            else if(types[i]==EventBus.class)args[i]=new EventBus();
        }
        return (ItemManager)constructor.newInstance(args);
    }
    private static ItemComposition definition(int id)
    {
        return (ItemComposition)Proxy.newProxyInstance(ItemComposition.class.getClassLoader(),new Class<?>[]{ItemComposition.class},(p,m,a)->{
            switch(m.getName())
            {
                case "getNote":return id==NOTE?799:-1;
                case "getLinkedNoteId":return id==NOTE?ItemID.COINS:-1;
                case "getPlaceholderTemplateId":return id==PLACEHOLDER?14401:-1;
                case "getPlaceholderId":return id==PLACEHOLDER?ItemID.GRACEFUL_HOOD:-1;
                default:throw new AssertionError("Unexpected item-definition access: "+m.getName());
            }
        });
    }
    private static ItemContainer container(Item... items)
    {
        return (ItemContainer)Proxy.newProxyInstance(ItemContainer.class.getClassLoader(),new Class<?>[]{ItemContainer.class},(p,m,a)->{
            if("getItems".equals(m.getName()))return items;
            throw new AssertionError("Unexpected container access: "+m.getName());
        });
    }

    @Test public void bankLayoutAndMembershipUseRuneLitesRealWornItemMappingWithoutBroadeningChargeRules() throws Exception
    {
        ItemManager items=itemManager();
        ConfigFixture f=InventoryFixtures.light();f.set("supplyBankView","LAYOUT");
        f.set("supplyEquipment",new EquipmentSetup(Map.of(
            EquipmentInventorySlot.HEAD,new EquipmentSetup.Entry(ItemID.GRACEFUL_HOOD_WORN,1),
            EquipmentInventorySlot.BOOTS,new EquipmentSetup.Entry(ItemID.IKOV_BOOTSOFLIGHTNESSWORN,1))).encode());
        List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
        BankSetupRequest original=BankSetupRequest.create(f.config,SupplyPlan.forRoute(f.config,route,false,false),InventoryFixtures.data(f.config,route),true);
        BankSetupRequest normalized=original.normalized(items::canonicalize);
        Layout layout=new Layout(SetupBankController.TAG,normalized.layout.clone());
        assertEquals(ItemID.GRACEFUL_HOOD,layout.getItemAtPos(1));assertEquals(ItemID.IKOV_BOOTSOFLIGHTNESS,layout.getItemAtPos(33));
        assertTrue(normalized.items.contains(ItemID.GRACEFUL_HOOD));assertFalse(normalized.items.contains(ItemID.GRACEFUL_HOOD_WORN));
        assertEquals("Normalization must not mutate an EDT request",ItemID.GRACEFUL_HOOD_WORN,original.layout[1]);
        assertEquals(ItemID.COINS,items.canonicalize(NOTE));assertEquals(ItemID.GRACEFUL_HOOD,items.canonicalize(PLACEHOLDER));
        assertEquals(ItemID.RING_OF_WEALTH_1,items.canonicalize(ItemID.RING_OF_WEALTH_1));
        assertFalse(TeleportDevice.WEALTH.matches(ItemID.RING_OF_WEALTH));
    }

    @Test public void actualRuneLiteSnapshotSupportsNewCapturesAndPreviouslySavedWornIdsWithoutCountingPlaceholders() throws Exception
    {
        ItemManager items=itemManager();Item[] worn=new Item[14];Arrays.fill(worn,new Item(-1,0));
        worn[EquipmentInventorySlot.HEAD.getSlotIdx()]=new Item(ItemID.GRACEFUL_HOOD_WORN,1);
        SupplySnapshot snapshot=SupplySnapshot.read(items,container(new Item(NOTE,25),new Item(PLACEHOLDER,1)),container(worn));
        assertEquals(Map.of(ItemID.GRACEFUL_HOOD,1L),snapshot.plain);assertEquals(Map.of(ItemID.COINS,25L),snapshot.notes);
        EquipmentSetup captured=new EquipmentSetup(snapshot.worn);
        assertEquals(ItemID.GRACEFUL_HOOD,captured.entries.get(EquipmentInventorySlot.HEAD).item);
        assertEquals(1,captured.worn(EquipmentInventorySlot.HEAD,snapshot));
        EquipmentSetup old=new EquipmentSetup(Map.of(EquipmentInventorySlot.HEAD,new EquipmentSetup.Entry(ItemID.GRACEFUL_HOOD_WORN,1)));
        assertEquals(1,old.worn(EquipmentInventorySlot.HEAD,snapshot));
        assertEquals(0,old.worn(EquipmentInventorySlot.HEAD,SupplySnapshot.read(items,container(new Item(ItemID.GRACEFUL_HOOD,1)),container())));
    }

    @Test public void realCustomTagRegistrationIsIsolatedAndSupportsBankNotesAndPlaceholders() throws Exception
    {
        ItemManager items=itemManager();Constructor<?> constructor=TagManager.class.getDeclaredConstructors()[0];constructor.setAccessible(true);
        TagManager tags=(TagManager)constructor.newInstance(items,null);
        Method lookup=TagManager.class.getDeclaredMethod("findTag",String.class);lookup.setAccessible(true);
        SupplyBankFilter filter=new SupplyBankFilter();filter.start(java.util.Set.of(ItemID.GRACEFUL_HOOD,ItemID.COINS));
        BankTag wanted=id->id>=0 && filter.contains(items.canonicalize(id)),other=id->true;
        tags.registerTag("other-plugin",other);tags.registerTag(SetupBankController.TAG,wanted);
        BankTag registered=(BankTag)lookup.invoke(tags,SetupBankController.TAG);
        assertTrue(registered.contains(ItemID.GRACEFUL_HOOD));assertTrue(registered.contains(ItemID.GRACEFUL_HOOD_WORN));
        assertTrue(registered.contains(NOTE));assertTrue(registered.contains(PLACEHOLDER));assertFalse(registered.contains(ItemID.RANARR_SEED));
        tags.unregisterTag(SetupBankController.TAG);assertNull(lookup.invoke(tags,SetupBankController.TAG));
        assertSame(other,lookup.invoke(tags,"other-plugin"));
    }
}
