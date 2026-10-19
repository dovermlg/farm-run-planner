package com.classictreerun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/** Bridges local cog settings to a prepared run. All methods are called on Swing's EDT. */
final class RunSettings
{
    static final String MIGRATED = "nativeSettingsV1";
    private final ClassicTreeRunConfig config;
    private final ClassicTreeRunPanel.Preferences preferences;
    private final ClassicTreeRunPanel panel;
    private final Timer delay;
    private Set<Patch> observed;
    private boolean dirty;
    private boolean disposed;
    private int version;
    private SwingWorker<List<Patch>,Void> worker;

    RunSettings(ClassicTreeRunConfig config, ClassicTreeRunPanel.Preferences preferences, ClassicTreeRunPanel panel)
    {
        this.config = config;
        this.preferences = preferences;
        this.panel = panel;
        delay = new Timer(200, event -> prepareNow());
        delay.setRepeats(false);
        if (!"true".equals(preferences.get(MIGRATED)))
        {
            Set<Patch> legacy = new LinkedHashSet<>(RouteState.active(config));
            Set<Patch> nativeSelection = NativePatchSelection.read(config);
            // Preserve patch flags imported from the earlier native settings editor.
            if (!nativeSelection.equals(new LinkedHashSet<>(Patch.classic()))) { legacy = nativeSelection; }
            writeNativeSelection(legacy);
            preferences.set(MIGRATED, "true");
        }
        observed = new LinkedHashSet<>(NativePatchSelection.read(config));
        mirrorSelection();
        queue();
    }

    void changed(String key)
    {
        if (disposed) { return; }
        if (key.startsWith("patch_"))
        {
            Set<Patch> selected = NativePatchSelection.read(config);
            if (!selected.equals(observed))
            {
                observed = new LinkedHashSet<>(selected);
                mirrorSelection();
                queue();
            }
        }
        else if (key.endsWith("Travel") || "autoOptimize".equals(key)) { queue(); }
        panel.reload();
    }

    void select(Set<Patch> selected)
    {
        writeNativeSelection(selected);
        observed = new LinkedHashSet<>(selected);
        mirrorSelection();
        queue();
    }

    private void writeNativeSelection(Set<Patch> selected)
    {
        Map<String, String> changes = new java.util.LinkedHashMap<>();
        for (Patch patch : Patch.values()) { changes.put("patch_" + patch.name(), Boolean.toString(selected.contains(patch))); }
        preferences.update(changes);
    }

    private void mirrorSelection()
    {
        Set<Patch> excluded = new LinkedHashSet<>(Arrays.asList(Patch.values()));
        excluded.removeAll(observed);
        Map<String, String> changes = new java.util.LinkedHashMap<>();
        changes.put("selectionV2", observed.isEmpty() ? "-" : RouteState.encode(observed));
        changes.put("excluded", RouteState.encode(excluded));
        for (String gate : new String[]{"trees", "fruit", "hardwood", "calquat", "otherPatches"}) { changes.put(gate, "true"); }
        preferences.update(changes);
    }

    private void queue()
    {
        panel.setRoutePreparing(true);
        if(worker!=null){worker.cancel(false);worker=null;}
        version++;
        dirty = true;
        panel.setStatus("Preparing your run...");
        delay.restart();
    }

    void prepareNow()
    {
        if(disposed)return;
        if (!dirty) { panel.reload(); return; }
        dirty = false;
        delay.stop();
        if (!config.autoOptimize()) { panel.setRoutePreparing(false);panel.setStatus(""); panel.reload(); return; }
        int requestedVersion = version;
        List<Patch> selected = RouteState.active(config);
        TravelCosts costs = new TravelCosts(selected, config);
        worker=new SwingWorker<List<Patch>, Void>()
        {
            protected List<Patch> doInBackground() { return RouteOptimizer.optimize(selected, costs.entry, costs.travel,this::isCancelled); }
            protected void done()
            {
                if (disposed || isCancelled() || requestedVersion != version) { return; }
                worker=null;
                panel.setRoutePreparing(false);
                try
                {
                    List<Patch> order = new ArrayList<>(get());
                    for (Patch patch : RouteState.ordered(config.order())) { if (!order.contains(patch)) { order.add(patch); } }
                    preferences.set("order", RouteState.encode(order));
                    panel.setStatus("");
                }
                catch (Exception error) { panel.setStatus("Couldn't reorder this run. Your patches are saved; change a travel setting to retry."); }
                panel.reload();
            }
        };
        worker.execute();
    }

    void dispose()
    {
        disposed = true;
        version++;
        delay.stop();
        if(worker!=null){worker.cancel(false);worker=null;}
    }
}

