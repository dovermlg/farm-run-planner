package com.classictreerun;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Offline stress case: all patches fit in one harvest-only load. No game client or account. */
public final class InventoryPlanningBenchmark
{
    private InventoryPlanningBenchmark() { }
    public static void main(String[] args)
    {
        ConfigFixture fixture=InventoryFixtures.light();
        for(String key:CropChoices.settings(fixture.config).keySet())
            if(key.startsWith("crop"))fixture.set(key,"NONE");
        List<Patch> route=List.of(Patch.values());
        InventoryItemData data=InventoryFixtures.data(fixture.config,route);
        ClassicTreeRunConfig config=new LocalRunConfig(fixture);
        for(int i=0;i<3;i++)InventoryLoadPlanner.plan(config,route,data);
        long[] planning=new long[7];
        for(int i=0;i<planning.length;i++)
        {
            long start=System.nanoTime();InventoryLoadPlanner.plan(config,route,data);
            planning[i]=System.nanoTime()-start;
        }
        Arrays.sort(planning);
        for(int i=0;i<100;i++)InventoryLoadPlanner.signature(config,route,data);
        long start=System.nanoTime();
        for(int i=0;i<1000;i++)InventoryLoadPlanner.signature(config,route,data);
        System.out.printf(Locale.ROOT,"All-99 harvest-only plan median %.3f ms; signature mean %.3f ms%n",
            planning[3]/1e6,(System.nanoTime()-start)/1e9);
        System.out.println("Offline fixture timings only; not game travel, rendering or real inventory measurements.");
    }
}
