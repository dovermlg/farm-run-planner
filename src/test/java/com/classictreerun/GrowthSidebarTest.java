package com.classictreerun;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class GrowthSidebarTest
{
    @Test public void countdownRefreshKeepsExistingControlsAndOnlyChangesLabels() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();f.set("selectionV2","TROLL_HERB");
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            CropGrowth growth=new CropGrowth(new CropReference());growth.observe(Patch.TROLL_HERB,4,1000,false);panel.setGrowth(growth);
            Component[] controls=panel.getComponents();
            panel.refreshGrowth(1000);String first=labels(panel);panel.refreshGrowth(1600);
            assertArrayEquals(controls,panel.getComponents());assertNotEquals(first,labels(panel));
            assertTrue(labels(panel).contains("1h 10m"));
        });
    }
    @Test public void perPatchCountdownAndFullRunSummaryFitInsideTheSidebar() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();f.set("loadPlanning","false");f.set("selectionV2","TROLL_HERB,LUMBRIDGE_TREE");f.set("runName","Herbs and trees");
            new PresetLibrary(f).save("Herbs and trees",f.config);
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);CropGrowth growth=new CropGrowth(new CropReference());long now=Instant.now().getEpochSecond();
            growth.observe(Patch.TROLL_HERB,32,now,false);growth.observe(Patch.LUMBRIDGE_TREE,48,now,false);panel.setGrowth(growth);
            String labels=labels(panel);assertTrue(labels.contains("ranarr"));assertTrue(labels.contains("magic"));assertTrue(labels.contains("Next full run estimated"));
            panel.setSize(225,2400);layout(panel);assertFit(panel);
            BufferedImage image=new BufferedImage(225,1000,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image.createGraphics();panel.printAll(graphics);graphics.dispose();
            try{java.io.File output=new java.io.File("build/reports/growth-preview.png");output.getParentFile().mkdirs();ImageIO.write(image,"png",output);}catch(java.io.IOException error){throw new AssertionError(error);}
        });
    }
    private static String labels(Container parent){StringBuilder all=new StringBuilder();for(Component child:parent.getComponents()){if(child instanceof JLabel)all.append(((JLabel)child).getText());if(child instanceof Container)all.append(labels((Container)child));}return all.toString();}
    private static void layout(Container parent){parent.doLayout();for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child);}
    private static void assertFit(Container parent){for(Component child:parent.getComponents()){if(child instanceof JLabel && ((JLabel)child).getText().startsWith("<html>"))assertTrue(child.getX()+child.getWidth()<=parent.getWidth());if(child instanceof Container)assertFit((Container)child);}}
}
