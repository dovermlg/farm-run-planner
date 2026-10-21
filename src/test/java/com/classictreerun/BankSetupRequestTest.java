package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class BankSetupRequestTest
{
    @Test public void equipmentIsOnTheLeftAndTheSameInventoryGridIsOnTheRight()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyBankView","LAYOUT");f.set("supplyEquipment",EquipmentSetupTest.gear().encode());
        f.set("supplyTools","true");List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.TROLL_HERB);
        SupplyPlan plan=SupplyPlan.forRoute(f.config,route,false,false);InventoryItemData data=InventoryFixtures.data(f.config,route);
        BankSetupRequest request=BankSetupRequest.create(f.config,plan,data,false);
        assertEquals(56,request.layout.length);assertEquals(ItemID.FAIRY_ENCHANTED_SECATEURS,request.layout[16]);
        assertEquals(ItemID.RING_OF_WEALTH_5,request.layout[34]);assertEquals(ItemID.BRONZE_ARROW,request.layout[10]);
        PackingLayout inventory=PackingLayout.build(plan,data);
        for(int i=0;i<inventory.cells.size();i++)
            assertEquals(plan.requirements.get(inventory.cells.get(i).requirement).item,request.layout[(i/4)*8+4+i%4]);
        assertEquals("Worn secateurs must not be duplicated in inventory",1,Arrays.stream(request.layout).filter(id->id==ItemID.FAIRY_ENCHANTED_SECATEURS).count());
        assertEquals(2,Arrays.stream(request.layout).filter(id->id==ItemID.PLANTPOT_OAK_SAPLING).count());
        assertTrue(request.items.contains(ItemID.RING_OF_WEALTH_1));
    }
    @Test public void notedPaymentsReferenceBankStockWhileSidebarUsesTheNoteSpriteAndOverflowIsBounded()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyBankView","LAYOUT");
        SupplyPlan plan=new SupplyPlan(List.of(new SupplyPlan.Requirement("Coconut",ItemID.COCONUT,25,true)),List.of());
        InventoryItemData data=new InventoryItemData(Map.of(ItemID.COCONUT,new InventoryItemData.ItemInfo(false,true,"Coconut",12345)));
        plan=plan.forInventory(true,data);assertEquals(12345,PackingLayout.build(plan,data).cells.get(0).item);
        BankSetupRequest request=BankSetupRequest.create(f.config,plan,data,false);
        assertEquals(ItemID.COCONUT,request.layout[4]);assertEquals(1,Arrays.stream(request.layout).filter(id->id>=0).count());
        plan=new SupplyPlan(List.of(new SupplyPlan.Requirement("Coconut",ItemID.COCONUT,1000,false)),List.of());
        request=BankSetupRequest.create(f.config,plan,data,false);
        assertEquals(28,Arrays.stream(request.layout).filter(id->id>=0).count());assertTrue(request.items.contains(ItemID.COCONUT));
    }
    @Test public void filterModeWorksWithoutMetadataWhileLayoutWaitsAndSignatureIncludesQuantitiesAndNames()
    {
        ConfigFixture f=InventoryFixtures.light();SupplyPlan plan=SupplyPlan.forRoute(f.config,List.of(Patch.LUMBRIDGE_TREE),false,false);
        BankSetupRequest request=BankSetupRequest.create(f.config,plan,InventoryItemData.EMPTY,false);
        assertEquals(0,request.layout.length);f.set("runName","Changed name");
        assertNotEquals(request.signature,BankSetupRequest.create(f.config,plan,InventoryItemData.EMPTY,false).signature);
        f.set("supplyBankView","LAYOUT");
        assertThrows(IllegalStateException.class,()->BankSetupRequest.create(f.config,plan,InventoryItemData.EMPTY,false));
    }
    @Test public void bankModeIsSavedAndBackedUpAndVersionFiveImportsGetTheDefaultLayout()
    {
        ConfigFixture f=new ConfigFixture();f.set("supplyBankView","FILTER");PresetLibrary library=new PresetLibrary(f);
        library.save("Mine",f.config);f.set("supplyBankView","OFF");library.load("Mine");assertEquals(ClassicTreeRunConfig.SetupBankView.FILTER,f.config.supplyBankView());
        String backup=PresetBackup.write(List.of(PresetBackup.capture("Mine",f.config)));
        assertEquals("FILTER",PresetBackup.read(backup).get(0).settings.get("supplyBankView"));
        JsonObject json=new JsonParser().parse(backup).getAsJsonObject();json.addProperty("version",5);
        JsonObject settings=json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings");settings.remove("supplyBankView");settings.remove("supplyInventory");
        assertEquals("LAYOUT",PresetBackup.read(json.toString()).get(0).settings.get("supplyBankView"));
        json.addProperty("version",6);assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        settings.addProperty("supplyBankView","INVALID");assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
    }
}
