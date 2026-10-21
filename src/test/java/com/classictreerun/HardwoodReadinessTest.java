package com.classictreerun;

import java.awt.Component;
import java.awt.Container;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class HardwoodReadinessTest
{
    private final CropReference reference=new CropReference();

    @Test public void everyHardwoodPatchAndRawStateKeepsMatureUnhealthyEmptyAndUnknownStops()
    {
        for(Patch patch:Patch.values())if(patch.getType()==Patch.Type.HARDWOOD)
            for(int raw=-1;raw<=256;raw++)
            {
                CropGrowth growth=new CropGrowth(reference);growth.observe(patch,raw,1000,false);
                HardwoodReadiness plan=new HardwoodReadiness();List<Patch> selected=List.of(patch,Patch.TROLL_HERB);
                plan.update("Test",selected,growth,true,1000,new RouteState());
                // RuneLite's five species use these immature raw ranges; final health-check stages stay included.
                boolean immature=raw>=8 && raw<=14 || raw>=30 && raw<=37 || raw>=55 && raw<=62
                    || raw>=80 && raw<=87 || raw>=105 && raw<=113;
                assertEquals(patch+" raw "+raw,!immature,plan.route(selected).contains(patch));
                assertTrue(plan.route(selected).contains(Patch.TROLL_HERB));
            }
    }

    @Test public void timersRestoreStopsOnlyBeforeStartingAndResetChecksANewCropCycle()
    {
        CropGrowth growth=new CropGrowth(reference);growth.observe(Patch.FOSSIL_EAST,14,1000,false);
        List<Patch> selected=List.of(Patch.FOSSIL_EAST,Patch.TROLL_HERB);RouteState progress=new RouteState();
        HardwoodReadiness plan=new HardwoodReadiness();
        plan.update("Mixed",selected,growth,true,1000,progress);assertEquals(Set.of(Patch.FOSSIL_EAST),plan.skipped());
        plan.lock();plan.update("Mixed",selected,growth,true,39400,progress);assertFalse(plan.route(selected).contains(Patch.FOSSIL_EAST));
        plan.restart();plan.update("Mixed",selected,growth,true,39400,progress);assertEquals(selected,plan.route(selected));
        plan.lock();growth.observe(Patch.FOSSIL_EAST,8,39500,false);
        plan.update("Mixed",selected,growth,true,39500,progress);assertEquals(selected,plan.route(selected));
        plan.restart();plan.update("Mixed",selected,growth,true,39500,progress);assertEquals(List.of(Patch.TROLL_HERB),plan.route(selected));
        plan.update("Mixed",selected,growth,true,39500+7L*640*60,progress);assertEquals(selected,plan.route(selected));
    }

    @Test public void missingDataDisabledTrackingAndClockRollbackDoNotHideTrees()
    {
        List<Patch> selected=List.of(Patch.FOSSIL_EAST);CropGrowth growth=new CropGrowth(reference);
        HardwoodReadiness plan=new HardwoodReadiness();RouteState progress=new RouteState();
        plan.update("Trees",selected,null,true,1000,progress);assertEquals(selected,plan.route(selected));
        growth.observe(Patch.FOSSIL_EAST,8,1000,false);
        plan.update("Trees",selected,growth,true,999,progress);assertEquals(selected,plan.route(selected));
        plan.update("Trees",selected,growth,true,1000,progress);assertTrue(plan.route(selected).isEmpty());
        plan.lock();plan.update("Trees",selected,growth,false,1001,progress);assertEquals(selected,plan.route(selected));
        plan.update("Trees",selected,growth,true,1001,progress);assertTrue(plan.route(selected).isEmpty());
        plan.lock();plan.update("Trees",List.of(Patch.TROLL_HERB),growth,true,1001,progress);assertTrue(plan.skipped().isEmpty());
    }

    @Test public void completedReplantedHardwoodsRemainVisibleWhenResuming()
    {
        List<Patch> selected=List.of(Patch.FOSSIL_EAST,Patch.FOSSIL_WEST,Patch.TROLL_HERB);
        CropGrowth growth=new CropGrowth(reference);for(Patch patch:selected)growth.observe(patch,8,1000,false);
        RouteState progress=new RouteState();progress.setDone(Patch.FOSSIL_EAST,true);
        HardwoodReadiness plan=new HardwoodReadiness();plan.update("Mixed",selected,growth,true,1000,progress);
        assertEquals(List.of(Patch.FOSSIL_EAST,Patch.TROLL_HERB),plan.route(selected));
        plan.update("Mixed",selected,growth,true,9999999,progress);assertEquals(Set.of(Patch.FOSSIL_WEST),plan.skipped());
    }

    @Test public void sidebarAndBankRequirementsFollowEffectiveStopsWithoutChangingPresets() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=light("TROLL_HERB,FOSSIL_EAST,FOSSIL_MIDDLE,FOSSIL_WEST");
            new PresetLibrary(f).save("Mixed",f.config);String saved=f.get(PresetLibrary.KEY),selection=f.config.selection();
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);panel.setConfirmReset(false);
            List<SupplyPlan> bank=new ArrayList<>();panel.setSupplyActions((plan,focus)->bank.add(plan),()->{});
            long[] now={1000};GrowthMonitor monitor=new GrowthMonitor(f.config,f,panel,message->{},reference,()->now[0],false);
            try
            {
                monitor.accept("a",List.of(sample(Patch.FOSSIL_EAST,8),sample(Patch.FOSSIL_MIDDLE,30),sample(Patch.FOSSIL_WEST,8)),false);
                assertEquals(List.of(Patch.TROLL_HERB),panel.plannedRoute());assertNotNull(find(panel,"skippedHardwoods"));
                assertNull(find(panel,Patch.FOSSIL_EAST.name()));assertNull(find(panel,"supplyItem_"+ItemID.PLANTPOT_TEAK_SAPLING));
                assertFalse(bank.isEmpty());assertFalse(bank.get(bank.size()-1).items.contains(ItemID.PLANTPOT_TEAK_SAPLING));
                assertFalse(bank.get(bank.size()-1).items.contains(TeleportDevice.DIGSITE.representative()));
                click(panel,"startRun");now[0]=9999999;monitor.poll();assertEquals(List.of(Patch.TROLL_HERB),panel.plannedRoute());
                click(panel,"resetRun");assertEquals(4,panel.plannedRoute().size());assertNotNull(find(panel,Patch.FOSSIL_EAST.name()));
                assertEquals(selection,f.config.selection());assertEquals(saved,f.get(PresetLibrary.KEY));
            }
            finally{monitor.dispose();panel.dispose();}
        });
    }

    @Test public void hardwoodOnlyRunWaitsForTimerAndDifferentCharactersHaveIndependentReadiness() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=light("FOSSIL_EAST");ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            long[] now={1000};GrowthMonitor monitor=new GrowthMonitor(f.config,f,panel,message->{},reference,()->now[0],false);
            try
            {
                monitor.accept("a",List.of(sample(Patch.FOSSIL_EAST,14)),false);
                assertTrue(panel.plannedRoute().isEmpty());assertFalse(((AbstractButton)find(panel,"startRun")).isEnabled());
                assertTrue(labels(panel).contains("No stops to plan right now"));
                monitor.accept("b",List.of(),false);assertEquals(List.of(Patch.FOSSIL_EAST),panel.plannedRoute());
                monitor.accept("a",List.of(),false);assertTrue(panel.plannedRoute().isEmpty());
                now[0]=39400;monitor.poll();assertEquals(List.of(Patch.FOSSIL_EAST),panel.plannedRoute());
                assertTrue(((AbstractButton)find(panel,"startRun")).isEnabled());
            }
            finally{monitor.dispose();panel.dispose();}
        });
    }

    @Test public void automaticReplantCompletionKeepsThisRunButAFreshRunSkipsItsNewTree() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=light("FOSSIL_EAST,TROLL_HERB");f.set("autoCompleteReplanted","true");
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);panel.setConfirmReset(false);
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,panel,message->{},reference,()->1000,false);
            try
            {
                monitor.accept("a",List.of(sample(Patch.FOSSIL_EAST,17)),false);
                monitor.accept("a",List.of(new GrowthSampler.Sample(Patch.FOSSIL_EAST,8,true)),false);
                assertEquals("FOSSIL_EAST",f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertEquals(2,panel.plannedRoute().size());
                click(panel,"startRun");assertEquals(List.of(Patch.TROLL_HERB),panel.plannedRoute());
                assertEquals("",f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            }
            finally{monitor.dispose();panel.dispose();}
        });
    }

    @Test public void skippedTreesStillBelongToFullPresetReadinessNotifications() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=light("FOSSIL_EAST,TROLL_HERB");new PresetLibrary(f).save("Mixed",f.config);
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);panel.setConfirmReset(false);
            long[] now={1000};List<String> notices=new ArrayList<>();
            GrowthMonitor monitor=new GrowthMonitor(f.config,f,panel,notices::add,reference,()->now[0],false);
            try
            {
                monitor.accept("a",List.of(sample(Patch.FOSSIL_EAST,14),sample(Patch.TROLL_HERB,4)),false);click(panel,"startRun");
                now[0]=5800;monitor.poll();assertTrue(notices.isEmpty());
                assertTrue(labels(panel).contains("Next full run estimated"));
                now[0]=39400;monitor.poll();assertEquals(1,notices.size());assertTrue(notices.get(0).contains("all 2 patches"));
                assertEquals(List.of(Patch.TROLL_HERB),panel.plannedRoute());
            }
            finally{monitor.dispose();panel.dispose();}
        });
    }

    @Test public void allHardwoodSkipCombinationsLeaveAnOptimalRemainingRoute()
    {
        List<Patch> hardwoods=new ArrayList<>();for(Patch patch:Patch.values())if(patch.getType()==Patch.Type.HARDWOOD)hardwoods.add(patch);
        for(int mask=0;mask<(1<<hardwoods.size());mask++)
        {
            CropGrowth growth=new CropGrowth(reference);List<Patch> selected=new ArrayList<>(hardwoods);
            selected.addAll(List.of(Patch.GUILD_TREE,Patch.GUILD_FRUIT,Patch.CATHERBY_FRUIT));
            for(int i=0;i<hardwoods.size();i++)growth.observe(hardwoods.get(i),(mask&(1<<i))!=0?8:15,1000,false);
            HardwoodReadiness readiness=new HardwoodReadiness();readiness.update("Mixed",selected,growth,true,1000,new RouteState());
            List<Patch> remaining=readiness.route(selected);TravelCosts costs=new TravelCosts(remaining,new ClassicTreeRunConfig(){});
            List<Patch> ordered=RouteOptimizer.optimize(remaining,costs.entry,costs.travel);
            assertEquals(new HashSet<>(remaining),new HashSet<>(ordered));assertOptimal(ordered,new ClassicTreeRunConfig(){});
            assertEquals(selected.size()-Integer.bitCount(mask),ordered.size());
        }
    }

    @Test public void skippedSaplingsRemoveUnnecessaryInventoryLoadsAndBankStops() throws Exception
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","26");
        List<Patch> selected=List.of(Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE,Patch.FOSSIL_WEST,Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE);
        f.set("selectionV2",RouteState.encode(selected));f.set("order",RouteState.encode(selected));
        InventoryItemData data=InventoryFixtures.data(f.config,selected);
        assertEquals(3,InventoryLoadPlanner.plan(f.config,selected,data).loads.size());
        ClassicTreeRunPanel[] panel={null};
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new ClassicTreeRunPanel(f.config,f);CropGrowth growth=new CropGrowth(reference);
            for(Patch patch:selected)if(patch.getType()==Patch.Type.HARDWOOD)growth.observe(patch,8,1000,false);
            panel[0].setGrowth(growth,1000);
            panel[0].setInventoryDataRequest(request->panel[0].setInventoryData(request,data));
        });
        try
        {
            boolean[] ready={false};long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            while(!ready[0] && System.nanoTime()<deadline)
            {SwingUtilities.invokeAndWait(()->ready[0]=find(panel[0],"inventoryLoad")!=null);if(!ready[0])Thread.sleep(20);}
            assertTrue(ready[0]);
            SwingUtilities.invokeAndWait(()->{
                assertTrue(((JLabel)find(panel[0],"inventoryLoad")).getText().contains("Inventory 1 of 1"));
                assertNull(find(panel[0],"bankStop_1"));
                assertEquals(2,InventoryLoadPlanner.plan(f.config,panel[0].plannedRoute(),data).loads.get(0).patches.size());
            });
        }
        finally{SwingUtilities.invokeAndWait(()->panel[0].dispose());}
    }

    @Test public void routeOptimizerReceivesRemainingPatchesAndReincludesReadyTrees() throws Exception
    {
        ConfigFixture f=light("GUILD_TREE,GUILD_FRUIT,FOSSIL_EAST,FOSSIL_WEST,CATHERBY_FRUIT");
        f.nativeSelection(RouteState.parse(f.config.selection()));f.set("autoOptimize","true");
        ClassicTreeRunPanel[] panel={null};RunSettings[] settings={null};CropGrowth growth=new CropGrowth(reference);
        growth.observe(Patch.FOSSIL_EAST,8,1000,false);growth.observe(Patch.FOSSIL_WEST,8,1000,false);
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new ClassicTreeRunPanel(f.config,f);panel[0].setGrowth(growth,1000);
            settings[0]=new RunSettings(f.config,f,panel[0]);f.orderSaved=new CountDownLatch(1);settings[0].prepareNow();
        });
        try
        {
            assertTrue(f.orderSaved.await(10,TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(()->{
                List<Patch> route=panel[0].plannedRoute();assertEquals(3,route.size());assertOptimal(route,f.config);
                assertEquals(5,NativePatchSelection.read(f.config).size());assertEquals(5,RouteState.active(f.config).size());
                f.orderSaved=new CountDownLatch(1);panel[0].refreshGrowth(9999999);settings[0].prepareNow();
            });
            assertTrue(f.orderSaved.await(10,TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(()->{assertEquals(5,panel[0].plannedRoute().size());assertOptimal(panel[0].plannedRoute(),f.config);});
        }
        finally{SwingUtilities.invokeAndWait(()->{settings[0].dispose();panel[0].dispose();});}
    }

    private static void assertOptimal(List<Patch> route,ClassicTreeRunConfig config)
    {
        TravelCosts costs=new TravelCosts(route,config);int actual=costs.entry[0];
        for(int i=1;i<route.size();i++)actual+=costs.travel[i-1][i];
        assertEquals(best(costs,-1,0,0,route.size()),actual);
    }
    private static int best(TravelCosts costs,int last,int mask,int total,int n)
    {
        if(mask==(1<<n)-1)return total;int best=Integer.MAX_VALUE;
        for(int i=0;i<n;i++)if((mask&(1<<i))==0)
            best=Math.min(best,best(costs,i,mask|(1<<i),total+(last<0?costs.entry[i]:costs.travel[last][i]),n));
        return best;
    }
    private static ConfigFixture light(String selection)
    {
        ConfigFixture f=new ConfigFixture();f.set("selectionV2",selection);f.set("runName","Mixed");
        f.set("loadPlanning","false");f.set("packingView","LIST");f.set("supplyBankView","FILTER");f.set("autoOptimize","false");return f;
    }
    private static GrowthSampler.Sample sample(Patch patch,int raw){return new GrowthSampler.Sample(patch,raw,false);}
    private static void click(Container root,String name){((AbstractButton)find(root,name)).doClick();}
    private static Component find(Container root,String name)
    {
        for(Component child:root.getComponents())
        {if(name.equals(child.getName()))return child;if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}}
        return null;
    }
    private static String labels(Container root)
    {StringBuilder out=new StringBuilder();for(Component child:root.getComponents()){if(child instanceof JLabel)out.append(((JLabel)child).getText());if(child instanceof Container)out.append(labels((Container)child));}return out.toString();}
}
