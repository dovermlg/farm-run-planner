package com.classictreerun;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlannerPreferencesTest
{
    @Test public void nativeCogSettingsAndLocalSnapshotsAreStoredSeparately()
    {
        ConfigFixture nativeValues = new ConfigFixture(); ConfigFixture local = new ConfigFixture();
        PlannerPreferences combined = new PlannerPreferences(nativeValues, local);
        combined.set("runName", "My herbs"); combined.set("brimhavenTravel", "CATHERBY_CHARTER");
        combined.set(PresetLibrary.KEY, "private snapshots"); combined.set(ClassicTreeRunPanel.PROGRESS_KEY, "TROLL_HERB");
        combined.set("growthDataV1.anonymous", "observations"); combined.set("growthNoticeV1.anonymous.run", "notified");
        assertNull(nativeValues.get("growthDataV1.anonymous")); assertNull(nativeValues.get("growthNoticeV1.anonymous.run"));
        assertEquals("observations",local.get("growthDataV1.anonymous"));
        assertEquals("My herbs", nativeValues.get("runName")); assertNull(local.get("runName"));
        assertEquals("private snapshots", local.get(PresetLibrary.KEY)); assertNull(nativeValues.get(PresetLibrary.KEY));
        assertEquals("TROLL_HERB", local.get(ClassicTreeRunPanel.PROGRESS_KEY)); assertNull(nativeValues.get(ClassicTreeRunPanel.PROGRESS_KEY));
        assertEquals("CATHERBY_CHARTER", combined.get("brimhavenTravel"));
    }

    @Test public void savedLocalSnapshotLoadsBackIntoNativeCogValues()
    {
        ConfigFixture nativeValues = new ConfigFixture(); ConfigFixture local = new ConfigFixture();
        PlannerPreferences combined = new PlannerPreferences(nativeValues, local);
        PresetLibrary library = new PresetLibrary(combined);
        combined.set("selectionV2", "TROLL_HERB"); combined.set("patch_TROLL_HERB", "true");
        combined.set("brimhavenTravel", "CATHERBY_CHARTER"); library.save("Herbs of mine", nativeValues.config);
        combined.set("selectionV2", "CORAL_E"); combined.set("patch_TROLL_HERB", "false");
        combined.set("brimhavenTravel", "SPIRIT_TREE");
        library.load("Herbs of mine");
        assertTrue(nativeValues.config.includeTROLL_HERB()); assertEquals("TROLL_HERB", nativeValues.config.selection());
        assertEquals(ClassicTreeRunConfig.BrimhavenTravel.CATHERBY_CHARTER, nativeValues.config.brimhavenTravel());
        assertNull(nativeValues.get(PresetLibrary.KEY)); assertNotNull(local.get(PresetLibrary.KEY));
    }

    @Test public void oneTimeImportDoesNotOverwriteLaterNativeEdits()
    {
        ConfigFixture nativeValues = new ConfigFixture(); ConfigFixture local = new ConfigFixture();
        local.set("runName", "Old local run"); local.set("patch_TROLL_HERB", "true");
        PlannerPreferences combined = new PlannerPreferences(nativeValues, local); combined.importLocalWorkingRun();
        combined.initializeCogActions();
        assertEquals("false", nativeValues.get("savePresetAction"));
        assertEquals("false", nativeValues.get("deletePresetAction"));
        nativeValues.set("savePresetAction", "true"); combined.initializeCogActions();
        assertEquals("true", nativeValues.get("savePresetAction"));
        assertEquals("Old local run", nativeValues.config.runName()); assertTrue(nativeValues.config.includeTROLL_HERB());
        combined.set("runName", "New cog run"); combined.importLocalWorkingRun();
        assertEquals("New cog run", nativeValues.config.runName());
    }

    @Test public void starterSeedingKeepsCustomNamesAndDoesNotRestoreDeletedDefaults()
    {
        ConfigFixture values = new ConfigFixture(); PresetLibrary library = new PresetLibrary(values);
        library.save("Trees", values.config); String custom = values.get(PresetLibrary.KEY);
        library.seedStarters(values.config);
        assertEquals(3, library.names().size());
        library.load("Trees"); assertEquals(16, RouteState.active(values.config).size());
        library.delete("Herbs"); new PresetLibrary(values).seedStarters(values.config);
        assertFalse(new PresetLibrary(values).contains("Herbs"));
    }

    @Test public void localFailurePreventsApplyingNativePartOfSnapshot()
    {
        ConfigFixture nativeValues = new ConfigFixture();
        ClassicTreeRunPanel.Preferences broken = new ClassicTreeRunPanel.Preferences() {
            public String get(String key) { return null; }
            public void set(String key, String value) { throw new IllegalStateException("disk failure"); }
        };
        PlannerPreferences combined = new PlannerPreferences(nativeValues, broken);
        Map<String, String> changes = new LinkedHashMap<>(); changes.put("runName", "Do not apply"); changes.put(PresetLibrary.KEY, "snapshot");
        try { combined.update(changes); fail("Expected local failure"); } catch (IllegalStateException expected) { }
        assertNull(nativeValues.get("runName"));
    }
}
