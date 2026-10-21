package com.classictreerun;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class SavedSetupSidebarTest
{
    @Test public void missingBankTagsExplainsTheRequirementWhileTheSidebarChecklistStillWorks() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("loadPlanning","false");f.set("selectionV2","LUMBRIDGE_TREE");f.set("runName","Mine");
            new PresetLibrary(f).save("Mine",f.config);ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            try
            {
                panel.setBankSetupAvailable(false);panel.reload();
                assertFalse(((AbstractButton)find(panel,"packInventory")).isEnabled());
                assertTrue(((javax.swing.JLabel)find(panel,"bankSetupStatus")).getText().contains("Enable RuneLite's Bank Tags"));
                assertTrue(((AbstractButton)find(panel,"LUMBRIDGE_TREE")).isEnabled());click(panel,"LUMBRIDGE_TREE");
                assertEquals("LUMBRIDGE_TREE",f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            }
            finally {panel.dispose();}
        });
    }
    @Test public void savingLinksTheSetupAndProgressUpdatesItWithoutRequiringStartRun() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("loadPlanning","false");f.set("runName","My trees");
            f.set("selectionV2","LUMBRIDGE_TREE,VARROCK_TREE");f.set("order","LUMBRIDGE_TREE,VARROCK_TREE");
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);List<SupplyPlan> plans=new ArrayList<>();List<Boolean> focuses=new ArrayList<>();int[] clears={0};
            panel.setSupplyActions((plan,focus)->{plans.add(plan);focuses.add(focus);},()->clears[0]++);
            try
            {
                panel.reload();assertTrue("An unsaved draft must not replace a saved bank setup",plans.isEmpty());
                new PresetLibrary(f).save("My trees",f.config);panel.presetSaved("My trees");
                assertEquals(1,plans.size());assertTrue(focuses.get(0));assertEquals(2,oak(plans.get(0)));
                panel.reload();assertEquals("A repaint must not reset the bank search",1,plans.size());
                click(panel,"LUMBRIDGE_TREE");String progress=f.get(ClassicTreeRunPanel.PROGRESS_KEY);
                assertEquals(2,plans.size());assertEquals(1,oak(plans.get(1)));assertFalse(focuses.get(1));
                click(panel,"clearBankFilter");panel.reload();assertEquals(2,plans.size());assertTrue(clears[0]>0);
                click(panel,"packInventory");assertEquals(progress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));assertTrue(focuses.get(focuses.size()-1));
                int before=clears[0];click(panel,"VARROCK_TREE");assertTrue("Completion clears the owned bank view",clears[0]>before);
            }
            finally {panel.dispose();}
        });
    }
    @Test public void editingPausesTheSavedViewUntilSavedAgainAndOffDisablesIt() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=InventoryFixtures.light();f.set("loadPlanning","false");f.set("runName","Mine");f.set("selectionV2","LUMBRIDGE_TREE");
            new PresetLibrary(f).save("Mine",f.config);ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            List<SupplyPlan> plans=new ArrayList<>();int[] clears={0};panel.setSupplyActions((plan,focus)->plans.add(plan),()->clears[0]++);
            try
            {
                panel.reload();assertEquals(1,plans.size());panel.presetEditing("Mine");assertTrue(clears[0]>0);
                f.set("cropTree","MAGIC");panel.reload();assertEquals(1,plans.size());
                new PresetLibrary(f).save("Mine",f.config);panel.presetSaved("Mine");assertEquals(2,plans.size());
                assertTrue(plans.get(1).items.contains(ItemID.PLANTPOT_MAGIC_TREE_SAPLING));
                f.set("supplyBankView","OFF");new PresetLibrary(f).save("Mine",f.config);panel.presetSaved("Mine");
                assertEquals(2,plans.size());assertFalse(((AbstractButton)find(panel,"packInventory")).isEnabled());
            }
            finally {panel.dispose();}
        });
    }
    @Test public void aSavedSetupAdvancesAutomaticallyToTheNextInventoryAndKeepsBankAcknowledgmentsSeparate() throws Exception
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","27");f.set("runName","Two loads");
        List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.CATHERBY_FRUIT);f.set("selectionV2",RouteState.encode(route));f.set("order",RouteState.encode(route));
        new PresetLibrary(f).save("Two loads",f.config);ClassicTreeRunPanel[] panel={null};List<SupplyPlan> plans=new ArrayList<>();
        SwingUtilities.invokeAndWait(()->{
            panel[0]=new ClassicTreeRunPanel(f.config,f);panel[0].setSupplyActions((plan,focus)->plans.add(plan),()->{});
            panel[0].setInventoryDataRequest(request->panel[0].setInventoryData(request,InventoryFixtures.data(f.config,route)));
        });
        try
        {
            for(int i=0;i<500;i++)
            {
                boolean[] ready={false};SwingUtilities.invokeAndWait(()->ready[0]=find(panel[0],"inventoryLoad")!=null);
                if(ready[0])break;Thread.sleep(10);
            }
            SwingUtilities.invokeAndWait(()->{
                assertFalse(plans.isEmpty());assertEquals(1,oak(plans.get(plans.size()-1)));
                int before=plans.size();click(panel[0],"LUMBRIDGE_TREE");assertNotNull(find(panel[0],"bankReady_1"));
                assertEquals(before+1,plans.size());assertTrue(plans.get(plans.size()-1).items.contains(ItemID.PLANTPOT_APPLE_SAPLING));
                String progress=f.get(ClassicTreeRunPanel.PROGRESS_KEY);click(panel[0],"bankReady_1");
                assertEquals(progress,f.get(ClassicTreeRunPanel.PROGRESS_KEY));
                assertFalse(((AbstractButton)find(panel[0],"bankReady_1")).isEnabled());
            });
        }
        finally {SwingUtilities.invokeAndWait(()->panel[0].dispose());}
    }
    private static int oak(SupplyPlan plan)
    {return plan.requirements.stream().filter(r->r.item==ItemID.PLANTPOT_OAK_SAPLING).findFirst().orElseThrow().quantity;}
    private static void click(Container panel,String name){((AbstractButton)find(panel,name)).doClick();}
    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {if(name.equals(child.getName()))return child;if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}}
        return null;
    }
}
