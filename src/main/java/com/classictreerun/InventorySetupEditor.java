package com.classictreerun;

import java.awt.Color;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A draft editor inside the plugin sidebar. Capture and live observations never save by themselves. */
final class InventorySetupEditor extends JPanel
{
    interface Save {void apply(SavedPackingSetup expected,SavedPackingSetup draft);}
    private final String name;
    private final Consumer<Consumer<SupplySnapshot>> reader;
    private final Runnable changed,back;
    private final Save save;
    private final BiConsumer<Integer,JLabel> icons;
    private SavedPackingSetup baseline,draft;
    private InventoryItemData data=InventoryItemData.EMPTY;
    private SupplySnapshot snapshot=SupplySnapshot.UNKNOWN;
    private final JPanel preview=new JPanel();
    private BackpackSetupView backpack;
    private EquipmentView equipment;
    private final JLabel status=new JLabel();
    private final JButton saveButton=new JButton("Save setup"),discard=new JButton("Discard changes and back");
    private long generation;
    private boolean disposed,capturing;
    InventorySetupEditor(String name,SavedPackingSetup initial,Consumer<Consumer<SupplySnapshot>> reader,Save save,Runnable back,Runnable changed,BiConsumer<Integer,JLabel> icons)
    {
        this.name=name;this.baseline=initial;this.draft=initial;this.reader=reader;this.save=save;this.back=back;this.changed=changed;this.icons=icons;
        setName("setupEditor");setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(5,4,6,4));
        JLabel title=new JLabel("Inventory setup");title.setForeground(Color.WHITE);add(title);
        JLabel target=new JLabel(name);target.putClientProperty("html.disable",Boolean.TRUE);target.setForeground(Color.LIGHT_GRAY);target.setToolTipText(PackingItemSlot.tooltip(name));add(target);
        add(note("Capture the backpack and gear you are wearing. Save attaches this setup to this farming preset."));
        JButton capture=new JButton("Capture backpack and gear");capture.setName("captureSetup");capture.addActionListener(event->capture());add(capture);
        JLabel bankLabel=new JLabel("Bank display");bankLabel.setForeground(Color.LIGHT_GRAY);add(bankLabel);
        JComboBox<ClassicTreeRunConfig.SetupBankView> bank=new JComboBox<>(ClassicTreeRunConfig.SetupBankView.values());
        bank.setName("setupBankView");bank.setSelectedItem(initial.bank);bank.setToolTipText("Bank display for this saved setup. Banking and equipping stay manual.");
        bank.setMinimumSize(new java.awt.Dimension(0,bank.getPreferredSize().height));
        bank.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE,bank.getPreferredSize().height));
        bank.addActionListener(event->{draft=new SavedPackingSetup(draft.backpack,draft.equipment,(ClassicTreeRunConfig.SetupBankView)bank.getSelectedItem());buttons();});add(bank);
        saveButton.setName("saveSetup");saveButton.addActionListener(event->{
            if(capturing || disposed)return;
            try {save.apply(baseline,draft);baseline=draft;status("Setup saved locally. Back to run applies the saved bank view.");buttons();}
            catch(IllegalArgumentException | IllegalStateException error){status(error.getMessage());}
        });add(saveButton);
        JButton automatic=new JButton("Use calculated run supplies");automatic.setName("useCalculatedSetup");
        automatic.addActionListener(event->{invalidateCapture();draft=new SavedPackingSetup(BackpackSetup.NONE,EquipmentSetup.EMPTY,draft.bank);status("Captured backpack and gear cleared from the preview. Save setup to use calculated supplies.");render();buttons();changed.run();});add(automatic);
        JButton returnButton=new JButton("Back to run");returnButton.setName("setupBack");returnButton.addActionListener(event->{
            invalidateCapture();if(dirty()){discard.setVisible(true);status("You have unsaved changes. Save setup or discard them before leaving.");}else back.run();
        });add(returnButton);
        discard.setName("discardSetup");discard.setVisible(false);discard.addActionListener(event->{invalidateCapture();back.run();});add(discard);
        status.setName("setupEditorStatus");status.setForeground(Color.LIGHT_GRAY);add(status);
        preview.setLayout(new BoxLayout(preview,BoxLayout.Y_AXIS));preview.setOpaque(false);add(preview);
        add(note("Captured slots stay fixed during the run. The run's calculated requirements remain available for checking crops, tools and teleports."));
        for(java.awt.Component child:getComponents())
        {
            ((javax.swing.JComponent)child).setAlignmentX(LEFT_ALIGNMENT);
            if(child instanceof JButton)child.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE,child.getPreferredSize().height));
        }
        render();buttons();
    }
    private static JLabel note(String value)
    {JLabel label=new JLabel(inline(value));label.setForeground(Color.LIGHT_GRAY);label.setBorder(javax.swing.BorderFactory.createEmptyBorder(4,0,6,0));return label;}
    private static String inline(String value){return PackingItemSlot.tooltip(value).replace("width:195px","width:145px");}
    String presetName(){return name;}
    Set<Integer> items(){return draft.items();}
    boolean dirty(){return !draft.same(baseline);}
    void setData(InventoryItemData value){if(data==value)return;data=value;render();}
    void refresh(SupplySnapshot value){snapshot=value;if(backpack!=null)backpack.refresh(value);if(equipment!=null)equipment.refresh(value);}
    private void buttons(){saveButton.setEnabled(!capturing && dirty());if(!dirty())discard.setVisible(false);}
    private void status(String value){status.setText(inline(value));revalidate();repaint();}
    void invalidateCapture(){generation++;capturing=false;buttons();}
    void dispose(){disposed=true;invalidateCapture();}
    private void capture()
    {
        long request=++generation;capturing=true;buttons();status("Reading your backpack and worn equipment...");
        reader.accept(value->{
            if(disposed || request!=generation)return;
            capturing=false;
            if(!value.known || !value.backpack.captured){status("Log in before capturing your backpack and worn gear. Existing choices kept.");buttons();return;}
            draft=new SavedPackingSetup(value.backpack,new EquipmentSetup(value.worn),draft.bank);snapshot=value;
            status("Captured "+value.backpack.occupied()+" backpack slots and "+value.worn.size()+" worn items. Save setup to keep this capture.");
            render();buttons();changed.run();
        });
    }
    private void render()
    {
        preview.removeAll();backpack=new BackpackSetupView(draft.backpack,data,true,snapshot,icons);preview.add(backpack);
        equipment=new EquipmentView(draft.equipment,data,true,snapshot,icons);preview.add(equipment);preview.revalidate();preview.repaint();
    }
}
