package com.classictreerun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryLoadPlannerTest
{
    @Test public void incrementalSlotCountsMatchFullPackingListsForMixedCropsAndPaymentForms()
    {
        java.util.Random random=new java.util.Random(818);
        for(boolean noted:List.of(false,true))for(String compost:List.of("NONE","COMPOST","ULTRACOMPOST","BOTTOMLESS"))
        {
            ConfigFixture f=new ConfigFixture();f.set("loadNotedProtection",Boolean.toString(noted));f.set("supplyCompost",compost);
            f.set("cropTree","MAGIC");f.set("cropAllotment","POTATO");f.set("cropFruit","APPLE");
            List<Patch> route=new java.util.ArrayList<>(List.of(Patch.values()));java.util.Collections.shuffle(route,random);
            InventoryItemData data=InventoryFixtures.data(f.config,route);
            // Include nonstacking planting supplies, compost applications/payments and shared tools.
            for(int start=0;start<route.size();start+=7)
            {
                InventoryLoadPlanner.SlotCounter counter=new InventoryLoadPlanner.SlotCounter(data);
                for(int end=start+1;end<=Math.min(route.size(),start+14);end++)
                {
                    int actual=counter.add(InventoryLoadPlanner.supplies(f.config,List.of(route.get(end-1)),data));
                    int expected=InventoryLoadPlanner.slots(InventoryLoadPlanner.supplies(f.config,route.subList(start,end),data),data);
                    assertEquals(noted+" / "+compost+" / "+route.subList(start,end),expected,actual);
                }
            }
        }
    }

    @Test public void fingerprintsKeepTheirFormatAndDoNotReadUnrelatedPatchToggles()
    {
        ConfigFixture f=InventoryFixtures.light();List<Patch> route=Patch.classic();
        ClassicTreeRunConfig config=new LocalRunConfig(new ClassicTreeRunPanel.Preferences(){
            public String get(String key){if(key.startsWith("patch_"))throw new AssertionError(key);return f.get(key);}
            public void set(String key,String value){throw new AssertionError("No writes while fingerprinting");}
        });
        InventoryItemData data=InventoryFixtures.data(config,route);
        java.util.Map<String,String> travel=new java.util.TreeMap<>();
        ConfigValues.read(f.config).forEach((key,value)->{if(key.endsWith("Travel"))travel.put(key,value);});
        String source=RouteState.encode(route)+"\n"+CropChoices.settings(f.config)+"\n"+InventoryLoadPlanner.settings(f.config)+"\n"+travel+"\n"
            +data.fingerprint(SupplyPlan.build(f.config,route).items);
        String expected=com.google.common.hash.Hashing.sha256().hashString(source,java.nio.charset.StandardCharsets.UTF_8).toString();
        assertEquals(expected,InventoryLoadPlanner.signature(config,route,data));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",Digests.sha256(""));
    }
    private List<Patch> type(Patch.Type type)
    {return Arrays.stream(Patch.values()).filter(p->p.getType()==type).collect(Collectors.toList());}

    @Test public void saplingsUseOneSlotEachAndEveryPatchRemainsInOrder()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","25");
        List<Patch> route=type(Patch.Type.TREE);InventoryItemData data=InventoryFixtures.data(f.config,route);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(3,plan.capacity);assertEquals(3,plan.loads.size());
        assertEquals(route,plan.loads.stream().flatMap(l->l.patches.stream()).collect(Collectors.toList()));
        for(InventoryLoadPlanner.Load load:plan.loads)assertTrue(load.slots<=3);
    }

    @Test public void stackableSeedsAndBottomlessBucketDoNotNeedOneSlotPerPatch()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","25");f.set("supplyCompost","BOTTOMLESS");
        List<Patch> route=type(Patch.Type.HERB);InventoryItemData data=InventoryFixtures.data(f.config,route);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(1,plan.loads.size());assertEquals(2,plan.loads.get(0).slots);
        assertEquals(10,InventoryLoadPlanner.supplies(f.config,route,data).requirements.stream().filter(r->r.item==ItemID.GUAM_SEED).findFirst().orElseThrow().quantity);
        f.set("supplyCompost","ULTRACOMPOST");data=InventoryFixtures.data(f.config,route);
        plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(5,plan.loads.size());
        for(InventoryLoadPlanner.Load load:plan.loads)assertEquals(3,load.slots);
    }

    @Test public void notedPaymentsAreOneStackButLoosePaymentsCanOverflowASingleStop()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyProtection","true");f.set("cropTree","MAGIC");
        List<Patch> route=List.of(Patch.LUMBRIDGE_TREE);InventoryItemData data=InventoryFixtures.data(f.config,route);
        assertEquals(2,InventoryLoadPlanner.plan(f.config,route,data).loads.get(0).slots);
        f.set("loadNotedProtection","false");
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(1,plan.loads.size());assertEquals(26,plan.loads.get(0).slots);assertTrue(plan.loads.get(0).slots>plan.capacity);
        assertEquals(route,plan.loads.get(0).patches);
    }

    @Test public void notedPackingRowsDoNotAcceptLoosePaymentsAndCompostFormsStaySeparate()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyProtection","true");f.set("supplyCompost","COMPOST");
        List<Patch> route=List.of(Patch.FALADOR_ALLOTMENT_NW);InventoryItemData data=InventoryFixtures.data(f.config,route);
        SupplyPlan supplies=InventoryLoadPlanner.supplies(f.config,route,data);
        assertEquals(3,InventoryLoadPlanner.slots(supplies,data)); // seed stack + one plain compost + noted payment stack
        SupplyPlan.Requirement payment=supplies.requirements.stream().filter(r->r.item==ItemID.BUCKET_COMPOST && r.notes).findFirst().orElseThrow();
        assertTrue(payment.notedOnly);int index=supplies.requirements.indexOf(payment);
        assertEquals(Long.valueOf(0),supplies.carried(new SupplySnapshot(true,Map.of(ItemID.BUCKET_COMPOST,20L),Map.of())).get(index));
        assertEquals(Long.valueOf(2),supplies.carried(new SupplySnapshot(true,Map.of(ItemID.BUCKET_COMPOST,1L),Map.of(ItemID.BUCKET_COMPOST,2L))).get(index));
    }

    @Test public void toolsAndCoinStackCountOnceInEveryLoad()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyTools","true");f.set("supplyRake","true");f.set("supplyCoins","10000");f.set("loadReservedSlots","23");
        List<Patch> route=type(Patch.Type.TREE);InventoryItemData data=InventoryFixtures.data(f.config,route);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(7,plan.loads.size());
        for(InventoryLoadPlanner.Load load:plan.loads)assertEquals(5,load.slots); // one sapling, spade, axe, rake, coin stack
    }

    @Test public void emptyRunAndHarvestOnlyDoNotInventPlantingSupplies()
    {
        ConfigFixture f=InventoryFixtures.light();
        assertTrue(InventoryLoadPlanner.plan(f.config,List.of(),InventoryItemData.EMPTY).loads.isEmpty());
        f.set("cropTree","NONE");List<Patch> route=type(Patch.Type.TREE);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,InventoryFixtures.data(f.config,route));
        assertEquals(1,plan.loads.size());assertEquals(0,plan.loads.get(0).slots);
    }

    @Test public void bankConfirmationIsLocalProgressAndInvalidatesWhenThePlanChanges()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","25");List<Patch> route=type(Patch.Type.TREE);
        InventoryItemData data=InventoryFixtures.data(f.config,route);InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        String confirmed=plan.confirmBank(null,1);assertTrue(plan.bankConfirmed(confirmed,1));assertFalse(plan.bankConfirmed(confirmed,2));
        RouteState state=new RouteState();for(Patch patch:plan.loads.get(0).patches)state.setDone(patch,true);
        assertEquals(1,plan.current(state));
        f.set("loadBank","CATHERBY");InventoryLoadPlanner.Plan changed=InventoryLoadPlanner.plan(f.config,route,data);
        assertFalse(changed.bankConfirmed(confirmed,1));
        for(Patch patch:route)state.setDone(patch,true);assertEquals(-1,plan.current(state));
    }

    @Test public void chosenBankCanMoveABoundaryToAvoidAnExtraDetour()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","26");
        List<Patch> route=List.of(Patch.CATHERBY_FRUIT,Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE);
        InventoryItemData data=InventoryFixtures.data(f.config,route);
        InventoryLoadPlanner.Plan castleWars=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(2,castleWars.loads.get(0).patches.size());
        f.set("loadBank","CATHERBY");
        InventoryLoadPlanner.Plan catherby=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(List.of(Patch.CATHERBY_FRUIT),catherby.loads.get(0).patches);
        assertEquals(List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE),catherby.loads.get(1).patches);
        assertTrue(BankDestination.CATHERBY.instructions(Patch.CATHERBY_FRUIT,f.config).contains("walk to Catherby bank"));
        assertTrue(BankDestination.CATHERBY.departureInstructions(Patch.CATHERBY_FRUIT,f.config).contains("walk east along the shore"));
        assertTrue(BankDestination.FARMING_GUILD.departureInstructions(Patch.GUILD_TREE,f.config).contains("West wing tree patch"));
        assertNull(BankDestination.CATHERBY.departureInstructions(Patch.LUMBRIDGE_TREE,f.config));
    }

    @Test public void travelPreferencesInvalidateLoadsEvenWhenPatchOrderStaysTheSame()
    {
        ConfigFixture f=InventoryFixtures.light();List<Patch> route=type(Patch.Type.TREE);
        InventoryItemData data=InventoryFixtures.data(f.config,route);
        String original=InventoryLoadPlanner.signature(f.config,route,data);
        f.set("faladorTravel","FALADOR_TELEPORT");
        assertNotEquals(original,InventoryLoadPlanner.signature(f.config,route,data));
    }

    @Test public void reopeningEarlierPatchClearsOnlyLaterBankStops()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("loadReservedSlots","27");
        List<Patch> route=List.of(Patch.LUMBRIDGE_TREE,Patch.VARROCK_TREE,Patch.FALADOR_TREE);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,InventoryFixtures.data(f.config,route));
        String confirmed=plan.confirmBank(plan.confirmBank(null,1),2);
        String reopened=plan.reopenPatch(confirmed,Patch.VARROCK_TREE);
        assertTrue(plan.bankConfirmed(reopened,1));assertFalse(plan.bankConfirmed(reopened,2));
        reopened=plan.reopenPatch(confirmed,Patch.LUMBRIDGE_TREE);
        assertFalse(plan.bankConfirmed(reopened,1));assertFalse(plan.bankConfirmed(reopened,2));
    }

    @Test public void allNinetyNinePatchesAreRetainedWithNoDuplicates()
    {
        ConfigFixture f=InventoryFixtures.light();f.set("supplyCompost","ULTRACOMPOST");
        List<Patch> route=List.of(Patch.values());InventoryItemData data=InventoryFixtures.data(f.config,route);
        InventoryLoadPlanner.Plan plan=InventoryLoadPlanner.plan(f.config,route,data);
        assertEquals(route,plan.loads.stream().flatMap(l->l.patches.stream()).collect(Collectors.toList()));
        for(InventoryLoadPlanner.Load load:plan.loads)assertTrue(load.slots<=plan.capacity || load.patches.size()==1);
    }
}
