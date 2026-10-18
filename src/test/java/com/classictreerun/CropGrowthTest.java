package com.classictreerun;

import java.util.*;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;
import static org.junit.Assert.*;

public class CropGrowthTest
{
    private final CropReference reference=new CropReference();
    @Test public void allPatchesHaveVerifiedBindingsAndEachCropFamilyHasStates()
    {
        assertEquals(99,reference.bindings.size());
        for(Patch patch:Patch.values())
        {
            boolean found=false;for(int value=0;value<256;value++){CropReference.Crop crop=reference.decode(patch,value);if(crop!=null&&crop.planted()){found=true;break;}}
            assertTrue("No decoded crop: "+patch,found);
        }
        assertEquals(VarbitID.FARMING_TRANSMIT_A,reference.bindings.get(Patch.FOSSIL_EAST).varbit);
        assertEquals(VarbitID.FARMING_TRANSMIT_B,reference.bindings.get(Patch.FOSSIL_MIDDLE).varbit);
        assertEquals(VarbitID.FARMING_TRANSMIT_C,reference.bindings.get(Patch.FOSSIL_WEST).varbit);
        assertEquals(VarbitID.FARMING_TRANSMIT_E,reference.bindings.get(Patch.GUILD_HERB).varbit);
    }
    @Test public void catherbyAndPortSarimBoundaryPayloadsDoNotCross()
    {
        CropReference.Binding farm=reference.bindings.get(Patch.CATHERBY_HERB),fruit=reference.bindings.get(Patch.CATHERBY_FRUIT);
        assertTrue(farm.applies(2830,3445,0));assertFalse(fruit.applies(2830,3445,0));
        assertFalse(farm.applies(2830,3445,1));assertTrue(fruit.applies(2830,3445,1));
        assertTrue(fruit.applies(2861,3434,0));assertFalse(farm.applies(2861,3434,0));
        assertFalse(reference.bindings.get(Patch.FALADOR_HERB).applies(3055,3271,0));
        assertTrue(reference.bindings.get(Patch.PORT_SARIM_SPIRIT).applies(3055,3271,0));
        assertTrue(reference.bindings.get(Patch.FALADOR_HERB).applies(3055,3272,0));
    }
    @Test public void fossilLaddersAndUpperFloorsAreNotReadAsHardwoods()
    {
        CropReference.Binding fossil=reference.bindings.get(Patch.FOSSIL_EAST);
        assertTrue(fossil.applies(3709,3835,0));assertFalse(fossil.applies(3709,3835,1));
        assertFalse(fossil.applies(3753,3869,0));assertFalse(fossil.applies(3729,3831,0));
    }
    @Test public void herbMagicAndPalmEstimatesUseTheirActualCropDurations()
    {
        CropGrowth model=new CropGrowth(reference);
        model.observe(Patch.TROLL_HERB,32,1000,false);model.observe(Patch.LUMBRIDGE_TREE,48,1000,false);model.observe(Patch.CATHERBY_FRUIT,200,1000,false);
        assertEquals(1000+80*60,model.observations.get(Patch.TROLL_HERB).readyAt);
        assertEquals(1000+480*60,model.observations.get(Patch.LUMBRIDGE_TREE).readyAt);
        assertEquals(1000+960*60,model.observations.get(Patch.CATHERBY_FRUIT).readyAt);
        assertTrue(model.describe(Patch.TROLL_HERB,1000).contains("ranarr"));
    }
    @Test public void unchangedStatesDoNotRestartCountdownAndSkippedGrowthCanMoveItEarlier()
    {
        CropGrowth model=new CropGrowth(reference);model.observe(Patch.TROLL_HERB,4,1000,false);
        assertFalse(model.observe(Patch.TROLL_HERB,4,1001,false));
        assertTrue(model.observe(Patch.TROLL_HERB,4,1600,false));assertEquals(5800,model.observations.get(Patch.TROLL_HERB).readyAt);
        model.observe(Patch.TROLL_HERB,6,1600,false);assertEquals(4000,model.observations.get(Patch.TROLL_HERB).readyAt);
    }
    @Test public void unknownEmptyDiseasedAndDeadPatchesBlockFullRunReadiness()
    {
        CropGrowth model=new CropGrowth(reference);List<Patch> group=Arrays.asList(Patch.TROLL_HERB,Patch.WEISS_HERB);
        model.observe(Patch.TROLL_HERB,4,1000,false);assertFalse(model.group(group,10000).ready(10000));
        for(int raw:new int[]{0,128,170,-1}){model.observe(Patch.WEISS_HERB,raw,10001,false);assertFalse(model.group(group,20000).ready(20000));}
    }
    @Test public void fullGroupWaitsForLatestCropAndEmptyGroupNeverNotifies()
    {
        CropGrowth model=new CropGrowth(reference);model.observe(Patch.TROLL_HERB,4,1000,false);model.observe(Patch.LUMBRIDGE_TREE,48,1000,false);
        List<Patch> group=Arrays.asList(Patch.TROLL_HERB,Patch.LUMBRIDGE_TREE);
        assertFalse(model.group(group,5800).ready(5800));assertTrue(model.group(group,29800).ready(29800));
        assertFalse(model.group(Collections.emptyList(),99999).ready(99999));
    }
    @Test public void healthCheckTreesAndMatureCoralAreReadyAtTheirFinalGrowthStage()
    {
        CropGrowth model=new CropGrowth(reference);model.observe(Patch.LUMBRIDGE_TREE,12,1000,false);model.observe(Patch.CORAL_E,8,1000,false);
        assertTrue(model.isKnownReady(Patch.LUMBRIDGE_TREE,1000));assertTrue(model.isKnownReady(Patch.CORAL_E,1000));
    }
    @Test public void regrowthWaitsForFullYieldAndAReplantedTreeStartsANewCycle()
    {
        CropGrowth model=new CropGrowth(reference);model.observe(Patch.CHAMPIONS_BUSH,14,1000,false);model.observe(Patch.CHAMPIONS_BUSH,10,1100,false);
        assertEquals(1100+80*60,model.observations.get(Patch.CHAMPIONS_BUSH).readyAt);assertTrue(model.observations.get(Patch.CHAMPIONS_BUSH).armed);
        model.observe(Patch.LUMBRIDGE_TREE,13,1000,false);model.observe(Patch.LUMBRIDGE_TREE,8,1100,false);
        assertEquals(1100,model.observations.get(Patch.LUMBRIDGE_TREE).cycle);
    }
    @Test public void seasonalRateAndPersistenceKeepTheOriginalEstimateAndCycle()
    {
        CropGrowth model=new CropGrowth(reference);model.observe(Patch.TROLL_HERB,4,1000,true);assertEquals(1960,model.observations.get(Patch.TROLL_HERB).readyAt);
        CropGrowth reopened=new CropGrowth(reference);reopened.load(model.save());assertEquals(model.save(),reopened.save());
        assertTrue(reopened.group(Collections.singleton(Patch.TROLL_HERB),3000).armed);
        reopened.load("bad;UNKNOWN=4,1000,5000,1000,true,false;TROLL_HERB=garbage");assertTrue(reopened.observations.isEmpty());
    }
    @Test public void stumpsNeverAcquireAFruitRegrowthTimerOrCountAsReady()
    {
        CropGrowth model=new CropGrowth(reference);
        for(Patch patch:new Patch[]{Patch.LUMBRIDGE_TREE,Patch.CATHERBY_FRUIT,Patch.FOSSIL_EAST,Patch.GUILD_CELASTRUS})
        {
            int raw=patch==Patch.LUMBRIDGE_TREE?14:patch==Patch.CATHERBY_FRUIT?33:patch==Patch.FOSSIL_EAST?17:28;
            model.observe(patch,raw,1000,false);
            assertEquals(0,model.observations.get(patch).readyAt);
            assertFalse(model.group(Collections.singleton(patch),999999).ready(999999));
            assertTrue(model.describe(patch,1000).contains("stump"));
        }
        // Also correct old 1.8 records whose decoder treated a fruit stump as regrowing.
        model.load("CATHERBY_FRUIT=33,1000,17200,1000,true,false");
        assertFalse(model.isKnownReady(Patch.CATHERBY_FRUIT,99999));
    }
    @Test public void animaCountsDownToReplacementAndReportsItsObservedLifecycle()
    {
        CropGrowth model=new CropGrowth(reference);
        model.observe(Patch.GUILD_ANIMA,8,1000,false);
        assertEquals(1000+8L*640*60,model.observations.get(Patch.GUILD_ANIMA).readyAt);
        assertTrue(model.describe(Patch.GUILD_ANIMA,1000).contains("replacement est."));
        model.observe(Patch.GUILD_ANIMA,15,2000,false);
        assertTrue(model.describe(Patch.GUILD_ANIMA,2000).contains("withering"));
        model.observe(Patch.GUILD_ANIMA,16,3000,false);
        assertTrue(model.describe(Patch.GUILD_ANIMA,3000).contains("expired when last observed"));
        assertTrue(model.group(Collections.singleton(Patch.GUILD_ANIMA),3000).ready(3000));
    }
    @Test public void aConfirmedUnchangedGrowingStateAfterTheDeadlineRevisesTheEstimate()
    {
        CropGrowth model=new CropGrowth(reference);model.observe(Patch.TROLL_HERB,7,1000,false);
        assertEquals(2200,model.observations.get(Patch.TROLL_HERB).readyAt);
        assertTrue(model.observe(Patch.TROLL_HERB,7,2300,false));
        assertEquals(3500,model.observations.get(Patch.TROLL_HERB).readyAt);
        model.observe(Patch.TROLL_HERB,128,2400,false);
        assertFalse(model.group(Collections.singleton(Patch.TROLL_HERB),99999).ready(99999));
    }
}
