package com.classictreerun;

/** Player-selected resupply destinations. These are instructions, never teleport actions. */
public enum BankDestination
{
    GRAND_EXCHANGE("Grand Exchange", "Ring of wealth to the Grand Exchange; use a banker. Varrock's Grand Exchange teleport needs the medium Varrock diary."),
    CASTLE_WARS("Castle Wars", "Ring of dueling to Castle Wars; use the lobby bank chest on a members world."),
    FEROX_ENCLAVE("Ferox Enclave", "Ring of dueling to Ferox Enclave; use the bank inside the enclave."),
    FARMING_GUILD("Farming Guild", "Skills necklace to the Farming Guild; enter and use the central bank chest. Requires 45 Farming to enter."),
    CATHERBY("Catherby", "Catherby teleport spell, tablet or POH portal; use Catherby bank. Alternatively, Camelot Teleport and walk south-east to Catherby."),
    SEERS_VILLAGE("Seers' Village", "Camelot Teleport, then walk west to Seers' bank. A direct Seers' bank destination requires the hard Kandarin diary."),
    EDGEVILLE("Edgeville", "Amulet of glory to Edgeville; use Edgeville bank."),
    DRAYNOR("Draynor Village", "Amulet of glory to Draynor Village; use the village bank."),
    FALADOR_WEST("Falador west", "Falador Teleport spell or tablet; walk to the west bank."),
    CUSTOM("Custom bank", "");

    private final String label, directions;
    BankDestination(String label,String directions) { this.label=label;this.directions=directions; }
    @Override public String toString() { return label; }
    String instructions(ClassicTreeRunConfig config)
    {
        return this==CUSTOM ? InventoryLoadPlanner.customInstructions(config.loadBankInstructions()) : directions;
    }
    String instructions(Patch from,ClassicTreeRunConfig config)
    {
        if(local(from))return this==CATHERBY ? "From this patch, walk to Catherby bank."
            : "From this patch, walk to the Farming Guild's central bank chest.";
        return instructions(config);
    }
    String departureInstructions(Patch to,ClassicTreeRunConfig config)
    {
        if(this==CATHERBY && config.brimhavenTravel()==ClassicTreeRunConfig.BrimhavenTravel.CATHERBY_CHARTER
            && (to==Patch.BRIMHAVEN_FRUIT || to==Patch.TAI_BWO_WANNAI_CALQUAT
                && config.calquatTravel()==ClassicTreeRunConfig.CalquatTravel.WALK_FROM_BRIMHAVEN))
        {
            String charter=TravelGuide.catherbyBankCharterInstructions();
            return to==Patch.TAI_BWO_WANNAI_CALQUAT?charter+" Then run south towards Tai Bwo Wannai to the calquat patch north of the village.":charter;
        }
        if(!local(to))return null;
        if(this==FARMING_GUILD)return "From the Farming Guild's central bank chest, walk to this patch: "+to.getDirections()+". Check its wing access requirements.";
        return to==Patch.CATHERBY_FRUIT ? "From Catherby bank, walk east along the shore to the fruit tree patch."
            : "From Catherby bank, walk to the farming patches north of town: "+to.getDirections()+".";
    }
    boolean local(Patch patch)
    {
        switch(this)
        {
            case FARMING_GUILD:return patch.getSite()==PatchSite.GUILD;
            case CATHERBY:return patch.getSite()==PatchSite.CATHERBY_FARM || patch.getSite()==PatchSite.CATHERBY_FRUIT;
            default:return false;
        }
    }
    int approachCost(Patch from) { return local(from)?10:25; }
    int departureCost(Patch to,ClassicTreeRunConfig config)
    { return local(to)?12:TravelCosts.arrival(to,config); }
}
