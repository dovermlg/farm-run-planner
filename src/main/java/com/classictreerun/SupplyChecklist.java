package com.classictreerun;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/** Compact item requirements. Detailed counts and packing advice live in tooltips. */
final class SupplyChecklist extends JPanel
{
    private final SupplyPlan plan;
    private final List<JLabel> labels=new ArrayList<>(), icons=new ArrayList<>();
    private final List<JPanel> rows=new ArrayList<>();

    SupplyChecklist(SupplyPlan plan,SupplySnapshot snapshot,BiConsumer<Integer,JLabel> itemIcons)
    {
        this.plan=plan;
        setBackground(ColorScheme.DARKER_GRAY_COLOR);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5,0,4,0));
        JLabel header=label("Item requirements:");
        header.setBorder(BorderFactory.createEmptyBorder(0,4,5,0));
        header.setToolTipText("Supplies for unfinished patches. Checks inventory and worn equipment only.");
        add(header);

        JPanel list=new JPanel();list.setName("supplyItems");
        list.setBackground(ColorScheme.DARK_GRAY_COLOR);
        list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(4,5,4,5));
        list.setAlignmentX(LEFT_ALIGNMENT);
        NumberFormat numbers=NumberFormat.getIntegerInstance(Locale.US);
        for(SupplyPlan.Requirement requirement:plan.requirements)
        {
            JPanel row=new JPanel(new BorderLayout(4,0));row.setOpaque(false);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMinimumSize(new Dimension(0,18));
            row.setPreferredSize(new Dimension(0,18));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE,18));
            JLabel text=label(numbers.format(requirement.quantity)+" x "+displayName(requirement));
            text.setName("supplyItem_"+requirement.item+(requirement.notes?"_payment":""));
            text.setMinimumSize(new Dimension(0,16));
            JLabel icon=new JLabel();icon.setName("supplyIcon_"+requirement.item);
            icon.setPreferredSize(new Dimension(18,16));
            row.add(text,BorderLayout.CENTER);row.add(icon,BorderLayout.EAST);
            labels.add(text);icons.add(icon);rows.add(row);list.add(row);
            itemIcons.accept(requirement.item,icon);
        }
        if(plan.requirements.isEmpty())list.add(label("No supplies needed."));
        add(list);

        if(!plan.requirements.isEmpty())
        {
            JLabel notes=label("Packing notes (hover)");notes.setName("packingNotes");
            notes.setForeground(Color.GRAY);notes.setBorder(BorderFactory.createEmptyBorder(3,4,0,0));
            StringBuilder advice=new StringBuilder("Counts cover unfinished patches and carried items only. Protection payments are optional.");
            for(String reminder:plan.reminders)advice.append("\n\n").append(reminder);
            advice.append("\n\nStored tools, seed boxes and bucket charges aren't counted. Teleport equipment checks possession only; check its charges and daily limits yourself.");
            notes.setToolTipText(tooltip(advice.toString()));add(notes);
        }
        refresh(snapshot);
    }

    void refresh(SupplySnapshot snapshot)
    {
        List<Long> counts=plan.carried(snapshot);
        for(int i=0;i<labels.size();i++)
        {
            SupplyPlan.Requirement requirement=plan.requirements.get(i);long have=counts.get(i);
            String status=!snapshot.known ? "Log in to check carried items." : "Carried: "+have+" / "+requirement.quantity+". "
                +(have<requirement.quantity ? "Short "+(requirement.quantity-have)+"." : "Packed.");
            String detail=requirement.label+"\n"+status;
            if(requirement.notedOnly)detail+="\nOptional protection payment. Withdraw as notes for this inventory; unnoted items do not count.";
            else if(requirement.notes)detail+="\nOptional protection payment. Noted items count.";
            else detail+="\nPlanting items and tools must be unnoted.";
            if(requirement.item==ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED)
                detail+="\nPossession of a filled bucket only. Check its compost type and charges yourself.";
            if(!requirement.detail.isEmpty())detail+="\n"+requirement.detail;
            String tooltip=tooltip(detail);
            labels.get(i).setToolTipText(tooltip);icons.get(i).setToolTipText(tooltip);rows.get(i).setToolTipText(tooltip);
            labels.get(i).setForeground(!snapshot.known ? Color.LIGHT_GRAY : have<requirement.quantity
                ? new Color(255,115,115) : new Color(103,188,116));
            labels.get(i).getAccessibleContext().setAccessibleDescription(detail);
        }
        repaint();
    }

    private static String displayName(SupplyPlan.Requirement requirement)
    {
        if(requirement.item==ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED)return "Bottomless compost bucket";
        String name=requirement.label.replace(" (optional protection)","").replace(" (your budget)","")
            .replace(" (magic recommended)","").replace(" (optional if paying for removal)","");
        name=name.replace("teleport tablet","tablet").replace("teleport scroll","scroll")
            .replace("Civitas illa Fortis","Fortis");
        if(name.endsWith(" buckets"))name=name.substring(0,name.length()-8);
        return name.substring(0,1).toUpperCase(Locale.ROOT)+name.substring(1)+(requirement.notedOnly?" (noted)":"");
    }

    private static String tooltip(String text)
    { return "<html><body style='width:195px'>"+text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>")+"</body></html>"; }
    private static JLabel label(String text)
    {
        JLabel label=new JLabel(text);label.setFont(FontManager.getRunescapeFont());
        label.setForeground(Color.LIGHT_GRAY);label.setAlignmentX(LEFT_ALIGNMENT);return label;
    }
}
