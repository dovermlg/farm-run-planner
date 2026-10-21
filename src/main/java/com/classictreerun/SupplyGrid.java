package com.classictreerun;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.client.ui.ColorScheme;

/** Generated 4-by-7 supply preview for the current load. Item order in game is unrestricted. */
final class SupplyGrid extends JPanel
{
    private final SupplyPlan plan;
    private final PackingLayout layout;
    private final List<PackingItemSlot> slots=new ArrayList<>();
    SupplyGrid(SupplyPlan plan,InventoryItemData data,int reserved,SupplySnapshot snapshot,BiConsumer<Integer,JLabel> icons)
    {
        this.plan=plan;
        setName("supplyGrid");setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
        JLabel header=new JLabel("Inventory supplies");header.setForeground(java.awt.Color.LIGHT_GRAY);add(header);
        JLabel guide=new JLabel("Red: missing or short");guide.setForeground(java.awt.Color.GRAY);
        guide.setToolTipText("Items can be anywhere in your backpack. This is a generated packing preview, not a required item order.");add(guide);
        if(!data.covers(plan.items))
        {
            layout=null;JLabel waiting=new JLabel("Reading item data...");waiting.setName("gridWaiting");
            waiting.setForeground(java.awt.Color.LIGHT_GRAY);add(waiting);return;
        }
        layout=PackingLayout.build(plan,data);
        JPanel grid=new JPanel(new GridLayout(7,4,3,3));grid.setName("inventorySlots");grid.setOpaque(false);
        Dimension size=new Dimension(185,298);grid.setPreferredSize(size);grid.setMaximumSize(size);grid.setAlignmentX(LEFT_ALIGNMENT);
        for(int i=0;i<28;i++)
        {
            if(i<layout.cells.size())
            {
                PackingLayout.Cell cell=layout.cells.get(i);PackingItemSlot slot=new PackingItemSlot(cell.quantity);
                slot.setName("inventorySlot_"+i);slot.getAccessibleContext().setAccessibleName(plan.requirements.get(cell.requirement).label);
                slots.add(slot);grid.add(slot);icons.accept(cell.item,slot);
            }
            else
            {
                PackingItemSlot empty=new PackingItemSlot(0);empty.setName("inventorySlot_"+i);
                boolean held=i>=28-reserved;
                empty.setToolTipText(held?"Reserved space for other gear and harvested crops.":"Free space in this supply preview.");
                empty.getAccessibleContext().setAccessibleName(held?"Reserved slot":"Free slot");grid.add(empty);
            }
        }
        add(grid);
        JLabel notes=new JLabel("Packing notes (hover)");notes.setName("gridPackingNotes");notes.setForeground(java.awt.Color.GRAY);
        StringBuilder advice=new StringBuilder("Items can be anywhere in your backpack. Red slots are missing or short. Worn gear is checked in Equipment below. Protection payments are optional.");
        for(String reminder:plan.reminders)advice.append("\n\n").append(reminder);
        advice.append("\n\nTeleport charges and stored supplies are not counted. Inventory planning conservatively counts tool and teleport slots even if saved as worn gear.");
        notes.setToolTipText(PackingItemSlot.tooltip(advice.toString()));add(notes);
        if(layout.total>28)
        {
            JLabel overflow=new JLabel(PackingItemSlot.tooltip((layout.total-28)+" additional item slots required. Switch to List for every requirement; adjust supplies or bank more often."));
            overflow.setName("packingOverflow");overflow.setForeground(new java.awt.Color(255,140,123));add(overflow);
        }
        refresh(snapshot);
    }
    void refresh(SupplySnapshot snapshot)
    {
        if(layout==null)return;
        List<Long> carried=plan.carried(snapshot);
        for(int i=0;i<layout.cells.size();i++)
        {
            PackingLayout.Cell cell=layout.cells.get(i);SupplyPlan.Requirement row=plan.requirements.get(cell.requirement);
            long have=carried.get(cell.requirement);
            String detail=row.label+(row.notedOnly?" (noted)":"")+"\nRequired: "+row.quantity+". Carried: "+have+".";
            if(!snapshot.known)detail+="\nLog in to check carried supplies.";
            else if(have<row.quantity)detail+="\nShort "+(row.quantity-have)+".";
            else detail+="\nPacked.";
            if(!cell.stack && row.quantity>1)detail+="\nItem "+(cell.index+1)+" of "+row.quantity+" in this preview.";
            if(row.notedOnly)detail+="\nWithdraw this protection payment as notes.";
            else if(row.notes)detail+="\nOptional protection payment; noted items also count.";
            else detail+="\nMust be unnoted.";
            if(!row.detail.isEmpty())detail+="\n"+row.detail;
            slots.get(i).status(snapshot.known,cell.packed(have),detail);
        }
    }
}
