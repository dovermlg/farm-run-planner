package com.classictreerun;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class GuildBankMigrationTest
{
    @Test public void updatesOnlyTheOldBankAndPreservesPresetContentsAndProgress()
    {
        ConfigFixture f=new ConfigFixture();f.set("loadBank","GRAND_EXCHANGE");f.set("runName","Trees");
        f.set("selectionV2","LUMBRIDGE_TREE,GUILD_TREE,GUILD_FRUIT");f.set("cropTree","MAGIC");
        f.set("supplyInventory",BackpackSetupTest.backpack().encode());f.set("supplyEquipment",EquipmentSetupTest.gear().encode());
        f.set("guildTravel","FARMING_CAPE");
        PresetLibrary library=new PresetLibrary(f);library.save("Trees",f.config);
        f.set("loadBank","CUSTOM");f.set("loadBankInstructions","Use my bank.");library.save("Custom",f.config);
        f.set("loadBank","CASTLE_WARS");library.save("Castle",f.config);
        library.load("Trees");f.set(ClassicTreeRunPanel.PROGRESS_KEY,"LUMBRIDGE_TREE");
        Map<String,String> before=new LinkedHashMap<>(ConfigValues.read(library.config("Trees")));
        List<Patch> route=library.patches("Trees");InventoryItemData data=InventoryFixtures.data(f.config,route);
        InventoryLoadPlanner.Plan oldPlan=InventoryLoadPlanner.plan(f.config,route,data);
        f.set(InventoryLoadPlanner.BANKS_KEY,oldPlan.signature+":GUILD_TREE");String banks=f.get(InventoryLoadPlanner.BANKS_KEY);
        library.migrateGuildBankDefault();
        before.put("loadBank","FARMING_GUILD");
        assertEquals(before,ConfigValues.read(library.config("Trees")));
        assertEquals(BankDestination.CUSTOM,library.config("Custom").loadBank());assertEquals("Use my bank.",library.config("Custom").loadBankInstructions());
        assertEquals(BankDestination.CASTLE_WARS,library.config("Castle").loadBank());assertEquals(BankDestination.FARMING_GUILD,f.config.loadBank());
        assertEquals("LUMBRIDGE_TREE",f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertEquals(banks,f.get(InventoryLoadPlanner.BANKS_KEY));
        assertNotEquals(oldPlan.signature,InventoryLoadPlanner.signature(f.config,route,data));
        assertEquals("true",f.get(PresetLibrary.GUILD_BANK_DEFAULT));
    }

    @Test public void runsOnceSoPlayersCanChooseGrandExchangeAgain()
    {
        ConfigFixture f=new ConfigFixture();f.set("loadBank","GRAND_EXCHANGE");
        PresetLibrary library=new PresetLibrary(f);library.save("Trees",f.config);library.migrateGuildBankDefault();
        f.set("loadBank","GRAND_EXCHANGE");library.save("Trees",f.config);String saved=f.get(PresetLibrary.KEY);
        new PresetLibrary(f).migrateGuildBankDefault();
        assertEquals(saved,f.get(PresetLibrary.KEY));assertEquals(BankDestination.GRAND_EXCHANGE,f.config.loadBank());
        assertEquals(BankDestination.GRAND_EXCHANGE,new PresetLibrary(f).config("Trees").loadBank());
    }

    @Test public void failedLocalSavePreservesTheLibraryAndActiveBankAndCanBeRetried()
    {
        ConfigFixture f=new ConfigFixture();f.set("loadBank","GRAND_EXCHANGE");new PresetLibrary(f).save("Trees",f.config);
        Map<String,String> original=new LinkedHashMap<>(f.values);boolean[] fail={true};
        ClassicTreeRunPanel.Preferences local=new ClassicTreeRunPanel.Preferences(){
            public String get(String key){return f.get(key);}
            public void set(String key,String value){if(fail[0])throw new IllegalStateException("disk failure");f.set(key,value);}
            public void update(Map<String,String> changes){if(fail[0])throw new IllegalStateException("disk failure");f.update(changes);}
        };
        PlannerPreferences combined=new PlannerPreferences(f,local);
        assertThrows(IllegalStateException.class,()->new PresetLibrary(combined).migrateGuildBankDefault());
        assertEquals(original,f.values);
        fail[0]=false;new PresetLibrary(combined).migrateGuildBankDefault();
        assertEquals(BankDestination.FARMING_GUILD,f.config.loadBank());assertEquals("true",f.get(PresetLibrary.GUILD_BANK_DEFAULT));
    }
}
