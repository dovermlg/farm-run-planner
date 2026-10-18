package com.classictreerun;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class RunPresetTest
{
    @Test public void starterSelectionsContainTheIntendedCropFamilies()
    {
        Set<Patch> herbs = RunPreset.HERBS.starter(Collections.emptySet());
        Set<Patch> trees = RunPreset.TREES.starter(Collections.emptySet());
        Set<Patch> combined = RunPreset.HERBS_AND_TREES.starter(Collections.emptySet());
        assertEquals(10, herbs.size());
        assertEquals(25, trees.size());
        assertEquals(35, combined.size());
        assertTrue(herbs.stream().allMatch(p -> p.getType() == Patch.Type.HERB));
        assertTrue(combined.containsAll(herbs));
        assertTrue(combined.containsAll(trees));
        assertFalse(trees.stream().anyMatch(p -> p.getType() == Patch.Type.SPIRIT));
    }

}
