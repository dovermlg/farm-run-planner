package com.classictreerun;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** Explicit snapshots: draft edits never overwrite a saved preset. */
final class PresetLibrary
{
    static final String KEY = "localPresetsV2";
    private final ClassicTreeRunPanel.Preferences preferences;
    private final Properties saved;
    PresetLibrary(ClassicTreeRunPanel.Preferences preferences)
    {
        this.preferences = preferences;
        saved = decode(preferences.get(KEY));
    }
    Set<String> names() { return new TreeSet<>(saved.stringPropertyNames()); }
    boolean contains(String name) { return name != null && saved.containsKey(name); }
    static String validName(String name)
    {
        String clean = name.trim();
        if (clean.isEmpty() || clean.length() > 60 || clean.chars().anyMatch(Character::isISOControl))
        { throw new IllegalArgumentException("Use a name of 1–60 characters, without line breaks."); }
        return clean;
    }

    void save(String name, ClassicTreeRunConfig config)
    {
        save(name, config, null, java.util.Collections.emptyMap());
    }

    void save(String name, ClassicTreeRunConfig config, String replacedName, Map<String, String> extras)
    {
        String clean = validName(name);
        Properties snapshot = snapshot(config);
        snapshot.setProperty("runName", clean);
        Properties next = new Properties();
        next.putAll(saved);
        if (replacedName != null && !replacedName.equals(clean)) { next.remove(replacedName); }
        next.setProperty(clean, encode(snapshot));
        Map<String, String> changes = new LinkedHashMap<>();
        changes.put(KEY, encode(next));
        changes.put("runName", clean);
        changes.putAll(extras);
        preferences.update(changes);
        saved.clear(); saved.putAll(next);
    }

    private static Properties snapshot(ClassicTreeRunConfig config)
    {
        Properties snapshot = new Properties();
        ConfigValues.read(config).forEach((key,value) -> { if (snapshotKey(key)) { snapshot.setProperty(key,value); } });
        return snapshot;
    }

    void seedStarters(ClassicTreeRunConfig config)
    {
        if ("true".equals(preferences.get("startersV2"))) { return; }
        Properties next = new Properties(); next.putAll(saved);
        for (RunPreset preset : new RunPreset[]{RunPreset.HERBS, RunPreset.TREES, RunPreset.HERBS_AND_TREES})
        {
            if (next.containsKey(preset.toString())) { continue; }
            Properties template = snapshot(config);
            Set<Patch> patches = preset.starter(java.util.Collections.emptySet());
            for (Patch patch : Patch.values()) { template.setProperty("patch_" + patch.name(), Boolean.toString(patches.contains(patch))); }
            template.setProperty("selectionV2", RouteState.encode(patches));
            template.setProperty("excluded", ""); template.setProperty("order", "");
            template.setProperty("runName", preset.toString());
            for (String gate : new String[]{"trees", "fruit", "hardwood", "calquat", "otherPatches"}) { template.setProperty(gate, "true"); }
            next.setProperty(preset.toString(), encode(template));
        }
        Map<String, String> changes = new LinkedHashMap<>(); changes.put(KEY, encode(next)); changes.put("startersV2", "true");
        preferences.update(changes); saved.clear(); saved.putAll(next);
    }

    void load(String name) { load(name, true); }

    void load(String name, boolean resetCompletion)
    {
        if (!contains(name)) { throw new IllegalArgumentException("Choose a saved preset first."); }
        Properties snapshot = decode(saved.getProperty(name));
        Map<String, String> changes = new LinkedHashMap<>();
        snapshot.forEach((key, value) -> { if (snapshotKey((String) key)) { changes.put((String) key, (String) value); } });
        changes.put("runName", name);
        // Loading a run starts a fresh checklist; completion is never part of a preset.
        if (resetCompletion) { changes.put(ClassicTreeRunPanel.PROGRESS_KEY, ""); }
        preferences.update(changes);
    }

    void delete(String name) { delete(name, java.util.Collections.emptyMap()); }

    void delete(String name, Map<String, String> extras)
    {
        Properties next = new Properties(); next.putAll(saved); next.remove(name);
        Map<String, String> changes = new LinkedHashMap<>(extras); changes.put(KEY, encode(next));
        preferences.update(changes);
        saved.clear(); saved.putAll(next);
    }

    java.util.List<Patch> patches(String name)
    {
        if(!contains(name))return java.util.Collections.emptyList();
        Properties snapshot=decode(saved.getProperty(name));
        ClassicTreeRunPanel.Preferences values=new ClassicTreeRunPanel.Preferences(){
            public String get(String key){return snapshot.getProperty(key);}
            public void set(String key,String value){throw new UnsupportedOperationException("Read-only preset");}
        };
        return RouteState.active(new LocalRunConfig(values));
    }

    private static boolean snapshotKey(String key)
    {
        return key.startsWith("patch_") || key.endsWith("Travel") || "runName".equals(key)
            || "autoOptimize".equals(key) || "showDetails".equals(key) || "hideCompleted".equals(key)
            || "order".equals(key) || "selectionV2".equals(key) || "excluded".equals(key)
            || "trees".equals(key) || "fruit".equals(key) || "hardwood".equals(key)
            || "calquat".equals(key) || "otherPatches".equals(key);
    }

    private static Properties decode(String text)
    {
        Properties result = new Properties();
        if (text == null) { return result; }
        try { result.load(new StringReader(text)); }
        catch (IOException | IllegalArgumentException error) { throw new IllegalStateException("Saved presets could not be read; original file preserved", error); }
        return result;
    }
    private static String encode(Properties values)
    {
        try { StringWriter writer = new StringWriter(); values.store(writer, null); return writer.toString(); }
        catch (IOException impossible) { throw new IllegalStateException(impossible); }
    }
}
