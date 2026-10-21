package com.classictreerun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.awt.Point;
import java.util.function.Supplier;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.Text;

/** Read-only current farming step, using RuneLite's movable panel overlay. */
final class RunStepOverlay extends OverlayPanel
{
    private final Client client;
    private final ClassicTreeRunConfig config;
    private final Supplier<RunStep> step;

    RunStepOverlay(Client client,ClassicTreeRunConfig config,Supplier<RunStep> step)
    {
        this.client=client;this.config=config;this.step=step;
        setPosition(OverlayPosition.TOP_LEFT);
        // OverlayManager replaces preferredSize with the saved size, or null on first load/reset.
        // Keep the default on the panel itself so RuneLite's unsized state still renders safely.
        panelComponent.setPreferredSize(new Dimension(240,0));
        panelComponent.setGap(new Point(0,4));
    }

    @Override public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
        if(!config.showRunOverlay() || client.getGameState()!=GameState.LOGGED_IN)return null;
        RunStep current=step.get();if(current==null)return null;
        line(graphics,"Farm Run Planner",new Color(255,200,70));
        line(graphics,current.runName,Color.LIGHT_GRAY);
        line(graphics,current.title,current.kind==RunStep.Kind.COMPLETE?NextTeleportOverlay.COLOR:Color.WHITE);
        line(graphics,current.instructions,Color.WHITE);
        line(graphics,current.crop,Color.LIGHT_GRAY);
        line(graphics,current.notes,Color.LIGHT_GRAY);
        line(graphics,current.progress,current.kind==RunStep.Kind.COMPLETE?NextTeleportOverlay.COLOR:Color.LIGHT_GRAY);
        return super.render(graphics);
    }

    private void line(Graphics2D graphics,String value,Color color)
    {
        if(value.isEmpty())return;
        // Names and custom bank instructions are plain text, not overlay color/image tags.
        Dimension size=getPreferredSize();
        int width=Math.max(20,(size==null?panelComponent.getPreferredSize():size).width-8);
        panelComponent.getChildren().add(LineComponent.builder().left(wrapLongWords(value,graphics.getFontMetrics(),width)).leftColor(color).build());
    }

    static String wrapLongWords(String value,FontMetrics metrics,int width)
    {
        StringBuilder result=new StringBuilder(),word=new StringBuilder();
        String plain=Text.removeTags(value);
        for(int i=0;i<plain.length();)
        {
            int cp=plain.codePointAt(i);String character=new String(Character.toChars(cp));i+=Character.charCount(cp);
            if(Character.isWhitespace(cp)){result.append(' ');word.setLength(0);continue;}
            if(word.length()>0 && metrics.stringWidth(word.toString()+character)>width)
            {result.append(' ');word.setLength(0);}
            word.append(character);result.append(character);
        }
        return result.toString();
    }
}
