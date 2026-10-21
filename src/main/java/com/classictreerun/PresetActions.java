package com.classictreerun;

/** Native cog commands and slot selection. Called on Swing's EDT. */
final class PresetActions
{
    private final ClassicTreeRunConfig config;
    private final ClassicTreeRunPanel.Preferences preferences;
    private final RunSettings settings;
    private final ClassicTreeRunPanel panel;
    private final PresetSlots slots;
    private final PresetTransfer transfer;
    private boolean changingSelection;

    PresetActions(ClassicTreeRunConfig config, ClassicTreeRunPanel.Preferences preferences,
        RunSettings settings, ClassicTreeRunPanel panel)
    {
        this(config, preferences, settings, panel, null);
    }

    PresetActions(ClassicTreeRunConfig config, ClassicTreeRunPanel.Preferences preferences,
        RunSettings settings, ClassicTreeRunPanel panel, PresetTransfer transfer)
    {
        this.config = config; this.preferences = preferences; this.settings = settings; this.panel = panel;
        this.transfer = transfer;
        slots = new PresetSlots(preferences);
    }
    static boolean isAction(String key)
    {
        return "savePresetAction".equals(key) || "deletePresetAction".equals(key)
            || "exportPresetsAction".equals(key) || "importPresetsAction".equals(key)
            || "captureEquipmentAction".equals(key) || "clearEquipmentAction".equals(key);
    }
    static boolean isCommand(String key) { return isAction(key) || "editorPreset".equals(key); }
    static boolean shouldQueue(String key, String oldValue, String newValue, boolean uiThread)
    {
        return isCommand(key) && uiThread && oldValue != null && newValue != null && !oldValue.equals(newValue);
    }
    boolean isChangingSelection() { return changingSelection; }
    private void setSelected(EditorPreset slot)
    {
        changingSelection = true;
        try { preferences.set("editorPreset", slot.name()); }
        finally { changingSelection = false; }
    }
    void syncForRun() { setSelected(slots.find(config.runName())); }

    void apply(String key)
    {
        if (!isCommand(key)) { return; }
        try
        {
            if(!"captureEquipmentAction".equals(key))panel.invalidateEquipmentCapture();
            if ("editorPreset".equals(key))
            {
                EditorPreset selected = config.editorPreset();
                if (selected == EditorPreset.CURRENT)
                {
                    panel.setStatus("Current draft kept. Choose an empty slot to create another preset.");
                    return;
                }
                String name = slots.name(selected);
                if (name == null)
                {
                    // A new slot starts as a copy of the current settings; nothing is saved yet.
                    preferences.set("runName", "New preset " + selected.number());
                    panel.presetEditing(null);
                }
                else
                {
                    // Editing a snapshot does not start a new checklist. Only main-tab selection does.
                    new PresetLibrary(preferences).load(name, false);
                    settings.changed("patch_"); settings.changed("autoOptimize"); settings.prepareNow();
                    panel.presetEditing(name);
                }
            }
            else if ("savePresetAction".equals(key))
            {
                settings.changed("patch_");
                EditorPreset saved = slots.save(config.editorPreset(), config.runName(), config);
                setSelected(saved);
                panel.presetSaved(config.runName());
            }
            else if ("deletePresetAction".equals(key))
            {
                slots.delete(config.editorPreset(), config.runName());
                panel.setStatus("Preset deleted. Current run kept; this slot is now empty.");
            }
            else if("captureEquipmentAction".equals(key))panel.captureEquipment();
            else if("clearEquipmentAction".equals(key))panel.clearEquipment();
            else
            {
                if (transfer == null) { throw new IllegalStateException("Local preset backup storage is unavailable."); }
                panel.setStatus("exportPresetsAction".equals(key)
                    ? transfer.exportPresets(config.backupFilename()) : transfer.importPresets(config.backupFilename(), slots));
            }
        }
        catch (IllegalArgumentException | IllegalStateException error) { panel.setStatus(error.getMessage()); }
    }
}
