package com.classictreerun;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.client.util.Filepath;
import net.runelite.client.Notifier;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldType;
import net.runelite.api.WidgetNode;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetModalMode;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(name="Farm Run Planner", internalName="farm-run-planner", legacyDataDirectory="farm-run-planner", description="Local named farming presets and estimated travel routes", tags={"farming", "tree", "herb", "route"}, enabledByDefault=false)
public class ClassicTreeRunPlugin extends Plugin
{
    @Inject private ClientToolbar toolbar;
    @Inject private Client client;
    @Inject private Notifier notifier;
    private volatile GrowthMonitor growthMonitor;
    private final CropReference cropReference=new CropReference();
    private final GrowthSampler growthSampler=new GrowthSampler();
    private String sampledCharacter;
    private long sampledHash;
    private int sampledMode;
    private GrowthMonitor sampledMonitor;
    @Inject private ConfigManager configManager;
    private NavigationButton navigation;
    private ClassicTreeRunPanel panel;
    private RunSettings settings;
    private PresetActions presetActions;
    private final java.util.Queue<String> pendingPresetActions = new java.util.concurrent.ConcurrentLinkedQueue<>();
    @Inject private ClassicTreeRunConfig config;
    private final java.util.Set<String> changed = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final java.util.concurrent.atomic.AtomicBoolean refreshQueued = new java.util.concurrent.atomic.AtomicBoolean();

    @Provides ClassicTreeRunConfig provideConfig(ConfigManager manager) { return manager.getConfig(ClassicTreeRunConfig.class); }

    @Override protected void startUp() throws Exception { onEdt(this::openProfile); }

    private void openProfile()
    {
        pendingPresetActions.clear();
        if(growthMonitor!=null){growthMonitor.dispose();growthMonitor=null;}
        EditorPreset.clearLabels();
        presetActions = null;
        if (settings != null) { settings.dispose(); }
        if (navigation != null) { toolbar.removeNavigation(navigation); navigation = null; }
        settings = null; panel = null;
        try
        {
            String profile = configManager.getProfile() == null ? "default" : Long.toString(configManager.getProfile().getId());
            Filepath path = getPluginDirectory().joinSegment("profile-" + profile + ".properties");
            LocalPreferences preferences = new LocalPreferences(path);
            if (!preferences.exists()) { importLegacy(preferences); }
            ClassicTreeRunPanel.Preferences nativeValues = new ClassicTreeRunPanel.Preferences()
            {
                public String get(String key) { return configManager.getConfiguration(ClassicTreeRunConfig.GROUP, key); }
                public void set(String key, String value)
                {
                    if (value == null) { configManager.unsetConfiguration(ClassicTreeRunConfig.GROUP, key); }
                    else { configManager.setConfiguration(ClassicTreeRunConfig.GROUP, key, value); }
                }
            };
            PlannerPreferences combined = new PlannerPreferences(nativeValues, preferences);
            combined.importLocalWorkingRun();
            combined.initializeCogActions();
            panel = new ClassicTreeRunPanel(config, combined);
            settings = new RunSettings(config, combined, panel);
            new PresetLibrary(combined).seedStarters(config);
            presetActions = new PresetActions(config, combined, settings, panel);
            panel.setPrepareRun(() -> { if (settings != null) { settings.prepareNow(); } });
            panel.setPresetLoaded(() -> {
                if (presetActions != null) { presetActions.syncForRun(); }
                if (settings != null) { settings.changed("patch_"); settings.changed("autoOptimize"); settings.prepareNow(); }
            });
            growthMonitor=new GrowthMonitor(config,combined,panel,message->notifier.notify(message),cropReference,
                ()->java.time.Instant.now().getEpochSecond(),true);
            panel.reload();
            navigation = NavigationButton.builder().tooltip("Farm Run Planner").icon(icon()).priority(6).panel(panel).build();
            toolbar.addNavigation(navigation);
        }
        catch (java.io.IOException | IllegalStateException error)
        {
            throw new IllegalStateException("Farm Run Planner couldn't open local settings. Existing data was preserved.", error);
        }
    }

