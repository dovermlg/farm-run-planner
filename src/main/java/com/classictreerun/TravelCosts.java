package com.classictreerun;

import java.util.List;

/** Relative travel-effort scores, NOT measured seconds, tiles, or live pathfinding. */
final class TravelCosts
{
    final int[] entry;
    final int[][] travel;

    static java.util.Map<String,String> settings(ClassicTreeRunConfig config)
    {
        java.util.Map<String,String> values=new java.util.LinkedHashMap<>();
        values.put("faladorTravel",config.faladorTravel().name());
        values.put("taverleyTravel",config.taverleyTravel().name());
        values.put("strongholdTravel",config.strongholdTravel().name());
        values.put("catherbyTravel",config.catherbyTravel().name());
        values.put("brimhavenTravel",config.brimhavenTravel().name());
        values.put("calquatTravel",config.calquatTravel().name());
        values.put("fossilTravel",config.fossilTravel().name());
        values.put("guildTravel",config.guildTravel().name());
        return values;
    }

    TravelCosts(List<Patch> patches, ClassicTreeRunConfig config)
    {
        int n = patches.size();
        entry = new int[n];
        travel = new int[n][n];
        for (int i = 0; i < n; i++)
        {
            entry[i] = arrival(patches.get(i), config);
            for (int j = 0; j < n; j++)
            {
                travel[i][j] = Math.min(arrival(patches.get(j), config), walking(patches.get(i), patches.get(j), config));
                // Leaving the Stronghold fruit patch gives convenient access to its spirit tree.
                if (patches.get(i) == Patch.STRONGHOLD_FRUIT && patches.get(j) == Patch.VILLAGE_FRUIT)
                {
                    travel[i][j] = Math.min(travel[i][j], 24);
                }
                if (patches.get(i) == Patch.STRONGHOLD_FRUIT && patches.get(j) == Patch.BRIMHAVEN_FRUIT
                    && config.brimhavenTravel() == ClassicTreeRunConfig.BrimhavenTravel.SPIRIT_TREE)
                {
                    travel[i][j] = Math.min(travel[i][j], 16);
                }
            }
        }
    }

    static int arrival(Patch p, ClassicTreeRunConfig c)
    {
        switch (p)
        {
            case LUMBRIDGE_TREE: return 22;
            case VARROCK_TREE: return 30;
            case FALADOR_TREE: return c.faladorTravel() == ClassicTreeRunConfig.FaladorTravel.RING_OF_WEALTH ? 8 : 25;
            case TAVERLEY_TREE:
                switch (c.taverleyTravel())
                {
                    case BALLOON: return 35; // Includes reaching an origin, not just the flight.
                    case FALADOR_TELEPORT: return 65;
                    default: return 20;
                }
            case STRONGHOLD_TREE: return c.strongholdTravel() == ClassicTreeRunConfig.StrongholdTravel.SLAYER_RING ? 12 : 38;
            case STRONGHOLD_FRUIT: return 32;
            case VILLAGE_FRUIT: return 45;
            case LLETYA_FRUIT: return 12;
            case CATHERBY_FRUIT: return c.catherbyTravel() == ClassicTreeRunConfig.CatherbyTravel.CATHERBY_TELEPORT ? 22 : 60;
            case BRIMHAVEN_FRUIT:
                switch (c.brimhavenTravel())
                {
                    case CATHERBY_CHARTER: return arrival(Patch.CATHERBY_FRUIT, c) + 45;
                    case SPIRIT_TREE: return 35;
                    case ARDOUGNE_BOAT: return 65;
                    default: return 25;
                }
            case TAI_BWO_WANNAI_CALQUAT:
                switch (c.calquatTravel())
                {
                    case FAIRY_RING_CKR: return 70;
                    case WALK_FROM_BRIMHAVEN: return arrival(Patch.BRIMHAVEN_FRUIT, c) + 90;
                    default: return 12;
                }
            case FOSSIL_EAST:
            case FOSSIL_MIDDLE:
            case FOSSIL_WEST:
                return (c.fossilTravel() == ClassicTreeRunConfig.FossilTravel.DIGSITE_BARGE ? 65 : 55)
                    + (p == Patch.FOSSIL_MIDDLE ? 1 : 0);
            case GUILD_TREE:
            case GUILD_FRUIT:
                switch (c.guildTravel())
                {
                    case FARMING_CAPE: return p == Patch.GUILD_TREE ? 22 : 20;
                    case SPIRIT_TREE: return p == Patch.GUILD_TREE ? 25 : 12;
                    default: return p == Patch.GUILD_TREE ? 25 : 35;
                }
            default:
                switch (p.getSite())
                {
                    case GUILD: return p == Patch.GUILD_HESPORI ? 40 : 30;
                    case FALADOR_FARM: case ARDOUGNE_FARM: return 12;
                    case HOSIDIUS_FARM: case PHASMATYS: return 25;
                    case TROLL: case WEISS: return 12;
                    case UNDERWATER: return 85;
                    case ANGLERS: return 110;
                    case CONCH: return 65;
                    case CORAL: return 90;
                    case ENTRANA: return 90;
                    case PRIF: return 35;
                    case KASTORI: return 50;
                    case NEMUS: case AUBURNVALE: return 55;
                    case CATHERBY_FARM: return c.catherbyTravel() == ClassicTreeRunConfig.CatherbyTravel.CATHERBY_TELEPORT ? 18 : 45;
                    default: return 45;
                }
        }
    }

