package com.classictreerun;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/** Paints an item outline only. Never changes widgets, menus, input or game state. */
final class NextTeleportOverlay extends WidgetItemOverlay
{
    static final Color COLOR = new Color(103, 188, 116);
    private final Client client;
    private final ClassicTreeRunConfig config;
    private final Supplier<TeleportDevice> next;
    private final BiFunction<Integer, Integer, BufferedImage> outlines;

    NextTeleportOverlay(Client client, ClassicTreeRunConfig config, Supplier<TeleportDevice> next,
        BiFunction<Integer, Integer, BufferedImage> outlines)
    {
        this.client = client;
        this.config = config;
        this.next = next;
        this.outlines = outlines;
        showOnInterfaces(InterfaceID.INVENTORY, InterfaceID.BANKSIDE, InterfaceID.WORNITEMS);
    }

    @Override public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem item)
    {
        if (!config.highlightNextTeleport() || client.getGameState() != GameState.LOGGED_IN
            || !next.get().matches(itemId) || item.getQuantity() <= 0 || item.getWidget() == null)
        { return; }
        int group = item.getWidget().getId() >>> 16;
        if (group != InterfaceID.INVENTORY && group != InterfaceID.BANKSIDE && group != InterfaceID.WORNITEMS)
        { return; }
        Rectangle bounds = item.getCanvasBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0) { return; }
        BufferedImage outline = outlines.apply(itemId, item.getQuantity());
        if (outline != null) { graphics.drawImage(outline, bounds.x, bounds.y, null); }
    }
}
