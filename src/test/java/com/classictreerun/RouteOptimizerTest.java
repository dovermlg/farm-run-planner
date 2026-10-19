package com.classictreerun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class RouteOptimizerTest
{
    @Test public void supersededExactAndLargeCalculationsCanStopWithoutThreadInterruption()
    {
        for(List<Patch> selected:List.of(Patch.classic(),List.of(Patch.values())))
        {
            TravelCosts costs=new TravelCosts(selected,new ClassicTreeRunConfig(){});
            java.util.concurrent.atomic.AtomicInteger checks=new java.util.concurrent.atomic.AtomicInteger();
            assertThrows(java.util.concurrent.CancellationException.class,()->RouteOptimizer.optimize(selected,
                costs.entry,costs.travel,()->checks.incrementAndGet()==3));
            assertEquals(3,checks.get());
        }
    }
    @Test public void directedRelocateDeltasReachTheSameLocalMinimumAsFullScoring()
    {
        Random random=new Random(190);
        List<Patch> selected=new ArrayList<>(Arrays.asList(Patch.values()).subList(0,19));
        for(int trial=0;trial<20;trial++)
        {
            int n=selected.size();int[] entry=new int[n];int[][] travel=new int[n][n];
            for(int i=0;i<n;i++){entry[i]=1+random.nextInt(200);for(int j=0;j<n;j++)travel[i][j]=1+random.nextInt(200);}
            List<Patch> result=RouteOptimizer.optimize(selected,entry,travel);
            int best=score(result,selected,entry,travel);
            assertTrue(best<=score(selected,selected,entry,travel));
            for(int from=0;from<n;from++)for(int to=0;to<n;to++)
            {
                List<Patch> moved=new ArrayList<>(result);Patch stop=moved.remove(from);moved.add(to,stop);
                assertTrue("Improving move missed: "+from+" -> "+to,score(moved,selected,entry,travel)>=best);
            }
        }
    }
    @Test public void catherbyCharterConnectsFruitPatchesAndHandlesIndependentArrival()
    {
        ClassicTreeRunConfig charter = new ClassicTreeRunConfig()
        {
            @Override public BrimhavenTravel brimhavenTravel() { return BrimhavenTravel.CATHERBY_CHARTER; }
        };
        // Reverse input verifies that this travel setting actually changes the planned sequence.
        List<Patch> reversed = Arrays.asList(Patch.BRIMHAVEN_FRUIT, Patch.CATHERBY_FRUIT);
        TravelCosts costs = new TravelCosts(reversed, charter);
        assertEquals(Arrays.asList(Patch.CATHERBY_FRUIT, Patch.BRIMHAVEN_FRUIT),
            RouteOptimizer.optimize(reversed, costs.entry, costs.travel));
        String connected = TravelCosts.walkingInstruction(Patch.CATHERBY_FRUIT, Patch.BRIMHAVEN_FRUIT, charter);
        assertTrue(connected.contains("Catherby docks"));
        assertTrue(connected.contains("Trader Crewmember"));
        assertTrue(connected.contains("coins"));
        List<Patch> alone = Collections.singletonList(Patch.BRIMHAVEN_FRUIT);
        TravelCosts independent = new TravelCosts(alone, charter);
        assertEquals(alone, RouteOptimizer.optimize(alone, independent.entry, independent.travel));
        assertTrue(TravelGuide.instructions(Patch.BRIMHAVEN_FRUIT, charter).contains("If you are elsewhere"));
        ClassicTreeRunConfig camelot = new ClassicTreeRunConfig()
        {
            @Override public BrimhavenTravel brimhavenTravel() { return BrimhavenTravel.CATHERBY_CHARTER; }
            @Override public CatherbyTravel catherbyTravel() { return CatherbyTravel.CAMELOT_TELEPORT; }
        };
        assertTrue(TravelGuide.instructions(Patch.BRIMHAVEN_FRUIT, camelot).contains("Camelot Teleport"));
        assertTrue(TravelCosts.arrival(Patch.BRIMHAVEN_FRUIT, camelot) > TravelCosts.arrival(Patch.BRIMHAVEN_FRUIT, charter));
        assertNull(TravelCosts.walkingInstruction(Patch.CATHERBY_FRUIT, Patch.BRIMHAVEN_FRUIT, new ClassicTreeRunConfig() { }));
    }

    @Test public void exactPlannerMatchesBruteForceForDirectedRoutes()
    {
        Random random = new Random(741);
        List<Patch> selected = Arrays.asList(Patch.LUMBRIDGE_TREE, Patch.GUILD_TREE, Patch.GUILD_FRUIT,
            Patch.FOSSIL_EAST, Patch.FOSSIL_WEST, Patch.CATHERBY_FRUIT);
        for (int trial = 0; trial < 10; trial++)
        {
            int n = selected.size();
            int[] entry = new int[n];
            int[][] travel = new int[n][n];
            for (int i = 0; i < n; i++)
            {
                entry[i] = 1 + random.nextInt(80);
                for (int j = 0; j < n; j++) { travel[i][j] = 1 + random.nextInt(80); }
            }
            List<Patch> result = RouteOptimizer.optimize(selected, entry, travel);
            assertEquals(bruteForce(selected, entry, travel, 0), score(result, selected, entry, travel));
            assertEquals(new HashSet<>(selected), new HashSet<>(result));
        }
    }

    @Test public void fullRunKeepsEveryPatchAndNeverWorsensEstimatedCost()
    {
        List<Patch> patches = new ArrayList<>(Patch.classic());
        Collections.rotate(patches, 7);
        TravelCosts costs = new TravelCosts(patches, new ClassicTreeRunConfig() { });
        List<Patch> planned = RouteOptimizer.optimize(patches, costs.entry, costs.travel);
        assertEquals(16, planned.size());
        assertEquals(16, new HashSet<>(planned).size());
        assertTrue(score(planned, patches, costs.entry, costs.travel) <= score(patches, patches, costs.entry, costs.travel));
        int east = planned.indexOf(Patch.FOSSIL_EAST);
        int middle = planned.indexOf(Patch.FOSSIL_MIDDLE);
        int west = planned.indexOf(Patch.FOSSIL_WEST);
        assertEquals(2, Math.max(east, Math.max(middle, west)) - Math.min(east, Math.min(middle, west)));
        assertEquals(1, Math.abs(planned.indexOf(Patch.GUILD_TREE) - planned.indexOf(Patch.GUILD_FRUIT)));
    }

    @Test public void supportsEmptySingleAndSparseSelections()
    {
        assertTrue(RouteOptimizer.optimize(Collections.emptyList(), new int[0], new int[0][0]).isEmpty());
        List<Patch> one = Collections.singletonList(Patch.FOSSIL_MIDDLE);
        assertEquals(one, RouteOptimizer.optimize(one, new int[]{50}, new int[][]{{0}}));
        List<Patch> two = Arrays.asList(Patch.FOSSIL_WEST, Patch.GUILD_FRUIT);
        TravelCosts costs = new TravelCosts(two, new ClassicTreeRunConfig() { });
        assertEquals(new HashSet<>(two), new HashSet<>(RouteOptimizer.optimize(two, costs.entry, costs.travel)));
    }

    @Test public void largeCatalogueUsesBoundedSearchWithoutDroppingOrAddingStops()
    {
        List<Patch> selected = new ArrayList<>(Arrays.asList(Patch.values()));
        Collections.shuffle(selected, new Random(45));
        TravelCosts costs = new TravelCosts(selected, new ClassicTreeRunConfig() { });
        List<Patch> result = RouteOptimizer.optimize(selected, costs.entry, costs.travel);
        assertEquals(selected.size(), result.size());
        assertEquals(new HashSet<>(selected), new HashSet<>(result));
        assertTrue(score(result, selected, costs.entry, costs.travel) <= score(selected, selected, costs.entry, costs.travel));
        assertEquals(Integer.MAX_VALUE, TravelCosts.walking(Patch.FOSSIL_EAST, Patch.ANGLERS_HARDWOOD, new ClassicTreeRunConfig() { }));
    }

    @Test public void teleportPreferencesChangeTheModelAndWalkingDirectionsMatch()
    {
        ClassicTreeRunConfig normal = new ClassicTreeRunConfig() { };
        ClassicTreeRunConfig walking = new ClassicTreeRunConfig()
        {
            @Override public CalquatTravel calquatTravel() { return CalquatTravel.WALK_FROM_BRIMHAVEN; }
        };
        List<Patch> patches = Arrays.asList(Patch.BRIMHAVEN_FRUIT, Patch.TAI_BWO_WANNAI_CALQUAT);
        TravelCosts scroll = new TravelCosts(patches, normal);
        TravelCosts walk = new TravelCosts(patches, walking);
        assertNotEquals(scroll.travel[0][1], walk.travel[0][1]);
        assertEquals(patches, RouteOptimizer.optimize(patches, walk.entry, walk.travel));
        assertTrue(TravelCosts.walkingInstruction(Patch.GUILD_FRUIT, Patch.GUILD_TREE, normal).contains("west wing"));
        assertNull(TravelCosts.walkingInstruction(null, Patch.GUILD_TREE, normal));
    }

    private static int score(List<Patch> order, List<Patch> indices, int[] entry, int[][] travel)
    {
        if (order.isEmpty()) { return 0; }
        int result = entry[indices.indexOf(order.get(0))];
        for (int i = 1; i < order.size(); i++) { result += travel[indices.indexOf(order.get(i - 1))][indices.indexOf(order.get(i))]; }
        return result;
    }

    private static int bruteForce(List<Patch> patches, int[] entry, int[][] travel, int position)
    {
        if (position == patches.size())
        {
            List<Patch> indices = Arrays.asList(Patch.LUMBRIDGE_TREE, Patch.GUILD_TREE, Patch.GUILD_FRUIT,
                Patch.FOSSIL_EAST, Patch.FOSSIL_WEST, Patch.CATHERBY_FRUIT);
            return score(patches, indices, entry, travel);
        }
        int best = Integer.MAX_VALUE;
        for (int next = position; next < patches.size(); next++)
        {
            Collections.swap(patches, position, next);
            best = Math.min(best, bruteForce(patches, entry, travel, position + 1));
            Collections.swap(patches, position, next);
        }
        return best;
    }
}
