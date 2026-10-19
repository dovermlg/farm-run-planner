package com.classictreerun;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import org.junit.Test;
import static org.junit.Assert.*;

public class NextTeleportOverlayTest
{
    private final ConfigFixture settings = new ConfigFixture();
    private final AtomicReference<GameState> game = new AtomicReference<>(GameState.LOGGED_IN);
    private final AtomicReference<TeleportDevice> device = new AtomicReference<>(TeleportDevice.WEALTH);
    private int spriteCalls;
    private final Client client = (Client)Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Client.class},
        (p,m,a) -> { if(m.getName().equals("getGameState"))return game.get();throw new AssertionError(m.getName()); });
    private final NextTeleportOverlay overlay = new NextTeleportOverlay(client, settings.config, device::get, (id,quantity) -> {
        spriteCalls++;
        BufferedImage sprite = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = sprite.createGraphics();
        try { graphics.setColor(NextTeleportOverlay.COLOR);graphics.drawRect(0,0,31,31); }
        finally { graphics.dispose(); }
        return sprite;
    });

    @Test public void drawsInInventoryWornEquipmentAndBankSideButNeverBankStockOrTrade()
    {
        for(int group:new int[]{InterfaceID.INVENTORY,InterfaceID.WORNITEMS,InterfaceID.BANKSIDE})
            assertEquals(NextTeleportOverlay.COLOR.getRGB(),render(group,ItemID.RING_OF_WEALTH_5,1,null).getRGB(10,10));
        assertEquals(3,spriteCalls);
        assertEquals(0,render(InterfaceID.BANKMAIN,ItemID.RING_OF_WEALTH_5,1,null).getRGB(10,10));
        assertEquals(0,render(335,ItemID.RING_OF_WEALTH_5,1,null).getRGB(10,10));
        assertEquals(3,spriteCalls);
    }

    @Test public void doesNotDrawWhenDisabledLoggedOutEmptyOrGivenAnotherItemForm()
    {
        settings.set("highlightNextTeleport","false");render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,1,null);
        settings.set("highlightNextTeleport","true");game.set(GameState.LOGIN_SCREEN);render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,1,null);
        game.set(GameState.LOGGED_IN);render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH,1,null);
        render(InterfaceID.INVENTORY,11981,1,null);render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,0,null);
        device.set(TeleportDevice.NONE);render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,1,null);
        assertEquals(0,spriteCalls);
    }

    @Test public void movingToAnotherRouteStepImmediatelyStopsDrawingTheOldDevice()
    {
        render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,1,null);assertEquals(1,spriteCalls);
        device.set(TeleportDevice.DUELING);
        render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,1,null);assertEquals(1,spriteCalls);
        render(InterfaceID.WORNITEMS,ItemID.RING_OF_DUELING_1,1,null);assertEquals(2,spriteCalls);
    }

    @Test public void followsDraggingBoundsWithoutMovingTheItem()
    {
        BufferedImage result=render(InterfaceID.INVENTORY,ItemID.RING_OF_WEALTH_5,1,new Rectangle(50,50,32,32));
        assertEquals(0,result.getRGB(10,10));assertEquals(NextTeleportOverlay.COLOR.getRGB(),result.getRGB(50,50));
    }

    private BufferedImage render(int group,int item,int quantity,Rectangle dragging)
    {
        Widget widget=(Widget)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Widget.class},
            (p,m,a) -> {if(m.getName().equals("getId"))return group<<16;throw new AssertionError(m.getName());});
        WidgetItem target=new WidgetItem(item,quantity,new Rectangle(10,10,32,32),widget,dragging);
        BufferedImage image=new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);Graphics2D graphics=image.createGraphics();
        try {overlay.renderItemOverlay(graphics,item,target);}finally{graphics.dispose();}
        return image;
    }
}
