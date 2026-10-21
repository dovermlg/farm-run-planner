package com.classictreerun;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventorySetupEditorTest
{
    private static ConfigFixture preset()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadPlanning","false");f.set("runName","My farming run");f.set("selectionV2","LUMBRIDGE_TREE,VARROCK_TREE");
        f.set("order","LUMBRIDGE_TREE,VARROCK_TREE");new PresetLibrary(f).save(f.config.runName(),f.config);
        f.set(ClassicTreeRunPanel.PROGRESS_KEY,"LUMBRIDGE_TREE");f.set(InventoryLoadPlanner.BANKS_KEY,"keep-bank-marks");return f;
    }
    @Test public void mainTabCaptureSaveAndReturnPreserveProgressAndUseTheCapturedBankSetup() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=preset();ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);List<BankSetupRequest> banks=new ArrayList<>();
            panel.setSupplyActions((plan,focus)->banks.add(panel.bankSetupRequest(plan,focus)),()->{});
            panel.setInventoryDataRequest(request->panel.setInventoryData(request,BackpackSetupTest.data(request.items)));
            SupplySnapshot captured=BackpackSetupTest.snapshot(BackpackSetupTest.backpack());panel.setEquipmentReader(ready->ready.accept(captured));
            try
            {
                click(panel,"editInventorySetup");assertNotNull(find(panel,"setupEditor"));assertNull(find(panel,"startRun"));
                click(panel,"captureSetup");assertEquals("",f.config.supplyInventory());assertEquals("",new PresetLibrary(f).config(f.config.runName()).supplyInventory());
                assertTrue(((AbstractButton)find(panel,"saveSetup")).isEnabled());click(panel,"saveSetup");
                assertEquals(BackpackSetupTest.backpack().encode(),f.config.supplyInventory());assertEquals(EquipmentSetupTest.gear().encode(),f.config.supplyEquipment());
                assertNotNull(find(panel,"setupEditor"));assertFalse(((AbstractButton)find(panel,"saveSetup")).isEnabled());
                click(panel,"setupBack");assertNull(find(panel,"setupEditor"));assertNotNull(find(panel,"capturedBackpack"));
                assertEquals("LUMBRIDGE_TREE",f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertEquals("keep-bank-marks",f.get(InventoryLoadPlanner.BANKS_KEY));
                assertNotNull(find(panel,"supplyItem_"+ItemID.PLANTPOT_OAK_SAPLING));
                click(panel,"packInventory");assertTrue(banks.get(banks.size()-1).items.contains(ItemID.COCONUT));
                panel.setSupplies(new SupplySnapshot(true,Map.of(),Map.of()));assertEquals(BackpackSetupTest.backpack().encode(),f.config.supplyInventory());
            }
            finally {panel.dispose();}
        });
    }
    @Test public void discardingADirtyPreviewDoesNotSaveAndLiveContainerChangesNeverOverwriteIt() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=preset();ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            panel.setEquipmentReader(ready->ready.accept(BackpackSetupTest.snapshot(BackpackSetupTest.backpack())));
            try
            {
                click(panel,"editInventorySetup");click(panel,"captureSetup");panel.setSupplies(SupplySnapshot.UNKNOWN);
                click(panel,"setupBack");assertNotNull(find(panel,"setupEditor"));assertTrue(find(panel,"discardSetup").isVisible());
                click(panel,"discardSetup");assertNull(find(panel,"setupEditor"));assertEquals("",f.config.supplyInventory());
                assertEquals("",new PresetLibrary(f).config(f.config.runName()).supplyInventory());assertEquals("LUMBRIDGE_TREE",f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            }
            finally {panel.dispose();}
        });
    }
    @Test public void loggedOutAndLateCapturesCannotReplaceADifferentOrClosedEditor() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            SavedPackingSetup initial=new SavedPackingSetup(BackpackSetupTest.backpack(),EquipmentSetupTest.gear(),ClassicTreeRunConfig.SetupBankView.FILTER);
            List<Consumer<SupplySnapshot>> pending=new ArrayList<>();List<SavedPackingSetup> saves=new ArrayList<>();int[] back={0};
            InventorySetupEditor editor=new InventorySetupEditor("Mine",initial,pending::add,(expected,draft)->saves.add(draft),()->back[0]++,()->{},(id,label)->{});
            click(editor,"captureSetup");pending.get(0).accept(SupplySnapshot.UNKNOWN);assertFalse(editor.dirty());
            click(editor,"captureSetup");click(editor,"setupBack");assertEquals(1,back[0]);
            pending.get(1).accept(BackpackSetupTest.snapshot(BackpackSetup.parse("v1|")));assertFalse(editor.dirty());
            click(editor,"captureSetup");editor.dispose();pending.get(2).accept(BackpackSetupTest.snapshot(BackpackSetup.parse("v1|")));assertFalse(editor.dirty());assertTrue(saves.isEmpty());
        });
    }
    @Test public void clearingCapturedSetupRequiresSaveAndReturnsToCalculatedSupplies() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=preset();f.set("supplyInventory",BackpackSetupTest.backpack().encode());f.set("supplyEquipment",EquipmentSetupTest.gear().encode());new PresetLibrary(f).save(f.config.runName(),f.config);
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            try
            {
                click(panel,"editInventorySetup");click(panel,"useCalculatedSetup");assertTrue(BackpackSetup.read(f.config).captured);
                click(panel,"saveSetup");assertFalse(BackpackSetup.read(f.config).captured);assertEquals("",f.config.supplyEquipment());
                click(panel,"setupBack");assertNull(find(panel,"capturedBackpack"));assertNotNull(find(panel,"supplyItem_"+ItemID.PLANTPOT_OAK_SAPLING));
            }
            finally {panel.dispose();}
        });
    }
    @Test public void failedSavesKeepTheDraftAndRenderTheEditorAtSidebarWidth() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            SavedPackingSetup initial=new SavedPackingSetup(BackpackSetup.NONE,EquipmentSetup.EMPTY,ClassicTreeRunConfig.SetupBankView.LAYOUT);
            boolean[] fail={true};
            InventorySetupEditor editor=new InventorySetupEditor("My farming run",initial,ready->ready.accept(BackpackSetupTest.snapshot(BackpackSetupTest.backpack())),
                (expected,draft)->{if(fail[0])throw new IllegalStateException("Could not save this setup.");},()->{},()->{},InventorySetupEditorTest::sampleIcon);
            try
            {
                click(editor,"captureSetup");editor.setData(BackpackSetupTest.data(editor.items()));click(editor,"saveSetup");assertTrue(editor.dirty());
                assertTrue(((JLabel)find(editor,"setupEditorStatus")).getText().contains("Could not save"));
                fail[0]=false;click(editor,"saveSetup");assertFalse(editor.dirty());
                int height=editor.getPreferredSize().height;editor.setSize(225,height);layout(editor);
                for(Component child:editor.getComponents())assertTrue("Control exceeds sidebar: "+child.getName(),child.getX()+child.getWidth()<=225);
                BufferedImage image=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=image.createGraphics();
                try{graphics.setColor(new java.awt.Color(32,32,32));graphics.fillRect(0,0,225,height);editor.printAll(graphics);}finally{graphics.dispose();}
                try{Path path=Path.of("build/reports/setup-editor-preview.png");Files.createDirectories(path.getParent());ImageIO.write(image,"png",path.toFile());}
                catch(java.io.IOException error){throw new AssertionError(error);}
            }
            finally {editor.dispose();}
        });
    }
    private static void sampleIcon(int id,JLabel label)
    {
        BufferedImage image=new BufferedImage(36,32,BufferedImage.TYPE_INT_ARGB);Graphics2D graphics=image.createGraphics();
        try{graphics.setColor(new java.awt.Color(135+(id%80),170,110));graphics.fillOval(7,4,24,24);}finally{graphics.dispose();}
        label.setIcon(new ImageIcon(image));
    }
    private static void click(Container root,String name){Component found=find(root,name);assertNotNull(name,found);((AbstractButton)found).doClick();}
    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {if(name.equals(child.getName()))return child;if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}}
        return null;
    }
    private static void layout(Container parent){parent.doLayout();for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child);}
}
