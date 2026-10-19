package com.classictreerun;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class SidebarTest
{
    @Test public void runTabContainsOnlyPreparedRouteAndProgressControls() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            ConfigFixture f = new ConfigFixture();
            f.set("loadPlanning","false");
            f.set("selectionV2", "TROLL_HERB,WEISS_HERB");
            f.set("runName", "Daily herbs");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            assertNotNull(find(panel, "TROLL_HERB"));
            assertNotNull(find(panel, "WEISS_HERB"));
            assertNull(find(panel, "LUMBRIDGE_TREE"));
            for (String removed : new String[]{"editRoute", "saveRun", "optimizeRun", "typeFilter", "patchSearch"})
            {
                assertNull("Setup control leaked into the run tab: " + removed, find(panel, removed));
            }
            capture(panel);
        });
    }

    @Test public void completionPersistsAndResetPreservesConfiguration() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            ConfigFixture f = new ConfigFixture();
            f.set("selectionV2", "TROLL_HERB,WEISS_HERB");
            f.set("hideCompleted", "true");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            panel.setConfirmReset(false);
            ((AbstractButton) find(panel, "TROLL_HERB")).doClick();
            assertNull(find(panel, "TROLL_HERB"));
            assertTrue(f.get(ClassicTreeRunPanel.PROGRESS_KEY).contains("TROLL_HERB"));
            ClassicTreeRunPanel reopened = new ClassicTreeRunPanel(f.config, f);
            assertNull(find(reopened, "TROLL_HERB"));
            ((AbstractButton) find(panel, "resetRun")).doClick();
            assertEquals("", f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            assertEquals("TROLL_HERB,WEISS_HERB", f.config.selection());
            assertNotNull(find(panel, "TROLL_HERB"));
        });
    }

    @Test public void returningToTabPreparesLatestSettings() throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            ConfigFixture f = new ConfigFixture();
            f.nativeSelection(new LinkedHashSet<>(Arrays.asList(Patch.CORAL_E, Patch.CORAL_W)));
            f.set("autoOptimize", "false");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            RunSettings settings = new RunSettings(f.config, f, panel);
            panel.setPrepareRun(settings::prepareNow);
            panel.onActivate();
            assertNotNull(find(panel, "CORAL_E"));
            assertNotNull(find(panel, "CORAL_W"));
            assertNull(find(panel, "FOSSIL_EAST"));
            settings.dispose();
        });
    }

    private static Component find(Container parent, String name)
    {
        for (Component child : parent.getComponents())
        {
            if (name.equals(child.getName())) { return child; }
            if (child instanceof Container)
            {
                Component found = find((Container) child, name);
                if (found != null) { return found; }
            }
        }
        return null;
    }

    private static void layout(Container parent)
    {
        parent.doLayout();
        for (Component child : parent.getComponents()) { if (child instanceof Container) { layout((Container) child); } }
    }
    private static void assertLabelsFit(Container parent)
    {
        for (Component child : parent.getComponents())
        {
            if (child instanceof JLabel && ((JLabel) child).getText().startsWith("<html>"))
            {
                assertTrue("Label clipped", child.getX() >= 0 && child.getX() + child.getWidth() <= parent.getWidth());
            }
            if (child instanceof Container) { assertLabelsFit((Container) child); }
        }
    }
    private static void capture(ClassicTreeRunPanel panel)
    {
        panel.setSize(225, 2000);
        layout(panel);
        assertLabelsFit(panel);
        BufferedImage image = new BufferedImage(225, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try { panel.printAll(graphics); } finally { graphics.dispose(); }
        try
        {
            File file = new File("build/reports/sidebar-preview.png");
            file.getParentFile().mkdirs();
            ImageIO.write(image, "png", file);
        }
        catch (java.io.IOException error) { throw new RuntimeException(error); }
    }
}
