package com.classictreerun;

import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;
import net.runelite.api.gameval.ItemID;

public class NextTeleportTest
{
    @Test public void followsChosenClassicTravelMethodsRatherThanOtherCarriedTeleports()
    {
        ConfigFixture f = new ConfigFixture();
        assertEquals(TeleportDevice.WEALTH, NextTeleport.arrival(Patch.FALADOR_TREE, f.config));
        f.set("faladorTravel", "FALADOR_TELEPORT");
        assertEquals(TeleportDevice.FALADOR, NextTeleport.arrival(Patch.FALADOR_TREE, f.config));
        assertEquals(TeleportDevice.TAVERLEY, NextTeleport.arrival(Patch.TAVERLEY_TREE, f.config));
        f.set("taverleyTravel", "BALLOON");
        assertEquals(TeleportDevice.NONE, NextTeleport.arrival(Patch.TAVERLEY_TREE, f.config));
        f.set("strongholdTravel", "SPIRIT_TREE");
        assertEquals(TeleportDevice.NONE, NextTeleport.arrival(Patch.STRONGHOLD_TREE, f.config));
        f.set("catherbyTravel", "CAMELOT_TELEPORT");
        assertEquals(TeleportDevice.CAMELOT, NextTeleport.arrival(Patch.CATHERBY_FRUIT, f.config));
        f.set("guildTravel", "FARMING_CAPE");
        assertEquals(TeleportDevice.FARMING_CAPE, NextTeleport.arrival(Patch.GUILD_TREE, f.config));
        f.set("guildTravel", "SPIRIT_TREE");
        assertEquals(TeleportDevice.NONE, NextTeleport.arrival(Patch.GUILD_FRUIT, f.config));
    }

    @Test public void herbDevicesUseTheRightDiaryTierAndPrimaryDirections()
    {
        ConfigFixture f = new ConfigFixture();
        assertEquals(TeleportDevice.EXPLORER, NextTeleport.arrival(Patch.FALADOR_HERB, f.config));
        assertFalse(TeleportDevice.EXPLORER.matches(ItemID.LUMBRIDGE_RING_EASY));
        assertTrue(TeleportDevice.EXPLORER.matches(ItemID.LUMBRIDGE_RING_MEDIUM));
        assertFalse(TeleportDevice.ARDOUGNE_FARM.matches(ItemID.ARDY_CAPE_EASY));
        assertTrue(TeleportDevice.ARDOUGNE_MONASTERY.matches(ItemID.ARDY_CAPE_EASY));
        assertEquals(TeleportDevice.ECTOPHIAL, NextTeleport.arrival(Patch.PHASMATYS_HERB, f.config));
        assertEquals(TeleportDevice.STONY_BASALT, NextTeleport.arrival(Patch.TROLL_HERB, f.config));
        assertEquals(TeleportDevice.ICY_BASALT, NextTeleport.arrival(Patch.WEISS_HERB, f.config));
        assertEquals(TeleportDevice.XERIC, NextTeleport.arrival(Patch.HOSIDIUS_HERB, f.config));
    }

    @Test public void chargedVariantsMatchButEmptyNotedAndUnrelatedItemsDoNot()
    {
        assertTrue(TeleportDevice.WEALTH.matches(ItemID.RING_OF_WEALTH_1));
        assertTrue(TeleportDevice.WEALTH.matches(ItemID.RING_OF_WEALTH_I5));
        assertFalse(TeleportDevice.WEALTH.matches(ItemID.RING_OF_WEALTH));
        assertFalse(TeleportDevice.WEALTH.matches(ItemID.RING_OF_WEALTH_I));
        assertFalse(TeleportDevice.WEALTH.matches(11981)); // Noted ring of wealth (5).
        assertTrue(TeleportDevice.GLORY.matches(ItemID.TRAIL_AMULET_OF_GLORY_1));
        assertTrue(TeleportDevice.GLORY.matches(ItemID.AMULET_OF_GLORY_INF));
        assertFalse(TeleportDevice.GLORY.matches(ItemID.AMULET_OF_GLORY));
        assertFalse(TeleportDevice.GLORY.matches(ItemID.TRAIL_AMULET_OF_GLORY));
        assertTrue(TeleportDevice.SLAYER.matches(ItemID.SLAYER_RING_ETERNAL));
        assertFalse(TeleportDevice.SKILLS.matches(ItemID.JEWL_NECKLACE_OF_SKILLS));
        assertFalse(TeleportDevice.DIGSITE.matches(ItemID.NECKLACE_OF_DIGSITE));
        assertTrue(TeleportDevice.CRYSTAL.matches(ItemID.PRIF_TELEPORT_CRYSTAL));
        assertFalse(TeleportDevice.ECTOPHIAL.matches(ItemID.ECTOPHIAL_EMPTY));
        assertFalse(TeleportDevice.XERIC.matches(ItemID.XERIC_TALISMAN_EMPTY));
        assertFalse(TeleportDevice.LUMBRIDGE.matches(ItemID.POH_TABLET_VARROCKTELEPORT));
        assertFalse(TeleportDevice.LUMBRIDGE.matches(ItemID._31_LUMBRIDGE_TELEPORT));
    }

