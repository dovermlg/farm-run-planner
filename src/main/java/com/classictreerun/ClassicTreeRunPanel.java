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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
/** Ready run selector and checklist. Creation and saving use the native settings cog. */
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
    private String displayedLoad;
    private boolean routePreparing;
    private final RouteState state = new RouteState();
    private boolean confirmReset = true;
    private String status = "";
    private Runnable prepare = () -> { };
    private Runnable loaded = () -> { };
    private java.util.function.Consumer<SupplyPlan> startSupplies=plan -> { };
    private Runnable clearSupplies=() -> { };
    private SupplySnapshot supplies=SupplySnapshot.UNKNOWN;
    private SupplyChecklist supplyChecklist;
    private boolean showSupplies=true;
    private boolean bankFilterActive;
    private JButton clearBankFilter;
    private JLabel bankFilterStatus;
    private String selectedPreset;
    private CropGrowth growth;
    private JLabel readiness;
    private List<Patch> displayedRoute = java.util.Collections.emptyList();
    private final java.util.Map<Patch,JLabel> cropLabels = new java.util.EnumMap<>(Patch.class);
    void setGrowth(CropGrowth growth){this.growth=growth;refreshGrowth(java.time.Instant.now().getEpochSecond());}

    /** Update existing labels so a countdown never closes an open preset dropdown. */
    void refreshGrowth(long now)
    {
        if(!config.trackGrowth() || readiness==null)return;
        boolean changed=updateText(readiness,growth==null ? "Crop readiness: log in and visit your patches to start tracking." : growth.summary(displayedRoute,now));
        for(java.util.Map.Entry<Patch,JLabel> entry:cropLabels.entrySet())
        {changed|=updateText(entry.getValue(),growth==null ? "Growth unknown - visit this patch to track it." : growth.describe(entry.getKey(),now));}
        if(changed){resizeRows();revalidate();repaint();}
    }

    void setPresetLoaded(Runnable loaded) { this.loaded = loaded; }
    void setSupplyActions(java.util.function.Consumer<SupplyPlan> start,Runnable clear)
    { startSupplies=start;clearSupplies=clear; }
    void setInventoryDataRequest(java.util.function.Consumer<java.util.Set<Integer>> request)
    { inventories.setRequest(request);reload(); }
    void setInventoryData(InventoryItemData data) { inventories.accept(data); }
    void setRoutePreparing(boolean preparing) { routePreparing=preparing; }
    void dispose() { inventories.dispose(); }
    void setSupplies(SupplySnapshot snapshot)
    {
        if(supplies.known==snapshot.known && supplies.plain.equals(snapshot.plain) && supplies.notes.equals(snapshot.notes))return;
        supplies=snapshot;if(supplyChecklist!=null){supplyChecklist.refresh(snapshot);resizeRows();revalidate();}
    }
    void setBankFilterActive(boolean active)
    {
        bankFilterActive=active;if(clearBankFilter!=null)clearBankFilter.setEnabled(active);
        if(bankFilterStatus!=null)updateText(bankFilterStatus,active ? "Bank filter: ready until the bank closes." : "Bank filter: off. Click Filter this inventory to pack.");
    }
    void presetSaved(String name) { selectedPreset = name; setStatus("Saved locally: " + name); }
    void presetEditing(String name)
    {
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
        removeAll();
        readiness=null;cropLabels.clear();
        List<Patch> route = RouteState.active(config);
        inventories.update(route);
        InventoryLoadPlanner.Plan inventoryPlan=inventories.get();
        int currentInventory=inventoryPlan==null?-1:inventoryPlan.current(state);
        boolean loadsReady=!config.loadPlanning() || inventoryPlan!=null;
        boolean bankNeeded=config.loadPlanning() && currentInventory>0
            && !inventoryPlan.bankConfirmed(preferences.get(InventoryLoadPlanner.BANKS_KEY),currentInventory);
        String loadKey=!config.loadPlanning()?"all":inventoryPlan==null?"waiting":inventoryPlan.signature+":"+currentInventory;
        if(displayedLoad!=null && !displayedLoad.equals(loadKey) && bankFilterActive)
        {clearSupplies.run();setBankFilterActive(false);}
        displayedLoad=loadKey;
        List<Patch> remaining=new java.util.ArrayList<>();
        List<Patch> packingPatches=config.loadPlanning() && inventoryPlan!=null
            ? currentInventory<0?java.util.Collections.emptyList():inventoryPlan.loads.get(currentInventory).patches : route;
        for(Patch patch:packingPatches)if(!state.isDone(patch))remaining.add(patch);
        SupplyPlan packing=loadsReady && config.loadPlanning()
            ? InventoryLoadPlanner.supplies(config,remaining,inventories.data()) : SupplyPlan.build(config,remaining);
        displayedRoute=route;
        add(text("Farm Run Planner"));
        add(text(config.runName().trim()));
        add(text("Create and save presets in RuneLite’s plugin cog. Choose your run below."));
        add(new PresetControls(config, preferences, loaded, selectedPreset, name -> selectedPreset = name, this::setStatus));
        JButton start=new JButton("Start run");start.setName("startRun");start.setEnabled(!route.isEmpty() && loadsReady && !routePreparing);
        start.addActionListener(event -> {
            if(state.count(route)>0 && confirmReset && JOptionPane.showConfirmDialog(this,
                "Start a fresh run and clear completion marks?","Start farming run",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
            try
            {
                preferences.update(java.util.Map.of(PROGRESS_KEY,"",InventoryLoadPlanner.BANKS_KEY,""));
                clearSupplies.run();setBankFilterActive(false);displayedLoad=null;
                List<Patch> first=inventoryPlan==null?route:inventoryPlan.loads.get(0).patches;
                startSupplies.accept(config.loadPlanning()?InventoryLoadPlanner.supplies(config,first,inventories.data()):SupplyPlan.build(config,first));
                setStatus("Run started. Pack the first inventory at your bank and withdraw supplies yourself. Clear other searches or tag tabs if items are hidden.");
            }
            catch(IllegalStateException error){setStatus(error.getMessage());}
        });
        add(start);
        JButton pack=new JButton("Filter this inventory");pack.setName("packInventory");
        pack.setEnabled(loadsReady && !routePreparing && !remaining.isEmpty());
        pack.addActionListener(event->{startSupplies.accept(packing);setStatus("Filter ready for this inventory. Withdraw supplies manually; completion marks are kept.");});
        add(pack);
        clearBankFilter=new JButton("Clear bank filter");clearBankFilter.setName("clearBankFilter");
        clearBankFilter.setEnabled(bankFilterActive);
        clearBankFilter.addActionListener(event -> {clearSupplies.run();setBankFilterActive(false);});
        add(clearBankFilter);
        bankFilterStatus=text("");add(bankFilterStatus);setBankFilterActive(bankFilterActive);
        if(config.loadPlanning())
        {
            if(inventoryPlan==null)add(text(inventories.error()==null?"Preparing inventory loads from RuneLite item data...":inventories.error()));
            else if(currentInventory>=0)
            {
                int slots=InventoryLoadPlanner.slots(packing,inventories.data());
                JLabel loadInfo=text("Inventory "+(currentInventory+1)+" of "+inventoryPlan.loads.size()+" - "+slots+" / "+inventoryPlan.capacity+" slots ("+InventoryLoadPlanner.reserved(config)+" reserved)");
                loadInfo.setName("inventoryLoad");
                loadInfo.setToolTipText(html("Starting-supply estimate. Tools count as backpack slots even if worn. Reserved slots cover travel items, other gear and harvested crops; note or bank produce manually."));
                add(loadInfo);
                JLabel bankInfo=text("Bank: "+config.loadBank());bankInfo.setToolTipText(html(config.loadBank().instructions(config)));add(bankInfo);
                if(slots>inventoryPlan.capacity)add(text("This patch's supplies exceed the available slots. Adjust reserved space, noted payments or tools before leaving the bank."));
            }
        }
        supplyChecklist=loadsReady?new SupplyChecklist(packing,supplies,supplyIcons):null;
        JCheckBox showPacking=new JCheckBox("Show packing checklist",showSupplies);
        showPacking.setName("showPackingChecklist");showPacking.setOpaque(false);showPacking.setForeground(Color.LIGHT_GRAY);
        showPacking.addActionListener(event->{showSupplies=showPacking.isSelected();if(supplyChecklist!=null)supplyChecklist.setVisible(showSupplies);resizeRows();revalidate();});
        add(showPacking);
        if(supplyChecklist!=null){supplyChecklist.setVisible(showSupplies);add(supplyChecklist);}
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
        if (!status.isEmpty()) { add(text(status)); }
        Patch next = state.next(route);
        add(text(bankNeeded ? "NEXT: Bank at "+config.loadBank()+" for inventory "+(currentInventory+1)+"."
            : route.isEmpty() ? "No patches selected. Open the settings cog to choose them."
            : next == null ? "Run complete. Reset when you're ready to start again."
            : "NEXT: " + next.getLabel()));
        JButton reset = new JButton("Reset run");
        reset.setName("resetRun");
        reset.addActionListener(event ->
        {
            if (!confirmReset || JOptionPane.showConfirmDialog(this,
                "Clear completion marks for all patches?", "Reset farming run", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
            {
                state.reset();
                clearSupplies.run();
                try { preferences.update(java.util.Map.of(PROGRESS_KEY,state.save(),InventoryLoadPlanner.BANKS_KEY,"")); reload(); }
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
                BorderFactory.createLineBorder(patch == next && !bankNeeded ? new Color(103, 188, 116) : Color.DARK_GRAY),
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
                    preferences.update(changes);reload();
                }
                catch (IllegalStateException error) { setStatus(error.getMessage()); }
            });
            content.add(complete);
            Patch previous = i > 0 && starts<=0 ? route.get(i - 1) : null;
            String walking = starts>0 ? config.loadBank().departureInstructions(patch) : TravelCosts.walkingInstruction(previous, patch, config);
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
        refreshGrowth(java.time.Instant.now().getEpochSecond());
        resizeRows();
        revalidate();
        repaint();
    }

    private JPanel bankCard(InventoryLoadPlanner.Plan plan,int index,int current,boolean waiting)
    {
        JPanel card=new JPanel();card.setName("bankStop_"+index);card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        boolean confirmed=plan.bankConfirmed(preferences.get(InventoryLoadPlanner.BANKS_KEY),index);
        card.setBackground(new Color(38,45,49));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(index==current && waiting
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
