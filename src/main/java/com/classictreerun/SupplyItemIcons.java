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
    private final Map<Long,Icon> icons=new HashMap<>();
    SupplyItemIcons(ItemManager itemManager) { this.itemManager=itemManager; }
    void add(int item,JLabel label)
    {
        Object requested=label.getClientProperty("itemIconWidth");
        int width=requested instanceof Integer?(Integer)requested:18;
        long key=((long)item<<32)|width;
        Icon cached=icons.get(key);
        if(cached!=null){label.setIcon(cached);return;}
        AsyncBufferedImage image=itemManager.getImage(item);
        image.onLoaded(()->SwingUtilities.invokeLater(()->{
            Icon ready=icons.computeIfAbsent(key,id->new ImageIcon(ImageUtil.resizeImage(image,width,width*8/9)));
            label.setIcon(ready);
        }));
    }
}
