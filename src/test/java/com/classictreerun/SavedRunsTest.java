package com.classictreerun;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class SavedRunsTest
{
    @Test public void namesAndExactSelectionsSurviveRoundTrip()
    {
        Map<String, Set<Patch>> runs = new LinkedHashMap<>();
        runs.put("Daily: fruit; 🌳", RouteState.parse("LLETYA_FRUIT,GUILD_FRUIT"));
        runs.put("Hardwoods only", RouteState.parse("FOSSIL_EAST,FOSSIL_MIDDLE,FOSSIL_WEST"));
        runs.put("New crops", RouteState.parse("TROLL_HERB,WEISS_HERB,CORAL_E,NEMUS_TREE"));
        runs.put("Empty", new LinkedHashSet<>());
        assertEquals(runs, SavedRuns.decode(SavedRuns.encode(runs)));
        assertTrue(SavedRuns.decode(null).isEmpty());
    }

    @Test public void overwriteAndBadRecordRecovery()
    {
        Map<String, Set<Patch>> runs = new LinkedHashMap<>();
        runs.put("Daily", Collections.singleton(Patch.GUILD_FRUIT));
        runs.put("Daily", Collections.singleton(Patch.LLETYA_FRUIT));
        Map<String, Set<Patch>> restored = SavedRuns.decode("broken;***:X;" + SavedRuns.encode(runs));
        assertEquals(1, restored.size());
        assertEquals(Collections.singleton(Patch.LLETYA_FRUIT), restored.get("Daily"));
    }
}
