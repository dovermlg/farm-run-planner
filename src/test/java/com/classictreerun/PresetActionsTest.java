package com.classictreerun;

import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.events.ConfigChanged;
import org.junit.Test;
import static org.junit.Assert.*;

public class PresetActionsTest
{
    @Test public void cogSaveCreatesArbitraryNamesAndKeepsIndependentSnapshots() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            RunSettings settings = new RunSettings(f.config, f, panel);
            PresetActions actions = new PresetActions(f.config, f, settings, panel);
            settings.select(RouteState.parse("TROLL_HERB,WEISS_HERB"));
            f.set("runName", "My herbs / \u00e9"); actions.apply("savePresetAction");
            f.set("editorPreset", "SLOT_02"); actions.apply("editorPreset");
            settings.select(RouteState.parse("CORAL_E,CORAL_W"));
            f.set("runName", "After work"); actions.apply("savePresetAction");
            PresetLibrary library = new PresetLibrary(f); assertEquals(2, library.names().size());
            library.load("My herbs / \u00e9"); assertEquals(RouteState.parse("TROLL_HERB,WEISS_HERB"), RouteState.parse(f.config.selection()));
            library.load("After work"); assertEquals(RouteState.parse("CORAL_E,CORAL_W"), RouteState.parse(f.config.selection()));
            settings.dispose();
        });
    }

    @Test public void editingDoesNotOverwriteUntilCogSaveAction() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            RunSettings settings = new RunSettings(f.config, f, panel);
            PresetActions actions = new PresetActions(f.config, f, settings, panel);
            f.set("runName", "Mine"); settings.select(RouteState.parse("TROLL_HERB")); actions.apply("savePresetAction");
            String saved = f.get(PresetLibrary.KEY);
            settings.select(RouteState.parse("WEISS_HERB")); assertEquals(saved, f.get(PresetLibrary.KEY));
            actions.apply("savePresetAction");
            new PresetLibrary(f).load("Mine"); assertEquals("WEISS_HERB", f.config.selection());
            settings.dispose();
        });
    }

    @Test public void deleteInCogKeepsWorkingRunAndInvalidNamesDoNotSave() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            RunSettings settings = new RunSettings(f.config, f, panel);
            PresetActions actions = new PresetActions(f.config, f, settings, panel);
            f.set("runName", "Mine"); actions.apply("savePresetAction");
            String working = f.config.selection(); actions.apply("deletePresetAction");
            assertFalse(new PresetLibrary(f).contains("Mine")); assertEquals(working, f.config.selection());
            f.set("runName", "  "); actions.apply("savePresetAction"); assertTrue(new PresetLibrary(f).names().isEmpty());
            settings.dispose();
        });
    }

    @Test public void onlyExplicitUiToggleChangesQueueActions()
    {
        assertTrue(PresetActions.shouldQueue("savePresetAction", "false", "true", true));
        assertTrue(PresetActions.shouldQueue("savePresetAction", "true", "false", true));
        assertFalse(PresetActions.shouldQueue("savePresetAction", null, "false", true));
        assertFalse(PresetActions.shouldQueue("savePresetAction", "false", "true", false));
        assertFalse(PresetActions.shouldQueue("savePresetAction", "false", "false", true));
        assertFalse(PresetActions.shouldQueue("savePresetAction", "true", null, true));
        assertFalse(PresetActions.shouldQueue("runName", "a", "b", true));
    }

    @Test public void managementActionsAreVisibleNativeCogControlsWithConfirmation() throws Exception
    {
        for (String method : new String[]{"savePresetAction", "deletePresetAction"})
        {
            ConfigItem item = ClassicTreeRunConfig.class.getMethod(method).getAnnotation(ConfigItem.class);
            assertEquals(ClassicTreeRunConfig.RUN, item.section()); assertFalse(item.hidden());
            assertFalse(item.warning().isEmpty()); assertEquals(boolean.class, ClassicTreeRunConfig.class.getMethod(method).getReturnType());
        }
    }

    @Test public void snapshotsExcludeActionFlagsSoSelectingCannotExecuteManagement()
    {
        ConfigFixture f = new ConfigFixture(); f.set("savePresetAction", "true"); f.set("deletePresetAction", "true");
        PresetLibrary library = new PresetLibrary(f); library.save("Mine", f.config);
        assertFalse(f.get(PresetLibrary.KEY).contains("savePresetAction")); assertFalse(f.get(PresetLibrary.KEY).contains("deletePresetAction"));
        f.set("savePresetAction", "false"); library.load("Mine"); assertEquals("false", f.get("savePresetAction"));
    }

    @Test public void nativeConfigResetCancelsQueuedManagementCommands() throws Exception
    {
        ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false");
        RunSettings[] settings = new RunSettings[1];
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
                settings[0] = new RunSettings(f.config, f, panel);
                ClassicTreeRunPlugin plugin = new ClassicTreeRunPlugin();
                wire(plugin, "settings", settings[0]);
                wire(plugin, "presetActions", new PresetActions(f.config, f, settings[0], panel));
                ConfigChanged event = new ConfigChanged(); event.setGroup(ClassicTreeRunConfig.GROUP);
                event.setKey("savePresetAction"); event.setOldValue("true"); event.setNewValue("false");
                plugin.onConfigChanged(event); plugin.resetConfiguration();
            }
            catch (ReflectiveOperationException error) { throw new AssertionError(error); }
        });
        SwingUtilities.invokeAndWait(() -> { assertNull(f.get(PresetLibrary.KEY)); settings[0].dispose(); });
    }
    private static void wire(Object target, String field, Object value) throws ReflectiveOperationException
    {
        java.lang.reflect.Field member = target.getClass().getDeclaredField(field); member.setAccessible(true); member.set(target, value);
    }
}
