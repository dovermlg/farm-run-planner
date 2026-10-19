package com.classictreerun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Splits the prepared patch order into loads. No game actions or live pathfinding. */
final class InventoryLoadPlanner
{
    static final int BACKPACK_SLOTS=28;
    static final String BANKS_KEY="bankedInventoriesV1";
    static final class Load
    {
        final List<Patch> patches;
        final int slots;
        Load(List<Patch> patches,int slots) { this.patches=List.copyOf(patches);this.slots=slots; }
    }
    static final class Plan
    {
        final String signature;
        final List<Load> loads;
        final int capacity;
        Plan(String signature,List<Load> loads,int capacity)
        { this.signature=signature;this.loads=List.copyOf(loads);this.capacity=capacity; }
        int current(RouteState state)
        {
            for(int i=0;i<loads.size();i++)if(loads.get(i).patches.stream().anyMatch(p->!state.isDone(p)))return i;
            return -1;
        }
        boolean bankConfirmed(String record,int load)
        {
            if(load<=0)return true;
            if(record==null || !record.startsWith(signature+":"))return false;
            return RouteState.parse(record.substring(signature.length()+1)).contains(loads.get(load).patches.get(0));
        }
        String confirmBank(String record,int load)
        {
            java.util.Set<Patch> confirmed=new java.util.LinkedHashSet<>();
            if(record!=null && record.startsWith(signature+":"))confirmed.addAll(RouteState.parse(record.substring(signature.length()+1)));
            confirmed.add(loads.get(load).patches.get(0));return signature+":"+RouteState.encode(confirmed);
        }
        String reopenPatch(String record,Patch patch)
        {
            if(record==null || !record.startsWith(signature+":"))return "";
            java.util.Set<Patch> confirmed=new java.util.LinkedHashSet<>(RouteState.parse(record.substring(signature.length()+1)));
            boolean later=false;
            for(Load load:loads)
            {
                if(later)confirmed.remove(load.patches.get(0));
                if(load.patches.contains(patch))later=true;
            }
            return signature+":"+RouteState.encode(confirmed);
        }
        int startsAt(Patch patch)
        {
            for(int i=0;i<loads.size();i++)if(loads.get(i).patches.get(0)==patch)return i;
            return -1;
        }
    }

    static int reserved(ClassicTreeRunConfig config) { return Math.max(0,Math.min(27,config.loadReservedSlots())); }
    static String customInstructions(String text)
    {
        String clean=text==null?"":text.trim();
        if(clean.isEmpty())return "Travel to your chosen bank and resupply manually.";
        return clean.substring(0,Math.min(500,clean.length()));
    }
    static Map<String,String> settings(ClassicTreeRunConfig config)
    {
        Map<String,String> values=new LinkedHashMap<>();
        values.put("loadPlanning",Boolean.toString(config.loadPlanning()));
        values.put("loadReservedSlots",Integer.toString(reserved(config)));
        values.put("loadNotedProtection",Boolean.toString(config.loadNotedProtection()));
        values.put("loadBank",config.loadBank().name());
        values.put("loadBankInstructions",customInstructions(config.loadBankInstructions()));
        return values;
    }
    static void validate(String key,String value)
    {
        switch(key)
        {
            case "loadPlanning":case "loadNotedProtection":
                if(!("true".equals(value)||"false".equals(value)))throw new IllegalArgumentException("Invalid inventory option");return;
            case "loadReservedSlots":
                if(!value.matches("[0-9]{1,2}") || Integer.parseInt(value)>27)throw new IllegalArgumentException("Reserve between 0 and 27 slots");return;
            case "loadBank":BankDestination.valueOf(value);return;
            case "loadBankInstructions":if(value.length()>500)throw new IllegalArgumentException("Bank instructions must be at most 500 characters");return;
            default:throw new IllegalArgumentException("Unknown inventory option");
        }
    }

    static SupplyPlan supplies(ClassicTreeRunConfig config,List<Patch> patches,InventoryItemData data)
    { return SupplyPlan.build(config,patches).forInventory(config.loadNotedProtection(),data); }

