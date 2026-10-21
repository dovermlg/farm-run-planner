package com.classictreerun;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import org.junit.Test;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.OverlayPosition;
import static org.junit.Assert.*;

public class RunStepOverlayTest
{
    @Test public void patchStepsFollowSidebarCompletionResetAndTheSetupEditor() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=light();f.set("selectionV2","LUMBRIDGE_TREE,VARROCK_TREE");f.set("order","LUMBRIDGE_TREE,VARROCK_TREE");
            f.set("runName","Tree run");new PresetLibrary(f).save("Tree run",f.config);
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);panel.setConfirmReset(false);
            try
            {
                assertNull("Selecting a preset alone does not start on-screen directions",panel.runStep());
                assertEquals(Color.DARK_GRAY,patchBorder(panel,"LUMBRIDGE_TREE"));
                click(panel,"startRun");
                assertEquals("Stop run",((AbstractButton)find(panel,"startRun")).getText());
                assertEquals(NextTeleportOverlay.COLOR,patchBorder(panel,"LUMBRIDGE_TREE"));
                RunStep first=panel.runStep();assertEquals(RunStep.Kind.PATCH,first.kind);
                assertTrue(first.title.contains("Lumbridge"));assertEquals(TravelGuide.instructions(Patch.LUMBRIDGE_TREE,f.config),first.instructions);
                click(panel,"LUMBRIDGE_TREE");assertTrue(panel.runStep().title.contains("Varrock"));assertEquals("1 / 2 patches complete",panel.runStep().progress);
                assertEquals(Color.DARK_GRAY,patchBorder(panel,"LUMBRIDGE_TREE"));
                assertEquals(NextTeleportOverlay.COLOR,patchBorder(panel,"VARROCK_TREE"));
                String keptProgress=f.get(ClassicTreeRunPanel.PROGRESS_KEY);
                f.set(InventoryLoadPlanner.BANKS_KEY,"keep-bank-marks");
                panel.setRoutePreparing(true);panel.reload();
                assertTrue("Stopping stays available during planning",find(panel,"startRun").isEnabled());
                click(panel,"startRun");
                assertEquals("Start run",((AbstractButton)find(panel,"startRun")).getText());
                assertNull(panel.runStep());assertEquals(TeleportDevice.NONE,panel.nextTeleport());
                assertEquals(Color.DARK_GRAY,patchBorder(panel,"VARROCK_TREE"));
                assertEquals(keptProgress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));
                assertEquals("keep-bank-marks",f.get(InventoryLoadPlanner.BANKS_KEY));
                panel.setRoutePreparing(false);panel.reload();click(panel,"startRun");
                assertEquals("Stop run",((AbstractButton)find(panel,"startRun")).getText());
                click(panel,"LUMBRIDGE_TREE");
                assertTrue(first.title.contains("Lumbridge")); // Old published values are immutable.
                click(panel,"VARROCK_TREE");assertNull("Completing the final patch hides directions",panel.runStep());
                assertEquals("Start run",((AbstractButton)find(panel,"startRun")).getText());
                click(panel,"VARROCK_TREE");assertNull("Undo does not start the overlay again",panel.runStep());
                assertEquals(Color.DARK_GRAY,patchBorder(panel,"VARROCK_TREE"));
                click(panel,"resetRun");assertNull(panel.runStep());
                assertEquals(Color.DARK_GRAY,patchBorder(panel,"LUMBRIDGE_TREE"));
                click(panel,"startRun");assertTrue(panel.runStep().title.contains("Lumbridge"));
                click(panel,"editInventorySetup");assertNull(panel.runStep());click(panel,"setupBack");assertNotNull(panel.runStep());
                panel.setRoutePreparing(true);panel.reload();assertEquals(RunStep.Kind.PREPARING,panel.runStep().kind);
                panel.setRoutePreparing(false);panel.reload();
                ((javax.swing.JComboBox<?>)find(panel,"savedPresets")).setSelectedItem("Tree run");
                assertNull("Loading even the same saved preset requires Start run again",panel.runStep());
                assertEquals(Color.DARK_GRAY,patchBorder(panel,"LUMBRIDGE_TREE"));
            }
            finally{panel.dispose();assertNull(panel.runStep());}
        });
    }

    @Test public void bankStepWaitsForAcknowledgementAndUsesBankDepartureAfterwards()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","27");
        List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,InventoryFixtures.data(f.config,route));
        RouteState state=new RouteState();state.setDone(Patch.LUMBRIDGE_TREE,true);
        RunStep bank=RunStep.current(f.config,route,state,plan,"",null,1000,false,false);
        assertEquals(RunStep.Kind.BANK,bank.kind);assertTrue(bank.notes.contains("inventory 2 of 2"));
        assertEquals(f.config.loadBank().instructions(Patch.LUMBRIDGE_TREE,f.config),bank.instructions);
        RunStep after=RunStep.current(f.config,route,state,plan,plan.confirmBank("",1),null,1000,false,false);
        assertEquals(RunStep.Kind.PATCH,after.kind);assertTrue(after.title.contains("Varrock"));
        String departure=f.config.loadBank().departureInstructions(Patch.VARROCK_TREE,f.config);
        assertEquals(departure==null?TravelGuide.instructions(Patch.VARROCK_TREE,f.config):departure,after.instructions);
    }

    @Test public void walkingInstructionsAndGrowthCountdownStayInSync()
    {
        ConfigFixture f=light();List<Patch> route=List.of(Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE);
        RouteState state=new RouteState();state.setDone(Patch.FOSSIL_EAST,true);
        CropGrowth growth=new CropGrowth(new CropReference());growth.observe(Patch.FOSSIL_MIDDLE,14,1000,false);
        RunStep first=RunStep.current(f.config,route,state,null,"",growth,1000,false,false);
        assertEquals(TravelCosts.walkingInstruction(Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE,f.config),first.instructions);
        assertTrue(first.crop.contains("teak"));
        RunStep later=RunStep.current(f.config,route,state,null,"",growth,1600,false,false);assertNotEquals(first.crop,later.crop);
        f.set("trackGrowth","false");f.set("showDetails","true");
        RunStep hidden=RunStep.current(f.config,route,state,null,"",growth,1600,false,false);assertEquals("",hidden.crop);assertTrue(hidden.notes.contains("Gardener"));
    }

    @Test public void growingHardwoodsAndEmptySelectionsHaveDistinctOverlayStates() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=light();f.set("selectionV2","FOSSIL_EAST");ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            try
            {
                CropGrowth growth=new CropGrowth(new CropReference());growth.observe(Patch.FOSSIL_EAST,8,1000,false);panel.setGrowth(growth,1000);
                assertNull("A waiting preset has not been started",panel.runStep());
                f.set("selectionV2","-");panel.reload();assertNull(panel.runStep());
            }
            finally{panel.dispose();}
        });
    }

    @Test public void renderingRespectsLoginToggleAndMissingProfileAndProducesAMovablePreview() throws Exception
    {
        ConfigFixture f=light();f.set("runName","Trees and herbs");
        CropGrowth growth=new CropGrowth(new CropReference());growth.observe(Patch.LUMBRIDGE_TREE,48,1000,false);
        AtomicReference<RunStep> step=new AtomicReference<>(RunStep.current(f.config,List.of(Patch.LUMBRIDGE_TREE),new RouteState(),null,"",growth,1000,false,false));
        AtomicReference<GameState> state=new AtomicReference<>(GameState.LOGGED_IN);
        Client client=(Client)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Client.class},
            (p,m,a)->{if(m.getName().equals("getGameState"))return state.get();throw new AssertionError("Unexpected game access: "+m.getName());});
        RunStepOverlay overlay=new RunStepOverlay(client,f.config,step::get);
        assertEquals(OverlayPosition.TOP_LEFT,overlay.getPosition());assertTrue(overlay.isMovable());assertTrue(overlay.isResizable());
        BufferedImage image=new BufferedImage(280,420,BufferedImage.TYPE_INT_ARGB);Graphics2D graphics=image.createGraphics();
        try
        {
            graphics.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
            Dimension size=null;for(int i=0;i<3;i++)size=overlay.render(graphics);
            assertNotNull(size);assertTrue(size.width<=240);assertTrue(size.height>100);assertTrue(size.height<420);
            assertTrue(overlay.getPanelComponent().getChildren().isEmpty());
            Path output=Path.of("build/reports/run-step-overlay-preview.png");Files.createDirectories(output.getParent());
            ImageIO.write(image.getSubimage(0,0,size.width,size.height),"png",output.toFile());
            state.set(GameState.LOGIN_SCREEN);assertNull(overlay.render(graphics));
            state.set(GameState.LOGGED_IN);f.set("showRunOverlay","false");assertNull(overlay.render(graphics));
            f.set("showRunOverlay","true");step.set(null);assertNull(overlay.render(graphics));
        }
        finally{graphics.dispose();}
    }

    @Test public void plainTextAndLongUnbrokenNamesFitWithoutInterpretingOverlayTags()
    {
        BufferedImage image=new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);Graphics2D graphics=image.createGraphics();
        try
        {
            String value=RunStepOverlay.wrapLongWords("<col=ff0000>"+"A".repeat(60)+"</col>\n"+"B".repeat(500),graphics.getFontMetrics(),180);
            assertFalse(value.contains("<col"));assertFalse(value.contains("\n"));
            for(String word:value.split(" "))assertTrue(graphics.getFontMetrics().stringWidth(word)<=180);
        }
        finally{graphics.dispose();}
    }

    @Test public void firstLoadAndResetWithoutSavedDimensionsRenderAndKeepTheDefaultWidth()
    {
        ConfigFixture f=light();
        RunStep step=RunStep.current(f.config,List.of(Patch.LUMBRIDGE_TREE),new RouteState(),null,"",null,1000,false,false);
        Client client=(Client)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Client.class},
            (p,m,a)->{if(m.getName().equals("getGameState"))return GameState.LOGGED_IN;throw new AssertionError(m.getName());});
        RunStepOverlay overlay=new RunStepOverlay(client,f.config,()->step);
        BufferedImage image=new BufferedImage(600,600,BufferedImage.TYPE_INT_ARGB);Graphics2D graphics=image.createGraphics();
        try
        {
            // OverlayManager.loadOverlay passes null when the player has no saved panel size.
            overlay.setPreferredSize(null);
            assertNull(overlay.getPreferredSize());
            for(int i=0;i<3;i++)assertNotNull(overlay.render(graphics));
            assertEquals(240,overlay.getPanelComponent().getBounds().width);
            overlay.setPreferredSize(new Dimension(320,0));
            for(int i=0;i<3;i++)assertNotNull(overlay.render(graphics));
            assertEquals(320,overlay.getPanelComponent().getBounds().width);
            // RuneLite's reset restores the null (unsized) state after the overlay was resized.
            overlay.setPreferredSize(null);
            for(int i=0;i<3;i++)assertNotNull(overlay.render(graphics));
            assertEquals(240,overlay.getPanelComponent().getBounds().width);
        }
        finally{graphics.dispose();}
    }

    @Test public void overlayToggleIsAGlobalDisplaySettingRatherThanASavedPresetOption()
    {
        ConfigFixture f=light();assertTrue(f.config.showRunOverlay());f.set("showRunOverlay","false");
        assertFalse(LocalRunConfig.snapshot(f.config).showRunOverlay());
        new PresetLibrary(f).save("Trees",f.config);f.set("showRunOverlay","true");new PresetLibrary(f).load("Trees",true);
        assertTrue(f.config.showRunOverlay());
    }

    private static ConfigFixture light()
    {ConfigFixture f=new ConfigFixture();f.set("loadPlanning","false");f.set("packingView","LIST");f.set("supplyBankView","OFF");return f;}
    private static void click(Container root,String name)
    {
        for(Component child:root.getComponents())
        {if(name.equals(child.getName())){((AbstractButton)child).doClick();return;}if(child instanceof Container && contains((Container)child,name)){click((Container)child,name);return;}}
        fail("Missing control: "+name);
    }
    private static boolean contains(Container root,String name)
    {for(Component child:root.getComponents()){if(name.equals(child.getName()))return true;if(child instanceof Container && contains((Container)child,name))return true;}return false;}
    private static Component find(Container root,String name)
    {for(Component child:root.getComponents()){if(name.equals(child.getName()))return child;if(child instanceof Container){Component match=find((Container)child,name);if(match!=null)return match;}}return null;}
    private static Color patchBorder(Container root,String patch)
    {
        javax.swing.JComponent card=(javax.swing.JComponent)find(root,patch).getParent().getParent();
        javax.swing.border.CompoundBorder border=(javax.swing.border.CompoundBorder)card.getBorder();
        return ((javax.swing.border.LineBorder)border.getOutsideBorder()).getLineColor();
    }
}
