package com.classictreerun;

import java.util.List;

/** Matches the next route instruction to a carried item, without performing travel. */
final class NextTeleport
{
    private NextTeleport() { }

    static TeleportDevice resolve(ClassicTreeRunConfig config, List<Patch> route, RouteState state,
        InventoryLoadPlanner.Plan plan, String bankRecord)
    {
        Patch next = state.next(route);
        if (next == null || config.loadPlanning() && plan == null) { return TeleportDevice.NONE; }
        int load = plan == null ? -1 : plan.current(state);
        if (config.loadPlanning() && load > 0 && !plan.bankConfirmed(bankRecord, load))
        {
            List<Patch> previousLoad = plan.loads.get(load - 1).patches;
            return config.loadBank().local(previousLoad.get(previousLoad.size() - 1))
                ? TeleportDevice.NONE : highlight(config,bank(config));
        }
        int index = route.indexOf(next);
        int starts = plan == null ? -1 : plan.startsAt(next);
        if (starts > 0 && config.loadBank().departureInstructions(next,config) != null) { return TeleportDevice.NONE; }
        Patch previous = index > 0 && starts <= 0 ? route.get(index - 1) : null;
        if (TravelCosts.walkingInstruction(previous, next, config) != null)
        { return TeleportDevice.NONE; }
        return highlight(config,arrival(next, config));
    }

    private static TeleportDevice highlight(ClassicTreeRunConfig config,TeleportDevice device)
    { return config.supplyTeleportMode()==ClassicTreeRunConfig.TeleportSupplies.SPELL_RUNES
        && !TravelSupplies.spellCost(device).isEmpty()?TeleportDevice.NONE:device; }

    static TeleportDevice bank(ClassicTreeRunConfig config)
    {
        switch (config.loadBank())
        {
            case GRAND_EXCHANGE: return TeleportDevice.WEALTH;
            case CASTLE_WARS: case FEROX_ENCLAVE: return TeleportDevice.DUELING;
            case FARMING_GUILD: return arrival(Patch.GUILD_TREE,config);
            case CATHERBY: return TeleportDevice.CATHERBY;
            case SEERS_VILLAGE: return TeleportDevice.CAMELOT;
            case EDGEVILLE: case DRAYNOR: return TeleportDevice.GLORY;
            case FALADOR_WEST: return TeleportDevice.FALADOR;
            default: return TeleportDevice.NONE;
        }
    }

    private static TeleportDevice catherby(ClassicTreeRunConfig config)
    {
        return config.catherbyTravel() == ClassicTreeRunConfig.CatherbyTravel.CATHERBY_TELEPORT
            ? TeleportDevice.CATHERBY : TeleportDevice.CAMELOT;
    }

    static TeleportDevice arrival(Patch patch, ClassicTreeRunConfig config)
    {
        switch (patch)
        {
            case LUMBRIDGE_TREE: return TeleportDevice.LUMBRIDGE;
            case VARROCK_TREE: return TeleportDevice.VARROCK;
            case FALADOR_TREE: return config.faladorTravel() == ClassicTreeRunConfig.FaladorTravel.RING_OF_WEALTH
                ? TeleportDevice.WEALTH : TeleportDevice.FALADOR;
            case TAVERLEY_TREE:
                switch (config.taverleyTravel())
                {
                    case REDIRECTED_TABLET: return TeleportDevice.TAVERLEY;
                    case FALADOR_TELEPORT: return TeleportDevice.FALADOR;
                    default: return TeleportDevice.NONE;
                }
            case STRONGHOLD_TREE: return config.strongholdTravel() == ClassicTreeRunConfig.StrongholdTravel.SLAYER_RING
                ? TeleportDevice.SLAYER : TeleportDevice.NONE;
            case CATHERBY_FRUIT: return catherby(config);
            case BRIMHAVEN_FRUIT:
                switch (config.brimhavenTravel())
                {
                    case REDIRECTED_TABLET: return TeleportDevice.BRIMHAVEN;
                    case ARDOUGNE_BOAT: return TeleportDevice.ARDOUGNE;
                    case CATHERBY_CHARTER: return catherby(config);
                    default: return TeleportDevice.NONE;
                }
            case TAI_BWO_WANNAI_CALQUAT:
                switch (config.calquatTravel())
                {
                    case TAI_BWO_WANNAI_SCROLL: return TeleportDevice.TAI_BWO_WANNAI;
                    case WALK_FROM_BRIMHAVEN: return arrival(Patch.BRIMHAVEN_FRUIT, config);
                    default: return TeleportDevice.NONE;
                }
            default: break;
        }
        switch (patch.getSite())
        {
            case LLETYA: case PRIF: return TeleportDevice.CRYSTAL;
            case FOSSIL: case UNDERWATER: return TeleportDevice.DIGSITE;
            case GUILD:
                switch (config.guildTravel())
                {
                    case SKILLS_NECKLACE: return TeleportDevice.SKILLS;
                    case FARMING_CAPE: return TeleportDevice.FARMING_CAPE;
                    default: return TeleportDevice.NONE;
                }
            case FALADOR_FARM: case PORT_SARIM: return TeleportDevice.EXPLORER;
            case ARDOUGNE_FARM: return TeleportDevice.ARDOUGNE_FARM;
            case ARDOUGNE_BUSH: return TeleportDevice.ARDOUGNE_MONASTERY;
            case PHASMATYS: return TeleportDevice.ECTOPHIAL;
            case CATHERBY_FARM: return catherby(config);
            case HOSIDIUS_FARM: return TeleportDevice.XERIC;
            case HARMONY: return TeleportDevice.HARMONY;
            case TROLL: return TeleportDevice.STONY_BASALT;
            case WEISS: return TeleportDevice.ICY_BASALT;
            case ORTUS: return TeleportDevice.QUETZAL;
            case LUMBRIDGE_HOPS: return TeleportDevice.CHRONICLE;
            case YANILLE: return TeleportDevice.YANILLE;
            case ENTRANA: return TeleportDevice.GLORY;
            case CHAMPIONS: return TeleportDevice.COMBAT;
            case RIMMINGTON: return TeleportDevice.RIMMINGTON;
            case ETCETERIA: return TeleportDevice.WEALTH;
            case AL_KHARID: return TeleportDevice.DUELING;
            case DRAYNOR: return TeleportDevice.DRAYNOR_MANOR;
            case HOSIDIUS_SPIRIT: return TeleportDevice.SKILLS;
            case KASTORI: return TeleportDevice.FORTIS;
            default: return TeleportDevice.NONE;
        }
    }
}
