package com.classictreerun;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.gameval.ItemID;

/** Packing requirements follow the same walking links and arrivals as the route cards. */
final class TravelSupplies
{
    private TravelSupplies() { }

    static SupplyPlan build(ClassicTreeRunConfig config,List<Patch> route,boolean fromBank,boolean returnToBank)
    { return build(config,route,patch->true,fromBank,returnToBank,null); }

    static SupplyPlan build(ClassicTreeRunConfig config,List<Patch> route,Predicate<Patch> unfinished,
        boolean fromBank,boolean returnToBank,Patch bankFrom)
    {
        Counter counter=new Counter(config);
        if(!config.supplyTeleports() || route.stream().noneMatch(unfinished))return counter.finish();
        if(bankFrom!=null)counter.bank(bankFrom);
        Patch previous=null;
        for(Patch patch:route)
        {
            boolean bankWalk=previous==null && fromBank && config.loadBank().departureInstructions(patch,config)!=null;
            if(unfinished.test(patch) && !bankWalk && TravelCosts.walkingInstruction(previous,patch,config)==null)
                counter.arrival(patch);
            previous=patch;
        }
        if(returnToBank)counter.bank(route.get(route.size()-1));
        return counter.finish();
    }

    /** A bank boundary can break any walking link, so cache every independent arrival too. */
    static Set<Integer> requiredItems(ClassicTreeRunConfig config,List<Patch> route)
    {
        Set<Integer> ids=new java.util.HashSet<>();
        if(!config.supplyTeleports())return ids;
        for(Patch patch:route)ids.addAll(build(config,List.of(patch),false,false).items);
        if(config.loadPlanning() && route.size()>1)
        {
            Counter counter=new Counter(config);counter.device(NextTeleport.bank(config));
            ids.addAll(counter.finish().items);
        }
        return ids;
    }

    static Map<Integer,Integer> spellCost(TeleportDevice device)
    {
        switch(device)
        {
            case LUMBRIDGE:return Map.of(ItemID.AIRRUNE,3,ItemID.EARTHRUNE,1,ItemID.LAWRUNE,1);
            case VARROCK:return Map.of(ItemID.AIRRUNE,3,ItemID.FIRERUNE,1,ItemID.LAWRUNE,1);
            case FALADOR:return Map.of(ItemID.AIRRUNE,3,ItemID.WATERRUNE,1,ItemID.LAWRUNE,1);
            case CAMELOT:return Map.of(ItemID.AIRRUNE,5,ItemID.LAWRUNE,1);
            case CATHERBY:return Map.of(ItemID.WATERRUNE,10,ItemID.ASTRALRUNE,3,ItemID.LAWRUNE,3);
            case ARDOUGNE:return Map.of(ItemID.WATERRUNE,2,ItemID.LAWRUNE,2);
            case HARMONY:return Map.of(ItemID.NATURERUNE,1,ItemID.SOULRUNE,1,ItemID.LAWRUNE,1);
            case DRAYNOR_MANOR:return Map.of(ItemID.WATERRUNE,1,ItemID.EARTHRUNE,1,ItemID.LAWRUNE,1);
            case FORTIS:return Map.of(ItemID.EARTHRUNE,1,ItemID.FIRERUNE,1,ItemID.LAWRUNE,2);
            default:return Map.of(); // Redirected house tablets stay destination-specific tablets.
        }
    }

    private static boolean fairyRing(Patch patch,ClassicTreeRunConfig config)
    {
        if(patch==Patch.TAI_BWO_WANNAI_CALQUAT)
            return config.calquatTravel()==ClassicTreeRunConfig.CalquatTravel.FAIRY_RING_CKR;
        switch(patch.getSite())
        {
            case NEMUS:case AUBURNVALE:case LOCUS:case CONCH:case CORAL:
            case SEERS_HOPS:case ALDARIN:case VINERY:case CANIFIS:return true;
            default:return false;
        }
    }

    private static String runeName(int item)
    {
        switch(item)
        {
            case ItemID.AIRRUNE:return "Air rune";
            case ItemID.WATERRUNE:return "Water rune";
            case ItemID.EARTHRUNE:return "Earth rune";
            case ItemID.FIRERUNE:return "Fire rune";
            case ItemID.LAWRUNE:return "Law rune";
            case ItemID.ASTRALRUNE:return "Astral rune";
            case ItemID.NATURERUNE:return "Nature rune";
            case ItemID.SOULRUNE:return "Soul rune";
            default:throw new IllegalArgumentException("Unknown travel rune");
        }
    }

