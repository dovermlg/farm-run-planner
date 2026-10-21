package com.classictreerun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.FontManager;

/** Item sprite, quantity and passive missing-item status. No game interaction handlers. */
final class PackingItemSlot extends JLabel
{
    static final Color MISSING=new Color(133,44,43),PACKED=new Color(43,63,48),UNKNOWN=new Color(48,47,45);
    private final int quantity;
    PackingItemSlot(int quantity)
    {
        this.quantity=quantity;
        putClientProperty("itemIconWidth",36);
        setHorizontalAlignment(SwingConstants.CENTER);setVerticalAlignment(SwingConstants.CENTER);
        setOpaque(true);setBackground(UNKNOWN);setForeground(new Color(125,125,125));
        setPreferredSize(new Dimension(44,40));setMinimumSize(getPreferredSize());setMaximumSize(getPreferredSize());
        setFont(FontManager.getRunescapeSmallFont());
        setBorder(BorderFactory.createLineBorder(new Color(30,30,30)));
    }
    void status(boolean known,boolean packed,String description)
    {
        setBackground(!known?UNKNOWN:packed?PACKED:MISSING);
        setBorder(BorderFactory.createLineBorder(!known?new Color(55,55,53):packed?new Color(78,117,82):new Color(206,80,69)));
        setToolTipText(tooltip(description));getAccessibleContext().setAccessibleDescription(description);repaint();
    }
    @Override protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        if(quantity<=1)return;
        String count=quantity>=10_000_000?quantity/1_000_000+"M":quantity>=10_000?quantity/1000+"K":Integer.toString(quantity);
        Graphics copy=graphics.create();
        try
        {
            copy.setFont(FontManager.getRunescapeSmallFont());copy.setColor(Color.BLACK);copy.drawString(count,3,12);
            copy.setColor(new Color(255,224,107));copy.drawString(count,2,11);
        }
        finally {copy.dispose();}
    }
    static String tooltip(String text)
    {return "<html><body style='width:195px'>"+text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br>")+"</body></html>";}
}