    static int slots(SupplyPlan plan,InventoryItemData data)
    {
        Map<Integer,Long> loose=new HashMap<>(),noted=new HashMap<>();
        for(SupplyPlan.Requirement row:plan.requirements)
            (row.notedOnly?noted:loose).merge(row.item,(long)row.quantity,Long::sum);
        long slots=noted.size();
        for(Map.Entry<Integer,Long> row:loose.entrySet())slots+=data.get(row.getKey()).stackable?1:row.getValue();
        return (int)Math.min(Integer.MAX_VALUE,slots);
    }

    /** Adds each patch once per candidate load; shared tools/buckets are counted once per load. */
    static final class SlotCounter
    {
        private final InventoryItemData data;
        private final Map<Integer,Long> consumable=new HashMap<>(),reusable=new HashMap<>();
        private final java.util.Set<Integer> notes=new java.util.HashSet<>();
        private long occupied;
        SlotCounter(InventoryItemData data) { this.data=data; }
        int add(SupplyPlan patch)
        {
            for(SupplyPlan.Requirement row:patch.requirements)
            {
                if(row.notedOnly){if(notes.add(row.item))occupied++;continue;}
                long before=consumable.getOrDefault(row.item,0L)+reusable.getOrDefault(row.item,0L);
                if(row.reusable)reusable.merge(row.item,(long)row.quantity,Math::max);
                else consumable.merge(row.item,(long)row.quantity,Long::sum);
                long after=consumable.getOrDefault(row.item,0L)+reusable.getOrDefault(row.item,0L);
                occupied+=data.get(row.item).stackable?(before==0 && after>0?1:0):after-before;
            }
            return (int)Math.min(Integer.MAX_VALUE,occupied);
        }
    }

    static String signature(ClassicTreeRunConfig config,List<Patch> route,InventoryItemData data)
    {
        Map<String,String> travel=new java.util.TreeMap<>();
        travel.putAll(TravelCosts.settings(config));
        String source=RouteState.encode(route)+"\n"+CropChoices.settings(config)+"\n"+settings(config)+"\n"+travel+"\n"
            +data.fingerprint(SupplyPlan.build(config,route).items);
        return Digests.sha256(source);
    }

    static Plan plan(ClassicTreeRunConfig config,List<Patch> route,InventoryItemData data)
    {
        String signature=signature(config,route,data);
        int n=route.size(),capacity=BACKPACK_SLOTS-reserved(config);
        int[] count=new int[n+1],cost=new int[n+1],next=new int[n],occupied=new int[n];
        java.util.Arrays.fill(count,Integer.MAX_VALUE);java.util.Arrays.fill(cost,Integer.MAX_VALUE);count[n]=0;cost[n]=0;
        BankDestination bank=config.loadBank();
        List<SupplyPlan> perPatch=new ArrayList<>();
        for(Patch patch:route)perPatch.add(supplies(config,List.of(patch),data));
        for(int i=n-1;i>=0;i--)
        {
            SlotCounter counter=new SlotCounter(data);
            for(int j=i+1;j<=n;j++)
            {
                int slots=counter.add(perPatch.get(j-1));
                if(slots>capacity && j>i+1)break;
                int loads=1+count[j];
                int boundary=j==n?0:bank.approachCost(route.get(j-1))+bank.departureCost(route.get(j),config)
                    -Math.min(TravelCosts.arrival(route.get(j),config),TravelCosts.walking(route.get(j-1),route.get(j),config));
                int travel=cost[j]+boundary;
                // Prefer fewer loads, then cheaper banking boundaries, then a fuller first inventory.
                if(loads<count[i] || loads==count[i] && (travel<cost[i] || travel==cost[i] && j>next[i]))
                {count[i]=loads;cost[i]=travel;next[i]=j;occupied[i]=slots;}
                if(slots>capacity)break; // An oversized single-patch load is retained and visibly flagged.
            }
        }
        List<Load> loads=new ArrayList<>();
        for(int i=0;i<n;i=next[i])loads.add(new Load(route.subList(i,next[i]),occupied[i]));
        return new Plan(signature,loads,capacity);
    }
}
