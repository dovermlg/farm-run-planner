package com.classictreerun;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/** Small native Swing icon for the packing section's visibility toggle. */
final class PackingEyeIcon implements Icon
{
    private final boolean visible;
    PackingEyeIcon(boolean visible) { this.visible=visible; }
    @Override public int getIconWidth() { return 20; }
    @Override public int getIconHeight() { return 16; }
    @Override public void paintIcon(Component component,Graphics graphics,int x,int y)
    {
        Graphics2D g=(Graphics2D)graphics.create();
        try
        {
            g.translate(x,y);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(component.isEnabled()?Color.LIGHT_GRAY:Color.GRAY);
            g.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            Path2D eye=new Path2D.Float();eye.moveTo(1,8);eye.curveTo(6,0,14,0,19,8);eye.curveTo(14,16,6,16,1,8);eye.closePath();
            g.draw(eye);g.fillOval(7,5,6,6);
            if(!visible)g.drawLine(2,1,18,15);
        }
        finally{g.dispose();}
    }
}
