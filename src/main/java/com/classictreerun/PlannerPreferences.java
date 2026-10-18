package com.classictreerun;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Native cog values use RuneLite storage; snapshots and progress use local files. */
final class PlannerPreferences implements ClassicTreeRunPanel.Preferences
{
    private final ClassicTreeRunPanel.Preferences nativeSettings;
    private final ClassicTreeRunPanel.Preferences local;
    private final Set<String> nativeKeys = ConfigValues.KEYS;
    PlannerPreferences(ClassicTreeRunPanel.Preferences nativeSettings, ClassicTreeRunPanel.Preferences local)
    {
        this.nativeSettings = nativeSettings; this.local = local;
    }
    public String get(String key) { return (nativeKeys.contains(key) ? nativeSettings : local).get(key); }
    public void set(String key, String value)
    {
        Map<String, String> changes = new LinkedHashMap<>(); changes.put(key, value); update(changes);
    }
    @Override public void update(Map<String, String> changes)
    {
        Map<String, String> localChanges = new LinkedHashMap<>();
        Map<String, String> nativeChanges = new LinkedHashMap<>();
        changes.forEach((key, value) -> (nativeKeys.contains(key) ? nativeChanges : localChanges).put(key, value));
        // A local write failure must not partially change the active native configuration.
        if (!localChanges.isEmpty()) { local.update(localChanges); }
        if (!nativeChanges.isEmpty()) { nativeSettings.update(nativeChanges); }
    }
    void initializeCogActions()
    {
        if (nativeSettings.get("editorPreset") == null) { nativeSettings.set("editorPreset", EditorPreset.CURRENT.name()); }
        // Ensure the very first user click has an old value and is distinguishable from initialization.
        for (String key : nativeKeys)
        {
            if (PresetActions.isAction(key) && nativeSettings.get(key) == null) { nativeSettings.set(key, "false"); }
        }
    }
    void importLocalWorkingRun()
    {
        if ("true".equals(local.get("nativeCogV2"))) { return; }
        Map<String, String> imported = new LinkedHashMap<>();
        for (String key : nativeKeys) { String value = local.get(key); if (value != null) { imported.put(key, value); } }
        nativeSettings.update(imported);
        local.set("nativeCogV2", "true");
    }
}
