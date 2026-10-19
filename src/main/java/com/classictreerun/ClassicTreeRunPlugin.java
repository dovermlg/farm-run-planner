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
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="Farm Run Planner", internalName="farm-run-planner", legacyDataDirectory="farm-run-planner", description="Local named farming presets and estimated travel routes", tags={"farming", "tree", "herb", "route"})
public class ClassicTreeRunPlugin extends Plugin
{
    @Inject private ClientToolbar toolbar;
    @Inject private Client client;
    @Inject private Notifier notifier;
    @Inject private ClientThread clientThread;
    @Inject private ItemManager itemManager;
    @Inject private BankSearch bankSearch;
    @Inject private OverlayManager overlayManager;
    private NextTeleportOverlay teleportOverlay;
    private final SupplyBankFilter supplyFilter=new SupplyBankFilter();
    private final java.util.concurrent.atomic.AtomicLong supplyGeneration=new java.util.concurrent.atomic.AtomicLong();
    private volatile GrowthMonitor growthMonitor;
    private final CropReference cropReference=new CropReference();
    private final GrowthSampler growthSampler=new GrowthSampler();
    private String sampledCharacter;
    private long sampledHash;
    private int sampledMode;
    private GrowthMonitor sampledMonitor;
    @Inject private ConfigManager configManager;
    private NavigationButton navigation;
    private volatile ClassicTreeRunPanel panel;
    private RunSettings settings;
    private PresetActions presetActions;
    private final java.util.Queue<String> pendingPresetActions = new java.util.concurrent.ConcurrentLinkedQueue<>();
    @Inject private ClassicTreeRunConfig config;
    private final java.util.Set<String> changed = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final java.util.concurrent.atomic.AtomicBoolean refreshQueued = new java.util.concurrent.atomic.AtomicBoolean();

    @Provides ClassicTreeRunConfig provideConfig(ConfigManager manager) { return manager.getConfig(ClassicTreeRunConfig.class); }

    @Override protected void startUp() throws Exception
    {
        onEdt(() -> {
            openProfile();
            teleportOverlay = new NextTeleportOverlay(client,config,
                () -> { ClassicTreeRunPanel current=panel;return current==null?TeleportDevice.NONE:current.nextTeleport(); },
                (id,quantity) -> itemManager.getItemOutline(id,quantity,NextTeleportOverlay.COLOR));
            overlayManager.add(teleportOverlay);
        });
    }

    private void openProfile()
    {
        clearSupplyFilter();
        pendingPresetActions.clear();
        if(growthMonitor!=null){growthMonitor.dispose();growthMonitor=null;}
        EditorPreset.clearLabels();
        presetActions = null;
        if (settings != null) { settings.dispose(); }
        if (navigation != null) { toolbar.removeNavigation(navigation); navigation = null; }
        settings = null;if(panel!=null)panel.dispose();panel = null;
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
            SupplyItemIcons supplyIcons=new SupplyItemIcons(itemManager);
            panel = new ClassicTreeRunPanel(config, combined, supplyIcons::add);
            settings = new RunSettings(config, combined, panel);
            new PresetLibrary(combined).seedStarters(config);
            presetActions = new PresetActions(config, combined, settings, panel,
                new PresetTransfer(getPluginDirectory().joinSegment("preset-backups"), combined));
            panel.setPrepareRun(() -> { if (settings != null) { settings.prepareNow(); } });
            panel.setPresetLoaded(() -> {
                clearSupplyFilter();
                if (presetActions != null) { presetActions.syncForRun(); }
                if (settings != null) { settings.changed("patch_"); settings.changed("autoOptimize"); settings.prepareNow(); }
            });
            panel.setSupplyActions(plan -> {
                long generation=supplyGeneration.get();
                clientThread.invoke(() -> {
                    if(generation!=supplyGeneration.get())return;
                    supplyFilter.start(plan);bankSearch.layoutBank();publishFilterState();
                });
            },this::clearSupplyFilter);
            ClassicTreeRunPanel inventoryPanel=panel;
            panel.setInventoryDataRequest(request->clientThread.invokeLater((java.util.function.BooleanSupplier)()->{
                if(inventoryPanel!=panel)return true;
                if(client.getGameState()==GameState.STARTING || client.getGameState()==GameState.UNKNOWN)return false;
                try
                {
                    InventoryItemData data=InventoryItemData.read(request.items,itemManager::getItemComposition);
                    SwingUtilities.invokeLater(()->{if(inventoryPanel==panel)inventoryPanel.setInventoryData(request,data);});
                }
                catch(RuntimeException failure)
                {
                    org.slf4j.LoggerFactory.getLogger(ClassicTreeRunPlugin.class).warn("Couldn't read farming item definitions",failure);
                    SwingUtilities.invokeLater(()->{if(inventoryPanel==panel)inventoryPanel.setInventoryDataError(request);});
                }
                return true;
            }));
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
        clearSupplyFilter();
        onEdt(() -> {
            if(teleportOverlay!=null){overlayManager.remove(teleportOverlay);teleportOverlay=null;}
            pendingPresetActions.clear(); presetActions = null; EditorPreset.clearLabels();
            if(growthMonitor!=null){growthMonitor.dispose();growthMonitor=null;}
            if (settings != null) { settings.dispose(); settings = null; }
            if (navigation != null) { toolbar.removeNavigation(navigation); navigation = null; }
            if(panel!=null)panel.dispose();panel = null;
        });
    }

