package com.classictreerun;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplyGridTest
{
    @Test public void layoutUsesOneSlotPerSaplingAndOnePerStackAndKeepsLargeListsBounded()
    {
        ConfigFixture f=InventoryFixtures.light();List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.FALADOR_ALLOTMENT_NW);
        InventoryItemData data=InventoryFixtures.data(f.config,route);SupplyPlan plan=SupplyPlan.forRoute(f.config,route,false,false);
        PackingLayout layout=PackingLayout.build(plan,data);assertEquals(3,layout.total);assertEquals(3,layout.cells.size());
        assertFalse(layout.cells.get(0).stack);assertEquals(1,layout.cells.get(0).quantity);
        assertEquals(0,layout.cells.get(1).requirement);assertEquals(1,layout.cells.get(1).index);
        assertTrue(layout.cells.get(2).stack);assertEquals(3,layout.cells.get(2).quantity);
        SupplyPlan huge=new SupplyPlan(List.of(new SupplyPlan.Requirement("Many saplings",ItemID.PLANTPOT_OAK_SAPLING,Integer.MAX_VALUE,false)),List.of());
        layout=PackingLayout.build(huge,data);assertEquals(Integer.MAX_VALUE,layout.total);assertEquals(28,layout.cells.size());
    }

    @Test public void notedPaymentsUseTheirNoteSpriteAndOneStackInsteadOfManyInventorySlots()
    {
        SupplyPlan plan=new SupplyPlan(List.of(new SupplyPlan.Requirement("Coconut",ItemID.COCONUT,25,true)),List.of());
        InventoryItemData data=new InventoryItemData(Map.of(ItemID.COCONUT,new InventoryItemData.ItemInfo(false,true,"Coconut",12345)));
        plan=plan.forInventory(true,data);PackingLayout layout=PackingLayout.build(plan,data);
        assertEquals(1,layout.total);assertEquals(12345,layout.cells.get(0).item);assertEquals(25,layout.cells.get(0).quantity);
        assertFalse(layout.cells.get(0).packed(24));assertTrue(layout.cells.get(0).packed(25));
    }

    @Test public void gridShowsPartialStacksPerItemShortagesAndUnknownStateWithoutRebuildingSlots() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.FALADOR_ALLOTMENT_NW);
            SupplyPlan plan=SupplyPlan.forRoute(f.config,route,false,false);
            SupplyGrid grid=new SupplyGrid(plan,InventoryFixtures.data(f.config,route),8,
                new SupplySnapshot(true,Map.of(ItemID.PLANTPOT_OAK_SAPLING,1L,ItemID.POTATO_SEED,2L),Map.of()),(id,label)->{ });
            JPanel cells=(JPanel)find(grid,"inventorySlots");assertEquals(28,cells.getComponentCount());
            JLabel first=(JLabel)find(grid,"inventorySlot_0"),second=(JLabel)find(grid,"inventorySlot_1"),seed=(JLabel)find(grid,"inventorySlot_2");
            assertEquals(PackingItemSlot.PACKED,first.getBackground());assertEquals(PackingItemSlot.MISSING,second.getBackground());
            assertEquals(PackingItemSlot.MISSING,seed.getBackground());assertTrue(seed.getToolTipText().contains("Short 1"));
            assertTrue(((JLabel)find(grid,"inventorySlot_27")).getToolTipText().contains("Reserved"));
            grid.refresh(new SupplySnapshot(true,Map.of(ItemID.PLANTPOT_OAK_SAPLING,2L,ItemID.POTATO_SEED,3L),Map.of()));
            assertSame(first,find(grid,"inventorySlot_0"));assertEquals(PackingItemSlot.PACKED,seed.getBackground());
            grid.refresh(SupplySnapshot.UNKNOWN);assertEquals(PackingItemSlot.UNKNOWN,first.getBackground());
            assertTrue(first.getToolTipText().contains("Log in"));
            grid.setSize(225,grid.getPreferredSize().height);layout(grid);
            for(Component slot:cells.getComponents())assertTrue(slot.getX()+slot.getWidth()<=225);
        });
    }

    @Test public void emptyMetadataNeverPretendsToKnowStackabilityAndOverflowIsVisible() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
            SupplyPlan plan=SupplyPlan.forRoute(f.config,route,false,false);
            SupplyGrid waiting=new SupplyGrid(plan,InventoryItemData.EMPTY,0,SupplySnapshot.UNKNOWN,(id,label)->{ });
            assertNotNull(find(waiting,"gridWaiting"));assertNull(find(waiting,"inventorySlots"));
            SupplyPlan huge=new SupplyPlan(List.of(new SupplyPlan.Requirement("Saplings",ItemID.PLANTPOT_OAK_SAPLING,1000,false)),List.of());
            SupplyGrid overflow=new SupplyGrid(huge,InventoryFixtures.data(f.config,route),0,SupplySnapshot.UNKNOWN,(id,label)->{ });
            assertEquals(28,((JPanel)find(overflow,"inventorySlots")).getComponentCount());
            assertTrue(((JLabel)find(overflow,"packingOverflow")).getText().contains("972 additional"));
        });
    }

    @Test public void plannedWornToolMovesToEquipmentAndCarryingItDoesNotSatisfyTheWornSlot() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("supplyTools","true");f.set("supplyEquipment",EquipmentSetupTest.gear().encode());
            List<Patch> route=List.of(Patch.TROLL_HERB);SupplyPlan plan=SupplyPlan.forRoute(f.config,route,false,false);
            InventoryItemData data=InventoryFixtures.data(f.config,route);PackingLayout layout=PackingLayout.build(plan,data);
            assertEquals("Seed and spade remain in inventory; secateurs are planned worn",2,layout.total);
            SupplySnapshot carried=new SupplySnapshot(true,Map.of(ItemID.FAIRY_ENCHANTED_SECATEURS,1L),Map.of());
            EquipmentView gear=new EquipmentView(plan.equipment,data,true,carried,(id,label)->{ });
            JLabel weapon=(JLabel)find(gear,"equipmentSlot_WEAPON");assertEquals(PackingItemSlot.MISSING,weapon.getBackground());
            gear.refresh(EquipmentSetupTest.worn(EquipmentSetupTest.gear()));assertEquals(PackingItemSlot.PACKED,weapon.getBackground());
            EquipmentSetup shortAmmo=new EquipmentSetup(Map.of(EquipmentInventorySlot.AMMO,new EquipmentSetup.Entry(ItemID.BRONZE_ARROW,100)));
            gear.refresh(EquipmentSetupTest.worn(shortAmmo));JLabel ammo=(JLabel)find(gear,"equipmentSlot_AMMO");
            assertEquals(PackingItemSlot.MISSING,ammo.getBackground());assertTrue(ammo.getToolTipText().contains("Short 400"));
        });
    }

    @Test public void gridMetadataLoadsWithInventoryPlanningOffAndFailedRequestsCanBeRetried() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("loadPlanning","false");f.set("packingView","GRID");
            InventoryPreparation preparation=new InventoryPreparation(f.config,()->{ });List<InventoryPreparation.ItemRequest> requests=new ArrayList<>();
            preparation.setRequest(requests::add);List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);
            try
            {
                preparation.update(route);assertEquals(1,requests.size());preparation.failed(requests.get(0));
                preparation.update(route);assertEquals("Failure must not create a retry loop",1,requests.size());
                f.set("loadPlanning","true");preparation.update(route);assertEquals(2,requests.size());
                preparation.accept(requests.get(0),InventoryFixtures.data(f.config,route));assertFalse(preparation.data().covers(requests.get(0).items));
                preparation.accept(requests.get(1),InventoryFixtures.data(f.config,route));
                f.set("loadPlanning","false");preparation.update(route);assertNull(preparation.get());
                assertTrue(preparation.data().covers(requests.get(1).items));
            }
            finally {preparation.dispose();}
        });
    }

    @Test public void switchingViewPreservesProgressAndEquipmentRefreshDetectsMovingCarriedGearIntoItsSlot() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("loadPlanning","false");f.set("selectionV2","LUMBRIDGE_TREE,VARROCK_TREE");
            f.set("order","LUMBRIDGE_TREE,VARROCK_TREE");f.set("supplyEquipment",EquipmentSetupTest.gear().encode());
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            try
            {
                panel.setConfirmReset(false);panel.setInventoryDataRequest(request->panel.setInventoryData(request,
                    InventoryFixtures.data(f.config,List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE))));
                assertFalse(find(panel,"packingBody").isVisible());
                assertSame(find(panel,"packingBody"),find(panel,"packingView").getParent());
                AbstractButton eye=(AbstractButton)find(panel,"showPackingChecklist");
                assertNotNull(eye.getIcon());assertEquals("Show packing checklist",eye.getAccessibleContext().getAccessibleName());
                eye.doClick();assertTrue(find(panel,"packingBody").isVisible());
                ((AbstractButton)find(panel,"LUMBRIDGE_TREE")).doClick();String progress=f.get(ClassicTreeRunPanel.PROGRESS_KEY);
                ((JComboBox<?>)find(panel,"packingView")).setSelectedItem(ClassicTreeRunConfig.PackingView.GRID);
                assertEquals(progress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertEquals(ClassicTreeRunConfig.PackingView.GRID,f.config.packingView());
                assertTrue("Progress and view refreshes keep the section expanded",find(panel,"packingBody").isVisible());
                ((AbstractButton)find(panel,"showPackingChecklist")).doClick();
                assertFalse(find(panel,"packingBody").isVisible());assertEquals(progress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));
                SupplySnapshot equipped=EquipmentSetupTest.worn(EquipmentSetupTest.gear());
                panel.setSupplies(new SupplySnapshot(true,equipped.plain,Map.of()));
                JLabel weapon=(JLabel)find(panel,"equipmentSlot_WEAPON");assertEquals(PackingItemSlot.MISSING,weapon.getBackground());
                panel.setSupplies(equipped);assertEquals(PackingItemSlot.PACKED,weapon.getBackground());
                assertSame(weapon,find(panel,"equipmentSlot_WEAPON"));
            }
            finally {panel.dispose();}
        });
    }

    @Test public void renderGeneratedPackingAndEquipmentAtSidebarWidth() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("supplyTeleports","true");f.set("supplyTools","true");f.set("supplyRake","true");
            f.set("supplyCompost","BOTTOMLESS");f.set("supplyCoins","10000");f.set("supplyEquipment",EquipmentSetupTest.gear().encode());
            List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.FALADOR_TREE,Patch.TROLL_HERB,Patch.WEISS_HERB);
            SupplyPlan plan=SupplyPlan.forRoute(f.config,route,false,false);InventoryItemData data=InventoryFixtures.data(f.config,route);
            SupplySnapshot carried=new SupplySnapshot(true,Map.of(ItemID.PLANTPOT_OAK_SAPLING,2L,ItemID.GUAM_SEED,1L,ItemID.COINS,10000L,
                ItemID.SPADE,1L,ItemID.POH_TABLET_LUMBRIDGETELEPORT,1L),Map.of(),
                Map.of(EquipmentInventorySlot.WEAPON,new EquipmentSetup.Entry(ItemID.FAIRY_ENCHANTED_SECATEURS,1)));
            JPanel preview=new JPanel();preview.setBackground(new Color(32,32,32));preview.setLayout(new javax.swing.BoxLayout(preview,javax.swing.BoxLayout.Y_AXIS));
            JLabel title=new JLabel("Offline preview - sample sprites");title.setForeground(Color.GRAY);preview.add(title);
            java.util.function.BiConsumer<Integer,JLabel> icons=SupplyGridTest::sampleIcon;
            preview.add(new SupplyGrid(plan,data,8,carried,icons));preview.add(new EquipmentView(plan.equipment,data,true,carried,icons));
            int height=preview.getPreferredSize().height;preview.setSize(225,height);layout(preview);
            assertTrue(height>450 && height<750);
            BufferedImage image=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();
            try{preview.printAll(g);}finally{g.dispose();}
            try
            {
                Path file=Path.of("build/reports/packing-grid-preview.png");Files.createDirectories(file.getParent());ImageIO.write(image,"png",file.toFile());
            }
            catch(java.io.IOException error){throw new AssertionError(error);}
        });
    }

    /** Deliberate geometric fixture sprites. Production uses RuneLite's ItemManager. */
    private static void sampleIcon(int id,JLabel label)
    {
        BufferedImage image=new BufferedImage(36,32,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try
        {
            if(id==ItemID.PLANTPOT_OAK_SAPLING)
            {g.setColor(new Color(163,108,54));g.fillPolygon(new int[]{10,27,24,13},new int[]{19,19,30,30},4);g.setColor(new Color(90,162,69));g.fillOval(10,4,18,15);}
            else if(id==ItemID.COINS)
            {g.setColor(new Color(223,183,57));g.fillOval(6,14,14,10);g.fillOval(16,8,14,10);g.fillOval(16,19,14,10);}
            else if(id==ItemID.POH_TABLET_LUMBRIDGETELEPORT || id==ItemID.POH_TABLET_VARROCKTELEPORT)
            {g.setColor(new Color(110,105,169));g.fillOval(5,4,27,25);g.setColor(new Color(125,179,221));g.drawOval(10,9,16,15);}
            else
            {g.setColor(new Color(160,166,164));g.fillRoundRect(10,8,18,18,5,5);g.setColor(new Color(93,106,100));g.drawLine(6,28,28,6);}
        }
        finally {g.dispose();}
        label.setIcon(new ImageIcon(image));
    }
    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {if(name.equals(child.getName()))return child;if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}}
        return null;
    }
    private static void layout(Container parent)
    {parent.doLayout();for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child);}
}
