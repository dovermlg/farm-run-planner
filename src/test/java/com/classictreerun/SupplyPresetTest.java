package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplyPresetTest
{
    @Test public void savedCropChoicesAreSeparateAndLoadingOldPresetsDoesNotLeakThePreviousCrop()
    {
        ConfigFixture f=new ConfigFixture();PresetLibrary library=new PresetLibrary(f);
        f.set("cropTree","MAGIC");f.set("cropHerb","TORSTOL");f.set("supplyCoins","12345");
        library.save("High",f.config);
        f.set("cropTree","WILLOW");f.set("cropHerb","RANARR");f.set("supplyProtection","false");
        library.save("Low",f.config);library.load("High");
        assertEquals(CropChoices.Tree.MAGIC,f.config.cropTree());assertEquals(CropChoices.Herb.TORSTOL,f.config.cropHerb());
        assertTrue(f.config.supplyProtection());
        assertEquals(12345,new PresetLibrary(f).config("High").supplyCoins());
        library.load("Low");assertEquals(CropChoices.Tree.WILLOW,f.config.cropTree());assertFalse(f.config.supplyProtection());
        // A library saved before crop settings existed should load defaults, not the previous run's choices.
        java.util.Properties old=new java.util.Properties();old.setProperty("runName","Old");old.setProperty("selectionV2","LUMBRIDGE_TREE");
        library.merge(Map.of("Old",old),Map.of());library.load("Old");
        assertEquals(CropChoices.Tree.OAK,f.config.cropTree());assertTrue(f.config.supplyProtection());
    }

    @Test public void backupVersionThreePreservesChosenCropsAndSupplySettings()
    {
        ConfigFixture f=new ConfigFixture();f.set("cropHerb","HUASCA");f.set("cropHardwood","ROSEWOOD");
        f.set("supplyCoins","32100");f.set("supplyDibber","false");f.set("supplyCompost","BOTTOMLESS");
        PresetBackup.Entry captured=PresetBackup.capture("Mine",f.config);
        String json=PresetBackup.write(List.of(captured));
        assertTrue(json.contains("\"version\": 3"));
        PresetBackup.Entry read=PresetBackup.read(json).get(0);assertTrue(captured.matches(read));
        ConfigFixture target=new ConfigFixture();new PresetSlots(target).importPresets(List.of(read));
        new PresetLibrary(target).load("Mine");assertEquals(CropChoices.Herb.HUASCA,target.config.cropHerb());
        assertEquals(32100,target.config.supplyCoins());assertFalse(target.config.supplyDibber());
        assertEquals(SupplyPlan.Compost.BOTTOMLESS,target.config.supplyCompost());
    }

    @Test public void versionOneBackupsRemainReadableEvenIfVersionComesLast()
    {
        JsonObject json=example();JsonObject settings=json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings");
        for(String key:List.copyOf(settings.keySet()))if(key.startsWith("crop")||key.startsWith("supply")||key.startsWith("load"))settings.remove(key);
        json.remove("version");json.addProperty("version",1);
        PresetBackup.Entry read=PresetBackup.read(json.toString()).get(0);
        assertEquals("OAK",read.settings.get("cropTree"));assertEquals("true",read.settings.get("supplyProtection"));
        assertEquals("10000",read.settings.get("supplyCoins"));
    }

    @Test public void rejectsUnknownCropsMalformedBudgetsAndIncompleteNewSettings()
    {
        for(String value:List.of("INVALID","RANARR_SEED","<html>"))
        {
            JsonObject json=example();settings(json).addProperty("cropHerb",value);
            assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        }
        for(Number value:List.of(-1,1.5,2147483648L))
        {
            JsonObject json=example();settings(json).addProperty("supplyCoins",value);
            assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
        }
        JsonObject missing=example();settings(missing).remove("cropTree");
        assertThrows(IllegalArgumentException.class,()->PresetBackup.read(missing.toString()));
        JsonObject mixed=example();mixed.addProperty("version",1);
        assertThrows(IllegalArgumentException.class,()->PresetBackup.read(mixed.toString()));
    }
    private static JsonObject example()
    { return new JsonParser().parse(PresetBackup.write(List.of(PresetBackup.capture("Example",new ClassicTreeRunConfig() { })))).getAsJsonObject(); }
    private static JsonObject settings(JsonObject json)
    { return json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings"); }
}
