package com.classictreerun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Preview and per-slot checks for an explicitly saved backpack. Never rearranges game items. */
final class BackpackSetupView extends JPanel
{
    private final BackpackSetup setup;
    private final InventoryItemData data;
    private final Map<Integer,JLabel> cells=new LinkedHashMap<>();
    private final JLabel summary=new JLabel();
    BackpackSetupView(BackpackSetup setup,InventoryItemData data,boolean grid,SupplySnapshot snapshot,BiConsumer<Integer,JLabel> icons)
    {
        this.setup=setup;this.data=data;setName("capturedBackpack");setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        JLabel title=new JLabel("Saved backpack");title.setForeground(Color.LIGHT_GRAY);add(title);
        if(!setup.captured)
        {JLabel empty=new JLabel("No captured backpack. Using run supplies.");empty.setForeground(Color.GRAY);add(empty);return;}
        JPanel body=new JPanel();body.setOpaque(false);
        if(grid)
        {
            body.setLayout(new GridLayout(7,4,3,3));Dimension size=new Dimension(185,298);
            body.setPreferredSize(size);body.setMaximumSize(size);body.setAlignmentX(LEFT_ALIGNMENT);
        }
        else body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        for(int i=0;i<28;i++)
        {
            BackpackSetup.Entry entry=setup.slots.get(i);if(entry==null && !grid)continue;
            JLabel cell=grid?new PackingItemSlot(entry==null?0:entry.quantity):new JLabel((i+1)+". "+entry.quantity+" x "+name(entry));
            cell.setName("capturedSlot_"+i);cells.put(i,cell);body.add(cell);
            if(entry!=null)
            {
                int icon=entry.item;
                if(entry.noted && data.covers(java.util.Set.of(entry.item)) && data.get(entry.item).noteItem>0)icon=data.get(entry.item).noteItem;
                icons.accept(icon,cell);
            }
        }
        add(body);summary.setName("capturedPackingStatus");add(summary);refresh(snapshot);
    }
    private String name(BackpackSetup.Entry entry)
    {return (data.covers(java.util.Set.of(entry.item)) && !data.get(entry.item).name.isEmpty()?data.get(entry.item).name:"Item "+entry.item)+(entry.noted?" (noted)":"");}
    void refresh(SupplySnapshot snapshot)
    {
        if(!setup.captured)return;
        boolean known=snapshot.known && snapshot.backpack.captured;int missing=0;
        for(int i=0;i<28;i++)
        {
            BackpackSetup.Entry expected=setup.slots.get(i),actual=snapshot.backpack.slots.get(i);
            boolean matches=expected==null?actual==null:expected.matches(actual);
            if(!matches)missing++;
            JLabel cell=cells.get(i);if(cell==null)continue;
            String detail="Backpack slot "+(i+1)+": "+(expected==null?"keep empty":expected.quantity+" x "+name(expected))+".\n";
            detail+=!known?"Log in to check backpack slots.":matches?"Matches the saved slot.":actual==null?"Missing from this slot."
                :"Currently "+actual.quantity+" x "+name(actual)+". Move or withdraw items manually.";
            if(expected!=null)detail+="\nChecks this position and at least the saved quantity. Noted and unnoted forms are separate.";
            if(cell instanceof PackingItemSlot)
            {
                ((PackingItemSlot)cell).status(known && (expected!=null || !matches),matches,detail);
                if(expected==null)cell.setText(known && !matches?"Extra":"");
            }
            else {cell.setForeground(!known?Color.LIGHT_GRAY:matches?new Color(103,188,116):new Color(255,115,115));cell.setToolTipText(PackingItemSlot.tooltip(detail));}
        }
        summary.setText(!known?"Backpack checks unknown":missing==0?"Backpack matches":"Check "+missing+" backpack slot"+(missing==1?"":"s"));
        summary.setForeground(!known?Color.GRAY:missing==0?new Color(103,188,116):new Color(255,115,115));
    }
}
