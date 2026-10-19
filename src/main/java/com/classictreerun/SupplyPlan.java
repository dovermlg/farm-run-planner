package com.classictreerun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/** Display-only packing list for the chosen crops. Does not inspect or change game actions. */
public final class SupplyPlan
{
    public enum Compost
    {
        NONE, COMPOST, SUPERCOMPOST, ULTRACOMPOST, BOTTOMLESS;
        @Override public String toString()
        {
            if(this==BOTTOMLESS)return "Bottomless compost bucket";
            return this==NONE ? "No compost" : name().substring(0,1)+name().substring(1).toLowerCase(java.util.Locale.ROOT);
        }
    }

    static final class Requirement
    {
        final String label;
        final int item, quantity;
        final boolean notes;
        final Set<Integer> alternatives;
        Requirement(String label, int item, int quantity, boolean notes, Integer... alternatives)
        {
            this.label=label; this.item=item; this.quantity=quantity; this.notes=notes;
            java.util.HashSet<Integer> ids=new java.util.HashSet<>(List.of(alternatives));
            ids.add(item); this.alternatives=Set.copyOf(ids);
        }
    }

    final List<Requirement> requirements;
    final Set<Integer> items;
    final List<String> reminders;

    private SupplyPlan(List<Requirement> requirements, List<String> reminders)
    {
        this.requirements=List.copyOf(requirements); this.reminders=List.copyOf(reminders);
        java.util.HashSet<Integer> ids=new java.util.HashSet<>();
        for(Requirement requirement:requirements)
        {
            ids.addAll(requirement.alternatives);
            // Let players find an empty bucket to fill, without counting it as usable compost.
            if(requirement.item==ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED)ids.add(ItemID.BOTTOMLESS_COMPOST_BUCKET);
        }
        items=Set.copyOf(ids);
    }

