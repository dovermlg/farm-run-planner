package com.classictreerun;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplyChecklistTest
{
    @Test public void compactRowsFitSidebarWithIconsAndKeepPackingDetailsInTooltips() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();f.set("cropHerb","RANARR");f.set("cropTree","MAGIC");f.set("supplyCompost","BOTTOMLESS");
            SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.TROLL_HERB,Patch.WEISS_HERB,Patch.LUMBRIDGE_TREE));
            SupplySnapshot snapshot=new SupplySnapshot(true,Map.of(ItemID.RANARR_SEED,1L,ItemID.COINS,10000L,
                ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED,1L,ItemID.SPADE,1L,ItemID.RAKE,1L),Map.of());
            BufferedImage placeholder=new BufferedImage(18,16,BufferedImage.TYPE_INT_ARGB);
            Graphics2D marker=placeholder.createGraphics();
            marker.setColor(new Color(120,120,120));marker.fillOval(5,4,8,8);marker.dispose();
            AtomicInteger iconRequests=new AtomicInteger();
            SupplyChecklist checklist=new SupplyChecklist(plan,snapshot,(item,label)->{
                iconRequests.incrementAndGet();label.setIcon(new ImageIcon(placeholder));
            });
            assertEquals(plan.requirements.size(),iconRequests.get());
            int height=checklist.getPreferredSize().height;
            assertTrue("Packing list must stay compact",height<270);
            checklist.setSize(225,height);layout(checklist);
            JPanel list=(JPanel)find(checklist,"supplyItems");
            assertEquals(plan.requirements.size(),list.getComponentCount());
            for(Component row:list.getComponents())
            {
                assertEquals(18,row.getHeight());
                assertTrue(row.getX()>=0 && row.getX()+row.getWidth()<=list.getWidth());
                JLabel text=(JLabel)((Container)row).getComponent(0),icon=(JLabel)((Container)row).getComponent(1);
                assertFalse(text.getText().startsWith("<html>"));
                assertTrue("Icon must sit to the right of text",icon.getX()>=text.getX()+text.getWidth());
                assertTrue("Icons must fit",icon.getX()+icon.getWidth()<=row.getWidth());
                assertTrue("Item name must fit without clipping: "+text.getText(),
                    text.getFontMetrics(text.getFont()).stringWidth(text.getText())<=text.getWidth());
                assertEquals(text.getToolTipText(),icon.getToolTipText());
            }
            JLabel ranarr=(JLabel)find(checklist,"supplyItem_"+ItemID.RANARR_SEED);
            assertEquals("2 x Ranarr seed",ranarr.getText());assertTrue(ranarr.getToolTipText().contains("Short 1"));
            JLabel bucket=(JLabel)find(checklist,"supplyItem_"+ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED);
            assertTrue(bucket.getToolTipText().contains("compost type and charges"));
            JLabel payment=(JLabel)find(checklist,"supplyItem_"+ItemID.COCONUT+"_payment");
            assertEquals("25 x Coconut",payment.getText());assertTrue(payment.getToolTipText().contains("Optional protection"));
            JLabel notes=(JLabel)find(checklist,"packingNotes");assertTrue(notes.getToolTipText().contains("remaining patches"));
            f.set("cropTree","OAK");
            SupplyChecklist oak=new SupplyChecklist(SupplyPlan.build(f.config,List.of(Patch.LUMBRIDGE_TREE)),snapshot,(item,label)->{ });
            JLabel basket=(JLabel)find(oak,"supplyItem_"+ItemID.BASKET_TOMATO_5+"_payment");
            assertEquals("1 x Tomatoes(5)",basket.getText());
            BufferedImage preview=new BufferedImage(225,height,BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics=preview.createGraphics();try{checklist.printAll(graphics);}finally{graphics.dispose();}
            try
            {
                Path output=Path.of("build/reports/requirements-preview.png");Files.createDirectories(output.getParent());
                ImageIO.write(preview,"png",output.toFile());
            }
            catch(java.io.IOException error){throw new AssertionError(error);}
            checklist.refresh(SupplySnapshot.UNKNOWN);
            assertSame("Inventory refresh must not rebuild rows",ranarr,find(checklist,"supplyItem_"+ItemID.RANARR_SEED));
            assertEquals(Color.LIGHT_GRAY,ranarr.getForeground());
        });
    }

    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {
            if(name.equals(child.getName()))return child;
            if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}
        }
        return null;
    }
    private static void layout(Container parent)
    {parent.doLayout();for(Component child:parent.getComponents())if(child instanceof Container)layout((Container)child);}
}