    private void importLegacy(LocalPreferences local)
    {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : ConfigValues.KEYS)
        {
            String value = configManager.getConfiguration(ClassicTreeRunConfig.GROUP, key);
            if (value != null) { values.put(key, value); }
        }
        for (String key : new String[]{SavedRuns.KEY, ClassicTreeRunPanel.PROGRESS_KEY, RunSettings.MIGRATED})
        {
            String value = configManager.getConfiguration(ClassicTreeRunConfig.GROUP, key);
            if (value != null) { values.put(key, value); }
        }
        local.update(values);
        LocalRunConfig config = new LocalRunConfig(local);
        Map<String, String> original = new LinkedHashMap<>(values);
        PresetLibrary library = new PresetLibrary(local);
        Map<String, java.util.Set<Patch>> old = SavedRuns.decode(values.get(SavedRuns.KEY));
        for (RunPreset preset : RunPreset.values())
        {
            String value = configManager.getConfiguration(ClassicTreeRunConfig.GROUP, preset.key());
            if (preset != RunPreset.CUSTOM && value != null) { old.putIfAbsent(preset.toString(), RouteState.parse(value)); }
        }
        for (Map.Entry<String, java.util.Set<Patch>> entry : old.entrySet())
        {
            try { PresetLibrary.validName(entry.getKey()); } catch (IllegalArgumentException invalid) { continue; }
            Map<String, String> selection = new LinkedHashMap<>();
            for (Patch patch : Patch.values()) { selection.put("patch_" + patch.name(), Boolean.toString(entry.getValue().contains(patch))); }
            selection.put("selectionV2", entry.getValue().isEmpty() ? "-" : RouteState.encode(entry.getValue()));
            selection.put("excluded", ""); selection.put("order", "");
            for (String gate : new String[]{"trees", "fruit", "hardwood", "calquat", "otherPatches"}) { selection.put(gate, "true"); }
            local.update(selection); library.save(entry.getKey(), config);
        }
        // Restore the working run without deleting the imported preset library.
        for (String key : ConfigValues.KEYS)
        {
            if (!original.containsKey(key)) { original.put(key, null); }
        }
        local.update(original);
    }

    @Override protected void shutDown() throws Exception
    {
        onEdt(() -> {
            pendingPresetActions.clear(); presetActions = null; EditorPreset.clearLabels();
            if(growthMonitor!=null){growthMonitor.dispose();growthMonitor=null;}
            if (settings != null) { settings.dispose(); settings = null; }
            if (navigation != null) { toolbar.removeNavigation(navigation); navigation = null; }
            panel = null;
        });
    }

    @Override public void resetConfiguration()
    {
        // RuneLite invokes this after resetting native fields. Reset must not execute a save/delete toggle.
        pendingPresetActions.clear();
    }

    @Subscribe public void onConfigChanged(ConfigChanged event)
    {
        if (!ClassicTreeRunConfig.GROUP.equals(event.getGroup())) { return; }
        String key = event.getKey();
        if (PresetActions.isCommand(key))
        {
            if (settings == null || presetActions == null || presetActions.isChangingSelection() || !PresetActions.shouldQueue(key, event.getOldValue(), event.getNewValue(), SwingUtilities.isEventDispatchThread())) { return; }
            pendingPresetActions.add(key);
        }
        else
        {
            if (!(key.startsWith("patch_") || key.endsWith("Travel") || "runName".equals(key)
                || "autoOptimize".equals(key) || "hideCompleted".equals(key) || "showDetails".equals(key)
                || "trackGrowth".equals(key) || "notifyWhenReady".equals(key) || "autoCompleteReplanted".equals(key))) { return; }
            changed.add(key.startsWith("patch_") ? "patch_" : key);
        }
        if (!refreshQueued.compareAndSet(false, true)) { return; }
        SwingUtilities.invokeLater(() -> {
            refreshQueued.set(false);
            java.util.Set<String> keys = new java.util.LinkedHashSet<>(changed); changed.removeAll(keys);
            if (settings != null) { for (String item : keys) { settings.changed(item); } }
            String action;
            while ((action = pendingPresetActions.poll()) != null)
            {
                if (presetActions != null) { presetActions.apply(action); }
            }
        });
    }

    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        growthSampler.reset();sampledCharacter=null;
        GrowthMonitor monitor=growthMonitor;
        boolean lobby=event.getGameState()==GameState.LOGIN_SCREEN;
        SwingUtilities.invokeLater(()->{if(monitor!=null && monitor==growthMonitor)monitor.allowNotifications(lobby);});
    }

    @Subscribe public void onGameTick(GameTick event)
    {
        GrowthMonitor monitor=growthMonitor;
        if(monitor==null || !config.trackGrowth() || client.getGameState()!=GameState.LOGGED_IN || client.getLocalPlayer()==null)
        {growthSampler.reset();return;}
        if(monitor!=sampledMonitor){growthSampler.reset();sampledMonitor=monitor;sampledCharacter=null;}
        long hash=client.getAccountHash();if(hash==-1 || hash==0){growthSampler.reset();return;}
        boolean deadman=client.getWorldType().contains(WorldType.DEADMAN);
        boolean seasonal=client.getWorldType().contains(WorldType.SEASONAL) && !deadman;
        // Modal dialogs can retain another region's farming payload: discard the sampling baseline.
        for(WidgetNode widget:client.getComponentTable())
        {if(widget.getModalMode()!=WidgetModalMode.NON_MODAL){growthSampler.reset();return;}}
        int mode=deadman?2:seasonal?1:0;
        boolean newContext=sampledCharacter==null || sampledHash!=hash || sampledMode!=mode;
        if(newContext)
        {growthSampler.reset();sampledHash=hash;sampledMode=mode;sampledCharacter=GrowthMonitor.characterKey(hash,seasonal,deadman);}
        String character=sampledCharacter;
        WorldPoint location=WorldPoint.fromLocalInstance(client,client.getLocalPlayer().getLocalLocation());
        java.util.List<GrowthSampler.Sample> samples=growthSampler.read(cropReference,location.getX(),location.getY(),location.getPlane(),client::getVarbitValue);
        if(samples.isEmpty() && !newContext)return;
        SwingUtilities.invokeLater(()->{if(monitor==growthMonitor)monitor.accept(character,samples,seasonal);});
    }

    @Subscribe public void onProfileChanged(ProfileChanged event)
    {
        pendingPresetActions.clear();
        SwingUtilities.invokeLater(() -> { if (panel != null) { openProfile(); } });
    }

    private static void onEdt(Runnable action) throws Exception
    {
        if (SwingUtilities.isEventDispatchThread()) { action.run(); }
        else { SwingUtilities.invokeAndWait(action); }
    }

    private static BufferedImage icon()
    {
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try
        {
            g.setColor(new Color(150, 103, 63));
            g.fillRect(10, 13, 4, 10);
            g.setColor(new Color(103, 188, 116));
            g.fillOval(3, 2, 18, 16);
        }
        finally { g.dispose(); }
        return image;
    }
}