    @Override public void resetConfiguration()
    {
        clearSupplyFilter();
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
            if("editorPreset".equals(key))clearSupplyFilter();
        }
        else
        {
            if (!(key.startsWith("patch_") || key.startsWith("crop") || key.startsWith("supply") || key.startsWith("load") || key.endsWith("Travel") || "runName".equals(key)
                || "autoOptimize".equals(key) || "hideCompleted".equals(key) || "showDetails".equals(key) || "highlightNextTeleport".equals(key)
                || "trackGrowth".equals(key) || "notifyWhenReady".equals(key) || "autoCompleteReplanted".equals(key))) { return; }
            changed.add(key.startsWith("patch_") ? "patch_" : key);
            if(key.startsWith("patch_") || key.startsWith("crop") || key.startsWith("supply") || key.startsWith("load"))clearSupplyFilter();
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
        if(event.getGameState()!=GameState.LOGGED_IN && event.getGameState()!=GameState.LOADING)
        {
            clearSupplyFilter();
            SwingUtilities.invokeLater(()->{if(panel!=null)panel.setSupplies(SupplySnapshot.UNKNOWN);});
        }
        growthSampler.reset();sampledCharacter=null;
        GrowthMonitor monitor=growthMonitor;
        boolean lobby=event.getGameState()==GameState.LOGIN_SCREEN;
        SwingUtilities.invokeLater(()->{if(monitor!=null && monitor==growthMonitor)monitor.allowNotifications(lobby);});
    }

    @Subscribe public void onGameTick(GameTick event)
    {
        if(client.getGameState()==GameState.LOGGED_IN)
        {
            SupplySnapshot snapshot=SupplySnapshot.read(itemManager,client.getItemContainer(InventoryID.INV),client.getItemContainer(InventoryID.WORN));
            ClassicTreeRunPanel target=panel;
            SwingUtilities.invokeLater(()->{if(target!=null && target==panel)target.setSupplies(snapshot);});
        }
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
        clearSupplyFilter();
        pendingPresetActions.clear();
        SwingUtilities.invokeLater(() -> { if (panel != null) { openProfile(); } });
    }

    private boolean bankOpen()
    {
        net.runelite.api.widgets.Widget bank=client.getWidget(InterfaceID.Bankmain.ITEMS);
        return bank!=null && !bank.isHidden() && client.getItemContainer(InventoryID.BANK)!=null
            && client.getVarbitValue(VarbitID.BANK_CURRENTTAB)!=15;
    }

    @Subscribe(priority=-1) public void onScriptCallbackEvent(ScriptCallbackEvent event)
    {
        if(!supplyFilter.active() || !bankOpen())return;
        int[] stack=client.getIntStack();int size=client.getIntStackSize();
        if("bankBuildTab".equals(event.getEventName()))
        {
            // The ordinary all-tabs layout bypasses bankSearchFilter without an active search.
            // This public callback uses the compact, filtered layout without editing chatbox input.
            supplyFilter.filter(event.getEventName(),true,stack,size,-1,false);
            return;
        }
        if(!"bankSearchFilter".equals(event.getEventName()))return;
        if(size<2 || size>stack.length)return;
        int itemId=stack[size-1];
        boolean placeholder=itemId<0 || itemManager.getItemComposition(itemId).getPlaceholderTemplateId()!=-1;
        supplyFilter.filter(event.getEventName(),true,stack,size,itemId<0?itemId:itemManager.canonicalize(itemId),placeholder);
    }

    @Subscribe public void onWidgetLoaded(WidgetLoaded event)
    {
        if(event.getGroupId()==InterfaceID.BANKMAIN && supplyFilter.active())
            clientThread.invokeLater(()->{if(bankOpen()){supplyFilter.bankVisible(true);bankSearch.layoutBank();}});
    }

    @Subscribe public void onWidgetClosed(WidgetClosed event)
    {
        if(event.getGroupId()==InterfaceID.BANKMAIN)
        {supplyFilter.bankVisible(true);supplyFilter.bankVisible(false);publishFilterState();}
    }

    private void clearSupplyFilter()
    {
        supplyGeneration.incrementAndGet();
        if(clientThread==null)return;
        clientThread.invoke(()->{supplyFilter.clear();bankSearch.layoutBank();publishFilterState();});
    }

    private void publishFilterState()
    {
        boolean active=supplyFilter.active();ClassicTreeRunPanel target=panel;
        SwingUtilities.invokeLater(()->{if(target!=null && target==panel)target.setBankFilterActive(active);});
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
