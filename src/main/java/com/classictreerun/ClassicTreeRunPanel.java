package com.classictreerun;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
/** Run checklist with a separate, explicitly saved backpack and gear editor in the sidebar. */
final class ClassicTreeRunPanel extends PluginPanel
{
    static final String PROGRESS_KEY = "completedV1";
    interface Preferences
    {
        String get(String key);
        void set(String key, String value);
        default void update(java.util.Map<String, String> changes) { changes.forEach(this::set); }
    }
    private final ClassicTreeRunConfig config;
    private final Preferences preferences;
    private final java.util.function.BiConsumer<Integer,JLabel> supplyIcons;
    private final InventoryPreparation inventories;
    private boolean routePreparing;
    private volatile TeleportDevice nextTeleport = TeleportDevice.NONE;
    TeleportDevice nextTeleport() { return nextTeleport; }
    private volatile RunStep runStep;
    private String startedRun;
    RunStep runStep() { return runStep; }
    private final RouteState state = new RouteState();
    private boolean confirmReset = true;
    private String status = "";
    private Runnable prepare = () -> { };
    private Runnable loaded = () -> { };
    private Runnable progressEdited = () -> { };
    private java.util.function.BiConsumer<SupplyPlan,Boolean> startSupplies=(plan,focus) -> { };
    private Runnable clearSupplies=() -> { };
    private SupplySnapshot supplies=SupplySnapshot.UNKNOWN;
    private SupplyChecklist supplyChecklist;
    private SupplyGrid supplyGrid;
    private EquipmentView equipmentView;
    private BackpackSetupView backpackView;
    private InventorySetupEditor setupEditor;
    private boolean editorBankWasPaused,editorSaved;
    private JPanel packingBody;
    private boolean disposed;
    private long equipmentCaptureGeneration;
    private java.util.function.Consumer<java.util.function.Consumer<SupplySnapshot>> readEquipment=ready->ready.accept(supplies);
    private boolean showSupplies;
    private boolean bankFilterActive;
    private boolean bankSetupAvailable=true;
    private JButton clearBankFilter;
    private JLabel bankFilterStatus;
    private String selectedPreset;
    private String publishedBankSignature;
    private boolean bankSetupPaused,focusBankSetup,manualBankSetup;
    private CropGrowth growth;
    private final HardwoodReadiness hardwoodReadiness=new HardwoodReadiness();
    private Runnable planningChanged=()->{ };
    private long growthTime=java.time.Instant.now().getEpochSecond();
    private JLabel readiness;
    private List<Patch> displayedRoute = java.util.Collections.emptyList();
    private final java.util.Map<Patch,JLabel> cropLabels = new java.util.EnumMap<>(Patch.class);
    void setGrowth(CropGrowth growth,long now)
    {if(this.growth!=growth)hardwoodReadiness.restart();this.growth=growth;refreshGrowth(now);}
    void setGrowth(CropGrowth growth) { setGrowth(growth,java.time.Instant.now().getEpochSecond()); }
    void setPlanningChanged(Runnable changed) { planningChanged=changed; }
    void lockRun() { hardwoodReadiness.lock(); }
    List<Patch> plannedRoute()
    {
        List<Patch> selected=RouteState.active(config);
        state.load(preferences.get(PROGRESS_KEY));
        if(hardwoodReadiness.update(config.runName(),selected,growth,config.trackGrowth(),growthTime,state))planningChanged.run();
        return hardwoodReadiness.route(selected);
    }

    /** Update existing labels so a countdown never closes an open preset dropdown. */
    void refreshGrowth(long now)
    {
        growthTime=now;
        List<Patch> route=plannedRoute();
        if(!route.equals(displayedRoute) && setupEditor==null){reload();return;}
        publishRunStep();
        if(!config.trackGrowth() || readiness==null)return;
        boolean changed=updateText(readiness,growth==null ? "Crop readiness: log in and visit your patches to start tracking." : growth.summary(RouteState.active(config),now));
        for(java.util.Map.Entry<Patch,JLabel> entry:cropLabels.entrySet())
        {changed|=updateText(entry.getValue(),growth==null ? "Growth unknown - visit this patch to track it." : growth.describe(entry.getKey(),now));}
        if(changed){resizeRows();revalidate();repaint();}
    }