    static SupplyPlan build(ClassicTreeRunConfig config, List<Patch> patches)
    {
        List<Requirement> result=new ArrayList<>();
        List<String> reminders=new ArrayList<>();
        Map<Integer,Requirement> planting=new LinkedHashMap<>(), payments=new LinkedHashMap<>();
        boolean seeds=false, secateurs=false, grapes=false, belladonna=false, coral=false, axe=false;
        int compost=0;
        for(Patch patch:patches)
        {
            Patch.Type type=patch.getType();
            CropChoices.Crop crop=CropChoices.forType(type,config);
            boolean tree=Set.of(Patch.Type.TREE,Patch.Type.FRUIT,Patch.Type.HARDWOOD,Patch.Type.CALQUAT,
                Patch.Type.SPIRIT,Patch.Type.CRYSTAL,Patch.Type.CELASTRUS,Patch.Type.REDWOOD).contains(type);
            if(crop.item!=-1)
            {
                String name=crop.label+(tree ? " sapling" : type==Patch.Type.CORAL ? " frag" : " seed");
                if(type==Patch.Type.SEAWEED)name="Seaweed spore";
                if(type==Patch.Type.MUSHROOM)name="Mushroom spore";
                if(type==Patch.Type.HERB && crop.item==ItemID.VILLAGE_RARE_TUBER)name="Gout tuber";
                add(planting,name,crop.item,crop.quantity,false);
                seeds|=!tree && type!=Patch.Type.CORAL && crop.item!=ItemID.VILLAGE_RARE_TUBER;
                if(config.supplyProtection() && !patch.getGardener().startsWith("No "))
                {
                    for(CropChoices.Payment payment:crop.payments)
                        add(payments,payment.label+" (optional protection)",payment.item,payment.quantity,true);
                }
                boolean immune=Set.of(Patch.Type.CORAL,Patch.Type.ANIMA,Patch.Type.HESPORI,Patch.Type.GRAPE).contains(type)
                    || crop.item==ItemID.POISONIVY_BUSH_SEED;
                // Paid tree protection prevents disease. Compost remains useful for variable-yield crops.
                boolean paidTree=tree && type!=Patch.Type.CELASTRUS && config.supplyProtection() && !crop.payments.isEmpty();
                if(!immune && !paidTree)compost++;
                if(type==Patch.Type.GRAPE)add(planting,"Saltpetre",ItemID.HOSIDIUS_SALTPETRE,1,false);
            }
            secateurs|=Set.of(Patch.Type.HERB,Patch.Type.ALLOTMENT,Patch.Type.HOPS,Patch.Type.BUSH,
                Patch.Type.GRAPE,Patch.Type.CELASTRUS).contains(type);
            grapes|=type==Patch.Type.GRAPE; belladonna|=type==Patch.Type.BELLADONNA;
            coral|=type==Patch.Type.CORAL;
            axe|=Set.of(Patch.Type.TREE,Patch.Type.FRUIT,Patch.Type.HARDWOOD,Patch.Type.SPIRIT,
                Patch.Type.CRYSTAL,Patch.Type.CELASTRUS,Patch.Type.REDWOOD).contains(type);
        }
        result.addAll(planting.values());
        if(compost>0 && config.supplyCompost()!=Compost.NONE)
        {
            if(config.supplyCompost()==Compost.BOTTOMLESS)
            {
                result.add(new Requirement("Filled bottomless bucket (presence only)",ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED,1,false));
                reminders.add("Bottomless bucket: check its compost type and charges for "+compost+" remaining patches. Charges are not counted.");
            }
            else
            {
                int item=config.supplyCompost()==Compost.COMPOST ? ItemID.BUCKET_COMPOST
                    : config.supplyCompost()==Compost.SUPERCOMPOST ? ItemID.BUCKET_SUPERCOMPOST : ItemID.BUCKET_ULTRACOMPOST;
                result.add(new Requirement(config.supplyCompost().name().toLowerCase(java.util.Locale.ROOT)+" buckets",item,compost,false));
            }
        }
        if(!patches.isEmpty())
        {
            if(config.supplyTools())
            {
                result.add(new Requirement("Spade",ItemID.SPADE,1,false));
                if(secateurs)result.add(new Requirement("Secateurs (magic recommended)",ItemID.FAIRY_ENCHANTED_SECATEURS,1,false,ItemID.SECATEURS));
                if(grapes)result.add(new Requirement("Gardening trowel",ItemID.GARDENING_TROWEL,1,false));
                if(axe)result.add(new Requirement("Axe (optional if paying for removal)",ItemID.BRONZE_AXE,1,false,
                    ItemID.IRON_AXE,ItemID.STEEL_AXE,ItemID.BLACK_AXE,ItemID.MITHRIL_AXE,ItemID.ADAMANT_AXE,
                    ItemID.RUNE_AXE,ItemID.DRAGON_AXE,ItemID.INFERNAL_AXE,ItemID.INFERNAL_AXE_EMPTY,ItemID.CRYSTAL_AXE,
                    ItemID.BRONZE_AXE_2H,ItemID.IRON_AXE_2H,ItemID.STEEL_AXE_2H,ItemID.BLACK_AXE_2H,
                    ItemID.MITHRIL_AXE_2H,ItemID.ADAMANT_AXE_2H,ItemID.RUNE_AXE_2H,ItemID.DRAGON_AXE_2H,ItemID.CRYSTAL_AXE_2H));
            }
            if(config.supplyRake())result.add(new Requirement("Rake",ItemID.RAKE,1,false));
            if(config.supplyDibber() && seeds)result.add(new Requirement("Seed dibber",ItemID.DIBBER,1,false));
            if(config.supplyCoins()>0)result.add(new Requirement("Coins (your budget)",ItemID.COINS,config.supplyCoins(),false));
            if(axe)reminders.add("Pack an axe if chopping trees yourself; bring extra coins for gardener removal and fares.");
            if(belladonna)reminders.add("Wear suitable gloves before harvesting belladonna.");
            if(coral)reminders.add("Coral nurseries require diving equipment or a medallion of the deep.");
            if(patches.stream().anyMatch(p->p.getType()==Patch.Type.HESPORI))reminders.add("Bring your own Hespori combat equipment.");
        }
        // Unnoted consumables are allocated first; noted protection cannot satisfy a planting requirement.
        result.addAll(payments.values());
        return new SupplyPlan(result,reminders);
    }

    private static void add(Map<Integer,Requirement> rows,String label,int item,int quantity,boolean notes)
    {
        Requirement previous=rows.get(item);
        rows.put(item,new Requirement(label,item,quantity+(previous==null?0:previous.quantity),notes));
    }

    /** Allocates shared supplies once (e.g. compost used both on a patch and as payment). */
    List<Long> carried(SupplySnapshot snapshot)
    {
        Map<Integer,Long> plain=new LinkedHashMap<>(snapshot.plain), notes=new LinkedHashMap<>(snapshot.notes);
        List<Long> counts=new ArrayList<>();
        for(Requirement row:requirements)
        {
            long available=0;
            for(int id:row.alternatives)
            {
                available+=plain.getOrDefault(id,0L);
                if(row.notes)available+=notes.getOrDefault(id,0L);
            }
            counts.add(available);
            long needed=row.quantity;
            if(row.notes)needed=consume(notes,row.alternatives,needed);
            consume(plain,row.alternatives,needed);
        }
        return Collections.unmodifiableList(counts);
    }

    private static long consume(Map<Integer,Long> counts,Set<Integer> ids,long needed)
    {
        for(int id:ids)
        {
            long used=Math.min(needed,counts.getOrDefault(id,0L));
            counts.put(id,counts.getOrDefault(id,0L)-used);needed-=used;
        }
        return needed;
    }
}
