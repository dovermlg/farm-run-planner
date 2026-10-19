package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigItem;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class PresetTransferTest
{
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private Path backupDirectory() { return temporary.getRoot().toPath().resolve("preset-backups"); }

    private PresetTransfer transfer(ConfigFixture fixture)
    { return new PresetTransfer(TestFiles.at(backupDirectory()), fixture); }

    private static void save(ConfigFixture fixture, String name, String patches)
    {
        fixture.nativeSelection(RouteState.parse(patches));
        fixture.set("selectionV2", patches.isEmpty() ? "-" : patches);
        fixture.set("excluded", "");
        fixture.set("order", patches);
        fixture.set("runName", name);
        new PresetLibrary(fixture).save(name, fixture.config);
    }

    private static List<PresetBackup.Entry> entries(ConfigFixture fixture)
    {
        PresetLibrary library = new PresetLibrary(fixture);
        List<PresetBackup.Entry> result = new ArrayList<>();
        for (String name : library.names()) { result.add(PresetBackup.capture(name, library.config(name))); }
        return result;
    }

    private static String example()
    {
        ConfigFixture fixture = new ConfigFixture();
        save(fixture, "Herbs", "TROLL_HERB");
        return PresetBackup.write(entries(fixture));
    }

    @Test public void exportUsesSavedSnapshotsAndExcludesCharacterAndDraftData() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        source.set("brimhavenTravel", "CATHERBY_CHARTER");
        source.set("autoOptimize", "false");
        save(source, "Après work / herbs", "WEISS_HERB,TROLL_HERB");
        save(source, "Empty", "");
        source.set("runName", "Unsaved draft");
        source.set("selectionV2", "CORAL_E");
        source.set(ClassicTreeRunPanel.PROGRESS_KEY, "TROLL_HERB");
        source.set("growthDataV1.private-character", "private-crop-record");
        source.set("growthNoticeV1.private-character", "private-reminder-record");
        source.set("exportPresetsAction", "true");
        source.set("backupFilename", "private-filename.json");
        String status = transfer(source).exportPresets("presets.json");
        String json = Files.readString(backupDirectory().resolve("presets.json"));
        assertTrue(status.contains("Exported 2"));
        for (String excluded : new String[]{"Unsaved draft", "private-character", "private-crop-record",
            "private-reminder-record", "completedV1", "editorSlotsV1", "exportPresetsAction", "backupFilename"})
        { assertFalse(excluded, json.contains(excluded)); }
        List<PresetBackup.Entry> decoded = PresetBackup.read(json);
        assertEquals(2, decoded.size());
        assertEquals(List.of(Patch.WEISS_HERB, Patch.TROLL_HERB), decoded.get(0).patches);
        assertEquals("CATHERBY_CHARTER", decoded.get(0).settings.get("brimhavenTravel"));
        assertTrue(decoded.get(1).patches.isEmpty());
        assertTrue(new PresetLibrary(source).contains("Après work / herbs"));
    }

    @Test public void importRestoresNamesOrderAndSettingsWithoutChangingTheWorkingRun() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        source.set("autoOptimize", "false");
        source.set("showDetails", "true");
        source.set("hideCompleted", "true");
        source.set("brimhavenTravel", "CATHERBY_CHARTER");
        save(source, "My herbs 🌿", "WEISS_HERB,TROLL_HERB");
        save(source, "Empty", "");
        transfer(source).exportPresets("presets.json");
        ConfigFixture target = new ConfigFixture();
        save(target, "Current trees", "LUMBRIDGE_TREE");
        target.set(ClassicTreeRunPanel.PROGRESS_KEY, "LUMBRIDGE_TREE");
        target.set("growthDataV1.test-character", "existing-observations");
        target.set("notifyWhenReady", "false");
        PresetSlots slots = new PresetSlots(target);
        Map<String, String> before = new HashMap<>(target.values);
        assertTrue(transfer(target).importPresets("presets.json", slots).contains("Imported 2"));
        PresetLibrary library = new PresetLibrary(target);
        assertEquals(Set.of("Current trees", "My herbs 🌿", "Empty"), library.names());
        assertEquals(List.of(Patch.WEISS_HERB, Patch.TROLL_HERB), library.patches("My herbs 🌿"));
        assertTrue(library.patches("Empty").isEmpty());
        assertEquals(ClassicTreeRunConfig.BrimhavenTravel.CATHERBY_CHARTER, library.config("My herbs 🌿").brimhavenTravel());
        assertFalse(library.config("My herbs 🌿").autoOptimize());
        assertTrue(library.config("My herbs 🌿").showDetails());
        assertTrue(library.config("My herbs 🌿").hideCompleted());
        assertNotEquals(EditorPreset.CURRENT, slots.find("My herbs 🌿"));
        before.forEach((key, value) -> {
            if (!PresetLibrary.KEY.equals(key) && !PresetSlots.KEY.equals(key)) { assertEquals(key, value, target.get(key)); }
        });
        ConfigFixture restarted = new ConfigFixture();
        restarted.values.putAll(target.values);
        assertEquals(slots.find("My herbs 🌿"), new PresetSlots(restarted).find("My herbs 🌿"));
        assertEquals(library.patches("My herbs 🌿"), new PresetLibrary(restarted).patches("My herbs 🌿"));
    }

    @Test public void importingAnUnchangedBackupAgainDoesNotDuplicateOrRewritePresets() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        save(source, "Herbs", "TROLL_HERB");
        transfer(source).exportPresets("presets.json");
        ConfigFixture target = new ConfigFixture();
        PresetSlots slots = new PresetSlots(target);
        transfer(target).importPresets("presets.json", slots);
        Map<String, String> before = new HashMap<>(target.values);
        assertTrue(transfer(target).importPresets("presets.json", slots).contains("1 identical copies skipped"));
        assertEquals(before, target.values);
    }

    @Test public void conflictingNamesAreRenamedWithoutTakingAnotherIncomingName() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        save(source, "Herbs", "TROLL_HERB");
        save(source, "Herbs (imported)", "WEISS_HERB");
        transfer(source).exportPresets("presets.json");
        ConfigFixture target = new ConfigFixture();
        save(target, "Herbs", "LUMBRIDGE_TREE");
        assertTrue(transfer(target).importPresets("presets.json", new PresetSlots(target)).contains("1 renamed"));
        PresetLibrary library = new PresetLibrary(target);
        assertEquals(List.of(Patch.LUMBRIDGE_TREE), library.patches("Herbs"));
        assertEquals(List.of(Patch.TROLL_HERB), library.patches("Herbs (imported 2)"));
        assertEquals(List.of(Patch.WEISS_HERB), library.patches("Herbs (imported)"));
    }

    @Test public void repeatedConflictingImportRecognizesThePreviouslyRenamedCopy() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        save(source, "Herbs", "TROLL_HERB");
        transfer(source).exportPresets("presets.json");
        ConfigFixture target = new ConfigFixture();
        save(target, "Herbs", "WEISS_HERB");
        PresetSlots slots = new PresetSlots(target);
        transfer(target).importPresets("presets.json", slots);
        Map<String, String> before = new HashMap<>(target.values);
        assertTrue(transfer(target).importPresets("presets.json", slots).contains("1 identical copies skipped"));
        assertEquals(before, target.values);
        assertEquals(Set.of("Herbs", "Herbs (imported)"), new PresetLibrary(target).names());
    }

    @Test public void reexportReplacesTheBackupWithTheLatestSavedSettings() throws Exception
    {
        ConfigFixture fixture = new ConfigFixture();
        save(fixture, "Herbs", "TROLL_HERB");
        PresetTransfer transfer = transfer(fixture);
        transfer.exportPresets("presets.json");
        save(fixture, "Herbs", "WEISS_HERB");
        transfer.exportPresets("presets.json");
        List<PresetBackup.Entry> result = PresetBackup.read(Files.readString(backupDirectory().resolve("presets.json")));
        assertEquals(List.of(Patch.WEISS_HERB), result.get(0).patches);
        try (var files = Files.list(backupDirectory())) { assertEquals(1, files.count()); }
    }

    @Test public void documentedExampleIsAValidPortableBackup() throws Exception
    {
        List<PresetBackup.Entry> example = PresetBackup.read(Files.readString(Path.of("docs/preset-backup-example.json")));
        assertEquals(2, example.size());
        assertEquals("Classic trees", example.get(0).name);
        assertEquals(16, example.get(0).patches.size());
        assertEquals(List.of(Patch.TROLL_HERB, Patch.WEISS_HERB), example.get(1).patches);
    }

    @Test public void renamedLongUnicodeNamesStayValid() throws Exception
    {
        String name = "x".repeat(48) + "🌿" + "x".repeat(10);
        ConfigFixture source = new ConfigFixture();
        save(source, name, "TROLL_HERB");
        transfer(source).exportPresets("presets.json");
        ConfigFixture target = new ConfigFixture();
        save(target, name, "WEISS_HERB");
        transfer(target).importPresets("presets.json", new PresetSlots(target));
        for (String saved : new PresetLibrary(target).names())
        {
            assertEquals(saved, PresetLibrary.validName(saved));
            assertFalse(saved.contains("\ud83c (") || saved.contains("\udf3f ("));
        }
    }

    @Test public void insufficientCapacityCancelsTheWholeImport() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        save(source, "New one", "TROLL_HERB");
        save(source, "New two", "WEISS_HERB");
        transfer(source).exportPresets("presets.json");
        ConfigFixture target = new ConfigFixture();
        for (int i = 0; i < 31; i++) { save(target, "Saved " + i, "LUMBRIDGE_TREE"); }
        PresetSlots slots = new PresetSlots(target);
        Map<String, String> before = new HashMap<>(target.values);
        assertThrows(IllegalArgumentException.class, () -> transfer(target).importPresets("presets.json", slots));
        assertEquals(before, target.values);
        assertEquals(EditorPreset.CURRENT, slots.find("New one"));
        assertEquals(31, new PresetLibrary(target).names().size());
    }

    @Test public void failedLocalSaveLeavesLibraryAndSlotLabelsUntouched() throws Exception
    {
        ConfigFixture target = new ConfigFixture();
        save(target, "Existing", "WEISS_HERB");
        PresetSlots initial = new PresetSlots(target);
        ClassicTreeRunPanel.Preferences failing = new ClassicTreeRunPanel.Preferences()
        {
            public String get(String key) { return target.get(key); }
            public void set(String key, String value) { throw new IllegalStateException("Disk full"); }
            public void update(Map<String, String> changes) { throw new IllegalStateException("Disk full"); }
        };
        PresetSlots slots = new PresetSlots(failing);
        Map<String, String> before = new HashMap<>(target.values);
        assertThrows(IllegalStateException.class, () -> slots.importPresets(PresetBackup.read(example())));
        assertEquals(before, target.values);
        assertEquals(initial.find("Existing"), slots.find("Existing"));
        assertEquals(EditorPreset.CURRENT, slots.find("Herbs"));
    }

    @Test public void invalidEntryAfterAValidOneMakesNoChanges() throws Exception
    {
        ConfigFixture source = new ConfigFixture();
        save(source, "One", "TROLL_HERB");
        save(source, "Two", "WEISS_HERB");
        transfer(source).exportPresets("presets.json");
        Path file = backupDirectory().resolve("presets.json");
        Files.writeString(file, Files.readString(file).replace("WEISS_HERB", "NOT_A_PATCH"));
        ConfigFixture target = new ConfigFixture();
        PresetSlots slots = new PresetSlots(target);
        Map<String, String> before = new HashMap<>(target.values);
        assertThrows(IllegalArgumentException.class, () -> transfer(target).importPresets("presets.json", slots));
        assertEquals(before, target.values);
    }

    @Test public void parserRejectsInvalidTypesUnknownOptionsAndManagementSettings()
    {
        for (String key : new String[]{"trackGrowth", "savePresetAction", "importPresetsAction", "backupFilename", "completedV1"})
        {
            JsonObject json = new JsonParser().parse(example()).getAsJsonObject();
            json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings").addProperty(key, true);
            assertThrows(key, IllegalArgumentException.class, () -> PresetBackup.read(json.toString()));
        }
        for (String invalid : new String[]{
            example().replace("\"version\": 1", "\"version\": 2"),
            example().replace("\"format\":", "\"format\": \"farm-run-planner-presets\", \"format\":"),
            example().replace("\"autoOptimize\": true", "\"autoOptimize\": \"true\""),
            example().replace("\"TROLL_HERB\"", "\"TROLL_HERB\", \"TROLL_HERB\""),
            example().replace("\"CATHERBY_TELEPORT\"", "\"INVALID_TELEPORT\""),
            example() + "{}", "{}", "null", "[]", "/* comment */" + example()})
        { assertThrows(IllegalArgumentException.class, () -> PresetBackup.read(invalid)); }
    }

    @Test public void duplicateNamesAndMissingFieldsAreRejected()
    {
        PresetBackup.Entry entry = PresetBackup.read(example()).get(0);
        assertThrows(IllegalArgumentException.class, () -> PresetBackup.read(PresetBackup.write(List.of(entry, entry))));
        JsonObject json = new JsonParser().parse(example()).getAsJsonObject();
        json.getAsJsonArray("presets").get(0).getAsJsonObject().remove("patches");
        assertThrows(IllegalArgumentException.class, () -> PresetBackup.read(json.toString()));
        JsonObject missing = new JsonParser().parse(example()).getAsJsonObject();
        missing.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings").remove("guildTravel");
        assertThrows(IllegalArgumentException.class, () -> PresetBackup.read(missing.toString()));
    }

    @Test public void fileAccessIsLimitedToSimpleJsonFilenames() throws Exception
    {
        ConfigFixture fixture = new ConfigFixture();
        PresetTransfer transfer = transfer(fixture);
        for (String filename : new String[]{"../outside.json", "..\\outside.json", "C:\\outside.json", "/outside.json",
            "sub/file.json", "backup.properties", "backup.json:stream", "bad\nname.json"})
        {
            assertThrows(filename, IllegalArgumentException.class, () -> transfer.exportPresets(filename));
            assertThrows(filename, IllegalArgumentException.class, () -> transfer.importPresets(filename, new PresetSlots(fixture)));
        }
        transfer.exportPresets("my-herbs_2.json");
        assertTrue(Files.isRegularFile(backupDirectory().resolve("my-herbs_2.json")));
    }

    @Test public void missingAndOversizedFilesKeepExistingPresets() throws Exception
    {
        ConfigFixture fixture = new ConfigFixture();
        save(fixture, "Existing", "WEISS_HERB");
        PresetSlots slots = new PresetSlots(fixture);
        Map<String, String> before = new HashMap<>(fixture.values);
        assertThrows(IllegalArgumentException.class, () -> transfer(fixture).importPresets("missing.json", slots));
        Files.createDirectories(backupDirectory());
        Files.write(backupDirectory().resolve("large.json"), new byte[PresetBackup.MAX_CHARACTERS + 1]);
        assertThrows(IllegalArgumentException.class, () -> transfer(fixture).importPresets("large.json", slots));
        assertEquals(before, fixture.values);
    }

    @Test public void backupActionsUseTheNativeCogAndAreExcludedFromSavedSnapshots() throws Exception
    {
        for (String method : new String[]{"exportPresetsAction", "importPresetsAction"})
        {
            ConfigItem item = ClassicTreeRunConfig.class.getMethod(method).getAnnotation(ConfigItem.class);
            assertEquals(ClassicTreeRunConfig.BACKUPS, item.section());
            assertFalse(item.hidden());
            assertFalse(item.warning().isEmpty());
            assertTrue(PresetActions.shouldQueue(item.keyName(), "true", "false", true));
            assertFalse(PresetActions.shouldQueue(item.keyName(), null, "false", true));
            assertFalse(PresetActions.shouldQueue(item.keyName(), "false", "true", false));
        }
        ConfigFixture fixture = new ConfigFixture();
        fixture.set("exportPresetsAction", "true");
        fixture.set("importPresetsAction", "true");
        fixture.set("backupFilename", "my-backup.json");
        save(fixture, "Herbs", "TROLL_HERB");
        assertFalse(fixture.get(PresetLibrary.KEY).contains("exportPresetsAction"));
        assertFalse(fixture.get(PresetLibrary.KEY).contains("importPresetsAction"));
        assertFalse(fixture.get(PresetLibrary.KEY).contains("backupFilename"));
    }

    @Test public void cogActionsExportAndImportThroughTheSameSlotController() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture fixture = new ConfigFixture();
            fixture.set("autoOptimize", "false");
            fixture.set("backupFilename", "presets.json");
            save(fixture, "Herbs", "TROLL_HERB");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(fixture.config, fixture);
            RunSettings settings = new RunSettings(fixture.config, fixture, panel);
            try
            {
                PresetActions actions = new PresetActions(fixture.config, fixture, settings, panel, transfer(fixture));
                actions.apply("exportPresetsAction");
                assertTrue(Files.isRegularFile(backupDirectory().resolve("presets.json")));
                actions.apply("deletePresetAction");
                assertTrue(new PresetLibrary(fixture).names().isEmpty());
                actions.apply("importPresetsAction");
                assertTrue(new PresetLibrary(fixture).contains("Herbs"));
                fixture.set("editorPreset", "SLOT_01");
                actions.apply("editorPreset");
                assertEquals("Herbs", fixture.config.runName());
                assertEquals("TROLL_HERB", fixture.config.selection());
            }
            finally { settings.dispose(); }
        });
    }
}
