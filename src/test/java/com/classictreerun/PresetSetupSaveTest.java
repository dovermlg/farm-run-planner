package com.classictreerun;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class PresetSetupSaveTest
{
    private static SavedPackingSetup replacement()
    {return new SavedPackingSetup(BackpackSetupTest.backpack(),EquipmentSetupTest.gear(),ClassicTreeRunConfig.SetupBankView.FILTER);}
    @Test public void setupSavePatchesOnlySetupFieldsPreservingRouteDraftProgressBankMarksAndOtherPresets()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("runName","Mine");f.set("selectionV2","LUMBRIDGE_TREE,VARROCK_TREE");
        PresetLibrary library=new PresetLibrary(f);library.save("Mine",f.config);library.save("Other",f.config);
        SavedPackingSetup expected=SavedPackingSetup.read(library.config("Mine"));String other=library.config("Other").supplyInventory();
        f.set(ClassicTreeRunPanel.PROGRESS_KEY,"LUMBRIDGE_TREE");f.set(InventoryLoadPlanner.BANKS_KEY,"keep-bank-progress");f.set("cropTree","MAGIC");
        library.saveSetup("Mine",expected,replacement(),true);
        assertEquals(BackpackSetupTest.backpack().encode(),f.config.supplyInventory());assertEquals(CropChoices.Tree.MAGIC,f.config.cropTree());
        assertEquals(CropChoices.Tree.OAK,library.config("Mine").cropTree());assertEquals("LUMBRIDGE_TREE",f.get(ClassicTreeRunPanel.PROGRESS_KEY));
        assertEquals("keep-bank-progress",f.get(InventoryLoadPlanner.BANKS_KEY));assertEquals(other,library.config("Other").supplyInventory());
        assertEquals(List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE),library.patches("Mine"));
    }
    @Test public void staleSetupSavesAndDeletedPresetsCannotBeOverwritten()
    {
        ConfigFixture f=InventoryFixtures.light();PresetLibrary library=new PresetLibrary(f);library.save("Mine",f.config);
        SavedPackingSetup old=SavedPackingSetup.read(library.config("Mine"));library.saveSetup("Mine",old,replacement(),false);
        assertThrows(IllegalStateException.class,()->new PresetLibrary(f).saveSetup("Mine",old,old,true));
        assertEquals("",f.config.supplyInventory());library.delete("Mine");
        assertThrows(IllegalStateException.class,()->new PresetLibrary(f).saveSetup("Mine",replacement(),old,true));
    }
    @Test public void aLocalWriteFailureLeavesTheActiveSetupAndSavedLibraryUntouched()
    {
        ConfigFixture nativeSettings=InventoryFixtures.light(),local=new ConfigFixture();boolean[] fail={false};
        ClassicTreeRunPanel.Preferences disk=new ClassicTreeRunPanel.Preferences(){
            public String get(String key){return local.get(key);}
            public void set(String key,String value){if(fail[0])throw new IllegalStateException("disk failure");local.set(key,value);}
            public void update(Map<String,String> changes){if(fail[0])throw new IllegalStateException("disk failure");local.update(changes);}
        };
        PlannerPreferences combined=new PlannerPreferences(nativeSettings,disk);PresetLibrary library=new PresetLibrary(combined);library.save("Mine",nativeSettings.config);
        String before=local.get(PresetLibrary.KEY);SavedPackingSetup old=SavedPackingSetup.read(library.config("Mine"));fail[0]=true;
        assertThrows(IllegalStateException.class,()->library.saveSetup("Mine",old,replacement(),true));
        assertEquals(before,local.get(PresetLibrary.KEY));assertEquals("",nativeSettings.config.supplyInventory());assertEquals("",library.config("Mine").supplyInventory());
    }
}