    static int walking(Patch from, Patch to, ClassicTreeRunConfig c)
    {
        // This local connection includes the walk to the docks and the charter journey.
        if (from == Patch.CATHERBY_FRUIT && to == Patch.BRIMHAVEN_FRUIT
            && c.brimhavenTravel() == ClassicTreeRunConfig.BrimhavenTravel.CATHERBY_CHARTER) { return 45; }
        if (from == Patch.STRONGHOLD_TREE && to == Patch.STRONGHOLD_FRUIT) { return 18; }
        if (from == Patch.STRONGHOLD_FRUIT && to == Patch.STRONGHOLD_TREE) { return 18; }
        if (from == Patch.GUILD_TREE && to == Patch.GUILD_FRUIT || from == Patch.GUILD_FRUIT && to == Patch.GUILD_TREE) { return 18; }
        if (from.getSite() == PatchSite.FOSSIL && to.getSite() == PatchSite.FOSSIL)
        {
            return Math.abs(from.ordinal() - to.ordinal()) * 2;
        }
        if (from != to && from.getSite() == to.getSite())
        {
            // Generic same-site grouping; scores are intentionally not stated as walking seconds.
            if (from.getSite() == PatchSite.GUILD) { return from == Patch.GUILD_HESPORI || to == Patch.GUILD_HESPORI ? 18 : 8; }
            if (from.getSite() == PatchSite.KASTORI || from.getSite() == PatchSite.BRIMHAVEN) { return 18; }
            return 3;
        }
        if (from == Patch.BRIMHAVEN_FRUIT && to == Patch.TAI_BWO_WANNAI_CALQUAT
            && c.calquatTravel() == ClassicTreeRunConfig.CalquatTravel.WALK_FROM_BRIMHAVEN) { return 90; }
        return Integer.MAX_VALUE;
    }

    static String walkingInstruction(Patch from, Patch to, ClassicTreeRunConfig c)
    {
        if (from == null || walking(from, to, c) >= arrival(to, c)) { return null; }
        if (from == Patch.CATHERBY_FRUIT && to == Patch.BRIMHAVEN_FRUIT
            && c.brimhavenTravel() == ClassicTreeRunConfig.BrimhavenTravel.CATHERBY_CHARTER)
        {
            return TravelGuide.catherbyCharterInstructions();
        }
        if (from.getSite() == PatchSite.FOSSIL && to.getSite() == PatchSite.FOSSIL)
        {
            return "Stay at the Fossil Island hardwood cluster and walk to the "
                + (to == Patch.FOSSIL_EAST ? "east" : to == Patch.FOSSIL_MIDDLE ? "middle" : "west") + " patch.";
        }
        if (from == Patch.STRONGHOLD_TREE && to == Patch.STRONGHOLD_FRUIT) { return "Walk north-east from the Stronghold normal tree to the fruit patch east of the spirit tree."; }
        if (from == Patch.STRONGHOLD_FRUIT && to == Patch.STRONGHOLD_TREE) { return "Walk south-west from the Stronghold fruit patch to the normal tree patch near the Slayer Cave."; }
        if (from.getSite() == PatchSite.GUILD && to == Patch.GUILD_TREE) { return "Stay in the Farming Guild; walk to the west wing normal tree patch."; }
        if (from.getSite() == PatchSite.GUILD && to == Patch.GUILD_FRUIT) { return "Stay in the Farming Guild; walk to the north wing fruit patch (85 Farming)."; }
        if (from.getSite() == to.getSite()) { return "Stay at " + to.getLocation() + "; walk to " + to.getDirections() + "."; }
        if(from==Patch.BRIMHAVEN_FRUIT && to==Patch.TAI_BWO_WANNAI_CALQUAT
            && c.calquatTravel()==ClassicTreeRunConfig.CalquatTravel.WALK_FROM_BRIMHAVEN)
        {return "From Brimhaven fruit patch, run south towards Tai Bwo Wannai to the calquat patch north of the village.";}
        return null;
    }
}
