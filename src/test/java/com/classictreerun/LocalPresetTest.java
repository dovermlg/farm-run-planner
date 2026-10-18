package com.classictreerun;

import java.awt.Component;

import java.awt.Container;

import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import javax.imageio.ImageIO;

import javax.swing.AbstractButton;

import javax.swing.JComboBox;

import javax.swing.JTextField;

import javax.swing.SwingUtilities;

import org.junit.Rule;

import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

public class LocalPresetTest

{

    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void namedSnapshotSurvivesRestartWithoutDraftEditsOverwritingIt() throws Exception

    {

        Path file = temporary.getRoot().toPath().resolve("profile-1.properties");

        LocalPreferences store = new LocalPreferences(TestFiles.at(file));

        LocalRunConfig config = new LocalRunConfig(store);

        store.set("selectionV2", "TROLL_HERB,WEISS_HERB");

        store.set("patch_TROLL_HERB", "true");

        store.set("brimhavenTravel", "CATHERBY_CHARTER");

        PresetLibrary library = new PresetLibrary(store);

        library.save("Daily herbs / \u00e9 \uD83C\uDF3F", config);

        store.set("selectionV2", "CORAL_E,CORAL_W");

        store.set("patch_TROLL_HERB", "false");

        store.set("brimhavenTravel", "SPIRIT_TREE");

        store.set(ClassicTreeRunPanel.PROGRESS_KEY, "TROLL_HERB");

        LocalPreferences reopened = new LocalPreferences(TestFiles.at(file));

        PresetLibrary loaded = new PresetLibrary(reopened);

        loaded.load("Daily herbs / \u00e9 \uD83C\uDF3F");

        LocalRunConfig restored = new LocalRunConfig(new LocalPreferences(TestFiles.at(file)));

        assertEquals("TROLL_HERB,WEISS_HERB", restored.selection());

        assertTrue(restored.includeTROLL_HERB());

        assertEquals(ClassicTreeRunConfig.BrimhavenTravel.CATHERBY_CHARTER, restored.brimhavenTravel());

        assertEquals("", reopened.get(ClassicTreeRunPanel.PROGRESS_KEY));

        assertEquals(1, loaded.names().size());

    }

    @Test public void multipleNamesEmptyRunsAndDeletionStayIndependent() throws Exception

    {

        LocalPreferences store = new LocalPreferences(TestFiles.at(temporary.getRoot().toPath().resolve("data.properties")));

        LocalRunConfig config = new LocalRunConfig(store); PresetLibrary library = new PresetLibrary(store);

        store.set("selectionV2", "-"); library.save("Empty", config);

        store.set("selectionV2", "TROLL_HERB"); library.save("My herbs", config);

        library.load("Empty"); assertTrue(RouteState.active(config).isEmpty());

        library.load("My herbs"); assertEquals("TROLL_HERB", config.selection());

        library.delete("Empty"); assertFalse(new PresetLibrary(new LocalPreferences(TestFiles.at(temporary.getRoot().toPath().resolve("data.properties")))).contains("Empty"));

        assertTrue(library.contains("My herbs"));

    }

    @Test public void profilesUseSeparateLocalFilesAndEnumsFallBackSafely() throws Exception

    {

        Path root = temporary.getRoot().toPath();

        LocalPreferences first = new LocalPreferences(TestFiles.at(root.resolve("profile-1.properties")));

        LocalPreferences second = new LocalPreferences(TestFiles.at(root.resolve("profile-2.properties")));

        first.set("runName", "Private herbs"); assertEquals("My farming run", new LocalRunConfig(second).runName());

        first.set("brimhavenTravel", "OBSOLETE_OPTION");

        assertEquals(ClassicTreeRunConfig.BrimhavenTravel.REDIRECTED_TABLET, new LocalRunConfig(first).brimhavenTravel());

    }

    @Test public void failedWritesDoNotChangeMemoryOrDestroyExistingData() throws Exception

