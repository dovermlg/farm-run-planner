package com.classictreerun;

import java.awt.Component;
import java.nio.file.Path;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.components.TitleCaseListCellRenderer;
import net.runelite.client.util.Text;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class PresetSlotsTest
{
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void priorNamesAreAdoptedAndDisplayedByRuneLitesActualEnumRenderer() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); PresetLibrary library = new PresetLibrary(f);
            library.save("Daily herbs", f.config); library.save("Weekend trees", f.config);
            PresetSlots slots = new PresetSlots(f);
            assertEquals("Daily herbs", slots.name(EditorPreset.SLOT_01));
            assertEquals("1: Daily herbs", Text.titleCase(EditorPreset.SLOT_01));
            Component label = new TitleCaseListCellRenderer().getListCellRendererComponent(new JList<>(), EditorPreset.SLOT_01, 0, false, false);
            assertEquals("1: Daily herbs", ((JLabel) label).getText());
            assertEquals("3: Empty", EditorPreset.SLOT_03.toString());
            assertEquals(33, EditorPreset.values().length);
            assertEquals(2, library.names().size());
        });
    }

    @Test public void emptySlotsCreateSeparateCopiesAndRenamingUpdatesOnlyThatSlot()
    {
        ConfigFixture f = new ConfigFixture(); PresetSlots slots = new PresetSlots(f);
        f.set("selectionV2", "TROLL_HERB"); slots.save(EditorPreset.SLOT_01, "Herbs", f.config);
        f.set("selectionV2", "CORAL_E"); slots.save(EditorPreset.SLOT_02, "Corals", f.config);
        slots.save(EditorPreset.SLOT_01, "Quick herbs", f.config);
        PresetLibrary library = new PresetLibrary(f);
        assertFalse(library.contains("Herbs")); assertTrue(library.contains("Quick herbs")); assertTrue(library.contains("Corals"));
        assertEquals(2, library.names().size());
        assertEquals(EditorPreset.SLOT_01, new PresetSlots(f).find("Quick herbs"));
        assertEquals(EditorPreset.SLOT_02, new PresetSlots(f).find("Corals"));
    }

    @Test public void duplicateNamesDoNotOverwriteAnotherSlot()
    {
        ConfigFixture f = new ConfigFixture(); PresetSlots slots = new PresetSlots(f);
        slots.save(EditorPreset.SLOT_01, "One", f.config); slots.save(EditorPreset.SLOT_02, "Two", f.config);
        String stored = f.get(PresetLibrary.KEY); String bindings = f.get(PresetSlots.KEY);
        try { slots.save(EditorPreset.SLOT_02, "One", f.config); fail("Duplicate name accepted"); } catch (IllegalArgumentException expected) { }
        assertEquals(stored, f.get(PresetLibrary.KEY)); assertEquals(bindings, f.get(PresetSlots.KEY));
        assertEquals("Two", slots.name(EditorPreset.SLOT_02));
    }

    @Test public void slotBindingsAndUnicodeNamesSurviveAFileRestart() throws Exception
    {
        Path file = temporary.getRoot().toPath().resolve("profile.properties");
        LocalPreferences store = new LocalPreferences(TestFiles.at(file)); LocalRunConfig config = new LocalRunConfig(store);
        PresetSlots slots = new PresetSlots(store);
        slots.save(EditorPreset.SLOT_07, "Daily \u00e9 / \uD83C\uDF3F", config);
        PresetSlots restarted = new PresetSlots(new LocalPreferences(TestFiles.at(file)));
        assertEquals("Daily \u00e9 / \uD83C\uDF3F", restarted.name(EditorPreset.SLOT_07));
        assertEquals(EditorPreset.SLOT_07, restarted.find("Daily \u00e9 / \uD83C\uDF3F"));
        assertTrue(new PresetLibrary(new LocalPreferences(TestFiles.at(file))).contains("Daily \u00e9 / \uD83C\uDF3F"));
    }

    @Test public void deletingUsesSelectedSlotAndFreesItWithoutRemovingOtherNames()
    {
        ConfigFixture f = new ConfigFixture(); PresetSlots slots = new PresetSlots(f);
        slots.save(EditorPreset.SLOT_01, "One", f.config); slots.save(EditorPreset.SLOT_02, "Two", f.config);
        slots.delete(EditorPreset.SLOT_01, "Two");
        assertFalse(new PresetLibrary(f).contains("One")); assertTrue(new PresetLibrary(f).contains("Two"));
        assertNull(slots.name(EditorPreset.SLOT_01));
        slots.save(EditorPreset.SLOT_01, "Replacement", f.config);
        assertEquals("Replacement", new PresetSlots(f).name(EditorPreset.SLOT_01));
    }

    @Test public void editingFromCogPreservesProgressAndDoesNotSaveWorkingEdits() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f); RunSettings settings = new RunSettings(f.config, f, panel);
            PresetActions actions = new PresetActions(f.config, f, settings, panel);
            settings.select(RouteState.parse("TROLL_HERB")); f.set("runName", "Saved herbs"); actions.apply("savePresetAction");
            String saved = f.get(PresetLibrary.KEY);
            settings.select(RouteState.parse("CORAL_E")); f.set(ClassicTreeRunPanel.PROGRESS_KEY, "TROLL_HERB");
            actions.apply("editorPreset");
            assertEquals("TROLL_HERB", f.config.selection()); assertEquals("TROLL_HERB", f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            settings.select(RouteState.parse("WEISS_HERB")); assertEquals(saved, f.get(PresetLibrary.KEY));
            f.set("editorPreset", "SLOT_02"); actions.apply("editorPreset");
            assertEquals("New preset 2", f.config.runName()); assertEquals("WEISS_HERB", f.config.selection());
            assertEquals(saved, f.get(PresetLibrary.KEY));
            settings.dispose();
        });
    }

    @Test public void savingAfterCurrentRunSelectionResolvesItsExistingSlot()
    {
        ConfigFixture f = new ConfigFixture(); PresetSlots slots = new PresetSlots(f);
        assertEquals(EditorPreset.SLOT_01, slots.save(EditorPreset.CURRENT, "Mine", f.config));
        assertEquals(EditorPreset.SLOT_01, slots.save(EditorPreset.CURRENT, "Mine", f.config));
        assertEquals(1, new PresetLibrary(f).names().size());
    }

    @Test public void fullSlotsRejectNewNamesAndPreserveOlderImportedOverflow()
    {
        ConfigFixture f = new ConfigFixture(); PresetLibrary library = new PresetLibrary(f);
        for (int i = 1; i <= 34; i++) { library.save(String.format("Run %02d", i), f.config); }
        PresetSlots slots = new PresetSlots(f); String saved = f.get(PresetLibrary.KEY);
        try { slots.save(EditorPreset.CURRENT, "New run", f.config); fail("Over-capacity save accepted"); } catch (IllegalArgumentException expected) { }
        assertEquals(saved, f.get(PresetLibrary.KEY)); assertEquals(34, new PresetLibrary(f).names().size());
        new PresetLibrary(f).load("Run 34"); assertEquals("Run 34", f.config.runName());
    }

    @Test public void profilesPublishTheirOwnLabelsAndKeepIndependentBindings()
    {
        ConfigFixture first = new ConfigFixture(); PresetSlots a = new PresetSlots(first); a.save(EditorPreset.SLOT_01, "First", first.config);
        ConfigFixture second = new ConfigFixture(); PresetSlots b = new PresetSlots(second); b.save(EditorPreset.SLOT_01, "Second", second.config);
        assertEquals("1: Second", EditorPreset.SLOT_01.toString());
        PresetSlots reopened = new PresetSlots(first);
        assertEquals("First", reopened.name(EditorPreset.SLOT_01)); assertEquals("1: First", EditorPreset.SLOT_01.toString());
        EditorPreset.clearLabels(); assertEquals("1: Empty", EditorPreset.SLOT_01.toString());
    }

    @Test public void realConfigEventsLoadUserSelectionsButDoNotReloadAfterSave() throws Exception
    {
        ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false"); f.set("editorPreset", "CURRENT");
        ClassicTreeRunPlugin plugin = new ClassicTreeRunPlugin();
        RunSettings[] settings = new RunSettings[1];
        ClassicTreeRunPanel.Preferences events = new ClassicTreeRunPanel.Preferences() {
            public String get(String key) { return f.get(key); }
            public void set(String key, String value)
            {
                String old = f.get(key); f.set(key, value);
                if ("editorPreset".equals(key))
                {
                    net.runelite.client.events.ConfigChanged event = new net.runelite.client.events.ConfigChanged();
                    event.setGroup(ClassicTreeRunConfig.GROUP); event.setKey(key); event.setOldValue(old); event.setNewValue(value);
                    plugin.onConfigChanged(event);
                }
            }
        };
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, events);
                settings[0] = new RunSettings(f.config, events, panel);
                PresetActions actions = new PresetActions(f.config, events, settings[0], panel);
                java.lang.reflect.Field controller = ClassicTreeRunPlugin.class.getDeclaredField("settings"); controller.setAccessible(true); controller.set(plugin, settings[0]);
                java.lang.reflect.Field commands = ClassicTreeRunPlugin.class.getDeclaredField("presetActions"); commands.setAccessible(true); commands.set(plugin, actions);
                settings[0].select(RouteState.parse("TROLL_HERB")); f.set("runName", "Mine");
                actions.apply("savePresetAction");
                settings[0].select(RouteState.parse("WEISS_HERB"));
                // Save sets the native slot ID programmatically. That must not reload its saved snapshot.
            }
            catch (ReflectiveOperationException error) { throw new AssertionError(error); }
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals("WEISS_HERB", f.config.selection());
            events.set("editorPreset", "CURRENT");
            events.set("editorPreset", "SLOT_01");
        });
        SwingUtilities.invokeAndWait(() -> {
            assertEquals("TROLL_HERB", f.config.selection()); assertEquals("Mine", f.config.runName());
            settings[0].dispose();
        });
    }

    @Test public void writeFailureDoesNotPartiallyRenameSnapshotOrSlot()
    {
        ConfigFixture memory = new ConfigFixture(); boolean[] fail = {false};
        ClassicTreeRunPanel.Preferences values = new ClassicTreeRunPanel.Preferences() {
            public String get(String key) { return memory.get(key); }
            public void set(String key, String value) { if (fail[0]) { throw new IllegalStateException("disk failure"); } memory.set(key, value); }
            public void update(java.util.Map<String, String> changes) { if (fail[0]) { throw new IllegalStateException("disk failure"); } memory.update(changes); }
        };
        PresetSlots slots = new PresetSlots(values); slots.save(EditorPreset.SLOT_01, "Original", memory.config);
        String saved = memory.get(PresetLibrary.KEY); String binding = memory.get(PresetSlots.KEY); fail[0] = true;
        try { slots.save(EditorPreset.SLOT_01, "Renamed", memory.config); fail("Write succeeded"); } catch (IllegalStateException expected) { }
        assertEquals(saved, memory.get(PresetLibrary.KEY)); assertEquals(binding, memory.get(PresetSlots.KEY));
        assertEquals("Original", slots.name(EditorPreset.SLOT_01));
    }
}
