package com.classictreerun;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import org.junit.Test;
import static org.junit.Assert.*;

public class RunSettingsTest
{
    @Test public void allPatchesHaveSettingsMetadataAndDeclaredCropSections() throws Exception
    {
        Set<String> sections = new java.util.HashSet<>();
        for (java.lang.reflect.Field field : ClassicTreeRunConfig.class.getDeclaredFields())
        {
            if (field.isAnnotationPresent(ConfigSection.class)) { sections.add((String) field.get(null)); }
        }
        int patches = 0;
        for (java.lang.reflect.Method method : ClassicTreeRunConfig.class.getMethods())
        {
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            assertNotNull("Only annotated configuration getters may live on RuneLite's proxy", item);
            if (item.keyName().startsWith("patch_"))
            {
                patches++;
                assertFalse(item.hidden());
                assertEquals(boolean.class, method.getReturnType());
                assertTrue("Missing native config section", sections.contains(item.section()));
            }
            if ("selectionV2".equals(item.keyName()) || "excluded".equals(item.keyName()) || "order".equals(item.keyName())) { assertTrue(item.hidden()); }
        }
        assertEquals(99, patches);
        assertEquals(27, sections.size());
    }

    @Test public void migrationKeepsLegacySelectionAndProgress() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            ConfigFixture f = new ConfigFixture();
            f.set("selectionV2", "TROLL_HERB,GUILD_FRUIT");
            f.set(ClassicTreeRunPanel.PROGRESS_KEY, "GUILD_FRUIT");
            f.set("autoOptimize", "false");
            RunSettings settings = new RunSettings(f.config, f, new ClassicTreeRunPanel(f.config, f));
            assertEquals(RouteState.parse("TROLL_HERB,GUILD_FRUIT"), NativePatchSelection.read(f.config));
            assertEquals("GUILD_FRUIT", f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            assertEquals("true", f.get(RunSettings.MIGRATED));
            settings.prepareNow();
            assertNull("Draft changes must not save a preset", f.get(PresetLibrary.KEY));
            settings.dispose();
        });
    }

    @Test public void changingLocalPatchToggleAutomaticallyPreparesWorkingRun() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            ConfigFixture f = new ConfigFixture();
            f.nativeSelection(new LinkedHashSet<>());
            f.set("autoOptimize", "false");
            RunSettings settings = new RunSettings(f.config, f, new ClassicTreeRunPanel(f.config, f));
            f.set("patch_TROLL_HERB", "true");
            settings.changed("patch_TROLL_HERB");
            settings.prepareNow();
            assertEquals(java.util.Collections.singletonList(Patch.TROLL_HERB), RouteState.active(f.config));
            assertNull("Draft changes must not save a preset", f.get(PresetLibrary.KEY));
            f.set("patch_TROLL_HERB", "false");
            settings.changed("patch_TROLL_HERB");
            assertTrue(RouteState.active(f.config).isEmpty());
            settings.dispose();
        });
    }

    @Test public void nativeSettingsEditedBeforeFirstEnableAreRespected() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            ConfigFixture f = new ConfigFixture();
            f.set("patch_TROLL_HERB", "true");
            f.set("autoOptimize", "false");
            RunSettings settings = new RunSettings(f.config, f, new ClassicTreeRunPanel(f.config, f));
            assertTrue(RouteState.active(f.config).contains(Patch.TROLL_HERB));
            assertEquals(17, RouteState.active(f.config).size());
            settings.dispose();
        });
    }

    @Test public void automaticOptimizationFinishesWithoutASeparateSaveButton() throws Exception
    {
        ConfigFixture f = new ConfigFixture();
        RunSettings[] controller = new RunSettings[1];
        SwingUtilities.invokeAndWait(() ->
        {
            f.nativeSelection(new LinkedHashSet<>(Arrays.asList(Patch.GUILD_FRUIT, Patch.FOSSIL_EAST, Patch.FOSSIL_MIDDLE)));
            f.orderSaved = new CountDownLatch(1);
            controller[0] = new RunSettings(f.config, f, new ClassicTreeRunPanel(f.config, f));
            controller[0].prepareNow();
        });
        assertTrue(f.orderSaved.await(10, TimeUnit.SECONDS));
        SwingUtilities.invokeAndWait(() ->
        {
            assertEquals(NativePatchSelection.read(f.config), new LinkedHashSet<>(RouteState.active(f.config)));
            assertNotNull(f.get("order"));
            controller[0].dispose();
        });
    }
}