    @Test public void completingAndUndoingPatchesFollowsTheNextLegWithoutReusingATeleportForLocalWalks()
    {
        ConfigFixture f = new ConfigFixture(); f.set("loadPlanning", "false");
        List<Patch> route = List.of(Patch.FOSSIL_EAST, Patch.FOSSIL_MIDDLE);
        RouteState state = new RouteState();
        assertEquals(TeleportDevice.DIGSITE, NextTeleport.resolve(f.config, route, state, null, null));
        state.setDone(Patch.FOSSIL_EAST, true);
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, route, state, null, null));
        state.setDone(Patch.FOSSIL_MIDDLE, true);
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, route, state, null, null));
        state.setDone(Patch.FOSSIL_EAST, false);
        assertEquals(TeleportDevice.DIGSITE, NextTeleport.resolve(f.config, route, state, null, null));
    }

    @Test public void chartersAndCalquatWalkingDoNotHighlightAnotherTeleport()
    {
        ConfigFixture f = new ConfigFixture(); f.set("loadPlanning", "false"); f.set("brimhavenTravel", "CATHERBY_CHARTER");
        RouteState state = new RouteState(); state.setDone(Patch.CATHERBY_FRUIT, true);
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config,
            List.of(Patch.CATHERBY_FRUIT, Patch.BRIMHAVEN_FRUIT), state, null, null));
        assertEquals(TeleportDevice.CATHERBY, NextTeleport.arrival(Patch.BRIMHAVEN_FRUIT, f.config));
        f.set("calquatTravel", "WALK_FROM_BRIMHAVEN"); state.setDone(Patch.BRIMHAVEN_FRUIT, true);
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config,
            List.of(Patch.BRIMHAVEN_FRUIT, Patch.TAI_BWO_WANNAI_CALQUAT), state, null, null));
        f.set("brimhavenTravel", "REDIRECTED_TABLET");
        assertEquals(TeleportDevice.BRIMHAVEN, NextTeleport.arrival(Patch.TAI_BWO_WANNAI_CALQUAT, f.config));
        f.set("calquatTravel", "FAIRY_RING_CKR");
        assertEquals(TeleportDevice.NONE, NextTeleport.arrival(Patch.TAI_BWO_WANNAI_CALQUAT, f.config));
    }

    @Test public void bankStopUsesItsDeviceThenIndependentPatchArrivalAfterAcknowledgment()
    {
        ConfigFixture f = InventoryFixtures.light(); f.set("loadReservedSlots", "27");
        List<Patch> route = List.of(Patch.FOSSIL_EAST, Patch.FOSSIL_MIDDLE);
        InventoryLoadPlanner.Plan plan = InventoryLoadPlanner.plan(f.config, route, InventoryFixtures.data(f.config, route));
        RouteState state = new RouteState(); state.setDone(Patch.FOSSIL_EAST, true);
        assertEquals(TeleportDevice.DUELING, NextTeleport.resolve(f.config, route, state, plan, null));
        assertEquals(TeleportDevice.DIGSITE, NextTeleport.resolve(f.config, route, state, plan, plan.confirmBank(null, 1)));
    }

    @Test public void localBankWalksAndCustomDirectionsDoNotInventADevice()
    {
        ConfigFixture f = InventoryFixtures.light(); f.set("loadReservedSlots", "27"); f.set("loadBank", "FARMING_GUILD");
        List<Patch> route = List.of(Patch.GUILD_TREE, Patch.GUILD_FRUIT);
        InventoryLoadPlanner.Plan plan = InventoryLoadPlanner.plan(f.config, route, InventoryFixtures.data(f.config, route));
        RouteState state = new RouteState(); state.setDone(Patch.GUILD_TREE, true);
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, route, state, plan, null));
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, route, state, plan, plan.confirmBank(null, 1)));
        f.set("loadBank", "CATHERBY"); f.set("brimhavenTravel", "CATHERBY_CHARTER");
        List<Patch> charter = List.of(Patch.LUMBRIDGE_TREE, Patch.BRIMHAVEN_FRUIT);
        InventoryLoadPlanner.Plan charterPlan = InventoryLoadPlanner.plan(f.config, charter, InventoryFixtures.data(f.config, charter));
        RouteState charterState = new RouteState(); charterState.setDone(Patch.LUMBRIDGE_TREE, true);
        assertEquals(TeleportDevice.CATHERBY, NextTeleport.resolve(f.config, charter, charterState, charterPlan, null));
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, charter, charterState, charterPlan, charterPlan.confirmBank(null, 1)));
        f.set("loadBank", "CUSTOM"); f.set("loadBankInstructions", "Use my ring of wealth.");
        assertEquals(TeleportDevice.NONE, NextTeleport.bank(f.config));
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, route, new RouteState(), null, null));
        assertEquals(TeleportDevice.NONE, NextTeleport.resolve(f.config, List.of(), new RouteState(), plan, null));
    }

    @Test public void sidebarPublishesProgressChangesAndClearsDuringPreparationDisableAndDisposal() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            ConfigFixture f = new ConfigFixture(); f.set("loadPlanning", "false");
            f.set("selectionV2", "FOSSIL_EAST,FOSSIL_MIDDLE"); f.set("order", "FOSSIL_EAST,FOSSIL_MIDDLE");
            ClassicTreeRunPanel panel = new ClassicTreeRunPanel(f.config, f);
            assertEquals(TeleportDevice.DIGSITE, panel.nextTeleport());
            ((AbstractButton)find(panel, "FOSSIL_EAST")).doClick();
            assertEquals(TeleportDevice.NONE, panel.nextTeleport());
            ((AbstractButton)find(panel, "FOSSIL_EAST")).doClick();
            assertEquals(TeleportDevice.DIGSITE, panel.nextTeleport());
            String progress = f.get(ClassicTreeRunPanel.PROGRESS_KEY);
            f.set("highlightNextTeleport", "false"); panel.reload();
            assertEquals(TeleportDevice.NONE, panel.nextTeleport());
            f.set("highlightNextTeleport", "true"); panel.reload();
            panel.setRoutePreparing(true); assertEquals(TeleportDevice.NONE, panel.nextTeleport());
            panel.setRoutePreparing(false); panel.reload(); assertEquals(TeleportDevice.DIGSITE, panel.nextTeleport());
            assertEquals(progress, f.get(ClassicTreeRunPanel.PROGRESS_KEY));
            panel.dispose(); assertEquals(TeleportDevice.NONE, panel.nextTeleport());
        });
    }

    @Test public void highlightPreferenceIsGlobalAndDoesNotChangeOrTravelWithSavedPresets()
    {
        ConfigFixture f = new ConfigFixture(); f.set("highlightNextTeleport", "false");
        new PresetLibrary(f).save("Mine", f.config);
        String backup = PresetBackup.write(List.of(PresetBackup.capture("Mine", f.config)));
        assertFalse(backup.contains("highlightNextTeleport"));
        f.set("highlightNextTeleport", "true"); new PresetLibrary(f).load("Mine");
        assertTrue(new LocalRunConfig(f).highlightNextTeleport());
    }

    private static java.awt.Component find(java.awt.Container parent, String name)
    {
        for (java.awt.Component child : parent.getComponents())
        {
            if (name.equals(child.getName())) { return child; }
            if (child instanceof java.awt.Container)
            {
                java.awt.Component found = find((java.awt.Container)child, name);
                if (found != null) { return found; }
            }
        }
        return null;
    }
}
