package com.classictreerun;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryRunSidebarTest
{
    @Test public void itemDataFailureIsVisibleAndTogglingPlanningCanRetry() throws Exception
    {
        ConfigFixture f=InventoryFixtures.light();f.set("selectionV2","LUMBRIDGE_TREE");
        ClassicTreeRunPanel[] panel={null};int[] requested={0};
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new ClassicTreeRunPanel(f.config,f);panel[0].setInventoryDataRequest(ids->requested[0]++);
            assertEquals(1,requested[0]);panel[0].setInventoryDataError();
            assertTrue(labels(panel[0]).contains("Toggle Plan inventory loads off and on"));
            assertFalse(((AbstractButton)find(panel[0],"startRun")).isEnabled());
            panel[0].reload();assertEquals(1,requested[0]);
            f.set("loadPlanning","false");panel[0].reload();
            assertTrue(((AbstractButton)find(panel[0],"startRun")).isEnabled());
            f.set("loadPlanning","true");panel[0].reload();assertEquals(2,requested[0]);
            panel[0].setInventoryData(InventoryFixtures.data(f.config,List.of(Patch.LUMBRIDGE_TREE)));
        });
        awaitReady(panel[0]);
        SwingUtilities.invokeAndWait(()->{assertTrue(((AbstractButton)find(panel[0],"startRun")).isEnabled());panel[0].dispose();});
    }
    @Test public void filtersOnlyCurrentInventoryAndBankingDoesNotResetPatchProgress() throws Exception
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","26");f.set("loadBank","CATHERBY");
        List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.FALADOR_TREE,Patch.TAVERLEY_TREE);
        f.set("selectionV2",RouteState.encode(route));f.set("order",RouteState.encode(route));
        InventoryItemData data=InventoryFixtures.data(f.config,route);
        ClassicTreeRunPanel[] panel={null};List<SupplyPlan> filters=new ArrayList<>();
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new ClassicTreeRunPanel(f.config,f);panel[0].setConfirmReset(false);
            panel[0].setSupplyActions(filters::add,()->{ });panel[0].setInventoryData(data);
        });
        awaitReady(panel[0]);
        SwingUtilities.invokeAndWait(()->{
            assertTrue(((JLabel)find(panel[0],"inventoryLoad")).getText().contains("Inventory 1 of 2"));
            ((AbstractButton)find(panel[0],"startRun")).doClick();
            assertEquals(2,quantity(filters.get(0),ItemID.PLANTPOT_OAK_SAPLING));
            ((AbstractButton)find(panel[0],"LUMBRIDGE_TREE")).doClick();
            assertEquals("1 x Oak sapling",((JLabel)find(panel[0],"supplyItem_"+ItemID.PLANTPOT_OAK_SAPLING)).getText());
            ((AbstractButton)find(panel[0],"VARROCK_TREE")).doClick();
            assertTrue(labels(panel[0]).contains("NEXT: Bank at Catherby for inventory 2"));
            assertEquals("2 x Oak sapling",((JLabel)find(panel[0],"supplyItem_"+ItemID.PLANTPOT_OAK_SAPLING)).getText());
            String progress=f.get(ClassicTreeRunPanel.PROGRESS_KEY);
            ((AbstractButton)find(panel[0],"packInventory")).doClick();
            assertEquals(progress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertEquals(2,quantity(filters.get(1),ItemID.PLANTPOT_OAK_SAPLING));
            ((AbstractButton)find(panel[0],"bankReady_1")).doClick();
            assertEquals(progress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            assertTrue(labels(panel[0]).contains("NEXT: Falador Park"));
            assertTrue(f.get(InventoryLoadPlanner.BANKS_KEY).contains("FALADOR_TREE"));
            capture(panel[0]);panel[0].dispose();
            panel[0]=new ClassicTreeRunPanel(f.config,f);panel[0].setConfirmReset(false);panel[0].setInventoryData(data);
        });
        awaitReady(panel[0]);
        SwingUtilities.invokeAndWait(()->{
            assertFalse(((AbstractButton)find(panel[0],"bankReady_1")).isEnabled());
            ((AbstractButton)find(panel[0],"VARROCK_TREE")).doClick();
            ((AbstractButton)find(panel[0],"VARROCK_TREE")).doClick();
            assertTrue(((AbstractButton)find(panel[0],"bankReady_1")).isEnabled());
            ((AbstractButton)find(panel[0],"resetRun")).doClick();
            assertEquals("",f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertEquals("",f.get(InventoryLoadPlanner.BANKS_KEY));
            assertTrue(((JLabel)find(panel[0],"inventoryLoad")).getText().contains("Inventory 1 of 2"));panel[0].dispose();
        });
    }

    @Test public void bankingBreaksPreviousPatchWalkingInstructions() throws Exception
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","27");
        List<Patch> route=List.of(Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE,Patch.FOSSIL_WEST);
        f.set("selectionV2",RouteState.encode(route));f.set("order",RouteState.encode(route));
        ClassicTreeRunPanel[] panel={null};
        SwingUtilities.invokeAndWait(()->{panel[0]=new ClassicTreeRunPanel(f.config,f);panel[0].setInventoryData(InventoryFixtures.data(f.config,route));});
        awaitReady(panel[0]);
        SwingUtilities.invokeAndWait(()->{
            assertNotNull(find(panel[0],"bankStop_1"));assertNotNull(find(panel[0],"bankStop_2"));
            assertFalse(labels(panel[0]).contains("Stay at the Fossil Island hardwood cluster"));
            assertTrue(labels(panel[0]).contains("Digsite pendant"));panel[0].dispose();
        });
    }

    @Test public void waitsForRealItemMetadataRatherThanClaimingAnUncheckedLoadFits() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("selectionV2","LUMBRIDGE_TREE");
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            assertFalse(((AbstractButton)find(panel,"startRun")).isEnabled());
            assertFalse(((AbstractButton)find(panel,"packInventory")).isEnabled());
            assertNull(find(panel,"inventoryLoad"));assertTrue(labels(panel).contains("RuneLite item data"));panel.dispose();
        });
    }

    private static int quantity(SupplyPlan plan,int item)
    {return plan.requirements.stream().filter(r->r.item==item).findFirst().orElseThrow().quantity;}
    private static void awaitReady(ClassicTreeRunPanel panel) throws Exception
    {
        for(int i=0;i<500;i++)
        {
            boolean[] ready={false};SwingUtilities.invokeAndWait(()->ready[0]=find(panel,"inventoryLoad")!=null);
            if(ready[0])return;Thread.sleep(10);
        }
        fail("Inventory planning did not complete");
    }
    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {if(name.equals(child.getName()))return child;if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}}
        return null;
    }
    private static String labels(Container parent)
    {
        StringBuilder text=new StringBuilder();for(Component child:parent.getComponents())
        {if(child instanceof JLabel)text.append(((JLabel)child).getText());if(child instanceof Container)text.append(labels((Container)child));}return text.toString();
    }
    private static void layout(Container parent)
    {parent.doLayout();for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child);}
    private static void capture(ClassicTreeRunPanel panel)
    {
        panel.setSize(225,2600);layout(panel);
        BufferedImage preview=new BufferedImage(225,1500,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=preview.createGraphics();
        try{panel.printAll(graphics);}finally{graphics.dispose();}
        try{Path file=Path.of("build/reports/inventory-run-preview.png");Files.createDirectories(file.getParent());ImageIO.write(preview,"png",file.toFile());}
        catch(java.io.IOException error){throw new AssertionError(error);}
    }
}
