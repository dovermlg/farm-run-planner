package com.classictreerun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.ui.ColorScheme;

/** Saved gear is checked against its worn slot; merely carrying it is not enough. */
final class EquipmentView extends JPanel
{
    private final EquipmentSetup setup;
    private final InventoryItemData data;
    private final Map<EquipmentInventorySlot,JLabel> labels=new EnumMap<>(EquipmentInventorySlot.class);
    EquipmentView(EquipmentSetup setup,InventoryItemData data,boolean grid,SupplySnapshot snapshot,BiConsumer<Integer,JLabel> icons)
    {
        this.setup=setup;this.data=data;
        setName("equipmentView");setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(6,0,5,0));
        JLabel title=new JLabel("Equipment");title.setForeground(Color.LIGHT_GRAY);add(title);
        if(setup.entries.isEmpty())
        {
            JLabel empty=new JLabel(PackingItemSlot.tooltip("Optional: open Inventory setup on the main tab to capture and save your gear."));
            empty.setName("noSavedEquipment");empty.setForeground(Color.GRAY);add(empty);return;
        }
        if(grid)
        {
            EquipmentInventorySlot[] positions={null,EquipmentInventorySlot.HEAD,null,
                EquipmentInventorySlot.CAPE,EquipmentInventorySlot.AMULET,EquipmentInventorySlot.AMMO,
                EquipmentInventorySlot.WEAPON,EquipmentInventorySlot.BODY,EquipmentInventorySlot.SHIELD,
                null,EquipmentInventorySlot.LEGS,null,
                EquipmentInventorySlot.GLOVES,EquipmentInventorySlot.BOOTS,EquipmentInventorySlot.RING};
            JPanel slots=new JPanel(new GridLayout(5,3,3,3));slots.setName("equipmentSlots");slots.setOpaque(false);
            Dimension size=new Dimension(138,212);slots.setPreferredSize(size);slots.setMaximumSize(size);slots.setAlignmentX(LEFT_ALIGNMENT);
            for(EquipmentInventorySlot slot:positions)
            {
                if(slot==null){JPanel space=new JPanel();space.setOpaque(false);slots.add(space);continue;}
                EquipmentSetup.Entry entry=setup.entries.get(slot);PackingItemSlot cell=new PackingItemSlot(entry==null?0:entry.quantity);
                cell.setName("equipmentSlot_"+slot.name());cell.getAccessibleContext().setAccessibleName(EquipmentSetup.label(slot));
                if(entry==null){cell.setText(EquipmentSetup.label(slot));cell.setToolTipText("No saved item for "+EquipmentSetup.label(slot)+".");}
                else {labels.put(slot,cell);icons.accept(entry.item,cell);}
                slots.add(cell);
            }
            add(slots);
        }
        else
        {
            for(EquipmentInventorySlot slot:EquipmentSetup.SLOTS)
            {
                EquipmentSetup.Entry entry=setup.entries.get(slot);if(entry==null)continue;
                JLabel label=new JLabel(EquipmentSetup.label(slot)+": "+name(entry.item));
                label.setName("equipmentSlot_"+slot.name());labels.put(slot,label);add(label);
            }
        }
        refresh(snapshot);
    }
    private String name(int id)
    {return data.covers(java.util.Set.of(id)) && !data.get(id).name.isEmpty()?data.get(id).name:"Item "+id;}
    void refresh(SupplySnapshot snapshot)
    {
        for(Map.Entry<EquipmentInventorySlot,JLabel> row:labels.entrySet())
        {
            EquipmentSetup.Entry entry=setup.entries.get(row.getKey());long worn=setup.worn(row.getKey(),snapshot);
            boolean packed=worn>=entry.quantity;
            String detail=EquipmentSetup.label(row.getKey())+": "+name(entry.item)+"\nRequired worn: "+entry.quantity+". Matching worn: "+worn+".";
            detail+=!snapshot.known?"\nLog in to check equipped items.":packed?"\nEquipped.":"\nWear this item in the saved slot. Short "+(entry.quantity-worn)+".";
            if(entry.alternatives().size()>1)detail+="\nUsable charge variants of this teleport jewellery count. Charges are not verified.";
            if(row.getValue() instanceof PackingItemSlot)((PackingItemSlot)row.getValue()).status(snapshot.known,packed,detail);
            else
            {
                row.getValue().setForeground(!snapshot.known?Color.LIGHT_GRAY:packed?new Color(103,188,116):new Color(255,115,115));
                row.getValue().setToolTipText(PackingItemSlot.tooltip(detail));
                row.getValue().getAccessibleContext().setAccessibleDescription(detail);
            }
        }
    }
}
