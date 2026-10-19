package com.classictreerun;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

final class SupplyChecklist extends JPanel
{
    private final SupplyPlan plan;
    private final List<JLabel> labels=new ArrayList<>();
    SupplyChecklist(SupplyPlan plan,SupplySnapshot snapshot)
    {
        this.plan=plan;
        setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(label("Supplies for remaining patches"));
        add(label("Inventory + worn equipment. Red = short. Protection payments are optional."));
        for(SupplyPlan.Requirement row:plan.requirements)
        {
            JLabel text=label("");labels.add(text);add(text);
        }
        for(String reminder:plan.reminders)add(label(reminder));
        if(!plan.requirements.isEmpty())add(label("Stored tools, seed boxes and bucket charges aren't counted. Pack your travel items separately."));
        refresh(snapshot);
    }
    void refresh(SupplySnapshot snapshot)
    {
        List<Long> counts=plan.carried(snapshot);
        for(int i=0;i<labels.size();i++)
        {
            SupplyPlan.Requirement row=plan.requirements.get(i);long have=counts.get(i);
            String status=!snapshot.known ? "log in to check" : have+" / "+row.quantity
                +(have<row.quantity ? " - short "+(row.quantity-have) : " - packed");
            labels.get(i).setText(html(row.label+": "+(snapshot.known?status:row.quantity+" - "+status)));
            labels.get(i).setForeground(!snapshot.known ? Color.LIGHT_GRAY : have<row.quantity
                ? new Color(255,115,115) : new Color(103,188,116));
        }
        revalidate();repaint();
    }
    private static String html(String text)
    { return "<html><body style='width:145px'>"+text+"</body></html>"; }
    private static JLabel label(String text)
    { JLabel label=new JLabel(html(text));label.setForeground(Color.LIGHT_GRAY);return label; }
}
