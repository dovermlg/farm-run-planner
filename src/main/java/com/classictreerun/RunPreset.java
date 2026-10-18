package com.classictreerun;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** Starter templates and legacy slot identifiers for one-time import. */
public enum RunPreset
{
    CUSTOM("Named / current run"),
    HERBS("Herbs"),
    TREES("Trees"),
    HERBS_AND_TREES("Herbs + trees"),
    CUSTOM_1("Custom preset 1"),
    CUSTOM_2("Custom preset 2");

    private final String label;
    RunPreset(String label) { this.label = label; }
    @Override public String toString() { return label; }

    String key() { return "presetSelection_" + name(); }

    Set<Patch> starter(Set<Patch> current)
    {
        if (this == CUSTOM || this == CUSTOM_1 || this == CUSTOM_2) { return new LinkedHashSet<>(current); }
        Set<Patch> selected = EnumSet.noneOf(Patch.class);
        for (Patch patch : Patch.values())
        {
            boolean herb = patch.getType() == Patch.Type.HERB;
            boolean tree = treeType(patch.getType());
            if (this == HERBS && herb || this == TREES && tree || this == HERBS_AND_TREES && (herb || tree))
            {
                selected.add(patch);
            }
        }
        return selected;
    }

    private static boolean treeType(Patch.Type type)
    {
        switch (type)
        {
            case TREE: case FRUIT: case HARDWOOD: case CALQUAT: case CRYSTAL: case CELASTRUS: case REDWOOD: return true;
            default: return false; // Spirit trees are usually planted once, so are opt-in edits.
        }
    }
}
