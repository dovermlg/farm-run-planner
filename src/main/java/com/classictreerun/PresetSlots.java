package com.classictreerun;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Local, stable mappings from native editor slot IDs to saved snapshot names. */
final class PresetSlots
{
    static final String KEY = "editorSlotsV1";
    private final ClassicTreeRunPanel.Preferences preferences;
    private final Map<EditorPreset, String> slots = new EnumMap<>(EditorPreset.class);

    PresetSlots(ClassicTreeRunPanel.Preferences preferences)
    {
        this.preferences = preferences;
        Properties stored = new Properties();
        try
        {
            if (preferences.get(KEY) != null) { stored.load(new StringReader(preferences.get(KEY))); }
        }
        catch (IOException | IllegalArgumentException error) { throw new IllegalStateException("Couldn't read local preset slots; original file preserved", error); }
        PresetLibrary library = new PresetLibrary(preferences);
        for (EditorPreset slot : EditorPreset.values())
        {
            String name = stored.getProperty(slot.name());
            if (slot != EditorPreset.CURRENT && library.contains(name) && !slots.containsValue(name)) { slots.put(slot, name); }
        }
        Map<EditorPreset, String> before = new EnumMap<>(slots);
        // Adopt earlier named presets without replacing or deleting their snapshots.
        for (String name : library.names())
        {
            if (slots.containsValue(name)) { continue; }
            EditorPreset free = firstFree(slots);
            if (free == null) { break; }
            slots.put(free, name);
        }
        if (preferences.get(KEY) == null || !before.equals(slots) || stored.size() != slots.size())
        { preferences.set(KEY, encode(slots)); }
        EditorPreset.publish(slots);
    }

    String name(EditorPreset slot) { return slots.get(slot); }
    EditorPreset find(String name)
    {
        for (Map.Entry<EditorPreset, String> entry : slots.entrySet()) { if (entry.getValue().equals(name)) { return entry.getKey(); } }
        return EditorPreset.CURRENT;
    }

    EditorPreset save(EditorPreset selected, String name, ClassicTreeRunConfig config)
    {
        String clean = PresetLibrary.validName(name);
        PresetLibrary library = new PresetLibrary(preferences);
        EditorPreset target = selected;
        if (target == EditorPreset.CURRENT)
        {
            target = find(clean);
            if (target == EditorPreset.CURRENT) { target = firstFree(slots); }
        }
        if (target == null) { throw new IllegalArgumentException("All 32 editor slots are used. Delete an unused preset or edit an existing slot."); }
        String previous = slots.get(target);
        if (library.contains(clean) && !clean.equals(previous))
        { throw new IllegalArgumentException("That name is already saved. Choose its slot or use a different name."); }
        Map<EditorPreset, String> next = new EnumMap<>(slots); next.put(target, clean);
        Map<String, String> extras = new LinkedHashMap<>(); extras.put(KEY, encode(next));
        library.save(clean, config, previous, extras);
        slots.clear(); slots.putAll(next); EditorPreset.publish(slots);
        return target;
    }

    void delete(EditorPreset selected, String currentName)
    {
        String target = selected == EditorPreset.CURRENT ? currentName : slots.get(selected);
        PresetLibrary library = new PresetLibrary(preferences);
        if (!library.contains(target)) { throw new IllegalArgumentException("Choose a saved preset slot to delete."); }
        Map<EditorPreset, String> next = new EnumMap<>(slots); next.values().removeIf(target::equals);
        Map<String, String> extras = new LinkedHashMap<>(); extras.put(KEY, encode(next));
        library.delete(target, extras);
        slots.clear(); slots.putAll(next); EditorPreset.publish(slots);
    }

    private static EditorPreset firstFree(Map<EditorPreset, String> values)
    {
        for (EditorPreset slot : EditorPreset.values()) { if (slot != EditorPreset.CURRENT && !values.containsKey(slot)) { return slot; } }
        return null;
    }
    private static String encode(Map<EditorPreset, String> values)
    {
        Properties data = new Properties(); values.forEach((slot, name) -> data.setProperty(slot.name(), name));
        try { StringWriter out = new StringWriter(); data.store(out, null); return out.toString(); }
        catch (IOException impossible) { throw new IllegalStateException(impossible); }
    }
}
