package com.classictreerun;

/** Reads the native settings without adding non-config methods to RuneLite's config proxy. */
final class NativePatchSelection
{
    static java.util.Set<Patch> read(ClassicTreeRunConfig config)
    {
        java.util.Set<Patch> selected = java.util.EnumSet.noneOf(Patch.class);
        if (config.includeLUMBRIDGE_TREE()) { selected.add(Patch.LUMBRIDGE_TREE); }
        if (config.includeVARROCK_TREE()) { selected.add(Patch.VARROCK_TREE); }
        if (config.includeFALADOR_TREE()) { selected.add(Patch.FALADOR_TREE); }
        if (config.includeTAVERLEY_TREE()) { selected.add(Patch.TAVERLEY_TREE); }
        if (config.includeSTRONGHOLD_TREE()) { selected.add(Patch.STRONGHOLD_TREE); }
        if (config.includeSTRONGHOLD_FRUIT()) { selected.add(Patch.STRONGHOLD_FRUIT); }
        if (config.includeVILLAGE_FRUIT()) { selected.add(Patch.VILLAGE_FRUIT); }
        if (config.includeLLETYA_FRUIT()) { selected.add(Patch.LLETYA_FRUIT); }
        if (config.includeCATHERBY_FRUIT()) { selected.add(Patch.CATHERBY_FRUIT); }
        if (config.includeBRIMHAVEN_FRUIT()) { selected.add(Patch.BRIMHAVEN_FRUIT); }
        if (config.includeTAI_BWO_WANNAI_CALQUAT()) { selected.add(Patch.TAI_BWO_WANNAI_CALQUAT); }
        if (config.includeFOSSIL_EAST()) { selected.add(Patch.FOSSIL_EAST); }
        if (config.includeFOSSIL_MIDDLE()) { selected.add(Patch.FOSSIL_MIDDLE); }
        if (config.includeFOSSIL_WEST()) { selected.add(Patch.FOSSIL_WEST); }
        if (config.includeGUILD_TREE()) { selected.add(Patch.GUILD_TREE); }
        if (config.includeGUILD_FRUIT()) { selected.add(Patch.GUILD_FRUIT); }
        if (config.includeNEMUS_TREE()) { selected.add(Patch.NEMUS_TREE); }
        if (config.includeKASTORI_FRUIT()) { selected.add(Patch.KASTORI_FRUIT); }
        if (config.includeLOCUS_HARDWOOD()) { selected.add(Patch.LOCUS_HARDWOOD); }
        if (config.includeANGLERS_HARDWOOD()) { selected.add(Patch.ANGLERS_HARDWOOD); }
        if (config.includeKASTORI_CALQUAT()) { selected.add(Patch.KASTORI_CALQUAT); }
        if (config.includeCONCH_CALQUAT()) { selected.add(Patch.CONCH_CALQUAT); }
        if (config.includeFALADOR_ALLOTMENT_NW()) { selected.add(Patch.FALADOR_ALLOTMENT_NW); }
        if (config.includeFALADOR_ALLOTMENT_SE()) { selected.add(Patch.FALADOR_ALLOTMENT_SE); }
        if (config.includeFALADOR_FLOWER()) { selected.add(Patch.FALADOR_FLOWER); }
        if (config.includeFALADOR_HERB()) { selected.add(Patch.FALADOR_HERB); }
        if (config.includePHASMATYS_ALLOTMENT_NW()) { selected.add(Patch.PHASMATYS_ALLOTMENT_NW); }
        if (config.includePHASMATYS_ALLOTMENT_SE()) { selected.add(Patch.PHASMATYS_ALLOTMENT_SE); }
        if (config.includePHASMATYS_FLOWER()) { selected.add(Patch.PHASMATYS_FLOWER); }
        if (config.includePHASMATYS_HERB()) { selected.add(Patch.PHASMATYS_HERB); }
        if (config.includeCATHERBY_ALLOTMENT_N()) { selected.add(Patch.CATHERBY_ALLOTMENT_N); }
        if (config.includeCATHERBY_ALLOTMENT_S()) { selected.add(Patch.CATHERBY_ALLOTMENT_S); }
        if (config.includeCATHERBY_FLOWER()) { selected.add(Patch.CATHERBY_FLOWER); }
        if (config.includeCATHERBY_HERB()) { selected.add(Patch.CATHERBY_HERB); }
        if (config.includeARDOUGNE_ALLOTMENT_N()) { selected.add(Patch.ARDOUGNE_ALLOTMENT_N); }
        if (config.includeARDOUGNE_ALLOTMENT_S()) { selected.add(Patch.ARDOUGNE_ALLOTMENT_S); }
        if (config.includeARDOUGNE_FLOWER()) { selected.add(Patch.ARDOUGNE_FLOWER); }
        if (config.includeARDOUGNE_HERB()) { selected.add(Patch.ARDOUGNE_HERB); }
        if (config.includeHOSIDIUS_ALLOTMENT_NE()) { selected.add(Patch.HOSIDIUS_ALLOTMENT_NE); }
        if (config.includeHOSIDIUS_ALLOTMENT_SW()) { selected.add(Patch.HOSIDIUS_ALLOTMENT_SW); }
        if (config.includeHOSIDIUS_FLOWER()) { selected.add(Patch.HOSIDIUS_FLOWER); }
        if (config.includeHOSIDIUS_HERB()) { selected.add(Patch.HOSIDIUS_HERB); }
        if (config.includeHARMONY_ALLOTMENT()) { selected.add(Patch.HARMONY_ALLOTMENT); }
        if (config.includeHARMONY_HERB()) { selected.add(Patch.HARMONY_HERB); }
        if (config.includeGUILD_ALLOTMENT_N()) { selected.add(Patch.GUILD_ALLOTMENT_N); }
        if (config.includeGUILD_ALLOTMENT_S()) { selected.add(Patch.GUILD_ALLOTMENT_S); }
        if (config.includeGUILD_FLOWER()) { selected.add(Patch.GUILD_FLOWER); }
        if (config.includeGUILD_HERB()) { selected.add(Patch.GUILD_HERB); }
        if (config.includePRIF_ALLOTMENT_N()) { selected.add(Patch.PRIF_ALLOTMENT_N); }
        if (config.includePRIF_ALLOTMENT_S()) { selected.add(Patch.PRIF_ALLOTMENT_S); }
        if (config.includePRIF_FLOWER()) { selected.add(Patch.PRIF_FLOWER); }
        if (config.includeTROLL_HERB()) { selected.add(Patch.TROLL_HERB); }
        if (config.includeWEISS_HERB()) { selected.add(Patch.WEISS_HERB); }
        if (config.includeORTUS_ALLOTMENT_N()) { selected.add(Patch.ORTUS_ALLOTMENT_N); }
        if (config.includeORTUS_ALLOTMENT_S()) { selected.add(Patch.ORTUS_ALLOTMENT_S); }
        if (config.includeORTUS_FLOWER()) { selected.add(Patch.ORTUS_FLOWER); }
        if (config.includeORTUS_HERB()) { selected.add(Patch.ORTUS_HERB); }
        if (config.includeKASTORI_FLOWER()) { selected.add(Patch.KASTORI_FLOWER); }
        if (config.includeLUMBRIDGE_HOPS()) { selected.add(Patch.LUMBRIDGE_HOPS); }
        if (config.includeSEERS_HOPS()) { selected.add(Patch.SEERS_HOPS); }
        if (config.includeYANILLE_HOPS()) { selected.add(Patch.YANILLE_HOPS); }
        if (config.includeENTRANA_HOPS()) { selected.add(Patch.ENTRANA_HOPS); }
        if (config.includeALDARIN_HOPS()) { selected.add(Patch.ALDARIN_HOPS); }
        if (config.includeCHAMPIONS_BUSH()) { selected.add(Patch.CHAMPIONS_BUSH); }
        if (config.includeRIMMINGTON_BUSH()) { selected.add(Patch.RIMMINGTON_BUSH); }
        if (config.includeARDOUGNE_BUSH()) { selected.add(Patch.ARDOUGNE_BUSH); }
        if (config.includeETCETERIA_BUSH()) { selected.add(Patch.ETCETERIA_BUSH); }
        if (config.includeGUILD_BUSH()) { selected.add(Patch.GUILD_BUSH); }
        if (config.includeAL_KHARID_CACTUS()) { selected.add(Patch.AL_KHARID_CACTUS); }
        if (config.includeGUILD_CACTUS()) { selected.add(Patch.GUILD_CACTUS); }
        if (config.includeSEAWEED_N()) { selected.add(Patch.SEAWEED_N); }
        if (config.includeSEAWEED_S()) { selected.add(Patch.SEAWEED_S); }
        if (config.includeVINERY_E1()) { selected.add(Patch.VINERY_E1); }
        if (config.includeVINERY_E2()) { selected.add(Patch.VINERY_E2); }
        if (config.includeVINERY_E3()) { selected.add(Patch.VINERY_E3); }
        if (config.includeVINERY_E4()) { selected.add(Patch.VINERY_E4); }
        if (config.includeVINERY_E5()) { selected.add(Patch.VINERY_E5); }
        if (config.includeVINERY_E6()) { selected.add(Patch.VINERY_E6); }
        if (config.includeVINERY_W1()) { selected.add(Patch.VINERY_W1); }
        if (config.includeVINERY_W2()) { selected.add(Patch.VINERY_W2); }
        if (config.includeVINERY_W3()) { selected.add(Patch.VINERY_W3); }
        if (config.includeVINERY_W4()) { selected.add(Patch.VINERY_W4); }
        if (config.includeVINERY_W5()) { selected.add(Patch.VINERY_W5); }
        if (config.includeVINERY_W6()) { selected.add(Patch.VINERY_W6); }
        if (config.includeCANIFIS_MUSHROOM()) { selected.add(Patch.CANIFIS_MUSHROOM); }
        if (config.includeDRAYNOR_BELLADONNA()) { selected.add(Patch.DRAYNOR_BELLADONNA); }
        if (config.includeAUBURNVALE_BELLADONNA()) { selected.add(Patch.AUBURNVALE_BELLADONNA); }
        if (config.includeGUILD_HESPORI()) { selected.add(Patch.GUILD_HESPORI); }
        if (config.includeGUILD_ANIMA()) { selected.add(Patch.GUILD_ANIMA); }
        if (config.includePRIF_CRYSTAL()) { selected.add(Patch.PRIF_CRYSTAL); }
        if (config.includeGUILD_CELASTRUS()) { selected.add(Patch.GUILD_CELASTRUS); }
        if (config.includeGUILD_REDWOOD()) { selected.add(Patch.GUILD_REDWOOD); }
        if (config.includeCORAL_E()) { selected.add(Patch.CORAL_E); }
        if (config.includeCORAL_W()) { selected.add(Patch.CORAL_W); }
        if (config.includeETCETERIA_SPIRIT()) { selected.add(Patch.ETCETERIA_SPIRIT); }
        if (config.includePORT_SARIM_SPIRIT()) { selected.add(Patch.PORT_SARIM_SPIRIT); }
        if (config.includeBRIMHAVEN_SPIRIT()) { selected.add(Patch.BRIMHAVEN_SPIRIT); }
        if (config.includeHOSIDIUS_SPIRIT()) { selected.add(Patch.HOSIDIUS_SPIRIT); }
        if (config.includeGUILD_SPIRIT()) { selected.add(Patch.GUILD_SPIRIT); }
        return selected;
    }
}