    private void publishRunStep()
    {
        endFinishedRun(displayedRoute);
        runStep=startedRun==null || disposed || setupEditor!=null?null:RunStep.current(config,displayedRoute,state,inventories.get(),
            preferences.get(InventoryLoadPlanner.BANKS_KEY),growth,growthTime,routePreparing,!hardwoodReadiness.skipped().isEmpty());
    }
    private void stopRunOverlay() { startedRun=null;runStep=null;nextTeleport=TeleportDevice.NONE; }
    private boolean runActive() { return startedRun!=null && startedRun.equals(config.runName()); }
    private boolean highlightActiveStep() { return runActive() && !routePreparing; }
    private void endFinishedRun(List<Patch> route)
    {if(startedRun!=null && (!runActive() || state.next(route)==null))stopRunOverlay();}

    void setPresetLoaded(Runnable loaded) { this.loaded = loaded; }
    void setProgressEdited(Runnable action) { progressEdited = action; }
    private void presetLoaded() { stopRunOverlay();hardwoodReadiness.restart();discardInventoryEditor();manualBankSetup=false;invalidateEquipmentCapture();bankSetupPaused=false;focusBankSetup=true;publishedBankSignature=null;progressEdited.run();loaded.run(); }
    void setEquipmentReader(java.util.function.Consumer<java.util.function.Consumer<SupplySnapshot>> read)
    { readEquipment=read; }
    void invalidateEquipmentCapture() { equipmentCaptureGeneration++;if(setupEditor!=null)setupEditor.invalidateCapture(); }

