package com.classictreerun;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class CatalogueTest
{
    @Test public void recurringPatchCatalogueMatchesAuditedTypeCounts() throws Exception
    {
        Map<Patch.Type, Integer> counts = new EnumMap<>(Patch.Type.class);
        for (Patch patch : Patch.values())
        {
            counts.merge(patch.getType(), 1, Integer::sum);
            assertNotNull(patch.getSite());
            assertFalse(patch.getRequirements().isEmpty());
            assertTrue(TravelGuide.instructions(patch, new ClassicTreeRunConfig() { }).length() > 40);
        }
        assertEquals(99, Patch.values().length);
        assertEquals(Integer.valueOf(7), counts.get(Patch.Type.TREE));
        assertEquals(Integer.valueOf(7), counts.get(Patch.Type.FRUIT));
        assertEquals(Integer.valueOf(5), counts.get(Patch.Type.HARDWOOD));
        assertEquals(Integer.valueOf(3), counts.get(Patch.Type.CALQUAT));
        assertEquals(Integer.valueOf(17), counts.get(Patch.Type.ALLOTMENT));
        assertEquals(Integer.valueOf(9), counts.get(Patch.Type.FLOWER));
        assertEquals(Integer.valueOf(10), counts.get(Patch.Type.HERB));
        assertEquals(Integer.valueOf(5), counts.get(Patch.Type.HOPS));
        assertEquals(Integer.valueOf(5), counts.get(Patch.Type.BUSH));
        assertEquals(Integer.valueOf(5), counts.get(Patch.Type.SPIRIT));
        assertEquals(Integer.valueOf(12), counts.get(Patch.Type.GRAPE));
        assertEquals(Integer.valueOf(2), counts.get(Patch.Type.CORAL));
        assertEquals(Integer.valueOf(2), counts.get(Patch.Type.SEAWEED));
        assertEquals(Integer.valueOf(2), counts.get(Patch.Type.CACTUS));
        assertEquals(Integer.valueOf(2), counts.get(Patch.Type.BELLADONNA));
        // Test output makes the shipped catalogue inspectable without launching RuneLite.
        StringBuilder catalogue = new StringBuilder("# Patch catalogue\n\n99 recurring crop patches, audited 2026-10-18. Each row is individually selectable.\n\n");
        catalogue.append("| ID | Type | Location | Patch | Access / minimum level |\n| --- | --- | --- | --- | --- |\n");
        for (Patch p : Patch.values())
        {
            catalogue.append("| ").append(p.name()).append(" | ").append(p.getType()).append(" | ")
                .append(p.getLocation()).append(" | ").append(p.getDirections()).append(" | ")
                .append(p.getRequirements()).append(" |\n");
        }
        Files.createDirectories(Paths.get("build/reports"));
        Files.write(Paths.get("build/reports/patch-catalogue.md"), catalogue.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Test public void expandedCatalogueDoesNotSilentlyChangeLegacySelections()
    {
        ClassicTreeRunConfig legacy = new ClassicTreeRunConfig() { };
        assertEquals(Patch.classic(), RouteState.active(legacy));
        ClassicTreeRunConfig herbs = new ClassicTreeRunConfig()
        {
            @Override public String selection() { return "TROLL_HERB,WEISS_HERB"; }
        };
        assertEquals(Arrays.asList(Patch.TROLL_HERB, Patch.WEISS_HERB), RouteState.active(herbs));
        ClassicTreeRunConfig empty = new ClassicTreeRunConfig()
        {
            @Override public String selection() { return "-"; }
        };
        assertTrue(RouteState.active(empty).isEmpty());
        assertEquals(16, RouteState.parse("LUMBRIDGE_TREE," + RouteState.encode(Patch.classic())).size());
    }
}
