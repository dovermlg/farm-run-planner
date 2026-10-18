package com.classictreerun;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Stable native enum choices; labels reflect this profile's locally saved slot names. */
public enum EditorPreset
{
    CURRENT,
    SLOT_01, SLOT_02, SLOT_03, SLOT_04, SLOT_05, SLOT_06, SLOT_07, SLOT_08, SLOT_09, SLOT_10, SLOT_11, SLOT_12, SLOT_13, SLOT_14, SLOT_15, SLOT_16, SLOT_17, SLOT_18, SLOT_19, SLOT_20, SLOT_21, SLOT_22, SLOT_23, SLOT_24, SLOT_25, SLOT_26, SLOT_27, SLOT_28, SLOT_29, SLOT_30, SLOT_31, SLOT_32;

    private static volatile Map<EditorPreset, String> labels = Collections.emptyMap();
    static void publish(Map<EditorPreset, String> names)
    {
        Map<EditorPreset, String> copy = new EnumMap<>(EditorPreset.class);
        copy.putAll(names); labels = Collections.unmodifiableMap(copy);
    }
    static void clearLabels() { labels = Collections.emptyMap(); }
    @Override public String toString()
    {
        if (this == CURRENT) { return "New / current run"; }
        String name = labels.get(this);
        if (name == null) { return number() + ": Empty"; }
        int count = name.codePointCount(0, name.length());
        String shortName = count > 14 ? name.substring(0, name.offsetByCodePoints(0, 14)) + "..." : name;
        return number() + ": " + shortName;
    }
    int number() { return this == CURRENT ? 0 : Integer.parseInt(name().substring(5)); }
}