    private void openInventoryEditor()
    {
        PresetLibrary library=new PresetLibrary(preferences);String name=config.runName();
        if(!library.contains(name)){setStatus("Save a farming preset in the cog first, then create its inventory setup here.");return;}
        editorBankWasPaused=bankSetupPaused;editorSaved=false;bankSetupPaused=true;
        clearSupplies.run();setBankFilterActive(false);
        setupEditor=new InventorySetupEditor(name,SavedPackingSetup.read(library.config(name)),readEquipment,
            (expected,draft)->{
                new PresetLibrary(preferences).saveSetup(name,expected,draft,name.equals(config.runName()));
                editorSaved=true;publishedBankSignature=null;focusBankSetup=true;
            },()->{
                boolean saved=editorSaved,paused=editorBankWasPaused;discardInventoryEditor();
                bankSetupPaused=saved?false:paused;focusBankSetup=saved;
                setStatus(saved?"Inventory setup saved locally. Your run and completion marks are kept.":"Back to your farming run. Saved setup kept.");
            },this::reload,supplyIcons);
        reload();
    }
    private void discardInventoryEditor()
    {if(setupEditor!=null){setupEditor.dispose();setupEditor=null;}}
    void captureEquipment()
    {
        long generation=++equipmentCaptureGeneration;
        String name=config.runName(),editor=config.editorPreset().name(),before=config.supplyEquipment();
        readEquipment.accept(snapshot->{
            if(disposed || generation!=equipmentCaptureGeneration)return;
            if(!name.equals(config.runName()) || !editor.equals(config.editorPreset().name()) || !before.equals(config.supplyEquipment()))
            {setStatus("The draft changed while equipment was being read. Capture again for the selected preset.");return;}
            if(!snapshot.known){setStatus("Log in and wear your farming gear before capturing equipment.");return;}
            if(snapshot.worn.isEmpty()){setStatus("No equipment is worn. Existing choices kept; use Clear saved equipment to remove them.");return;}
            try
            {
                preferences.set("supplyEquipment",new EquipmentSetup(snapshot.worn).encode());
                clearSupplies.run();setBankFilterActive(false);
                setStatus("Worn equipment captured for this draft. Save preset in the cog to keep it.");
            }
            catch(IllegalArgumentException | IllegalStateException error){setStatus(error.getMessage());}
        });
    }
    void clearEquipment()
    {
        invalidateEquipmentCapture();preferences.set("supplyEquipment","");
        clearSupplies.run();setBankFilterActive(false);setStatus("Equipment cleared from this draft. Save preset in the cog to keep it.");
    }
    void setSupplyActions(java.util.function.BiConsumer<SupplyPlan,Boolean> start,Runnable clear)
    { startSupplies=start;clearSupplies=clear;publishedBankSignature=null; }
    BankSetupRequest bankSetupRequest(SupplyPlan plan,boolean focus)
    {return BankSetupRequest.create(config,plan,inventories.data(),focus);}
    void setInventoryDataRequest(java.util.function.Consumer<InventoryPreparation.ItemRequest> request)
    { inventories.setRequest(request);reload(); }
    void setInventoryData(InventoryPreparation.ItemRequest request,InventoryItemData data) { inventories.accept(request,data); }
    void setInventoryDataError(InventoryPreparation.ItemRequest request) { inventories.failed(request); }
    void setRoutePreparing(boolean preparing) { routePreparing=preparing;if(preparing)nextTeleport=TeleportDevice.NONE; }
    void dispose() { disposed=true;stopRunOverlay();discardInventoryEditor();invalidateEquipmentCapture();nextTeleport=TeleportDevice.NONE;inventories.dispose(); }
    void setSupplies(SupplySnapshot snapshot)
    {
        if(supplies.known==snapshot.known && supplies.plain.equals(snapshot.plain) && supplies.notes.equals(snapshot.notes) && supplies.worn.equals(snapshot.worn) && supplies.wornAliases.equals(snapshot.wornAliases)
            && supplies.backpack.captured==snapshot.backpack.captured && supplies.backpack.slots.equals(snapshot.backpack.slots))return;
        if(!snapshot.known)invalidateEquipmentCapture();
        supplies=snapshot;
        if(supplyChecklist!=null)supplyChecklist.refresh(snapshot);
        if(supplyGrid!=null)supplyGrid.refresh(snapshot);
        if(equipmentView!=null)equipmentView.refresh(snapshot);
        if(backpackView!=null)backpackView.refresh(snapshot);
        if(setupEditor!=null)setupEditor.refresh(snapshot);
        resizeRows();revalidate();
    }
    void setBankFilterActive(boolean active)
    {
        active=active && bankSetupAvailable;
        if(bankFilterActive && !active){publishedBankSignature=null;if(bankSetupAvailable)manualBankSetup=false;}
        bankFilterActive=active;if(clearBankFilter!=null)clearBankFilter.setEnabled(active);
        if(bankFilterStatus!=null)updateText(bankFilterStatus,!bankSetupAvailable ? "Enable RuneLite's Bank Tags plugin to use the setup bank display. Your sidebar run still works."
            : active ? "Setup linked to the bank. Withdraw and equip items manually." : "Bank setup closed. Select a saved run or click Show setup in bank.");
    }
    boolean setBankSetupAvailable(boolean available)
    {boolean changed=bankSetupAvailable!=available;bankSetupAvailable=available;return changed;}
    void presetSaved(String name) { manualBankSetup=false;selectedPreset=name;bankSetupPaused=false;focusBankSetup=true;setStatus("Saved locally: "+name); }
    void presetEditing(String name)
    {
        stopRunOverlay();
        discardInventoryEditor();
        invalidateEquipmentCapture();
        manualBankSetup=false;bankSetupPaused=true;publishedBankSignature=null;clearSupplies.run();setBankFilterActive(false);
        selectedPreset = name;
        setStatus(name == null ? "Empty slot ready. Reopen the cog, name it, edit and Save preset."
            : "Editing: " + name + ". Reopen the cog to refresh its controls before editing.");
    }
    ClassicTreeRunPanel(ClassicTreeRunConfig config, Preferences preferences)
    { this(config,preferences,(item,label)->{ }); }
    ClassicTreeRunPanel(ClassicTreeRunConfig config, Preferences preferences,java.util.function.BiConsumer<Integer,JLabel> supplyIcons)
    {
        this.config = config;
        this.preferences = preferences;
        this.supplyIcons = supplyIcons;
        this.inventories = new InventoryPreparation(config,this::reload);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        reload();
    }
    void setPrepareRun(Runnable prepare) { this.prepare = prepare; }
    @Override public void onActivate() { prepare.run(); reload(); }
    void setConfirmReset(boolean confirm) { confirmReset = confirm; }
    void setStatus(String value) { status = value; reload(); }
    void reload()
    {
        state.load(preferences.get(PROGRESS_KEY));
        List<Patch> route = plannedRoute();
        endFinishedRun(route);
        inventories.update(route,setupEditor==null?java.util.Set.of():setupEditor.items());
        // A metadata response may synchronously refresh the panel; replace that intermediate view.
        removeAll();
        readiness=null;cropLabels.clear();
        if(setupEditor!=null)
        {
            runStep=null;nextTeleport=TeleportDevice.NONE;supplyChecklist=null;supplyGrid=null;equipmentView=null;backpackView=null;
            setupEditor.setData(inventories.data());setupEditor.refresh(supplies);add(setupEditor);
            resizeRows();revalidate();repaint();return;
        }
        InventoryLoadPlanner.Plan inventoryPlan=inventories.get();
        int currentInventory=inventoryPlan==null?-1:inventoryPlan.current(state);
        boolean loadsReady=!config.loadPlanning() || inventoryPlan!=null;
        nextTeleport = runActive() && loadsReady && !routePreparing && config.highlightNextTeleport()
            ? NextTeleport.resolve(config,route,state,inventoryPlan,preferences.get(InventoryLoadPlanner.BANKS_KEY))
            : TeleportDevice.NONE;
        boolean bankNeeded=config.loadPlanning() && currentInventory>0
            && !inventoryPlan.bankConfirmed(preferences.get(InventoryLoadPlanner.BANKS_KEY),currentInventory);
        List<Patch> remaining=new java.util.ArrayList<>();
        List<Patch> packingPatches=config.loadPlanning() && inventoryPlan!=null
            ? currentInventory<0?java.util.Collections.emptyList():inventoryPlan.loads.get(currentInventory).patches : route;
        for(Patch patch:packingPatches)if(!state.isDone(patch))remaining.add(patch);
        Patch bankFrom=bankNeeded?inventoryPlan.loads.get(currentInventory-1).patches
            .get(inventoryPlan.loads.get(currentInventory-1).patches.size()-1):null;
        SupplyPlan packing=SupplyPlan.forRoute(config,packingPatches,patch->!state.isDone(patch),currentInventory>0,
            inventoryPlan!=null && currentInventory>=0 && currentInventory<inventoryPlan.loads.size()-1,bankFrom);
        if(loadsReady && config.loadPlanning())packing=packing.forInventory(config.loadNotedProtection(),inventories.data());
        final SupplyPlan packingList=packing;
        SavedPackingSetup savedSetup=SavedPackingSetup.read(config);
        java.util.Set<Integer> bankItems=savedSetup.backpack.captured?savedSetup.items():packing.items;
        displayedRoute=route;
        add(text("Farm Run Planner"));
        add(text(config.runName().trim()));
        add(text("Create farming presets in the cog. Capture and save backpack and gear with Inventory setup below."));
        add(new PresetControls(config, preferences, this::presetLoaded, selectedPreset, name -> selectedPreset = name, this::setStatus));
        JButton editSetup=new JButton("Inventory setup");editSetup.setName("editInventorySetup");
        editSetup.setToolTipText("Open this saved farming preset's backpack and worn-gear editor inside the plugin tab.");
        editSetup.addActionListener(event->openInventoryEditor());add(editSetup);
        boolean bankDataReady=savedSetup.backpack.captured || config.supplyBankView()!=ClassicTreeRunConfig.SetupBankView.LAYOUT || inventories.data().covers(packing.items);
        JButton start=new JButton(runActive()?"Stop run":"Start run");start.setName("startRun");
        start.setEnabled(runActive() || !route.isEmpty() && loadsReady && !routePreparing && bankDataReady);
        start.setToolTipText(runActive()?"Stop directions and highlighting; keep completion marks.":"Start a fresh farming run.");
        start.addActionListener(event -> {
            if(runActive())
            {
                stopRunOverlay();bankSetupPaused=true;focusBankSetup=false;manualBankSetup=false;publishedBankSignature=null;
                clearSupplies.run();setBankFilterActive(false);
                setStatus("Run stopped. Patch and bank completion marks kept.");return;
            }
            if(state.count(route)>0 && confirmReset && JOptionPane.showConfirmDialog(this,
                "Start a fresh run and clear completion marks?","Start farming run",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            try
            {
                preferences.update(java.util.Map.of(PROGRESS_KEY,"",InventoryLoadPlanner.BANKS_KEY,""));
                progressEdited.run();
                clearSupplies.run();setBankFilterActive(false);
                hardwoodReadiness.restart();plannedRoute();lockRun();
                startedRun=config.runName();
                bankSetupPaused=false;focusBankSetup=true;manualBankSetup=true;
                setStatus("Run started. Preparing the first inventory's setup; open your bank and withdraw supplies manually.");
            }
            catch(IllegalStateException error){setStatus(error.getMessage());}
        });
        add(start);
        JButton pack=new JButton("Show setup in bank");pack.setName("packInventory");
        pack.setEnabled(bankSetupAvailable && loadsReady && !routePreparing && !remaining.isEmpty() && !bankItems.isEmpty() && bankDataReady && config.supplyBankView()!=ClassicTreeRunConfig.SetupBankView.OFF);
        pack.addActionListener(event->{
            bankSetupPaused=false;focusBankSetup=false;manualBankSetup=true;publishBankSetup(packingList,true);
            setBankFilterActive(true);setStatus("Setup ready for this inventory. Open your bank and withdraw items manually; patch progress is kept.");
        });
        clearBankFilter=new JButton("Close setup bank view");clearBankFilter.setName("clearBankFilter");
        clearBankFilter.setEnabled(bankFilterActive);
        clearBankFilter.addActionListener(event -> {bankSetupPaused=true;publishedBankSignature=null;clearSupplies.run();setBankFilterActive(false);});
        bankFilterStatus=text("");bankFilterStatus.setName("bankSetupStatus");setBankFilterActive(bankFilterActive);
        if(config.loadPlanning())
        {
            if(inventoryPlan==null)add(text(inventories.error()==null?"Preparing inventory loads from RuneLite item data...":inventories.error()));
            else if(currentInventory>=0)
            {
                int slots=InventoryLoadPlanner.slots(packing,inventories.data());
                JLabel loadInfo=text("Inventory "+(currentInventory+1)+" of "+inventoryPlan.loads.size()+" - "+slots+" / "+inventoryPlan.capacity+" slots ("+InventoryLoadPlanner.reserved(config)+" reserved)");
                loadInfo.setName("inventoryLoad");
                loadInfo.setToolTipText(html("Starting-supply estimate, including configured teleport supplies. Tools and teleport equipment count as backpack slots even if worn. Reserved slots cover other gear and harvested crops; note or bank produce manually."));
                add(loadInfo);
                JLabel bankInfo=text("Bank: "+config.loadBank());bankInfo.setToolTipText(html(config.loadBank().instructions(config)));add(bankInfo);
                if(slots>inventoryPlan.capacity)add(text("This patch's supplies exceed the available slots. Adjust reserved space, noted payments or tools before leaving the bank."));
            }
        }
        boolean grid=config.packingView()==ClassicTreeRunConfig.PackingView.GRID;
        if(!config.loadPlanning() && inventories.error()!=null)add(text(inventories.error()));
        JComboBox<ClassicTreeRunConfig.PackingView> view=new JComboBox<>(ClassicTreeRunConfig.PackingView.values());
        view.setName("packingView");view.setSelectedItem(config.packingView());
        view.setToolTipText(savedSetup.backpack.captured?"Show saved backpack slots as a Grid or List. Captured slots are checked by position; calculated requirements remain underneath."
            :"Show the current inventory's supplies as a Grid or compact List. Items may be anywhere in your backpack.");
        view.addActionListener(event->{
            try{preferences.set("packingView",((ClassicTreeRunConfig.PackingView)view.getSelectedItem()).name());reload();}
            catch(IllegalStateException error){setStatus(error.getMessage());}
        });
        supplyChecklist=loadsReady?new SupplyChecklist(packing,supplies,supplyIcons):null;
        supplyGrid=loadsReady && !savedSetup.backpack.captured?new SupplyGrid(packing,inventories.data(),config.loadPlanning()?InventoryLoadPlanner.reserved(config):0,supplies,supplyIcons):null;
        equipmentView=loadsReady || savedSetup.backpack.captured?new EquipmentView(savedSetup.equipment,inventories.data(),grid,supplies,supplyIcons):null;
        backpackView=savedSetup.backpack.captured?new BackpackSetupView(savedSetup.backpack,inventories.data(),grid,supplies,supplyIcons):null;
        packingBody=new JPanel();packingBody.setName("packingBody");packingBody.setOpaque(false);packingBody.setLayout(new BoxLayout(packingBody,BoxLayout.Y_AXIS));
        packingBody.add(view);
        if(backpackView!=null)
        {
            packingBody.add(backpackView);
            if(equipmentView!=null)packingBody.add(equipmentView);
            packingBody.add(text("Your captured setup stays fixed. Compare the current run requirements below; capture again to change the bank setup."));
        }
        if(supplyChecklist!=null){supplyChecklist.setVisible(savedSetup.backpack.captured || !grid);packingBody.add(supplyChecklist);}
        if(supplyGrid!=null){supplyGrid.setVisible(grid);packingBody.add(supplyGrid);}
        if(equipmentView!=null && backpackView==null)packingBody.add(equipmentView);
        JPanel packingHeader=new JPanel(new BorderLayout());packingHeader.setOpaque(false);
        packingHeader.add(text("Packing checklist"),BorderLayout.CENTER);
        javax.swing.JToggleButton showPacking=new javax.swing.JToggleButton(new PackingEyeIcon(false),showSupplies);
        showPacking.setSelectedIcon(new PackingEyeIcon(true));showPacking.setName("showPackingChecklist");
        showPacking.setMargin(new java.awt.Insets(3,5,3,5));
        showPacking.setToolTipText(showSupplies?"Hide packing checklist":"Show packing checklist");
        showPacking.getAccessibleContext().setAccessibleName(showPacking.getToolTipText());
        showPacking.addActionListener(event->{
            showSupplies=showPacking.isSelected();packingBody.setVisible(showSupplies);
            showPacking.setToolTipText(showSupplies?"Hide packing checklist":"Show packing checklist");
            showPacking.getAccessibleContext().setAccessibleName(showPacking.getToolTipText());
            resizeRows();revalidate();repaint();
        });
        packingHeader.add(showPacking,BorderLayout.EAST);add(packingHeader);
        packingBody.setVisible(showSupplies);add(packingBody);
        add(pack);add(clearBankFilter);add(bankFilterStatus);
        JProgressBar progress = new JProgressBar(0, Math.max(1, route.size()));
        int completed = state.count(route);
        progress.setValue(completed);
        if (!route.isEmpty() && completed == route.size())
        {
            // Render the completion color consistently across client themes.
            progress.setUI(new BasicProgressBarUI()
            {
                @Override protected Color getSelectionForeground() { return Color.BLACK; }
            });
            progress.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        }
        progress.setStringPainted(true);
        progress.setString(completed + " / " + route.size() + " patches complete");
        add(progress);
        if(config.trackGrowth()) { readiness=text("");add(readiness); }
        if(!hardwoodReadiness.skipped().isEmpty())
        {
            JLabel skipped=text(hardwoodReadiness.skipped().size()+" growing hardwood patches skipped for this run. Kept in your preset; reset or start a fresh run to check again.");
            skipped.setName("skippedHardwoods");add(skipped);
            for(Patch patch:RouteState.active(config))if(hardwoodReadiness.skipped().contains(patch))
            {
                add(text("Skipped: "+patch.getLabel()));
                JLabel crop=text("");cropLabels.put(patch,crop);add(crop);
            }
        }
        if (!status.isEmpty()) { add(text(status)); }
        Patch next = state.next(route);
        add(text(bankNeeded ? "NEXT: Bank at "+config.loadBank()+" for inventory "+(currentInventory+1)+"."
            : route.isEmpty() ? hardwoodReadiness.skipped().isEmpty()?"No patches selected. Open the settings cog to choose them."
                :"Selected hardwoods are still growing. No stops to plan right now."
            : next == null ? "Run complete. Reset when you're ready to start again."
            : "NEXT: " + next.getLabel()));
        if(nextTeleport!=TeleportDevice.NONE)
        {
            JLabel teleport=text("Teleport item: "+nextTeleport.label);
            teleport.setName("nextTeleport");
            teleport.setToolTipText(html("Matching carried items have a green outline. Use the route directions for the destination. Check charges, daily limits and unlocks yourself; this does not teleport or open menus."));
            add(teleport);
        }
        JButton reset = new JButton("Reset run");
        reset.setName("resetRun");
        reset.addActionListener(event ->
        {
            if (!confirmReset || JOptionPane.showConfirmDialog(this,
                "Clear completion marks for all patches?", "Reset farming run", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
            {
                state.reset();
                invalidateEquipmentCapture();
                clearSupplies.run();
                try { preferences.update(java.util.Map.of(PROGRESS_KEY,state.save(),InventoryLoadPlanner.BANKS_KEY,""));stopRunOverlay();hardwoodReadiness.restart();progressEdited.run(); reload(); }
                catch (IllegalStateException error) { setStatus(error.getMessage()); }
            }
        });
        add(reset);
        for (int i = 0; i < route.size(); i++)
        {
            Patch patch = route.get(i);
            int starts=inventoryPlan==null?-1:inventoryPlan.startsAt(patch);
            if(starts>0 && !(config.hideCompleted() && inventoryPlan.loads.get(starts).patches.stream().allMatch(state::isDone)))
                add(bankCard(inventoryPlan,starts,currentInventory,bankNeeded));
            if (config.hideCompleted() && state.isDone(patch)) { continue; }
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(new Color(48, 48, 48));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(highlightActiveStep() && loadsReady && patch == next && !bankNeeded ? new Color(103, 188, 116) : Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 7, 5)));
            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(text((i + 1) + ". " + patch.getLabel()));
            JCheckBox complete = new JCheckBox("Completed", state.isDone(patch));
            complete.setName(patch.name());
            complete.setOpaque(false);
            complete.setForeground(Color.WHITE);
            complete.addActionListener(event ->
            {
                state.setDone(patch, complete.isSelected());
                try
                {
                    java.util.Map<String,String> changes=new java.util.LinkedHashMap<>();
                    changes.put(PROGRESS_KEY,state.save());
                    if(!complete.isSelected() && inventoryPlan!=null)
                        changes.put(InventoryLoadPlanner.BANKS_KEY,inventoryPlan.reopenPatch(preferences.get(InventoryLoadPlanner.BANKS_KEY),patch));
                    preferences.update(changes);lockRun();progressEdited.run();reload();
                }
                catch (IllegalStateException error) { setStatus(error.getMessage()); }
            });
            content.add(complete);
            Patch previous = i > 0 && starts<=0 ? route.get(i - 1) : null;
            String walking = starts>0 ? config.loadBank().departureInstructions(patch,config) : TravelCosts.walkingInstruction(previous, patch, config);
            content.add(text(walking == null ? TravelGuide.instructions(patch, config) : walking));
            if(config.trackGrowth()) { JLabel cropLabel=text("");cropLabels.put(patch,cropLabel);content.add(cropLabel); }
            if (config.showDetails())
            {
                content.add(text(patch.getDirections() + ". Gardener: " + patch.getGardener() + "."));
                content.add(text("Access: " + patch.getRequirements() + "."));
            }
            for (java.awt.Component child : content.getComponents()) { ((JComponent) child).setAlignmentX(LEFT_ALIGNMENT); }
            card.add(content, BorderLayout.CENTER);
            add(card);
        }
        refreshGrowth(growthTime);
        syncSavedBankSetup(packingList,loadsReady && bankDataReady && !routePreparing && !remaining.isEmpty());
        resizeRows();
        revalidate();
        repaint();
    }

    private void syncSavedBankSetup(SupplyPlan packing,boolean ready)
    {
        if(!bankSetupAvailable)return;
        PresetLibrary library=new PresetLibrary(preferences);
        boolean saved=library.contains(config.runName());
        if(saved)
        {
            ClassicTreeRunConfig snapshot=library.config(config.runName());
            saved=CropChoices.settings(config).equals(CropChoices.settings(snapshot))
                && TravelCosts.settings(config).equals(TravelCosts.settings(snapshot))
                && InventoryLoadPlanner.settings(config).equals(InventoryLoadPlanner.settings(snapshot))
                && new java.util.HashSet<>(RouteState.active(config)).equals(new java.util.HashSet<>(RouteState.active(snapshot)));
        }
        if(!ready || !(saved || manualBankSetup) || bankSetupPaused || config.supplyBankView()==ClassicTreeRunConfig.SetupBankView.OFF)
        {
            if(publishedBankSignature!=null){publishedBankSignature=null;clearSupplies.run();setBankFilterActive(false);}
            return;
        }
        BankSetupRequest request=bankSetupRequest(packing,focusBankSetup);
        if(request.items.isEmpty())
        {if(publishedBankSignature!=null){publishedBankSignature=null;clearSupplies.run();setBankFilterActive(false);}return;}
        if(!request.signature.equals(publishedBankSignature) || focusBankSetup)
        {
            boolean focus=focusBankSetup;focusBankSetup=false;publishBankSetup(packing,focus);
        }
    }

    private void publishBankSetup(SupplyPlan packing,boolean focus)
    {
        BankSetupRequest request=bankSetupRequest(packing,focus);
        if(request.mode==ClassicTreeRunConfig.SetupBankView.OFF || request.items.isEmpty())
        {publishedBankSignature=null;manualBankSetup=false;clearSupplies.run();setBankFilterActive(false);return;}
        startSupplies.accept(packing,focus);publishedBankSignature=request.signature;setBankFilterActive(true);
    }

    private JPanel bankCard(InventoryLoadPlanner.Plan plan,int index,int current,boolean waiting)
    {
        JPanel card=new JPanel();card.setName("bankStop_"+index);card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        boolean confirmed=plan.bankConfirmed(preferences.get(InventoryLoadPlanner.BANKS_KEY),index);
        card.setBackground(new Color(38,45,49));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(highlightActiveStep() && index==current && waiting
            ?new Color(103,188,116):Color.DARK_GRAY),BorderFactory.createEmptyBorder(5,5,7,5)));
        card.add(text("BANK STOP - "+config.loadBank()));
        card.add(text("Prepare inventory "+(index+1)+" of "+plan.loads.size()+". Deposit harvested produce and unneeded supplies; keep your tools and travel items."));
        List<Patch> before=plan.loads.get(index-1).patches;
        card.add(text(config.loadBank().instructions(before.get(before.size()-1),config)));
        JButton packed=new JButton(confirmed?"Bank stop complete":"Banked - continue");packed.setName("bankReady_"+index);
        packed.setEnabled(index==current && !confirmed);
        packed.addActionListener(event->{
            try
            {
                preferences.set(InventoryLoadPlanner.BANKS_KEY,plan.confirmBank(preferences.get(InventoryLoadPlanner.BANKS_KEY),index));
                setStatus("Bank stop marked complete. Continue to inventory "+(index+1)+"'s first patch.");
            }
            catch(IllegalStateException error){setStatus(error.getMessage());}
        });
        card.add(packed);return card;
    }

    private void resizeRows()
    {
        for (java.awt.Component child : getComponents())
        {
            JComponent component = (JComponent) child;
            component.setAlignmentX(LEFT_ALIGNMENT);
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        }
    }

    private static boolean updateText(JLabel label,String value)
    {
        String html=html(value);
        if(html.equals(label.getText()))return false;
        label.setText(html);return true;
    }

    private static String html(String value)
    {
        return "<html><body style='width:145px'>"+value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")+"</body></html>";
    }

    private static JLabel text(String value)
    {
        JLabel label = new JLabel(html(value));
        label.setForeground(new Color(225, 225, 225));
        label.setBorder(BorderFactory.createEmptyBorder(3, 0, 5, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }
}
