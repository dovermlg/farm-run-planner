package com.classictreerun;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class RouteStateTest
{
    private final ClassicTreeRunConfig defaults = new ClassicTreeRunConfig() { };

    @Test public void classicSetHasExactCountsAndStableOrder()
    {
        List<Patch> route = RouteState.active(defaults);
        assertEquals(16, route.size());
        assertEquals(6, route.stream().filter(p -> p.getType() == Patch.Type.TREE).count());
        assertEquals(6, route.stream().filter(p -> p.getType() == Patch.Type.FRUIT).count());
        assertEquals(3, route.stream().filter(p -> p.getType() == Patch.Type.HARDWOOD).count());
        assertEquals(1, route.stream().filter(p -> p.getType() == Patch.Type.CALQUAT).count());
        assertEquals(Patch.GUILD_FRUIT, route.get(15));
    }

    @Test public void customOrderIgnoresUnknownAndDuplicateIdsButNeverLosesPatches()
    {
        List<Patch> route = RouteState.ordered(" FOSSIL_WEST,unknown,FOSSIL_WEST,LUMBRIDGE_TREE");
        assertEquals(Patch.values().length, route.size());
        assertEquals(Patch.FOSSIL_WEST, route.get(0));
        assertEquals(Patch.LUMBRIDGE_TREE, route.get(1));
        assertEquals(Patch.values().length, new java.util.HashSet<>(route).size());
    }

    @Test public void categoryAndPerPatchExclusionsCombine()
    {
        ClassicTreeRunConfig c = new ClassicTreeRunConfig()
        {
            @Override public boolean hardwood() { return false; }
            @Override public String excluded() { return "GUILD_TREE,GUILD_FRUIT"; }
        };
        assertEquals(11, RouteState.active(c).size());
        assertFalse(RouteState.active(c).contains(Patch.GUILD_FRUIT));
    }

    @Test public void completionRoundTripsAndResetAlsoClearsDisabledPatches()
    {
        RouteState state = new RouteState();
        state.setDone(Patch.FOSSIL_EAST, true);
        state.setDone(Patch.FOSSIL_WEST, true);
        RouteState restored = new RouteState();
        restored.load(state.save() + ",obsolete-id");
        assertTrue(restored.isDone(Patch.FOSSIL_EAST));
        assertFalse(restored.isDone(Patch.FOSSIL_MIDDLE));
        assertEquals(1, restored.count(Arrays.asList(Patch.FOSSIL_EAST, Patch.FOSSIL_MIDDLE)));
        restored.setDone(Patch.FOSSIL_EAST, false);
        assertFalse(restored.isDone(Patch.FOSSIL_EAST));
        restored.reset();
        assertEquals("", restored.save());
        restored.load(null);
        assertEquals(0, restored.count(RouteState.active(defaults)));
    }

    @Test public void nextHandlesPartialCompleteAndEmptyRuns()
    {
        RouteState state = new RouteState();
        List<Patch> route = RouteState.active(defaults);
        assertEquals(Patch.LUMBRIDGE_TREE, state.next(route));
        route.forEach(p -> state.setDone(p, true));
        assertNull(state.next(route));
        assertNull(state.next(java.util.Collections.emptyList()));
    }

    @Test public void everyPatchAndTravelOptionHasUsableInstructions() throws Exception
    {
        for (Patch patch : Patch.values())
        {
            assertTrue(TravelGuide.instructions(patch, defaults).length() > 40);
        }
        // Exercise every enum-valued travel preference, not just the defaults.
        for (java.lang.reflect.Method method : ClassicTreeRunConfig.class.getDeclaredMethods())
        {
            if (!method.getReturnType().isEnum()) { continue; }
            for (Object option : method.getReturnType().getEnumConstants())
            {
                ClassicTreeRunConfig config = (ClassicTreeRunConfig) java.lang.reflect.Proxy.newProxyInstance(
                    ClassicTreeRunConfig.class.getClassLoader(), new Class<?>[]{ClassicTreeRunConfig.class},
                    (proxy, called, args) -> called.equals(method) ? option : called.invoke(defaults, args));
                for (Patch patch : Patch.values())
                {
                    assertTrue(TravelGuide.instructions(patch, config).length() > 40);
                }
            }
        }
        assertTrue(TravelGuide.instructions(Patch.TAI_BWO_WANNAI_CALQUAT, defaults).contains("Trio"));
        assertTrue(TravelGuide.instructions(Patch.VILLAGE_FRUIT, defaults).contains("Elkoy"));
    }
}
