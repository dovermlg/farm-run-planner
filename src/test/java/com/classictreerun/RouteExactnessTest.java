package com.classictreerun;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class RouteExactnessTest
{
    @Test public void structuredEighteenStopRouteReachesTheGlobalOptimum()
    {
        Random random=new Random(2237);int n=18;
        int[] entry=new int[n];int[][] travel=new int[n][n];
        for(int i=0;i<n;i++)entry[i]=100+random.nextInt(100);
        for(int i=0;i<n;i++)for(int j=0;j<n;j++)travel[i][j]=i/9==j/9?1+random.nextInt(entry[j]-1):entry[j];
        List<Patch> input=List.of(Patch.values()).subList(0,n);
        int expected=RouteAuditOracle.subsetCosts(entry,travel)[(1<<n)-1];
        assertEquals(603,expected);
        assertEquals(expected,RouteAuditOracle.score(RouteOptimizer.optimize(input,entry,travel),input,entry,travel));
    }

    @Test public void allTravelSettingsKeepEveryDiscountGroupWithinTheExactLimit()
    {
        int maximum=0,count=0;List<Patch> patches=List.of(Patch.values());
        assertEquals(99,patches.size());
        for(ClassicTreeRunConfig config:RouteAuditOracle.models())
        {
            TravelCosts costs=new TravelCosts(patches,config);
            for(List<Integer> group:RouteAuditOracle.groups(costs.entry,costs.travel))maximum=Math.max(maximum,group.size());
            count++;
        }
        assertEquals(1728,count);assertEquals(13,maximum);
    }

    @Test public void sparseDisconnectedChainsAndAsymmetricEntriesMatchTheIndependentOracle()
    {
        Random random=new Random(6073);List<Patch> patches=Patch.classic().subList(0,8);
        for(int trial=0;trial<40;trial++)
        {
            int n=patches.size();int[] entry=new int[n];int[][] travel=new int[n][n];
            for(int i=0;i<n;i++)entry[i]=10+random.nextInt(80);
            for(int i=0;i<n;i++)for(int j=0;j<n;j++)
                travel[i][j]=i/4==j/4 && random.nextBoolean()?random.nextInt(entry[j]):entry[j];
            int expected=RouteAuditOracle.subsetCosts(entry,travel)[(1<<n)-1];
            assertEquals(expected,RouteAuditOracle.score(RouteOptimizer.optimize(patches,entry,travel),patches,entry,travel));
        }
    }

    @Test public void componentOrderingCannotDiscardExpensiveCrossComponentEdges()
    {
        // This arbitrary matrix violates the destination-teleport bound; decomposition is invalid.
        List<Patch> patches=Patch.classic().subList(0,4);int[] entry={10,10,10,10};
        int[][] travel={{0,1,100,100},{100,0,100,100},{100,100,0,1},{1,100,100,0}};
        assertEquals(13,RouteAuditOracle.score(RouteOptimizer.optimize(patches,entry,travel),patches,entry,travel));
    }
}
