package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class BackpackSetupTest
{
    static BackpackSetup backpack()
    {return BackpackSetup.parse("v1|0:"+ItemID.COINS+":10000:P;3:"+ItemID.PLANTPOT_OAK_SAPLING+":1:P;7:"+ItemID.POH_TABLET_VARROCKTELEPORT+":3:P;12:"+ItemID.COCONUT+":25:N;27:"+ItemID.SPADE+":1:P");}
    static SupplySnapshot snapshot(BackpackSetup backpack)
    {return new SupplySnapshot(true,Map.of(ItemID.COINS,10000L,ItemID.PLANTPOT_OAK_SAPLING,1L,ItemID.POH_TABLET_VARROCKTELEPORT,3L,ItemID.SPADE,1L),Map.of(ItemID.COCONUT,25L),EquipmentSetupTest.gear().entries,backpack);}
    static InventoryItemData data(java.util.Set<Integer> items)
    {
        Map<Integer,InventoryItemData.ItemInfo> values=new java.util.HashMap<>();
        for(int item:items)values.put(item,new InventoryItemData.ItemInfo(item==ItemID.COINS || item==ItemID.POH_TABLET_VARROCKTELEPORT,true,"Item "+item,item==ItemID.COCONUT?12345:-1));
        return new InventoryItemData(values);
    }
    @Test public void encodingPreservesGapsNotesAndQuantitiesAndDistinguishesAnEmptyCaptureFromNoCapture()
    {
        BackpackSetup setup=backpack();assertEquals(5,setup.occupied());assertEquals(28,setup.slots.size());assertNull(setup.slots.get(1));
        assertTrue(setup.slots.get(12).noted);assertEquals(25,setup.slots.get(12).quantity);
        assertEquals(setup.encode(),BackpackSetup.parse(setup.encode()).encode());assertTrue(BackpackSetup.parse("v1|").captured);
        assertFalse(BackpackSetup.parse("").captured);assertThrows(UnsupportedOperationException.class,()->setup.slots.clear());
    }
    @Test public void rejectsInvalidSlotsDuplicatesOverflowInvalidFormsAndTrailingRows()
    {
        for(String value:List.of("v2|","v1|28:1:1:P","v1|-1:1:1:P","v1|0:1:1:P;0:2:1:P","v1|0:0:1:P",
            "v1|0:1000001:1:P","v1|0:1:0:P","v1|0:1:2147483648:P","v1|0:1:1:X","v1|0:1:1:P;","v1|"+"x".repeat(2050)))
            assertThrows(value,IllegalArgumentException.class,()->BackpackSetup.parse(value));
    }
    @Test public void capturedBankLayoutKeepsExactSlotsAndExcludesGeneratedItemsNotInTheCapture()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyTeleports","true");f.set("supplyInventory",backpack().encode());f.set("supplyEquipment",EquipmentSetupTest.gear().encode());f.set("supplyBankView","LAYOUT");
        SupplyPlan requirements=SupplyPlan.forRoute(f.config,List.of(Patch.CATHERBY_FRUIT),false,false);
        BankSetupRequest request=BankSetupRequest.create(f.config,requirements,InventoryItemData.EMPTY,false);
        assertEquals(ItemID.COINS,request.layout[4]);assertEquals(-1,request.layout[5]);assertEquals(ItemID.PLANTPOT_OAK_SAPLING,request.layout[7]);
        assertEquals(ItemID.POH_TABLET_VARROCKTELEPORT,request.layout[15]);assertEquals(ItemID.COCONUT,request.layout[28]);assertEquals(ItemID.SPADE,request.layout[55]);
        assertEquals(ItemID.FAIRY_ENCHANTED_SECATEURS,request.layout[16]);assertTrue(request.items.contains(ItemID.RING_OF_WEALTH_1));
        assertFalse(request.items.contains(ItemID.PLANTPOT_APPLE_SAPLING));assertTrue(requirements.items.contains(ItemID.PLANTPOT_APPLE_SAPLING));
        assertTrue(requirements.items.contains(ItemID.LUNAR_TABLET_CATHERBY_TELEPORT));assertFalse(request.items.contains(ItemID.LUNAR_TABLET_CATHERBY_TELEPORT));
        assertEquals(request.signature,BankSetupRequest.create(f.config,SupplyPlan.forRoute(f.config,List.of(Patch.LUMBRIDGE_TREE),false,false),InventoryItemData.EMPTY,false).signature);
    }
    @Test public void slotChecksDetectReorderingNotesShortStacksAndExtraItemsInSavedEmptySlots() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            BackpackSetup expected=backpack();List<Integer> icons=new java.util.ArrayList<>();
            BackpackSetupView view=new BackpackSetupView(expected,data(expected.items()),true,snapshot(expected),(id,label)->icons.add(id));
            assertTrue(icons.contains(12345));assertEquals(PackingItemSlot.PACKED,find(view,"capturedSlot_12").getBackground());
            BackpackSetup changed=BackpackSetup.parse("v1|1:"+ItemID.COINS+":10000:P;3:"+ItemID.PLANTPOT_OAK_SAPLING+":1:P;7:"+ItemID.POH_TABLET_VARROCKTELEPORT+":2:P;12:"+ItemID.COCONUT+":25:P;27:"+ItemID.SPADE+":1:P");
            view.refresh(snapshot(changed));
            for(int slot:List.of(0,1,7,12))assertEquals(PackingItemSlot.MISSING,find(view,"capturedSlot_"+slot).getBackground());
            assertEquals(PackingItemSlot.PACKED,find(view,"capturedSlot_3").getBackground());
            assertTrue(find(view,"capturedPackingStatus").getText().contains("4 backpack slots"));
            view.refresh(SupplySnapshot.UNKNOWN);assertEquals(PackingItemSlot.UNKNOWN,find(view,"capturedSlot_3").getBackground());
        });
    }
    @Test public void backupsPreserveOnlyExplicitSavedCapturesAndVersionSixImportsDefaultToCalculatedSupplies()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyInventory",backpack().encode());
        String backup=PresetBackup.write(List.of(PresetBackup.capture("Mine",f.config)));assertTrue(backup.contains("\"version\": 7"));
        assertEquals(backpack().encode(),PresetBackup.read(backup).get(0).settings.get("supplyInventory"));
        JsonObject json=new JsonParser().parse(backup).getAsJsonObject();JsonObject settings=json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings");
        settings.remove("supplyInventory");json.addProperty("version",6);assertEquals("",PresetBackup.read(json.toString()).get(0).settings.get("supplyInventory"));
        json.addProperty("version",7);assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        settings.addProperty("supplyInventory","v1|99:1:1:P");assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
    }
    private static JLabel find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {if(name.equals(child.getName()))return (JLabel)child;if(child instanceof Container){JLabel result=find((Container)child,name);if(result!=null)return result;}}
        return null;
    }
}
