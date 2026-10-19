package com.classictreerun;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

/** Item sprites come from RuneLite's cache. Rendering never blocks the sidebar. */
final class SupplyItemIcons
{
    private final ItemManager itemManager;
    private final Map<Integer,Icon> icons=new HashMap<>();
    SupplyItemIcons(ItemManager itemManager) { this.itemManager=itemManager; }
    void add(int item,JLabel label)
    {
        Icon cached=icons.get(item);
        if(cached!=null){label.setIcon(cached);return;}
        AsyncBufferedImage image=itemManager.getImage(item);
        image.onLoaded(()->SwingUtilities.invokeLater(()->{
            Icon ready=icons.computeIfAbsent(item,id->new ImageIcon(ImageUtil.resizeImage(image,18,16)));
            label.setIcon(ready);
        }));
    }
}
