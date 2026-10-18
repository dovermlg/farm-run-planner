package com.classictreerun;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class GrowthSamplerTest
{
    private final CropReference reference=new CropReference();
    @Test public void regionLoadNeedsSettlingAndTwoMatchingReads()
    {
        GrowthSampler sampler=new GrowthSampler();
        for(int i=0;i<4;i++)assertTrue(sampler.read(reference,2828,3696,0,id->4).isEmpty());
        List<GrowthSampler.Sample> samples=sampler.read(reference,2828,3696,0,id->4);assertEquals(1,samples.size());assertFalse(samples.get(0).transition);
    }
    @Test public void onlyStableChangesDuringSameVisitAreTransitions()
    {
        GrowthSampler sampler=new GrowthSampler();for(int i=0;i<5;i++)sampler.read(reference,2828,3696,0,id->0);
        assertTrue(sampler.read(reference,2828,3696,0,id->4).isEmpty());
        assertTrue(sampler.read(reference,2828,3696,0,id->4).get(0).transition);
        assertTrue(sampler.read(reference,2828,3696,0,id->4).isEmpty());
    }
    @Test public void changingAreaAndModalResetDiscardOldBaselines()
    {
        GrowthSampler sampler=new GrowthSampler();for(int i=0;i<5;i++)sampler.read(reference,2828,3696,0,id->0);
        for(int i=0;i<4;i++)assertTrue(sampler.read(reference,2847,3933,0,id->4).isEmpty());
        GrowthSampler.Sample sample=sampler.read(reference,2847,3933,0,id->4).get(0);assertEquals(Patch.WEISS_HERB,sample.patch);assertFalse(sample.transition);
        sampler.reset();assertTrue(sampler.read(reference,2847,3933,0,id->4).isEmpty());
        assertTrue(sampler.read(reference,0,0,0,id->{throw new AssertionError("Unexpected read");}).isEmpty());
    }
    @Test public void unchangedPayloadsAreRefreshedPeriodicallyWithoutTransitionFlags()
    {
        GrowthSampler sampler=new GrowthSampler();for(int i=0;i<5;i++)sampler.read(reference,2828,3696,0,id->4);
        for(int i=0;i<499;i++)assertTrue(sampler.read(reference,2828,3696,0,id->4).isEmpty());
        List<GrowthSampler.Sample> refresh=sampler.read(reference,2828,3696,0,id->4);
        assertEquals(1,refresh.size());assertFalse(refresh.get(0).transition);
    }
}
