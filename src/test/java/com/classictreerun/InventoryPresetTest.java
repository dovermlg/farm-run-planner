package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryPresetTest
{
    private static String export(ConfigFixture f)
    {return PresetBackup.write(List.of(PresetBackup.capture("My run",f.config)));}
    private static JsonObject entry(JsonObject json)
    {return json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings");}

    @Test public void savesBankCapacityAndNotedPaymentChoicesWithThePreset()
    {
        ConfigFixture f=new ConfigFixture();f.set("loadBank","CUSTOM");f.set("loadBankInstructions","Use my chosen bank.");
        f.set("loadReservedSlots","12");f.set("loadNotedProtection","false");
        new PresetLibrary(f).save("Custom banking",f.config);
        f.set("loadBank","GRAND_EXCHANGE");f.set("loadReservedSlots","0");new PresetLibrary(f).load("Custom banking");
        assertEquals(BankDestination.CUSTOM,f.config.loadBank());assertEquals(12,f.config.loadReservedSlots());assertFalse(f.config.loadNotedProtection());
        ClassicTreeRunConfig restored=new PresetLibrary(f).config("Custom banking");
        assertEquals("Use my chosen bank.",restored.loadBankInstructions());assertEquals(BankDestination.CUSTOM,restored.loadBank());
        String json=export(f);assertTrue(json.contains("\"version\": 7"));
        ConfigFixture imported=new ConfigFixture();new PresetSlots(imported).importPresets(PresetBackup.read(json));new PresetLibrary(imported).load("My run");
        assertEquals(12,imported.config.loadReservedSlots());assertEquals(BankDestination.CUSTOM,imported.config.loadBank());
    }

    @Test public void versionTwoBackupsGetInventoryDefaultsAndDoNotInheritTheCurrentDraft()
    {
        ConfigFixture f=new ConfigFixture();JsonObject json=new JsonParser().parse(export(f)).getAsJsonObject();
        for(String key:List.copyOf(entry(json).keySet()))if(key.startsWith("load"))entry(json).remove(key);
        for(String key:List.of("supplyTeleports","supplyTeleportMode","supplyFairyStaff","supplyEquipment","supplyBankView","supplyInventory"))entry(json).remove(key);
        json.addProperty("version",2);
        ConfigFixture target=new ConfigFixture();target.set("loadBank","CUSTOM");target.set("loadReservedSlots","27");
        new PresetSlots(target).importPresets(PresetBackup.read(json.toString()));new PresetLibrary(target).load("My run");
        assertEquals(BankDestination.GRAND_EXCHANGE,target.config.loadBank());assertEquals(8,target.config.loadReservedSlots());assertTrue(target.config.loadPlanning());
    }

    @Test public void rejectsUnknownBanksInvalidCapacitiesAndIncompleteInventorySettings()
    {
        for(String value:List.of("UNKNOWN","<html>"))
        {
            JsonObject json=new JsonParser().parse(export(new ConfigFixture())).getAsJsonObject();entry(json).addProperty("loadBank",value);
            assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        }
        for(Number value:List.of(-1,28,1.5,2147483648L))
        {
            JsonObject json=new JsonParser().parse(export(new ConfigFixture())).getAsJsonObject();entry(json).addProperty("loadReservedSlots",value);
            assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        }
        JsonObject json=new JsonParser().parse(export(new ConfigFixture())).getAsJsonObject();entry(json).remove("loadBank");
        assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
    }

    @Test public void bankStopProgressIsNotPartOfAPresetOrItsBackup()
    {
        ConfigFixture f=new ConfigFixture();f.set(InventoryLoadPlanner.BANKS_KEY,"local-bank-progress");
        new PresetLibrary(f).save("Mine",f.config);assertFalse(export(f).contains("local-bank-progress"));
        new PresetLibrary(f).load("Mine");assertEquals("",f.get(InventoryLoadPlanner.BANKS_KEY));
    }
}