    private static final class Counter
    {
        private final ClassicTreeRunConfig config;
        private final Map<TeleportDevice,Integer> uses=new EnumMap<>(TeleportDevice.class);
        private final Set<String> reminders=new LinkedHashSet<>();
        private boolean fairy;
        Counter(ClassicTreeRunConfig config) { this.config=config; }
        void device(TeleportDevice device)
        { if(device!=TeleportDevice.NONE)uses.merge(device,1,Integer::sum); }
        void bank(Patch from)
        {
            if(config.loadBank().local(from))return;
            device(NextTeleport.bank(config));
            if(config.loadBank()==BankDestination.CUSTOM)reminders.add("Pack your own travel supplies for the custom bank.");
        }
        void arrival(Patch patch)
        {
            device(NextTeleport.arrival(patch,config));
            fairy|=fairyRing(patch,config);
            if(NextTeleport.arrival(patch,config)==TeleportDevice.NONE)
                reminders.add("Arrange access to the transport network in the route directions; travel to its starting point is not calculated.");
            if(patch==Patch.TAVERLEY_TREE && config.taverleyTravel()==ClassicTreeRunConfig.TaverleyTravel.BALLOON)
                reminders.add("Pack the fuel for your chosen balloon departure point and check its weight restrictions.");
        }
        SupplyPlan finish()
        {
            List<SupplyPlan.Requirement> rows=new ArrayList<>();
            Map<Integer,Integer> runes=new java.util.TreeMap<>();
            Set<String> spellbooks=new LinkedHashSet<>();
            // One higher-tier cloak can cover farm and monastery arrivals.
            if(uses.containsKey(TeleportDevice.ARDOUGNE_FARM) && uses.containsKey(TeleportDevice.ARDOUGNE_MONASTERY))
                uses.merge(TeleportDevice.ARDOUGNE_FARM,uses.remove(TeleportDevice.ARDOUGNE_MONASTERY),Integer::sum);
            for(Map.Entry<TeleportDevice,Integer> entry:uses.entrySet())
            {
                TeleportDevice device=entry.getKey();int count=entry.getValue();
                Map<Integer,Integer> cost=spellCost(device);
                if(config.supplyTeleportMode()==ClassicTreeRunConfig.TeleportSupplies.SPELL_RUNES && !cost.isEmpty())
                {
                    cost.forEach((id,quantity)->runes.merge(id,quantity*count,Integer::sum));
                    spellbooks.add(device==TeleportDevice.CATHERBY?"Lunar":device==TeleportDevice.HARMONY
                        || device==TeleportDevice.DRAYNOR_MANOR?"Arceuus":"Standard");
                    continue;
                }
                boolean consumable=device.consumable();
                String detail=consumable ? count+" use(s) for the unfinished route and required bank travel."
                    : "Presence only: "+count+" planned teleport(s). Check charges, stored charges, daily limits and destination unlocks yourself.";
                rows.add(SupplyPlan.Requirement.travel(device.label,device.representative(),consumable?count:1,
                    !consumable,detail,device.items()));
            }
            for(Map.Entry<Integer,Integer> rune:runes.entrySet())
                rows.add(SupplyPlan.Requirement.travel(runeName(rune.getKey()),rune.getKey(),rune.getValue(),false,
                    "Ordinary loose runes only. Rune pouches, combination runes and rune-saving equipment are not counted.",Set.of(rune.getKey())));
            if(!spellbooks.isEmpty())reminders.add("Spell runes require access to the correct spellbook(s): "+String.join(", ",spellbooks)
                +". Check levels and quest unlocks. Different spellbooks require your own switching method.");
            if(fairy && config.supplyFairyStaff())
                rows.add(SupplyPlan.Requirement.travel("Fairy ring staff",ItemID.DRAMEN_STAFF,1,true,
                    "Dramen or lunar staff. Disable Include fairy ring staff in the cog if your account is exempt.",
                    Set.of(ItemID.DRAMEN_STAFF,ItemID.LUNAR_MOONCLAN_LIMINAL_STAFF)));
            if(fairy)reminders.add("Arrange access to a fairy ring before using the destination code.");
            if(uses.keySet().stream().anyMatch(device->!device.consumable()))
                reminders.add("Teleport equipment checks possession only; green does not confirm enough charges or daily teleports. Chronicle cards, talisman charges and whistle charges stay inside their devices.");
            return new SupplyPlan(rows,new ArrayList<>(reminders));
        }
    }
}
