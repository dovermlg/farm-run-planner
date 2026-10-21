package com.classictreerun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class TravelSuppliesTest
{
    private static ConfigFixture config()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyTeleports","true");return f;
    }
    private static SupplyPlan travel(ClassicTreeRunConfig config,Patch... patches)
    { return TravelSupplies.build(config,List.of(patches),false,false); }
    private static SupplyPlan.Requirement row(SupplyPlan plan,int item)
    { return plan.requirements.stream().filter(r->r.alternatives.contains(item)).findFirst().orElseThrow(); }

    @Test public void defaultsToTabletsAndRepeatedJourneysAddConsumablesButNotEquipment()
    {
        ConfigFixture f=config();assertEquals(ClassicTreeRunConfig.TeleportSupplies.TABLETS,f.config.supplyTeleportMode());
        SupplyPlan plan=travel(f.config,Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.TAVERLEY_TREE);
        assertEquals(1,row(plan,ItemID.POH_TABLET_LUMBRIDGETELEPORT).quantity);
        assertEquals(1,row(plan,ItemID.POH_TABLET_VARROCKTELEPORT).quantity);
        assertTrue(plan.items.contains(ItemID.NZONE_TELETAB_TAVERLEY));assertFalse(plan.items.contains(ItemID.LAWRUNE));
        f.set("faladorTravel","FALADOR_TELEPORT");f.set("taverleyTravel","FALADOR_TELEPORT");
        assertEquals(2,row(travel(f.config,Patch.FALADOR_TREE,Patch.TAVERLEY_TREE),ItemID.POH_TABLET_FALADORTELEPORT).quantity);
        plan=travel(f.config,Patch.FALADOR_TREE,Patch.ETCETERIA_BUSH);
        assertEquals(1,row(plan,ItemID.RING_OF_WEALTH_5).quantity);
        assertTrue(row(plan,ItemID.RING_OF_WEALTH_5).detail.contains("1 planned teleport"));
        f.set("faladorTravel","RING_OF_WEALTH");plan=travel(f.config,Patch.FALADOR_TREE,Patch.ETCETERIA_BUSH);
        assertEquals(1,row(plan,ItemID.RING_OF_WEALTH_1).quantity);
        assertTrue(row(plan,ItemID.RING_OF_WEALTH_1).detail.contains("2 planned teleport"));
    }

    @Test public void onePendantCoversTheHardwoodClusterAndWalkingDoesNotSpendExtraTablets()
    {
        ConfigFixture f=config();SupplyPlan plan=travel(f.config,Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE,Patch.FOSSIL_WEST);
        assertEquals(1,plan.requirements.size());assertEquals(1,row(plan,ItemID.NECKLACE_OF_DIGSITE_5).quantity);
        assertTrue(row(plan,ItemID.NECKLACE_OF_DIGSITE_5).detail.contains("1 planned teleport"));
        f.set("brimhavenTravel","CATHERBY_CHARTER");f.set("calquatTravel","WALK_FROM_BRIMHAVEN");
        plan=travel(f.config,Patch.CATHERBY_FRUIT,Patch.BRIMHAVEN_FRUIT,Patch.TAI_BWO_WANNAI_CALQUAT);
        assertEquals(1,plan.requirements.size());assertEquals(1,row(plan,ItemID.LUNAR_TABLET_CATHERBY_TELEPORT).quantity);
        plan=travel(f.config,Patch.BRIMHAVEN_FRUIT);
        assertTrue(plan.items.contains(ItemID.LUNAR_TABLET_CATHERBY_TELEPORT));
        assertFalse(plan.items.contains(ItemID.NZONE_TELETAB_BRIMHAVEN));
        f.set("calquatTravel","TAI_BWO_WANNAI_SCROLL");
        assertTrue(travel(f.config,Patch.TAI_BWO_WANNAI_CALQUAT).items.contains(ItemID.TELEPORTSCROLL_TAIBWO));
    }

    @Test public void completingEarlierPatchesKeepsWalkingContextAndRemovesSpentArrivals()
    {
        ConfigFixture f=config();List<Patch> route=List.of(Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE,Patch.VARROCK_TREE);
        RouteState state=new RouteState();state.setDone(Patch.FOSSIL_EAST,true);
        SupplyPlan plan=SupplyPlan.forRoute(f.config,route,patch->!state.isDone(patch),false,false,null);
        assertFalse(plan.items.contains(ItemID.NECKLACE_OF_DIGSITE_5));
        assertEquals(1,row(plan,ItemID.POH_TABLET_VARROCKTELEPORT).quantity);
        state.setDone(Patch.FOSSIL_MIDDLE,true);state.setDone(Patch.VARROCK_TREE,true);
        assertTrue(SupplyPlan.forRoute(f.config,route,patch->!state.isDone(patch),false,false,null).requirements.isEmpty());
    }

    @Test public void equipmentAlternativesCountButNotedOrUnchargedJewelleryDoesNot()
    {
        SupplyPlan plan=travel(config().config,Patch.FALADOR_TREE);SupplyPlan.Requirement ring=row(plan,ItemID.RING_OF_WEALTH_5);
        int index=plan.requirements.indexOf(ring);
        assertEquals(Long.valueOf(1),plan.carried(new SupplySnapshot(true,Map.of(ItemID.RING_OF_WEALTH_3,1L),Map.of())).get(index));
        assertEquals(Long.valueOf(0),plan.carried(new SupplySnapshot(true,Map.of(ItemID.RING_OF_WEALTH,1L),Map.of())).get(index));
        assertEquals(Long.valueOf(0),plan.carried(new SupplySnapshot(true,Map.of(),Map.of(ItemID.RING_OF_WEALTH_3,1L))).get(index));
        assertTrue(ring.detail.contains("Presence only"));assertTrue(ring.detail.contains("Check charges"));
    }

    @Test public void oneHigherTierArdougneCloakCoversFarmAndMonastery()
    {
        SupplyPlan plan=travel(config().config,Patch.ARDOUGNE_HERB,Patch.ARDOUGNE_BUSH);
        assertEquals(1,plan.requirements.size());assertEquals(1,row(plan,ItemID.ARDY_CAPE_MEDIUM).quantity);
        assertFalse(plan.items.contains(ItemID.ARDY_CAPE_EASY));
        assertTrue(row(plan,ItemID.ARDY_CAPE_MEDIUM).detail.contains("2 planned teleport"));
    }

    @Test public void fairyStaffCanBeOmittedForExemptAccountsAndNetworksDoNotInventTeleports()
    {
        ConfigFixture f=config();f.set("calquatTravel","FAIRY_RING_CKR");
        SupplyPlan plan=travel(f.config,Patch.TAI_BWO_WANNAI_CALQUAT,Patch.CANIFIS_MUSHROOM);
        assertEquals(1,plan.requirements.size());assertTrue(plan.items.contains(ItemID.DRAMEN_STAFF));
        assertTrue(plan.items.contains(ItemID.LUNAR_MOONCLAN_LIMINAL_STAFF));
        f.set("supplyFairyStaff","false");assertTrue(travel(f.config,Patch.TAI_BWO_WANNAI_CALQUAT).requirements.isEmpty());
        assertTrue(travel(f.config,Patch.VILLAGE_FRUIT).requirements.isEmpty());
        f.set("taverleyTravel","BALLOON");assertTrue(travel(f.config,Patch.TAVERLEY_TREE).reminders.stream().anyMatch(s->s.contains("fuel")));
    }

    @Test public void spellModeTotalsRunesAcrossDestinationsAndKeepsRedirectedTablets()
    {
        ConfigFixture f=config();f.set("supplyTeleportMode","SPELL_RUNES");
        f.set("faladorTravel","FALADOR_TELEPORT");
        SupplyPlan plan=travel(f.config,Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.FALADOR_TREE,Patch.CATHERBY_FRUIT,Patch.TAVERLEY_TREE);
        assertEquals(6,row(plan,ItemID.LAWRUNE).quantity);assertEquals(9,row(plan,ItemID.AIRRUNE).quantity);
        assertEquals(11,row(plan,ItemID.WATERRUNE).quantity);assertEquals(3,row(plan,ItemID.ASTRALRUNE).quantity);
        assertEquals(1,row(plan,ItemID.FIRERUNE).quantity);assertEquals(1,row(plan,ItemID.EARTHRUNE).quantity);
        assertTrue(plan.items.contains(ItemID.NZONE_TELETAB_TAVERLEY));assertFalse(plan.items.contains(ItemID.POH_TABLET_LUMBRIDGETELEPORT));
        assertTrue(plan.reminders.stream().anyMatch(s->s.contains("Standard") && s.contains("Lunar")));
        plan=travel(f.config,Patch.HARMONY_HERB,Patch.DRAYNOR_BELLADONNA,Patch.KASTORI_FRUIT);
        assertEquals(4,row(plan,ItemID.LAWRUNE).quantity);assertEquals(1,row(plan,ItemID.SOULRUNE).quantity);
        assertEquals(1,row(plan,ItemID.NATURERUNE).quantity);assertEquals(2,row(plan,ItemID.EARTHRUNE).quantity);
        f.set("loadPlanning","false");
        assertEquals(TeleportDevice.NONE,NextTeleport.resolve(f.config,List.of(Patch.LUMBRIDGE_TREE),new RouteState(),null,null));
        assertEquals(TeleportDevice.TAVERLEY,NextTeleport.resolve(f.config,List.of(Patch.TAVERLEY_TREE),new RouteState(),null,null));
    }

    @Test public void returnToBankSuppliesAreIncludedUntilTheLastInventoryAndLocalBanksNeedNoTeleport()
    {
        ConfigFixture f=config();
        assertTrue(TravelSupplies.build(f.config,List.of(Patch.LUMBRIDGE_TREE),false,true).items.contains(ItemID.RING_OF_DUELING_8));
        assertFalse(TravelSupplies.build(f.config,List.of(Patch.LUMBRIDGE_TREE),false,false).items.contains(ItemID.RING_OF_DUELING_8));
        f.set("loadBank","CATHERBY");
        assertTrue(TravelSupplies.build(f.config,List.of(Patch.CATHERBY_FRUIT),true,true).requirements.isEmpty());
        SupplyPlan pending=TravelSupplies.build(f.config,List.of(Patch.CATHERBY_FRUIT),patch->true,true,false,Patch.VARROCK_TREE);
        assertEquals(1,row(pending,ItemID.LUNAR_TABLET_CATHERBY_TELEPORT).quantity);
        f.set("loadBank","CUSTOM");assertTrue(TravelSupplies.build(f.config,List.of(Patch.LUMBRIDGE_TREE),false,true)
            .reminders.stream().anyMatch(s->s.contains("custom bank")));
    }

    @Test public void teleportSlotsCanSplitALoadAndThePlannerMatchesTheResultingPackingLists()
    {
        ConfigFixture f=config();f.set("loadReservedSlots","25");List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE);
        InventoryItemData data=InventoryFixtures.data(f.config,route);InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(2,plan.loads.size());assertEquals(3,plan.loads.get(0).slots);assertEquals(2,plan.loads.get(1).slots);
        for(int i=0;i<plan.loads.size();i++)assertEquals(plan.loads.get(i).slots,InventoryLoadPlanner.slots(
            InventoryLoadPlanner.supplies(f.config,plan.loads.get(i).patches,data,i>0,i<plan.loads.size()-1),data));
        f.set("supplyTeleports","false");assertEquals(1,InventoryLoadPlanner.plan(f.config,route,data).loads.size());
    }

    @Test public void cachedDefinitionsCoverAllIndependentArrivalsAndBankChoicesInBothModes()
    {
        ConfigFixture f=config();List<Patch> route=List.of(Patch.values());
        for(BankDestination bank:BankDestination.values())for(ClassicTreeRunConfig.TeleportSupplies mode:ClassicTreeRunConfig.TeleportSupplies.values())
        {
            f.set("loadBank",bank.name());f.set("supplyTeleportMode",mode.name());
            Set<Integer> required=InventoryLoadPlanner.requiredItems(f.config,route);
            for(Patch patch:route)
            {
                SupplyPlan possible=SupplyPlan.forRoute(f.config,List.of(patch),true,true);
                assertTrue(bank+" / "+mode+" / "+patch,required.containsAll(possible.items));
            }
        }
    }

    @Test public void everySmallRoutePartitionAgreesOnCapacityAndTheMinimumNumberOfLoads()
    {
        List<Patch> pool=List.of(Patch.LUMBRIDGE_TREE,Patch.FOSSIL_EAST,Patch.FOSSIL_MIDDLE,
            Patch.CATHERBY_FRUIT,Patch.GUILD_TREE,Patch.GUILD_FRUIT);
        for(String mode:List.of("TABLETS","SPELL_RUNES"))for(String bank:List.of("CASTLE_WARS","CATHERBY","FARMING_GUILD"))
        for(int reserve:List.of(20,25))for(int subset=1;subset<(1<<pool.size());subset++)
        {
            ConfigFixture f=config();f.set("supplyTeleportMode",mode);f.set("loadBank",bank);f.set("loadReservedSlots",Integer.toString(reserve));
            List<Patch> route=new ArrayList<>();for(int i=0;i<pool.size();i++)if((subset&(1<<i))!=0)route.add(pool.get(i));
            InventoryItemData data=InventoryFixtures.data(f.config,route);int minimum=Integer.MAX_VALUE;
            int[][] slots=new int[route.size()][route.size()+1];
            for(int i=0;i<route.size();i++)for(int end=i+1;end<=route.size();end++)
                slots[i][end]=InventoryLoadPlanner.slots(InventoryLoadPlanner.supplies(f.config,route.subList(i,end),data,i>0,end<route.size()),data);
            for(int cuts=0;cuts<(1<<(route.size()-1));cuts++)
            {
                int start=0,loads=0;boolean fits=true;
                for(int end=1;end<=route.size();end++)if(end==route.size() || (cuts&(1<<(end-1)))!=0)
                {
                    // Preserve the planner's explicit oversized-single-stop warning behavior.
                    fits&=slots[start][end]<=28-reserve || end==start+1;loads++;start=end;
                }
                if(fits)minimum=Math.min(minimum,loads);
            }
            InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
            assertEquals(mode+" / "+bank+" / "+reserve+" / "+route,minimum,plan.loads.size());
            int start=0;
            for(InventoryLoadPlanner.Load load:plan.loads)
            {int end=start+load.patches.size();assertEquals(slots[start][end],load.slots);start=end;}
        }
    }

    @Test public void settingsSurviveNamedPresetsAndBackupsAndOldVersionThreeGetsDefaults()
    {
        ConfigFixture f=config();f.set("supplyTeleportMode","SPELL_RUNES");f.set("supplyFairyStaff","false");
        PresetLibrary library=new PresetLibrary(f);library.save("Runes",f.config);
        f.set("supplyTeleportMode","TABLETS");f.set("supplyTeleports","false");library.load("Runes");
        assertTrue(f.config.supplyTeleports());assertEquals(ClassicTreeRunConfig.TeleportSupplies.SPELL_RUNES,f.config.supplyTeleportMode());
        assertFalse(f.config.supplyFairyStaff());
        String backup=PresetBackup.write(List.of(PresetBackup.capture("Runes",f.config)));
        PresetBackup.Entry read=PresetBackup.read(backup).get(0);assertEquals("SPELL_RUNES",read.settings.get("supplyTeleportMode"));
        JsonObject json=new JsonParser().parse(backup).getAsJsonObject();
        JsonObject settings=json.getAsJsonArray("presets").get(0).getAsJsonObject().getAsJsonObject("settings");
        json.addProperty("version",3);
        for(String key:List.of("supplyTeleports","supplyTeleportMode","supplyFairyStaff","supplyEquipment","supplyBankView","supplyInventory"))settings.remove(key);
        read=PresetBackup.read(json.toString()).get(0);assertEquals("TABLETS",read.settings.get("supplyTeleportMode"));
        assertEquals("true",read.settings.get("supplyTeleports"));assertEquals("true",read.settings.get("supplyFairyStaff"));
        json.addProperty("version",4);assertThrows(IllegalArgumentException.class,()->PresetBackup.read(json.toString()));
    }

    @Test public void sidebarShowsMissingTeleportsAndBankFilterReceivesThemThenCompletionClearsThem() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=config();f.set("loadPlanning","false");f.set("selectionV2","LUMBRIDGE_TREE,VARROCK_TREE");
            f.set("order","LUMBRIDGE_TREE,VARROCK_TREE");ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);
            try
            {
                panel.setConfirmReset(false);List<SupplyPlan> filtered=new ArrayList<>();panel.setSupplyActions((plan,focus)->filtered.add(plan),()->{ });
                panel.setSupplies(new SupplySnapshot(true,Map.of(ItemID.POH_TABLET_LUMBRIDGETELEPORT,1L),Map.of()));
                JLabel lumbridge=(JLabel)find(panel,"supplyItem_"+ItemID.POH_TABLET_LUMBRIDGETELEPORT);
                JLabel varrock=(JLabel)find(panel,"supplyItem_"+ItemID.POH_TABLET_VARROCKTELEPORT);
                assertNotNull(lumbridge);assertEquals(new Color(103,188,116),lumbridge.getForeground());
                assertEquals(new Color(255,115,115),varrock.getForeground());assertTrue(varrock.getToolTipText().contains("Short 1"));
                ((AbstractButton)find(panel,"startRun")).doClick();assertTrue(filtered.get(0).items.contains(ItemID.POH_TABLET_VARROCKTELEPORT));
                SupplyBankFilter filter=new SupplyBankFilter();filter.start(filtered.get(0));
                assertTrue(filter.contains(ItemID.POH_TABLET_VARROCKTELEPORT));assertFalse(filter.contains(ItemID.RANARR_SEED));
                filter.clear();assertFalse(filter.contains(ItemID.POH_TABLET_VARROCKTELEPORT));
                ((AbstractButton)find(panel,"LUMBRIDGE_TREE")).doClick();assertNull(find(panel,"supplyItem_"+ItemID.POH_TABLET_LUMBRIDGETELEPORT));
                assertNotNull(find(panel,"supplyItem_"+ItemID.POH_TABLET_VARROCKTELEPORT));
                ((AbstractButton)find(panel,"VARROCK_TREE")).doClick();assertNull(find(panel,"supplyItem_"+ItemID.POH_TABLET_VARROCKTELEPORT));
            }
            finally { panel.dispose(); }
        });
    }

    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {
            if(name.equals(child.getName()))return child;
            if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}
        }
        return null;
    }
}
