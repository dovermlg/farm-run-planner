package com.classictreerun;

final class TravelGuide
{
    private TravelGuide() { }

    static String instructions(Patch patch, ClassicTreeRunConfig c)
    {
        switch (patch)
        {
            case LUMBRIDGE_TREE:
                return "Lumbridge Teleport spell/tablet; leave the castle grounds and go west to the patch. Use the spell/tablet, not the slow Home Teleport.";
            case VARROCK_TREE:
                return "Varrock Teleport to the city centre; run north into the castle courtyard. If your default teleport is the Grand Exchange, select the city-centre destination.";
            case FALADOR_TREE:
                return c.faladorTravel() == ClassicTreeRunConfig.FaladorTravel.RING_OF_WEALTH
                    ? "Ring of wealth: Falador Park. Go to the tree patch inside the park."
                    : "Falador Teleport spell/tablet; run east into Falador Park to the tree patch.";
            case TAVERLEY_TREE:
                switch (c.taverleyTravel())
                {
                    case BALLOON: return "Use an unlocked balloon route to Taverley; the patch is near the landing. Requires Enlightened Journey, the route unlock, appropriate fuel and balloon weight restrictions. Access a balloon first (e.g. Crafting Guild).";
                    case FALADOR_TELEPORT: return "Falador Teleport; travel north-west to Taverley, using the western wall shortcut if available, then go to the tree patch.";
                    default: return "Break a Taverley teleport tablet (house tablet + scroll of redirection; 10 Construction). Walk to the tree patch. Alternatively teleport outside a house actually located in Taverley.";
                }
            case STRONGHOLD_TREE:
                return c.strongholdTravel() == ClassicTreeRunConfig.StrongholdTravel.SLAYER_RING
                    ? "Slayer ring: Stronghold Slayer Cave. Leave the cave entrance area for the nearby normal tree patch."
                    : "Use any accessible spirit tree (e.g. Grand Exchange) to Tree Gnome Stronghold; run south-west to the normal tree patch.";
            case STRONGHOLD_FRUIT:
                return "From the Stronghold normal tree, run north-east to the fruit patch east of the spirit tree. If arriving independently, use a spirit tree to Tree Gnome Stronghold.";
            case VILLAGE_FRUIT:
                return "From an accessible spirit tree (the Stronghold tree is convenient), select Tree Gnome Village. Squeeze through the loose railing and follow Elkoy out of the maze; the fruit patch is outside, west of the maze. Alternative: fairy ring CIQ.";
            case LLETYA_FRUIT:
                return "Teleport crystal or eternal teleport crystal: Lletya. Walk to the village fruit patch. Lletya access is obtained during Mourning's End Part I; this is not the Prifddinas crystal-tree patch.";
            case CATHERBY_FRUIT:
                return c.catherbyTravel() == ClassicTreeRunConfig.CatherbyTravel.CATHERBY_TELEPORT
                    ? "Catherby Teleport (Lunar spell, tablet or configured POH portal/nexus); run east along the shore to the fruit patch near the fishing spots."
                    : "Camelot Teleport to Camelot (not the Seers' bank destination); run south-east through Catherby, then east along the shore to the fruit patch.";
            case BRIMHAVEN_FRUIT:
                switch (c.brimhavenTravel())
                {
                    case CATHERBY_CHARTER:
                        return catherbyCharterInstructions() + " If you are elsewhere, first reach Catherby using "
                            + (c.catherbyTravel() == ClassicTreeRunConfig.CatherbyTravel.CATHERBY_TELEPORT
                                ? "Catherby Teleport (spell, tablet or POH)."
                                : "Camelot Teleport to Camelot, then run south-east to Catherby.")
                            + " You do not need to visit the fruit patch if it is not in your run.";
                    case SPIRIT_TREE: return "Use an accessible spirit tree to your grown Brimhaven spirit tree; walk to the fruit patch north of the town. Requires your own planted Brimhaven spirit tree.";
                    case ARDOUGNE_BOAT: return "Ardougne Teleport; run to the docks and pay Captain Barnaby for passage to Brimhaven. Walk to the nearby fruit patch; carry the fare.";
                    default: return "Break a Brimhaven teleport tablet (house tablet + scroll of redirection; 40 Construction); run north to the fruit patch. Alternatively teleport outside your Brimhaven house.";
                }
            case TAI_BWO_WANNAI_CALQUAT:
                switch (c.calquatTravel())
                {
                    case FAIRY_RING_CKR: return "Fairy ring CKR; travel north to the calquat patch north of Tai Bwo Wannai village. Requires fairy ring access and a staff unless exempt.";
                    case WALK_FROM_BRIMHAVEN: return "From Brimhaven fruit patch, run south towards Tai Bwo Wannai; stop at the calquat patch north of the village. If elsewhere, first reach Brimhaven. This saves a scroll but adds running.";
                    default: return "Use a Tai bwo wannai teleport scroll; walk north to the calquat patch. The scroll requires completion of Tai Bwo Wannai Trio.";
                }
            case FOSSIL_EAST:
            case FOSSIL_MIDDLE:
            case FOSSIL_WEST:
                String entry = c.fossilTravel() == ClassicTreeRunConfig.FossilTravel.DIGSITE_BARGE
                    ? "Digsite pendant: Digsite; use the barge's quick-travel to Fossil Island, then run north-west from the camp to the hardwood cluster. Requires Bone Voyage."
                    : "Digsite pendant: Fossil Island (House on the Hill, unlocked destination); use the unlocked mushtree to Mushroom Meadow, then run south-east to the hardwood cluster. Requires Bone Voyage and these transport unlocks.";
                return entry + " If already at the cluster, move to the "
                    + (patch == Patch.FOSSIL_EAST ? "east" : patch == Patch.FOSSIL_MIDDLE ? "middle" : "west")
                    + " patch. Each of the three patches has its own checkbox.";
            case GUILD_TREE:
                return guild(c) + " Go to the west wing normal tree patch (65 Farming).";
            case GUILD_FRUIT:
                return "If already in the Farming Guild, walk to the north wing fruit patch (85 Farming). Otherwise: " + guild(c);
            default:
                String travel = patch.getSite() == PatchSite.GUILD ? guild(c) : patch.getSite().travel;
                if (patch.getSite() == PatchSite.CATHERBY_FARM && c.catherbyTravel() == ClassicTreeRunConfig.CatherbyTravel.CAMELOT_TELEPORT)
                {
                    travel = "Camelot Teleport to Camelot; run east towards Catherby and the farming patches north of town.";
                }
                return travel + " At the location: " + patch.getDirections() + ".";
        }
    }

    static String catherbyCharterInstructions()
    {
        return "From Catherby fruit patch, walk west along the shore to the charter ship at Catherby docks. "
            + "Choose Charter on a Trader Crewmember, select Brimhaven, then walk to the nearby fruit patch. Bring coins for the fare.";
    }

    private static String guild(ClassicTreeRunConfig c)
    {
        switch (c.guildTravel())
        {
            case FARMING_CAPE: return "Farming cape teleport to the Farming Guild (99 Farming).";
            case SPIRIT_TREE: return "Use an accessible spirit tree to your grown Farming Guild spirit tree; north wing access is required.";
            default: return "Skills necklace: Farming Guild; enter the guild.";
        }
    }
}
