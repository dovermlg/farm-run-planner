package com.classictreerun;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplyPlanTest
{
    private static SupplyPlan.Requirement row(SupplyPlan plan,int item,boolean notes)
    { return plan.requirements.stream().filter(r->r.item==item && r.notes==notes).findFirst().orElseThrow(); }

    @Test public void classicMixedRunUsesExactlyChosenSaplingsAndAggregatesPayments()
    {
        ConfigFixture f=new ConfigFixture();f.set("cropTree","MAGIC");f.set("cropFruit","DRAGONFRUIT");
        f.set("cropHardwood","MAHOGANY");
        SupplyPlan plan=SupplyPlan.build(f.config,Patch.classic());
        assertEquals(6,row(plan,ItemID.PLANTPOT_MAGIC_TREE_SAPLING,false).quantity);
        assertEquals(6,row(plan,ItemID.PLANTPOT_DRAGONFRUIT_SAPLING,false).quantity);
        assertEquals(3,row(plan,ItemID.PLANTPOT_MAHOGANY_SAPLING,false).quantity);
        assertEquals(1,row(plan,ItemID.PLANTPOT_CALQUAT_SAPLING,false).quantity);
        assertEquals(240,row(plan,ItemID.COCONUT,true).quantity);
        assertEquals(75,row(plan,ItemID.YANILLIAN_HOPS,true).quantity);
        assertEquals(8,row(plan,ItemID.POISONIVY_BERRIES,true).quantity);
        assertFalse(plan.items.contains(ItemID.PLANTPOT_YEW_SAPLING));
        assertFalse(plan.items.contains(ItemID.BUCKET_ULTRACOMPOST));
        assertTrue(plan.items.contains(ItemID.COINS));
    }

    @Test public void basketsAreNotLooseProduceAndNoGardenerMeansNoPayment()
    {
        ConfigFixture f=new ConfigFixture();f.set("cropAllotment","TOMATO");
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.LUMBRIDGE_TREE,Patch.FALADOR_ALLOTMENT_NW,Patch.HARMONY_ALLOTMENT));
        assertEquals(1,row(plan,ItemID.BASKET_TOMATO_5,true).quantity);
        assertEquals(2,row(plan,ItemID.SACK_CABBAGE_10,true).quantity);
        assertEquals(6,row(plan,ItemID.TOMATO_SEED,false).quantity);
        assertFalse(plan.items.contains(ItemID.TOMATO));
    }

    @Test public void hopsUseThreeOrFourSeedsAndSpiritTreesHaveThreePaymentItems()
    {
        ConfigFixture f=new ConfigFixture();
        Patch hop=java.util.Arrays.stream(Patch.values()).filter(p->p.getType()==Patch.Type.HOPS).findFirst().orElseThrow();
        f.set("cropHops","HEMP");
        assertEquals(3,row(SupplyPlan.build(f.config,List.of(hop)),ItemID.HEMP_SEED,false).quantity);
        f.set("cropHops","WILDBLOOD");
        assertEquals(4,row(SupplyPlan.build(f.config,List.of(hop)),ItemID.WILDBLOOD_HOP_SEED,false).quantity);
        Patch spirit=java.util.Arrays.stream(Patch.values()).filter(p->p.getType()==Patch.Type.SPIRIT).findFirst().orElseThrow();
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(spirit));
        assertEquals(5,row(plan,ItemID.MM_MONKEY_NUTS,true).quantity);
        assertEquals(1,row(plan,ItemID.MM_MONKEY_BAR,true).quantity);
        assertEquals(1,row(plan,ItemID.LUNAR_GROUNDTOOTH,true).quantity);
    }

    @Test public void allCropFamiliesHaveHarvestOnlyAndValidPlantingChoices() throws Exception
    {
        ClassicTreeRunConfig config=new ClassicTreeRunConfig() { };
        int families=0,crops=0;
        for(Method method:ClassicTreeRunConfig.class.getDeclaredMethods())
        {
            if(!method.getName().startsWith("crop"))continue;
            families++;
            CropChoices.Choice[] values=(CropChoices.Choice[])method.getReturnType().getEnumConstants();
            assertEquals(-1,values[0].crop().item);
            for(int i=1;i<values.length;i++)
            {
                CropChoices.Crop crop=values[i].crop();assertTrue(crop.item>0);assertTrue(crop.quantity>0);
                for(CropChoices.Payment payment:crop.payments){assertTrue(payment.item>0);assertTrue(payment.quantity>0);}
                crops++;
            }
            assertNotNull(method.invoke(config));
        }
        assertEquals(21,families);assertEquals(82,crops);
        for(Patch.Type type:Patch.Type.values())assertNotNull(CropChoices.forType(type,config));
        assertEquals(65,CropChoices.Herb.HUASCA.crop().level);
    }

    @Test public void optionalPaymentsCanBeOmittedWithoutDroppingCoinsOrSeeds()
    {
        ConfigFixture f=new ConfigFixture();f.set("supplyProtection","false");f.set("supplyCoins","25000");
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.LUMBRIDGE_TREE));
        assertFalse(plan.items.contains(ItemID.BASKET_TOMATO_5));
        assertTrue(plan.items.contains(ItemID.PLANTPOT_OAK_SAPLING));
        assertTrue(plan.items.contains(ItemID.BUCKET_ULTRACOMPOST));
        assertEquals(25000,row(plan,ItemID.COINS,false).quantity);
    }

    @Test public void notedSaplingsNeverCountButNotedProtectionDoes()
    {
        SupplyPlan plan=SupplyPlan.build(new ClassicTreeRunConfig() { },List.of(Patch.LUMBRIDGE_TREE));
        SupplySnapshot snapshot=new SupplySnapshot(true,Map.of(),Map.of(ItemID.PLANTPOT_OAK_SAPLING,10L,ItemID.BASKET_TOMATO_5,3L));
        List<Long> counts=plan.carried(snapshot);
        assertEquals(Long.valueOf(0),counts.get(plan.requirements.indexOf(row(plan,ItemID.PLANTPOT_OAK_SAPLING,false))));
        assertEquals(Long.valueOf(3),counts.get(plan.requirements.indexOf(row(plan,ItemID.BASKET_TOMATO_5,true))));
    }

    @Test public void compostCannotBeCountedTwiceForPlantingAndPayment()
    {
        ConfigFixture f=new ConfigFixture();f.set("supplyCompost","COMPOST");
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.FALADOR_ALLOTMENT_NW));
        SupplySnapshot snapshot=new SupplySnapshot(true,Map.of(ItemID.BUCKET_COMPOST,2L),Map.of());
        List<Long> counts=plan.carried(snapshot);
        assertEquals(Long.valueOf(2),counts.get(plan.requirements.indexOf(row(plan,ItemID.BUCKET_COMPOST,false))));
        assertEquals(Long.valueOf(1),counts.get(plan.requirements.indexOf(row(plan,ItemID.BUCKET_COMPOST,true))));
        assertEquals(2,row(plan,ItemID.BUCKET_COMPOST,true).quantity);
    }

    @Test public void bottomlessPacksOneFilledBucketAndNeverTreatsAnEmptyBucketAsCompost()
    {
        ConfigFixture f=new ConfigFixture();f.set("supplyCompost","BOTTOMLESS");
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.TROLL_HERB,Patch.WEISS_HERB));
        SupplyPlan.Requirement bucket=row(plan,ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED,false);
        assertEquals(1,bucket.quantity);
        assertTrue(plan.items.contains(ItemID.BOTTOMLESS_COMPOST_BUCKET));
        assertFalse(plan.items.contains(ItemID.BUCKET_ULTRACOMPOST));
        assertTrue(plan.reminders.stream().anyMatch(text->text.contains("2 remaining patches") && text.contains("Charges are not counted")));
        int index=plan.requirements.indexOf(bucket);
        assertEquals(Long.valueOf(0),plan.carried(new SupplySnapshot(true,Map.of(ItemID.BOTTOMLESS_COMPOST_BUCKET,1L),Map.of())).get(index));
        assertEquals(Long.valueOf(1),plan.carried(new SupplySnapshot(true,Map.of(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED,1L),Map.of())).get(index));
        assertEquals(Long.valueOf(0),plan.carried(new SupplySnapshot(true,Map.of(),Map.of(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED,1L))).get(index));
    }

    @Test public void bottomlessDoesNotReplaceProtectionPaymentsOrAppearWithoutEligiblePatches()
    {
        ConfigFixture f=new ConfigFixture();f.set("supplyCompost","BOTTOMLESS");
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.FALADOR_ALLOTMENT_NW));
        assertEquals(1,row(plan,ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED,false).quantity);
        assertEquals(2,row(plan,ItemID.BUCKET_COMPOST,true).quantity);
        for(List<Patch> patches:List.of(List.<Patch>of(),List.of(Patch.CORAL_E),List.of(Patch.LUMBRIDGE_TREE)))
        {
            SupplyPlan noCompost=SupplyPlan.build(f.config,patches);
            assertFalse(noCompost.items.contains(ItemID.BOTTOMLESS_COMPOST_BUCKET));
            assertFalse(noCompost.items.contains(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED));
        }
        f.set("cropHerb","NONE");
        assertFalse(SupplyPlan.build(f.config,List.of(Patch.TROLL_HERB)).items.contains(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED));
    }

    @Test public void coralCannotReceiveCompostAndGrapesNeedSaltpetreAndTrowel()
    {
        ConfigFixture f=new ConfigFixture();f.set("cropCoral","UMBRAL");
        SupplyPlan coral=SupplyPlan.build(f.config,List.of(Patch.CORAL_E,Patch.CORAL_W));
        assertFalse(coral.items.contains(ItemID.BUCKET_ULTRACOMPOST));
        assertEquals(10,row(coral,ItemID.CORAL_PILLAR,true).quantity);
        List<Patch> grapes=java.util.Arrays.stream(Patch.values()).filter(p->p.getType()==Patch.Type.GRAPE).collect(java.util.stream.Collectors.toList());
        SupplyPlan plan=SupplyPlan.build(f.config,grapes);
        assertEquals(12,row(plan,ItemID.GRAPE_SEED,false).quantity);
        assertEquals(12,row(plan,ItemID.HOSIDIUS_SALTPETRE,false).quantity);
        assertTrue(plan.items.contains(ItemID.GARDENING_TROWEL));
        Patch celastrus=java.util.Arrays.stream(Patch.values()).filter(p->p.getType()==Patch.Type.CELASTRUS).findFirst().orElseThrow();
        SupplyPlan special=SupplyPlan.build(f.config,List.of(celastrus));
        assertEquals(8,row(special,ItemID.CACTUS_POTATO,true).quantity);
        assertEquals(1,row(special,ItemID.BUCKET_ULTRACOMPOST,false).quantity);
        assertTrue(special.items.contains(ItemID.FAIRY_ENCHANTED_SECATEURS));
    }

    @Test public void harvestOnlyHasNoReplantingCostsAndEmptyRunHasNoRequirements()
    {
        ConfigFixture f=new ConfigFixture();f.set("cropTree","NONE");
        SupplyPlan plan=SupplyPlan.build(f.config,List.of(Patch.LUMBRIDGE_TREE));
        assertFalse(plan.items.contains(ItemID.PLANTPOT_OAK_SAPLING));assertFalse(plan.items.contains(ItemID.BASKET_TOMATO_5));
        assertTrue(SupplyPlan.build(f.config,List.of()).requirements.isEmpty());
        f.set("cropHerb","NONE");plan=SupplyPlan.build(f.config,List.of(Patch.TROLL_HERB));
        assertFalse(plan.items.contains(ItemID.DIBBER));
    }

    @Test public void sidebarShowsShortagesInRedAndStartClearButtonsAreDisplayActions() throws Exception
    {
        SwingUtilities.invokeAndWait(()->{
            ConfigFixture f=new ConfigFixture();f.set("loadPlanning","false");f.set("selectionV2","TROLL_HERB,WEISS_HERB");f.set("cropHerb","RANARR");
            ClassicTreeRunPanel panel=new ClassicTreeRunPanel(f.config,f);panel.setConfirmReset(false);
            panel.setSupplies(new SupplySnapshot(true,Map.of(ItemID.RANARR_SEED,1L),Map.of()));
            JLabel label=findLabel(panel,"Ranarr seed");assertNotNull(label);assertEquals("2 x Ranarr seed",label.getText());
            assertTrue(label.getToolTipText().contains("Short 1"));
            assertEquals(new Color(255,115,115),label.getForeground());
            final SupplyPlan[] started={null};final boolean[] cleared={false};
            panel.setSupplyActions(plan->started[0]=plan,()->cleared[0]=true);
            ((AbstractButton)find(panel,"startRun")).doClick();assertNotNull(started[0]);
            assertEquals(2,row(started[0],ItemID.RANARR_SEED,false).quantity);
            ((AbstractButton)find(panel,"TROLL_HERB")).doClick();
            label=findLabel(panel,"Ranarr seed");assertEquals("1 x Ranarr seed",label.getText());
            assertTrue(label.getToolTipText().contains("Carried: 1 / 1"));
            assertEquals(new Color(103,188,116),label.getForeground());
            panel.setBankFilterActive(true);((AbstractButton)find(panel,"clearBankFilter")).doClick();assertTrue(cleared[0]);
        });
    }

    private static Component find(Container parent,String name)
    {
        for(Component child:parent.getComponents())
        {if(name.equals(child.getName()))return child;if(child instanceof Container){Component found=find((Container)child,name);if(found!=null)return found;}}
        return null;
    }
    private static JLabel findLabel(Container parent,String prefix)
    {
        for(Component child:parent.getComponents())
        {if(child instanceof JLabel && ((JLabel)child).getText().contains(prefix))return (JLabel)child;
            if(child instanceof Container){JLabel found=findLabel((Container)child,prefix);if(found!=null)return found;}}
        return null;
    }
}