    {

        Path parent = temporary.getRoot().toPath().resolve("parent"); Files.createDirectories(parent);

        Path file = parent.resolve("data.properties"); LocalPreferences store = new LocalPreferences(TestFiles.at(file));

        store.set("runName", "Kept"); byte[] original = Files.readAllBytes(file);

        Files.delete(file); Files.delete(parent); Files.write(parent, original);

        try { store.set("runName", "Lost"); fail("Write should fail"); } catch (IllegalStateException expected) { }

        assertEquals("Kept", store.get("runName")); assertArrayEquals(original, Files.readAllBytes(parent));

    }

    @Test public void invalidNamesAndMalformedFilesAreRejectedWithoutOverwrite() throws Exception

    {

        for (String invalid : new String[]{"   ", "x\ny", "a".repeat(61)})

        {

            try { PresetLibrary.validName(invalid); fail("Invalid name accepted"); } catch (IllegalArgumentException expected) { }

        }

        Path file = temporary.getRoot().toPath().resolve("bad.properties");

        byte[] bad = "runName=\\uZZZZ".getBytes(StandardCharsets.UTF_8); Files.write(file, bad);

        try { new LocalPreferences(TestFiles.at(file)); fail("Invalid file accepted"); } catch (java.io.IOException expected) { }

        assertArrayEquals(bad, Files.readAllBytes(file));

    }

    @Test public void cogSavesAndMainDropdownStartsRunsWithoutManagementButtons() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); f.set("autoOptimize", "false");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            RunSettings settings = new RunSettings(f.config, f, panel);
            new PresetLibrary(f).seedStarters(f.config);
            PresetActions actions = new PresetActions(f.config, f, settings, panel);
            panel.setPresetLoaded(() -> { actions.syncForRun(); settings.changed("patch_"); settings.changed("autoOptimize"); settings.prepareNow(); });
            settings.select(RunPreset.HERBS.starter(java.util.Collections.emptySet()));
            f.set("runName", "Quick herbs");
            actions.apply("savePresetAction");
            assertEquals("Quick herbs", ((JComboBox<?>) find(panel, "savedPresets")).getSelectedItem());
            ((JComboBox<?>) find(panel, "savedPresets")).setSelectedItem("Trees");
            assertEquals(25, RouteState.active(f.config).size());
            assertEquals(EditorPreset.SLOT_03, f.config.editorPreset());
            f.set(ClassicTreeRunPanel.PROGRESS_KEY, "GUILD_TREE");
            panel.setStatus("Background route prepared");
            assertEquals("Trees", ((JComboBox<?>) find(panel, "savedPresets")).getSelectedItem());
            assertEquals("GUILD_TREE", f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            ((JComboBox<?>) find(panel, "savedPresets")).setSelectedItem("Quick herbs");
            assertEquals(10, RouteState.active(f.config).size()); assertEquals("Quick herbs", f.config.runName());
            assertEquals("", f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            for (String removed : new String[]{"settingsCog", "edit_TROLL_HERB", "savePreset", "loadPreset", "deletePreset"}) { assertNull(find(panel, removed)); }
            assertNull("No detached editor window", SwingUtilities.getWindowAncestor(panel));
            panel.setSize(225, 2200); layout(panel);
            BufferedImage image = new BufferedImage(225, 800, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics(); panel.printAll(graphics); graphics.dispose();
            try {
                java.io.File output = new java.io.File("build/reports/preset-controls-preview.png"); output.getParentFile().mkdirs(); ImageIO.write(image, "png", output);
            } catch (java.io.IOException error) { throw new AssertionError(error); }
            settings.dispose();
        });
    }
    private static Component find(Container parent, String name)

    {

        for (Component child : parent.getComponents())

        {

            if (name.equals(child.getName())) { return child; }

            if (child instanceof Container) { Component found = find((Container) child, name); if (found != null) { return found; } }

        }

        return null;

    }

    private static void layout(Container parent)

    {

        parent.doLayout(); for (Component child : parent.getComponents()) { if (child instanceof Container) { layout((Container) child); } }

    }

}

