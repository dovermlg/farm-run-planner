package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class EquipmentSetupTest
{
    static EquipmentSetup gear()
    {
        return new EquipmentSetup(Map.of(EquipmentInventorySlot.WEAPON,new EquipmentSetup.Entry(ItemID.FAIRY_ENCHANTED_SECATEURS,1),
            EquipmentInventorySlot.RING,new EquipmentSetup.Entry(ItemID.RING_OF_WEALTH_5,1),
            EquipmentInventorySlot.AMMO,new EquipmentSetup.Entry(ItemID.BRONZE_ARROW,500)));
    }
    static SupplySnapshot worn(EquipmentSetup setup)
    {
        Map<Integer,Long> counts=new java.util.HashMap<>();
        setup.entries.values().forEach(entry->counts.merge(entry.item,(long)entry.quantity,Long::sum));
        return new SupplySnapshot(true,counts,Map.of(),setup.entries);
    }

    @Test public void encodingIsBoundedDeterministicAndRejectsInvalidSlotsOrStackSizes()
    {
        String encoded=gear().encode();assertEquals(encoded,EquipmentSetup.parse(encoded).encode());
        assertTrue(encoded.startsWith("WEAPON:"));assertEquals(3,EquipmentSetup.parse(encoded).entries.size());
        for(String invalid:List.of("RING:1:1,RING:2:1","ARMS:1:1","RING:0:1","RING:1000001:1",
            "RING:1:2","RING:1:0","AMMO:1:2147483648","RING:1:1,","<html>","RING:-1:1"))
            assertThrows(invalid,IllegalArgumentException.class,()->EquipmentSetup.parse(invalid));
        assertThrows(IllegalArgumentException.class,()->EquipmentSetup.parse("RING:1:1".repeat(100)));
        assertTrue(EquipmentSetup.parse("").entries.isEmpty());
    }

    @Test public void jewelleryChargeVariantsCountOnlyInTheExpectedWornSlot()
    {
        EquipmentSetup setup=gear();EquipmentSetup differentCharge=new EquipmentSetup(Map.of(EquipmentInventorySlot.RING,
            new EquipmentSetup.Entry(ItemID.RING_OF_WEALTH_1,1)));
        assertEquals(1,setup.worn(EquipmentInventorySlot.RING,worn(differentCharge)));
        SupplySnapshot carried=new SupplySnapshot(true,Map.of(ItemID.RING_OF_WEALTH_5,1L),Map.of());
        assertEquals(0,setup.worn(EquipmentInventorySlot.RING,carried));
        assertEquals(0,setup.worn(EquipmentInventorySlot.RING,worn(new EquipmentSetup(Map.of(EquipmentInventorySlot.RING,
            new EquipmentSetup.Entry(ItemID.RING_OF_WEALTH,1))))));
        assertEquals(0,setup.worn(EquipmentInventorySlot.RING,worn(new EquipmentSetup(Map.of(EquipmentInventorySlot.WEAPON,
            new EquipmentSetup.Entry(ItemID.RING_OF_WEALTH_5,1))))));
    }

    @Test public void savedGearIsIndependentPerPresetAndOlderPresetsClearThePreviousGear()
    {
        ConfigFixture f=new ConfigFixture();PresetLibrary library=new PresetLibrary(f);
        f.set("supplyEquipment",gear().encode());f.set("packingView","GRID");library.save("Gear",f.config);
        f.set("supplyEquipment","");library.save("No gear",f.config);
        f.set("packingView","LIST");library.load("Gear");assertEquals(gear().encode(),f.config.supplyEquipment());
        assertEquals("Display choice is global",ClassicTreeRunConfig.PackingView.LIST,f.config.packingView());
        library.load("No gear");assertEquals("",f.config.supplyEquipment());
        java.util.Properties old=new java.util.Properties();old.setProperty("selectionV2","LUMBRIDGE_TREE");
        library.merge(Map.of("Old",old),Map.of());f.set("supplyEquipment",gear().encode());library.load("Old");
        assertEquals("",f.config.supplyEquipment());
    }

    @Test public void backupsPreserveGearRejectMalformedGearAndImportVersionFourWithNoGear()
    {
        ConfigFixture f=new ConfigFixture();f.set("supplyEquipment",gear().encode());
        String exported=PresetBackup.write(List.of(PresetBackup.capture("Mine",f.config)));
        assertTrue(exported.contains("\"version\": 7"));assertFalse(exported.contains("packingView"));
        assertFalse(exported.contains("captureEquipmentAction"));
        PresetBackup.Entry entry=PresetBackup.read(exported).get(0);assertEquals(gear().encode(),entry.settings.get("supplyEquipment"));
        JsonObject json=new JsonParser().parse(exported).getAsJsonObject();
        JsonObject settings=json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings");
        settings.addProperty("supplyEquipment","RING:1:1,RING:2:1");
        assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        settings.remove("supplyEquipment");settings.remove("supplyBankView");settings.remove("supplyInventory");json.addProperty("version",4);
        entry=PresetBackup.read(json.toString()).get(0);assertEquals("",entry.settings.get("supplyEquipment"));
        json.addProperty("version",6);assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
    }

    @Test public void gearIsInTheBankFilterWithoutBecomingExtraPlantingOrBackpackSupplies()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyEquipment",gear().encode());List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
        InventoryItemData data=InventoryFixtures.data(f.config,route);SupplyPlan supplies=InventoryLoadPlanner.supplies(f.config,route,data);
        assertEquals(1,InventoryLoadPlanner.slots(supplies,data));assertTrue(supplies.items.contains(ItemID.BRONZE_ARROW));
        assertTrue(supplies.items.contains(ItemID.RING_OF_WEALTH_1));
        SupplyBankFilter filter=new SupplyBankFilter();filter.start(supplies);
        assertTrue(filter.contains(ItemID.BRONZE_ARROW));assertFalse(filter.contains(ItemID.RANARR_SEED));
        assertTrue(SupplyPlan.forRoute(f.config,route,p->false,false,false,null).items.isEmpty());
    }

    @Test public void cogCaptureChangesOnlyTheDraftUntilSaveAndClearDoesNotChangeGameGear() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("autoOptimize","false");f.set("runName","My farming gear");
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);RunSettings settings=new RunSettings(f.config,f,panel);
            PresetActions actions=new PresetActions(f.config,f,settings,panel);
            try
            {
                SupplySnapshot snapshot=worn(gear());panel.setSupplies(snapshot);actions.apply("captureEquipmentAction");
                assertEquals(gear().encode(),f.config.supplyEquipment());assertNull(f.get(PresetLibrary.KEY));
                actions.apply("savePresetAction");assertEquals(gear().encode(),new PresetLibrary(f).config(f.config.runName()).supplyEquipment());
                actions.apply("clearEquipmentAction");assertEquals("",f.config.supplyEquipment());
                assertEquals(gear().encode(),new PresetLibrary(f).config(f.config.runName()).supplyEquipment());
                assertEquals(3,snapshot.worn.size());
                actions.apply("savePresetAction");assertEquals("",new PresetLibrary(f).config(f.config.runName()).supplyEquipment());
            }
            finally {settings.dispose();panel.dispose();}
        });
    }

    @Test public void unknownEmptyOrLateCaptureCannotOverwriteAnotherDraftOrDisposedPanel() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("supplyEquipment",gear().encode());
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            try
            {
                panel.captureEquipment();assertEquals(gear().encode(),f.config.supplyEquipment());
                panel.setSupplies(new SupplySnapshot(true,Map.of(),Map.of()));panel.captureEquipment();
                assertEquals(gear().encode(),f.config.supplyEquipment());
                List<Consumer<SupplySnapshot>> waiting=new ArrayList<>();panel.setEquipmentReader(waiting::add);
                panel.captureEquipment();f.set("runName","Different preset");waiting.get(0).accept(worn(gear()));
                assertEquals(gear().encode(),f.config.supplyEquipment());
                panel.captureEquipment();panel.clearEquipment();waiting.get(1).accept(worn(gear()));assertEquals("",f.config.supplyEquipment());
                panel.captureEquipment();panel.dispose();waiting.get(2).accept(worn(gear()));assertEquals("",f.config.supplyEquipment());
            }
            finally {panel.dispose();}
        });
    }
}
