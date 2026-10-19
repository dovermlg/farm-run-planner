package com.classictreerun;

/** Typed view of local files only. Never registered with RuneLite's syncing ConfigManager. */
final class LocalRunConfig implements ClassicTreeRunConfig
{
    private final ClassicTreeRunPanel.Preferences values;
    LocalRunConfig(ClassicTreeRunPanel.Preferences values) { this.values = values; }
    @Override public boolean highlightNextTeleport() { return bool("highlightNextTeleport",ClassicTreeRunConfig.super.highlightNextTeleport()); }
    static ClassicTreeRunConfig snapshot(ClassicTreeRunConfig source)
    {
        java.util.Map<String,String> copy=java.util.Map.copyOf(ConfigValues.read(source));
        return new LocalRunConfig(new ClassicTreeRunPanel.Preferences()
        {
            public String get(String key){return copy.get(key);}
            public void set(String key,String value){throw new UnsupportedOperationException("Read-only settings snapshot");}
        });
    }
    @Override public boolean loadPlanning() { return bool("loadPlanning",ClassicTreeRunConfig.super.loadPlanning()); }
    @Override public int loadReservedSlots()
    {
        try{return Math.max(0,Math.min(27,Integer.parseInt(values.get("loadReservedSlots"))));}
        catch(NumberFormatException error){return ClassicTreeRunConfig.super.loadReservedSlots();}
    }
    @Override public boolean loadNotedProtection() { return bool("loadNotedProtection",ClassicTreeRunConfig.super.loadNotedProtection()); }
    @Override public BankDestination loadBank() { return choice("loadBank",ClassicTreeRunConfig.super.loadBank()); }
    @Override public String loadBankInstructions() { return string("loadBankInstructions",ClassicTreeRunConfig.super.loadBankInstructions()); }
    private boolean bool(String key, boolean fallback) {
        String value = values.get(key);
        return "true".equals(value) ? true : "false".equals(value) ? false : fallback;
    }
    private String string(String key, String fallback) { String v = values.get(key); return v == null ? fallback : v; }
    private <E extends Enum<E>> E choice(String key, E fallback) {
        try { return Enum.valueOf(fallback.getDeclaringClass(), values.get(key)); }
        catch (IllegalArgumentException | NullPointerException error) { return fallback; }
    }
    @Override public CropChoices.Tree cropTree() { return choice("cropTree", ClassicTreeRunConfig.super.cropTree()); }
    @Override public CropChoices.Fruit cropFruit() { return choice("cropFruit", ClassicTreeRunConfig.super.cropFruit()); }
    @Override public CropChoices.Hardwood cropHardwood() { return choice("cropHardwood", ClassicTreeRunConfig.super.cropHardwood()); }
    @Override public CropChoices.Allotment cropAllotment() { return choice("cropAllotment", ClassicTreeRunConfig.super.cropAllotment()); }
    @Override public CropChoices.Flower cropFlower() { return choice("cropFlower", ClassicTreeRunConfig.super.cropFlower()); }
    @Override public CropChoices.Herb cropHerb() { return choice("cropHerb", ClassicTreeRunConfig.super.cropHerb()); }
    @Override public CropChoices.Hops cropHops() { return choice("cropHops", ClassicTreeRunConfig.super.cropHops()); }
    @Override public CropChoices.Bush cropBush() { return choice("cropBush", ClassicTreeRunConfig.super.cropBush()); }
    @Override public CropChoices.Cactus cropCactus() { return choice("cropCactus", ClassicTreeRunConfig.super.cropCactus()); }
    @Override public CropChoices.Calquat cropCalquat() { return choice("cropCalquat", ClassicTreeRunConfig.super.cropCalquat()); }
    @Override public CropChoices.Seaweed cropSeaweed() { return choice("cropSeaweed", ClassicTreeRunConfig.super.cropSeaweed()); }
    @Override public CropChoices.Grape cropGrape() { return choice("cropGrape", ClassicTreeRunConfig.super.cropGrape()); }
    @Override public CropChoices.Mushroom cropMushroom() { return choice("cropMushroom", ClassicTreeRunConfig.super.cropMushroom()); }
    @Override public CropChoices.Belladonna cropBelladonna() { return choice("cropBelladonna", ClassicTreeRunConfig.super.cropBelladonna()); }
    @Override public CropChoices.Hespori cropHespori() { return choice("cropHespori", ClassicTreeRunConfig.super.cropHespori()); }
    @Override public CropChoices.Anima cropAnima() { return choice("cropAnima", ClassicTreeRunConfig.super.cropAnima()); }
    @Override public CropChoices.Crystal cropCrystal() { return choice("cropCrystal", ClassicTreeRunConfig.super.cropCrystal()); }
    @Override public CropChoices.Celastrus cropCelastrus() { return choice("cropCelastrus", ClassicTreeRunConfig.super.cropCelastrus()); }
    @Override public CropChoices.Redwood cropRedwood() { return choice("cropRedwood", ClassicTreeRunConfig.super.cropRedwood()); }
    @Override public CropChoices.Coral cropCoral() { return choice("cropCoral", ClassicTreeRunConfig.super.cropCoral()); }
    @Override public CropChoices.Spirit cropSpirit() { return choice("cropSpirit", ClassicTreeRunConfig.super.cropSpirit()); }
    @Override public boolean supplyProtection() { return bool("supplyProtection", ClassicTreeRunConfig.super.supplyProtection()); }
    @Override public boolean supplyTools() { return bool("supplyTools", ClassicTreeRunConfig.super.supplyTools()); }
    @Override public boolean supplyRake() { return bool("supplyRake", ClassicTreeRunConfig.super.supplyRake()); }
    @Override public boolean supplyDibber() { return bool("supplyDibber", ClassicTreeRunConfig.super.supplyDibber()); }
    @Override public SupplyPlan.Compost supplyCompost() { return choice("supplyCompost", ClassicTreeRunConfig.super.supplyCompost()); }
    @Override public int supplyCoins() { try { return Math.max(0,Integer.parseInt(values.get("supplyCoins"))); } catch(NumberFormatException error) { return ClassicTreeRunConfig.super.supplyCoins(); } }
    @Override public RunPreset preset() { return choice("preset", ClassicTreeRunConfig.super.preset()); }
    @Override public String runName() { return string("runName", ClassicTreeRunConfig.super.runName()); }
    @Override public boolean autoOptimize() { return bool("autoOptimize", ClassicTreeRunConfig.super.autoOptimize()); }
    @Override public boolean showDetails() { return bool("showDetails", ClassicTreeRunConfig.super.showDetails()); }
    @Override public boolean trees() { return bool("trees", ClassicTreeRunConfig.super.trees()); }
    @Override public boolean fruit() { return bool("fruit", ClassicTreeRunConfig.super.fruit()); }
    @Override public boolean hardwood() { return bool("hardwood", ClassicTreeRunConfig.super.hardwood()); }
    @Override public boolean calquat() { return bool("calquat", ClassicTreeRunConfig.super.calquat()); }
    @Override public boolean hideCompleted() { return bool("hideCompleted", ClassicTreeRunConfig.super.hideCompleted()); }
    @Override public boolean otherPatches() { return bool("otherPatches", ClassicTreeRunConfig.super.otherPatches()); }
    @Override public String selection() { return string("selectionV2", ClassicTreeRunConfig.super.selection()); }
    @Override public FaladorTravel faladorTravel() { return choice("faladorTravel", ClassicTreeRunConfig.super.faladorTravel()); }
    @Override public TaverleyTravel taverleyTravel() { return choice("taverleyTravel", ClassicTreeRunConfig.super.taverleyTravel()); }
    @Override public StrongholdTravel strongholdTravel() { return choice("strongholdTravel", ClassicTreeRunConfig.super.strongholdTravel()); }
    @Override public CatherbyTravel catherbyTravel() { return choice("catherbyTravel", ClassicTreeRunConfig.super.catherbyTravel()); }
    @Override public BrimhavenTravel brimhavenTravel() { return choice("brimhavenTravel", ClassicTreeRunConfig.super.brimhavenTravel()); }
    @Override public CalquatTravel calquatTravel() { return choice("calquatTravel", ClassicTreeRunConfig.super.calquatTravel()); }
    @Override public FossilTravel fossilTravel() { return choice("fossilTravel", ClassicTreeRunConfig.super.fossilTravel()); }
    @Override public GuildTravel guildTravel() { return choice("guildTravel", ClassicTreeRunConfig.super.guildTravel()); }
    @Override public String order() { return string("order", ClassicTreeRunConfig.super.order()); }
    @Override public String excluded() { return string("excluded", ClassicTreeRunConfig.super.excluded()); }
    @Override public boolean includeLUMBRIDGE_TREE() { return bool("patch_LUMBRIDGE_TREE", ClassicTreeRunConfig.super.includeLUMBRIDGE_TREE()); }
    @Override public boolean includeVARROCK_TREE() { return bool("patch_VARROCK_TREE", ClassicTreeRunConfig.super.includeVARROCK_TREE()); }
    @Override public boolean includeFALADOR_TREE() { return bool("patch_FALADOR_TREE", ClassicTreeRunConfig.super.includeFALADOR_TREE()); }
    @Override public boolean includeTAVERLEY_TREE() { return bool("patch_TAVERLEY_TREE", ClassicTreeRunConfig.super.includeTAVERLEY_TREE()); }
    @Override public boolean includeSTRONGHOLD_TREE() { return bool("patch_STRONGHOLD_TREE", ClassicTreeRunConfig.super.includeSTRONGHOLD_TREE()); }
    @Override public boolean includeSTRONGHOLD_FRUIT() { return bool("patch_STRONGHOLD_FRUIT", ClassicTreeRunConfig.super.includeSTRONGHOLD_FRUIT()); }
    @Override public boolean includeVILLAGE_FRUIT() { return bool("patch_VILLAGE_FRUIT", ClassicTreeRunConfig.super.includeVILLAGE_FRUIT()); }
    @Override public boolean includeLLETYA_FRUIT() { return bool("patch_LLETYA_FRUIT", ClassicTreeRunConfig.super.includeLLETYA_FRUIT()); }
    @Override public boolean includeCATHERBY_FRUIT() { return bool("patch_CATHERBY_FRUIT", ClassicTreeRunConfig.super.includeCATHERBY_FRUIT()); }
    @Override public boolean includeBRIMHAVEN_FRUIT() { return bool("patch_BRIMHAVEN_FRUIT", ClassicTreeRunConfig.super.includeBRIMHAVEN_FRUIT()); }
    @Override public boolean includeTAI_BWO_WANNAI_CALQUAT() { return bool("patch_TAI_BWO_WANNAI_CALQUAT", ClassicTreeRunConfig.super.includeTAI_BWO_WANNAI_CALQUAT()); }
    @Override public boolean includeFOSSIL_EAST() { return bool("patch_FOSSIL_EAST", ClassicTreeRunConfig.super.includeFOSSIL_EAST()); }
    @Override public boolean includeFOSSIL_MIDDLE() { return bool("patch_FOSSIL_MIDDLE", ClassicTreeRunConfig.super.includeFOSSIL_MIDDLE()); }
    @Override public boolean includeFOSSIL_WEST() { return bool("patch_FOSSIL_WEST", ClassicTreeRunConfig.super.includeFOSSIL_WEST()); }
    @Override public boolean includeGUILD_TREE() { return bool("patch_GUILD_TREE", ClassicTreeRunConfig.super.includeGUILD_TREE()); }
    @Override public boolean includeGUILD_FRUIT() { return bool("patch_GUILD_FRUIT", ClassicTreeRunConfig.super.includeGUILD_FRUIT()); }
    @Override public boolean includeNEMUS_TREE() { return bool("patch_NEMUS_TREE", ClassicTreeRunConfig.super.includeNEMUS_TREE()); }
    @Override public boolean includeKASTORI_FRUIT() { return bool("patch_KASTORI_FRUIT", ClassicTreeRunConfig.super.includeKASTORI_FRUIT()); }
    @Override public boolean includeLOCUS_HARDWOOD() { return bool("patch_LOCUS_HARDWOOD", ClassicTreeRunConfig.super.includeLOCUS_HARDWOOD()); }
    @Override public boolean includeANGLERS_HARDWOOD() { return bool("patch_ANGLERS_HARDWOOD", ClassicTreeRunConfig.super.includeANGLERS_HARDWOOD()); }
    @Override public boolean includeKASTORI_CALQUAT() { return bool("patch_KASTORI_CALQUAT", ClassicTreeRunConfig.super.includeKASTORI_CALQUAT()); }
    @Override public boolean includeCONCH_CALQUAT() { return bool("patch_CONCH_CALQUAT", ClassicTreeRunConfig.super.includeCONCH_CALQUAT()); }
    @Override public boolean includeFALADOR_ALLOTMENT_NW() { return bool("patch_FALADOR_ALLOTMENT_NW", ClassicTreeRunConfig.super.includeFALADOR_ALLOTMENT_NW()); }
    @Override public boolean includeFALADOR_ALLOTMENT_SE() { return bool("patch_FALADOR_ALLOTMENT_SE", ClassicTreeRunConfig.super.includeFALADOR_ALLOTMENT_SE()); }
    @Override public boolean includeFALADOR_FLOWER() { return bool("patch_FALADOR_FLOWER", ClassicTreeRunConfig.super.includeFALADOR_FLOWER()); }
    @Override public boolean includeFALADOR_HERB() { return bool("patch_FALADOR_HERB", ClassicTreeRunConfig.super.includeFALADOR_HERB()); }
    @Override public boolean includePHASMATYS_ALLOTMENT_NW() { return bool("patch_PHASMATYS_ALLOTMENT_NW", ClassicTreeRunConfig.super.includePHASMATYS_ALLOTMENT_NW()); }
    @Override public boolean includePHASMATYS_ALLOTMENT_SE() { return bool("patch_PHASMATYS_ALLOTMENT_SE", ClassicTreeRunConfig.super.includePHASMATYS_ALLOTMENT_SE()); }
    @Override public boolean includePHASMATYS_FLOWER() { return bool("patch_PHASMATYS_FLOWER", ClassicTreeRunConfig.super.includePHASMATYS_FLOWER()); }
    @Override public boolean includePHASMATYS_HERB() { return bool("patch_PHASMATYS_HERB", ClassicTreeRunConfig.super.includePHASMATYS_HERB()); }
    @Override public boolean includeCATHERBY_ALLOTMENT_N() { return bool("patch_CATHERBY_ALLOTMENT_N", ClassicTreeRunConfig.super.includeCATHERBY_ALLOTMENT_N()); }
    @Override public boolean includeCATHERBY_ALLOTMENT_S() { return bool("patch_CATHERBY_ALLOTMENT_S", ClassicTreeRunConfig.super.includeCATHERBY_ALLOTMENT_S()); }
    @Override public boolean includeCATHERBY_FLOWER() { return bool("patch_CATHERBY_FLOWER", ClassicTreeRunConfig.super.includeCATHERBY_FLOWER()); }
    @Override public boolean includeCATHERBY_HERB() { return bool("patch_CATHERBY_HERB", ClassicTreeRunConfig.super.includeCATHERBY_HERB()); }
    @Override public boolean includeARDOUGNE_ALLOTMENT_N() { return bool("patch_ARDOUGNE_ALLOTMENT_N", ClassicTreeRunConfig.super.includeARDOUGNE_ALLOTMENT_N()); }
    @Override public boolean includeARDOUGNE_ALLOTMENT_S() { return bool("patch_ARDOUGNE_ALLOTMENT_S", ClassicTreeRunConfig.super.includeARDOUGNE_ALLOTMENT_S()); }
    @Override public boolean includeARDOUGNE_FLOWER() { return bool("patch_ARDOUGNE_FLOWER", ClassicTreeRunConfig.super.includeARDOUGNE_FLOWER()); }
    @Override public boolean includeARDOUGNE_HERB() { return bool("patch_ARDOUGNE_HERB", ClassicTreeRunConfig.super.includeARDOUGNE_HERB()); }
    @Override public boolean includeHOSIDIUS_ALLOTMENT_NE() { return bool("patch_HOSIDIUS_ALLOTMENT_NE", ClassicTreeRunConfig.super.includeHOSIDIUS_ALLOTMENT_NE()); }
    @Override public boolean includeHOSIDIUS_ALLOTMENT_SW() { return bool("patch_HOSIDIUS_ALLOTMENT_SW", ClassicTreeRunConfig.super.includeHOSIDIUS_ALLOTMENT_SW()); }
    @Override public boolean includeHOSIDIUS_FLOWER() { return bool("patch_HOSIDIUS_FLOWER", ClassicTreeRunConfig.super.includeHOSIDIUS_FLOWER()); }
    @Override public boolean includeHOSIDIUS_HERB() { return bool("patch_HOSIDIUS_HERB", ClassicTreeRunConfig.super.includeHOSIDIUS_HERB()); }
    @Override public boolean includeHARMONY_ALLOTMENT() { return bool("patch_HARMONY_ALLOTMENT", ClassicTreeRunConfig.super.includeHARMONY_ALLOTMENT()); }
    @Override public boolean includeHARMONY_HERB() { return bool("patch_HARMONY_HERB", ClassicTreeRunConfig.super.includeHARMONY_HERB()); }
    @Override public boolean includeGUILD_ALLOTMENT_N() { return bool("patch_GUILD_ALLOTMENT_N", ClassicTreeRunConfig.super.includeGUILD_ALLOTMENT_N()); }
    @Override public boolean includeGUILD_ALLOTMENT_S() { return bool("patch_GUILD_ALLOTMENT_S", ClassicTreeRunConfig.super.includeGUILD_ALLOTMENT_S()); }
    @Override public boolean includeGUILD_FLOWER() { return bool("patch_GUILD_FLOWER", ClassicTreeRunConfig.super.includeGUILD_FLOWER()); }
    @Override public boolean includeGUILD_HERB() { return bool("patch_GUILD_HERB", ClassicTreeRunConfig.super.includeGUILD_HERB()); }
    @Override public boolean includePRIF_ALLOTMENT_N() { return bool("patch_PRIF_ALLOTMENT_N", ClassicTreeRunConfig.super.includePRIF_ALLOTMENT_N()); }
    @Override public boolean includePRIF_ALLOTMENT_S() { return bool("patch_PRIF_ALLOTMENT_S", ClassicTreeRunConfig.super.includePRIF_ALLOTMENT_S()); }
    @Override public boolean includePRIF_FLOWER() { return bool("patch_PRIF_FLOWER", ClassicTreeRunConfig.super.includePRIF_FLOWER()); }
    @Override public boolean includeTROLL_HERB() { return bool("patch_TROLL_HERB", ClassicTreeRunConfig.super.includeTROLL_HERB()); }
    @Override public boolean includeWEISS_HERB() { return bool("patch_WEISS_HERB", ClassicTreeRunConfig.super.includeWEISS_HERB()); }
    @Override public boolean includeORTUS_ALLOTMENT_N() { return bool("patch_ORTUS_ALLOTMENT_N", ClassicTreeRunConfig.super.includeORTUS_ALLOTMENT_N()); }
    @Override public boolean includeORTUS_ALLOTMENT_S() { return bool("patch_ORTUS_ALLOTMENT_S", ClassicTreeRunConfig.super.includeORTUS_ALLOTMENT_S()); }
    @Override public boolean includeORTUS_FLOWER() { return bool("patch_ORTUS_FLOWER", ClassicTreeRunConfig.super.includeORTUS_FLOWER()); }
    @Override public boolean includeORTUS_HERB() { return bool("patch_ORTUS_HERB", ClassicTreeRunConfig.super.includeORTUS_HERB()); }
    @Override public boolean includeKASTORI_FLOWER() { return bool("patch_KASTORI_FLOWER", ClassicTreeRunConfig.super.includeKASTORI_FLOWER()); }
    @Override public boolean includeLUMBRIDGE_HOPS() { return bool("patch_LUMBRIDGE_HOPS", ClassicTreeRunConfig.super.includeLUMBRIDGE_HOPS()); }
    @Override public boolean includeSEERS_HOPS() { return bool("patch_SEERS_HOPS", ClassicTreeRunConfig.super.includeSEERS_HOPS()); }
    @Override public boolean includeYANILLE_HOPS() { return bool("patch_YANILLE_HOPS", ClassicTreeRunConfig.super.includeYANILLE_HOPS()); }
    @Override public boolean includeENTRANA_HOPS() { return bool("patch_ENTRANA_HOPS", ClassicTreeRunConfig.super.includeENTRANA_HOPS()); }
    @Override public boolean includeALDARIN_HOPS() { return bool("patch_ALDARIN_HOPS", ClassicTreeRunConfig.super.includeALDARIN_HOPS()); }
    @Override public boolean includeCHAMPIONS_BUSH() { return bool("patch_CHAMPIONS_BUSH", ClassicTreeRunConfig.super.includeCHAMPIONS_BUSH()); }
    @Override public boolean includeRIMMINGTON_BUSH() { return bool("patch_RIMMINGTON_BUSH", ClassicTreeRunConfig.super.includeRIMMINGTON_BUSH()); }
    @Override public boolean includeARDOUGNE_BUSH() { return bool("patch_ARDOUGNE_BUSH", ClassicTreeRunConfig.super.includeARDOUGNE_BUSH()); }
    @Override public boolean includeETCETERIA_BUSH() { return bool("patch_ETCETERIA_BUSH", ClassicTreeRunConfig.super.includeETCETERIA_BUSH()); }
    @Override public boolean includeGUILD_BUSH() { return bool("patch_GUILD_BUSH", ClassicTreeRunConfig.super.includeGUILD_BUSH()); }
    @Override public boolean includeAL_KHARID_CACTUS() { return bool("patch_AL_KHARID_CACTUS", ClassicTreeRunConfig.super.includeAL_KHARID_CACTUS()); }
    @Override public boolean includeGUILD_CACTUS() { return bool("patch_GUILD_CACTUS", ClassicTreeRunConfig.super.includeGUILD_CACTUS()); }
    @Override public boolean includeSEAWEED_N() { return bool("patch_SEAWEED_N", ClassicTreeRunConfig.super.includeSEAWEED_N()); }
    @Override public boolean includeSEAWEED_S() { return bool("patch_SEAWEED_S", ClassicTreeRunConfig.super.includeSEAWEED_S()); }
    @Override public boolean includeVINERY_E1() { return bool("patch_VINERY_E1", ClassicTreeRunConfig.super.includeVINERY_E1()); }
    @Override public boolean includeVINERY_E2() { return bool("patch_VINERY_E2", ClassicTreeRunConfig.super.includeVINERY_E2()); }
    @Override public boolean includeVINERY_E3() { return bool("patch_VINERY_E3", ClassicTreeRunConfig.super.includeVINERY_E3()); }
    @Override public boolean includeVINERY_E4() { return bool("patch_VINERY_E4", ClassicTreeRunConfig.super.includeVINERY_E4()); }
    @Override public boolean includeVINERY_E5() { return bool("patch_VINERY_E5", ClassicTreeRunConfig.super.includeVINERY_E5()); }
    @Override public boolean includeVINERY_E6() { return bool("patch_VINERY_E6", ClassicTreeRunConfig.super.includeVINERY_E6()); }
    @Override public boolean includeVINERY_W1() { return bool("patch_VINERY_W1", ClassicTreeRunConfig.super.includeVINERY_W1()); }
    @Override public boolean includeVINERY_W2() { return bool("patch_VINERY_W2", ClassicTreeRunConfig.super.includeVINERY_W2()); }
    @Override public boolean includeVINERY_W3() { return bool("patch_VINERY_W3", ClassicTreeRunConfig.super.includeVINERY_W3()); }
    @Override public boolean includeVINERY_W4() { return bool("patch_VINERY_W4", ClassicTreeRunConfig.super.includeVINERY_W4()); }
    @Override public boolean includeVINERY_W5() { return bool("patch_VINERY_W5", ClassicTreeRunConfig.super.includeVINERY_W5()); }
    @Override public boolean includeVINERY_W6() { return bool("patch_VINERY_W6", ClassicTreeRunConfig.super.includeVINERY_W6()); }
    @Override public boolean includeCANIFIS_MUSHROOM() { return bool("patch_CANIFIS_MUSHROOM", ClassicTreeRunConfig.super.includeCANIFIS_MUSHROOM()); }
    @Override public boolean includeDRAYNOR_BELLADONNA() { return bool("patch_DRAYNOR_BELLADONNA", ClassicTreeRunConfig.super.includeDRAYNOR_BELLADONNA()); }
    @Override public boolean includeAUBURNVALE_BELLADONNA() { return bool("patch_AUBURNVALE_BELLADONNA", ClassicTreeRunConfig.super.includeAUBURNVALE_BELLADONNA()); }
    @Override public boolean includeGUILD_HESPORI() { return bool("patch_GUILD_HESPORI", ClassicTreeRunConfig.super.includeGUILD_HESPORI()); }
    @Override public boolean includeGUILD_ANIMA() { return bool("patch_GUILD_ANIMA", ClassicTreeRunConfig.super.includeGUILD_ANIMA()); }
    @Override public boolean includePRIF_CRYSTAL() { return bool("patch_PRIF_CRYSTAL", ClassicTreeRunConfig.super.includePRIF_CRYSTAL()); }
    @Override public boolean includeGUILD_CELASTRUS() { return bool("patch_GUILD_CELASTRUS", ClassicTreeRunConfig.super.includeGUILD_CELASTRUS()); }
    @Override public boolean includeGUILD_REDWOOD() { return bool("patch_GUILD_REDWOOD", ClassicTreeRunConfig.super.includeGUILD_REDWOOD()); }
    @Override public boolean includeCORAL_E() { return bool("patch_CORAL_E", ClassicTreeRunConfig.super.includeCORAL_E()); }
    @Override public boolean includeCORAL_W() { return bool("patch_CORAL_W", ClassicTreeRunConfig.super.includeCORAL_W()); }
    @Override public boolean includeETCETERIA_SPIRIT() { return bool("patch_ETCETERIA_SPIRIT", ClassicTreeRunConfig.super.includeETCETERIA_SPIRIT()); }
    @Override public boolean includePORT_SARIM_SPIRIT() { return bool("patch_PORT_SARIM_SPIRIT", ClassicTreeRunConfig.super.includePORT_SARIM_SPIRIT()); }
    @Override public boolean includeBRIMHAVEN_SPIRIT() { return bool("patch_BRIMHAVEN_SPIRIT", ClassicTreeRunConfig.super.includeBRIMHAVEN_SPIRIT()); }
    @Override public boolean includeHOSIDIUS_SPIRIT() { return bool("patch_HOSIDIUS_SPIRIT", ClassicTreeRunConfig.super.includeHOSIDIUS_SPIRIT()); }
    @Override public boolean includeGUILD_SPIRIT() { return bool("patch_GUILD_SPIRIT", ClassicTreeRunConfig.super.includeGUILD_SPIRIT()); }
}
