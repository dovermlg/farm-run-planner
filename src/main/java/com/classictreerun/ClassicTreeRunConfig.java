package com.classictreerun;

import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(ClassicTreeRunConfig.GROUP)
public interface ClassicTreeRunConfig extends net.runelite.client.config.Config
{
    String GROUP = "classictreerun";
    enum FaladorTravel { RING_OF_WEALTH, FALADOR_TELEPORT }
    enum TaverleyTravel { REDIRECTED_TABLET, BALLOON, FALADOR_TELEPORT }
    enum StrongholdTravel { SLAYER_RING, SPIRIT_TREE }
    enum CatherbyTravel { CATHERBY_TELEPORT, CAMELOT_TELEPORT }
    enum BrimhavenTravel
    {
        REDIRECTED_TABLET, SPIRIT_TREE, ARDOUGNE_BOAT, CATHERBY_CHARTER;
        @Override public String toString()
        {
            return this == CATHERBY_CHARTER ? "Catherby charter ship" : name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        }
    }
    enum CalquatTravel { TAI_BWO_WANNAI_SCROLL, FAIRY_RING_CKR, WALK_FROM_BRIMHAVEN }
    enum FossilTravel { DIGSITE_BARGE, HOUSE_ON_HILL_MUSHTREE }
    enum GuildTravel { SKILLS_NECKLACE, FARMING_CAPE, SPIRIT_TREE }

    @ConfigSection(name="Your run", description="Create and save named presets here; choose a saved run in the tree tab", position=0, closedByDefault=false)
    String RUN = "run";
    @ConfigSection(name="Travel", description="Choose the teleport methods you can use", position=1, closedByDefault=true)
    String TRAVEL = "travel";
    @ConfigSection(name="Checklist display", description="Control the run tab", position=2, closedByDefault=true)
    String DISPLAY = "display";
    @ConfigSection(name="Preset backups", description="Import or export saved presets as local JSON files. Results appear in the run tab.", position=3, closedByDefault=true)
    String BACKUPS = "backups";

    @ConfigSection(name="Crops and supplies", description="Choose what to replant for each patch type. Save preset to keep these choices. No lower-tier substitutions.", position=4, closedByDefault=true)
    String CROPS = "crops";

    @ConfigItem(keyName="cropTree", name="Tree", description="Chosen crop for every selected tree patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=0)
    default CropChoices.Tree cropTree() { return CropChoices.Tree.OAK; }
    @ConfigItem(keyName="cropFruit", name="Fruit tree", description="Chosen crop for every selected fruit patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=1)
    default CropChoices.Fruit cropFruit() { return CropChoices.Fruit.APPLE; }
    @ConfigItem(keyName="cropHardwood", name="Hardwood", description="Chosen crop for every selected hardwood patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=2)
    default CropChoices.Hardwood cropHardwood() { return CropChoices.Hardwood.TEAK; }
    @ConfigItem(keyName="cropAllotment", name="Allotment", description="Chosen crop for every selected allotment patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=3)
    default CropChoices.Allotment cropAllotment() { return CropChoices.Allotment.POTATO; }
    @ConfigItem(keyName="cropFlower", name="Flower", description="Chosen crop for every selected flower patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=4)
    default CropChoices.Flower cropFlower() { return CropChoices.Flower.MARIGOLD; }
    @ConfigItem(keyName="cropHerb", name="Herb", description="Chosen crop for every selected herb patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=5)
    default CropChoices.Herb cropHerb() { return CropChoices.Herb.GUAM; }
    @ConfigItem(keyName="cropHops", name="Hops", description="Chosen crop for every selected hops patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=6)
    default CropChoices.Hops cropHops() { return CropChoices.Hops.BARLEY; }
    @ConfigItem(keyName="cropBush", name="Bush", description="Chosen crop for every selected bush patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=7)
    default CropChoices.Bush cropBush() { return CropChoices.Bush.REDBERRIES; }
    @ConfigItem(keyName="cropCactus", name="Cactus", description="Chosen crop for every selected cactus patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=8)
    default CropChoices.Cactus cropCactus() { return CropChoices.Cactus.CACTUS; }
    @ConfigItem(keyName="cropCalquat", name="Calquat", description="Chosen crop for every selected calquat patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=9)
    default CropChoices.Calquat cropCalquat() { return CropChoices.Calquat.CALQUAT; }
    @ConfigItem(keyName="cropSeaweed", name="Seaweed", description="Chosen crop for every selected seaweed patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=10)
    default CropChoices.Seaweed cropSeaweed() { return CropChoices.Seaweed.GIANT_SEAWEED; }
    @ConfigItem(keyName="cropGrape", name="Grape", description="Chosen crop for every selected grape patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=11)
    default CropChoices.Grape cropGrape() { return CropChoices.Grape.GRAPE; }
    @ConfigItem(keyName="cropMushroom", name="Mushroom", description="Chosen crop for every selected mushroom patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=12)
    default CropChoices.Mushroom cropMushroom() { return CropChoices.Mushroom.BITTERCAP; }
    @ConfigItem(keyName="cropBelladonna", name="Belladonna", description="Chosen crop for every selected belladonna patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=13)
    default CropChoices.Belladonna cropBelladonna() { return CropChoices.Belladonna.BELLADONNA; }
    @ConfigItem(keyName="cropHespori", name="Hespori", description="Chosen crop for every selected hespori patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=14)
    default CropChoices.Hespori cropHespori() { return CropChoices.Hespori.HESPORI; }
    @ConfigItem(keyName="cropAnima", name="Anima", description="Chosen crop for every selected anima patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=15)
    default CropChoices.Anima cropAnima() { return CropChoices.Anima.ATTAS; }
    @ConfigItem(keyName="cropCrystal", name="Crystal", description="Chosen crop for every selected crystal patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=16)
    default CropChoices.Crystal cropCrystal() { return CropChoices.Crystal.CRYSTAL; }
    @ConfigItem(keyName="cropCelastrus", name="Celastrus", description="Chosen crop for every selected celastrus patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=17)
    default CropChoices.Celastrus cropCelastrus() { return CropChoices.Celastrus.CELASTRUS; }
    @ConfigItem(keyName="cropRedwood", name="Redwood", description="Chosen crop for every selected redwood patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=18)
    default CropChoices.Redwood cropRedwood() { return CropChoices.Redwood.REDWOOD; }
    @ConfigItem(keyName="cropCoral", name="Coral", description="Chosen crop for every selected coral patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=19)
    default CropChoices.Coral cropCoral() { return CropChoices.Coral.ELKHORN; }
    @ConfigItem(keyName="cropSpirit", name="Spirit", description="Chosen crop for every selected spirit patch. Harvest / check only omits replanting supplies and payments.", section=CROPS, position=20)
    default CropChoices.Spirit cropSpirit() { return CropChoices.Spirit.SPIRIT; }
    @ConfigItem(keyName="supplyProtection", name="Include protection payments", description="Optional gardener payments are included by default, where this patch has a gardener. Not owning them does not block starting a run.", section=CROPS, position=30)
    default boolean supplyProtection() { return true; }
    @ConfigItem(keyName="supplyTools", name="Include farming tools", description="Recommend a spade, secateurs where useful and special tools. Turn off if you use stored tools. Axes and combat equipment remain your choice.", section=CROPS, position=31)
    default boolean supplyTools() { return true; }
    @ConfigItem(keyName="supplyRake", name="Include rake", description="Turn off if you have Auto Weed or use a leprechaun rake.", section=CROPS, position=32)
    default boolean supplyRake() { return true; }
    @ConfigItem(keyName="supplyDibber", name="Include seed dibber", description="Turn off if Barbarian Training lets you plant seeds without a dibber.", section=CROPS, position=33)
    default boolean supplyDibber() { return true; }
    @ConfigItem(keyName="supplyCoins", name="Coin budget", description="Coins to pack. Choose a budget for your fares and gardener removal fees; this is not a calculated total.", section=CROPS, position=34)
    @net.runelite.client.config.Range(min=0)
    default int supplyCoins() { return 10000; }
    @ConfigItem(keyName="supplyCompost", name="Compost", description="Buckets for replanting eligible crops. Paid trees without variable yields and crops that cannot use compost are omitted. Bottomless bucket charges and leprechaun storage are not counted.", section=CROPS, position=35)
    default SupplyPlan.Compost supplyCompost() { return SupplyPlan.Compost.ULTRACOMPOST; }

    @ConfigItem(keyName="backupFilename", name="Backup filename", description="A .json filename inside plugin-data/farm-run-planner/preset-backups, for example presets.json. Save draft edits before exporting.", section=BACKUPS, position=0)
    default String backupFilename() { return "presets.json"; }
    @ConfigItem(keyName="exportPresetsAction", name="Export presets", description="Export all saved presets to the local backup file. Click in either direction; the result appears in the run tab.", warning="Export all saved presets? An existing backup with this filename will be replaced. Character data, crop observations and completion marks are excluded.", section=BACKUPS, position=1)
    default boolean exportPresetsAction() { return false; }
    @ConfigItem(keyName="importPresetsAction", name="Import presets", description="Import a JSON backup from the local preset-backups folder. Keeps existing presets, skips identical copies and renames conflicting imports.", warning="Import saved presets from the selected local backup file? Existing presets and the current run will be kept. The whole import is cancelled if the file is invalid or there are not enough free slots.", section=BACKUPS, position=2)
    default boolean importPresetsAction() { return false; }

    @ConfigItem(keyName="preset", name="Preset", description="Legacy preset identifier retained for migration", section=RUN, position=0, hidden=true)
    default RunPreset preset() { return RunPreset.CUSTOM; }
    @ConfigItem(keyName="editorPreset", name="Preset to edit", description="Choose a saved slot or an empty slot. After switching, leave and reopen this cog page so its controls refresh. Changes are saved only with Save preset.", section=RUN, position=0)
    default EditorPreset editorPreset() { return EditorPreset.CURRENT; }
    @ConfigItem(keyName="runName", name="Preset name", description="Name your preset (1-60 characters), choose patches and travel, then click Save preset below.", section=RUN, position=1)
    default String runName() { return "My farming run"; }
    @ConfigItem(keyName="savePresetAction", name="Save preset", description="Click in either direction to save the selected slot. Changing its name renames that preset; choose an empty slot to create a separate copy.", warning="Save the current settings to the selected preset slot? Its saved contents and name will be updated.", section=RUN, position=3)
    default boolean savePresetAction() { return false; }

    @ConfigItem(keyName="deletePresetAction", name="Delete named preset", description="Click in either direction to delete the selected saved slot. Keeps the current working run.", warning="Delete the selected saved preset? The current working run will be kept.", section=RUN, position=4)
    default boolean deletePresetAction() { return false; }

    @ConfigItem(keyName="autoOptimize", name="Optimize route", description="Automatically order selected patches using estimated travel costs", section=RUN, position=2)
    default boolean autoOptimize() { return true; }
    @ConfigItem(keyName="trackGrowth", name="Track crop readiness", description="Observe nearby crop states, show per-patch estimated ready times and a full-run estimate. Unknown or unhealthy crops block full-run readiness.", section=RUN, position=5)
    default boolean trackGrowth() { return true; }
    @ConfigItem(keyName="notifyWhenReady", name="Notify when run is ready", description="Use RuneLite notifications once all selected patches in a saved preset are estimated ready. RuneLite must remain running; visit patches to obtain observations.", section=RUN, position=6)
    default boolean notifyWhenReady() { return true; }
    @ConfigItem(keyName="autoCompleteReplanted", name="Auto-mark replanted patches", description="Optionally tick a stop after observing a confirmed replanting transition during this visit. Never performs game actions. Manual completion remains available.", section=RUN, position=7)
    default boolean autoCompleteReplanted() { return false; }
    @ConfigItem(keyName="showDetails", name="Show access notes", description="Show gardener and access requirements on each checklist stop", section=DISPLAY, position=1)
    default boolean showDetails() { return false; }

    @ConfigItem(keyName="trees", name="Normal trees", description="Legacy category switch", hidden=true)
    default boolean trees() { return true; }
    @ConfigItem(keyName="fruit", name="Fruit trees", description="Legacy category switch", hidden=true)
    default boolean fruit() { return true; }
    @ConfigItem(keyName="hardwood", name="Hardwoods", description="Legacy category switch", hidden=true)
    default boolean hardwood() { return true; }
    @ConfigItem(keyName="calquat", name="Calquat", description="Legacy category switch", hidden=true)
    default boolean calquat() { return true; }
    @ConfigItem(keyName="hideCompleted", name="Hide completed", description="Hide checked stops from the run view", section=DISPLAY, position=0)
    default boolean hideCompleted() { return false; }
    @ConfigItem(keyName="otherPatches", name="Other crop types", description="Legacy category switch", hidden=true)
    default boolean otherPatches() { return true; }
    @ConfigItem(keyName="selectionV2", name="Patch selection", description="Mirrors the selected patch settings", hidden=true)
    default String selection() { return ""; }
    @ConfigItem(keyName="faladorTravel", name="Falador travel", description="Preferred travel instruction", section=TRAVEL, position=10)
    default FaladorTravel faladorTravel() { return FaladorTravel.RING_OF_WEALTH; }
    @ConfigItem(keyName="taverleyTravel", name="Taverley travel", description="Balloon requires Enlightened Journey and route unlock", section=TRAVEL, position=11)
    default TaverleyTravel taverleyTravel() { return TaverleyTravel.REDIRECTED_TABLET; }
    @ConfigItem(keyName="strongholdTravel", name="Stronghold tree travel", description="Preferred entry for the normal tree", section=TRAVEL, position=12)
    default StrongholdTravel strongholdTravel() { return StrongholdTravel.SLAYER_RING; }
    @ConfigItem(keyName="catherbyTravel", name="Catherby travel", description="Catherby spell is Lunar; tablets avoid spellbook switching", section=TRAVEL, position=13)
    default CatherbyTravel catherbyTravel() { return CatherbyTravel.CATHERBY_TELEPORT; }
    @ConfigItem(keyName="brimhavenTravel", name="Brimhaven travel", description="Catherby charter walks from the fruit patch to the docks; bring coins. Spirit-tree option requires your grown tree.", section=TRAVEL, position=14)
    default BrimhavenTravel brimhavenTravel() { return BrimhavenTravel.REDIRECTED_TABLET; }
    @ConfigItem(keyName="calquatTravel", name="Calquat travel", description="Scroll requires Tai Bwo Wannai Trio", section=TRAVEL, position=15)
    default CalquatTravel calquatTravel() { return CalquatTravel.TAI_BWO_WANNAI_SCROLL; }
    @ConfigItem(keyName="fossilTravel", name="Fossil Island travel", description="Choose barge or unlocked pendant/mushtree route", section=TRAVEL, position=16)
    default FossilTravel fossilTravel() { return FossilTravel.DIGSITE_BARGE; }
    @ConfigItem(keyName="guildTravel", name="Guild travel", description="Farming cape requires 99; spirit tree must be grown", section=TRAVEL, position=17)
    default GuildTravel guildTravel() { return GuildTravel.SKILLS_NECKLACE; }

    @ConfigItem(keyName="order", name="Custom order", description="Prepared route order", hidden=true)
    default String order() { return ""; }
    @ConfigItem(keyName="excluded", name="Excluded patches", description="Managed by patch settings", hidden=true)
    default String excluded() { return ""; }

    @ConfigSection(name="Normal tree patches", description="Choose the patches to include in your run", position=10, closedByDefault=true)
    String PATCHES_TREE = "patches_tree";

    @ConfigSection(name="Fruit tree patches", description="Choose the patches to include in your run", position=11, closedByDefault=true)
    String PATCHES_FRUIT = "patches_fruit";

    @ConfigSection(name="Hardwood patches", description="Choose the patches to include in your run", position=12, closedByDefault=true)
    String PATCHES_HARDWOOD = "patches_hardwood";

    @ConfigSection(name="Calquat patches", description="Choose the patches to include in your run", position=13, closedByDefault=true)
    String PATCHES_CALQUAT = "patches_calquat";

    @ConfigSection(name="Allotment patches", description="Choose the patches to include in your run", position=14, closedByDefault=true)
    String PATCHES_ALLOTMENT = "patches_allotment";

    @ConfigSection(name="Flower patches", description="Choose the patches to include in your run", position=15, closedByDefault=true)
    String PATCHES_FLOWER = "patches_flower";

    @ConfigSection(name="Herb patches", description="Choose the patches to include in your run", position=16, closedByDefault=true)
    String PATCHES_HERB = "patches_herb";

    @ConfigSection(name="Hops patches", description="Choose the patches to include in your run", position=17, closedByDefault=true)
    String PATCHES_HOPS = "patches_hops";

    @ConfigSection(name="Bush patches", description="Choose the patches to include in your run", position=18, closedByDefault=true)
    String PATCHES_BUSH = "patches_bush";

    @ConfigSection(name="Cactus patches", description="Choose the patches to include in your run", position=19, closedByDefault=true)
    String PATCHES_CACTUS = "patches_cactus";

    @ConfigSection(name="Seaweed patches", description="Choose the patches to include in your run", position=20, closedByDefault=true)
    String PATCHES_SEAWEED = "patches_seaweed";

    @ConfigSection(name="Grape patches", description="Choose the patches to include in your run", position=21, closedByDefault=true)
    String PATCHES_GRAPE = "patches_grape";

    @ConfigSection(name="Mushroom patches", description="Choose the patches to include in your run", position=22, closedByDefault=true)
    String PATCHES_MUSHROOM = "patches_mushroom";

    @ConfigSection(name="Belladonna patches", description="Choose the patches to include in your run", position=23, closedByDefault=true)
    String PATCHES_BELLADONNA = "patches_belladonna";

    @ConfigSection(name="Hespori patches", description="Choose the patches to include in your run", position=24, closedByDefault=true)
    String PATCHES_HESPORI = "patches_hespori";

    @ConfigSection(name="Anima patches", description="Choose the patches to include in your run", position=25, closedByDefault=true)
    String PATCHES_ANIMA = "patches_anima";

    @ConfigSection(name="Crystal tree patches", description="Choose the patches to include in your run", position=26, closedByDefault=true)
    String PATCHES_CRYSTAL = "patches_crystal";

    @ConfigSection(name="Celastrus patches", description="Choose the patches to include in your run", position=27, closedByDefault=true)
    String PATCHES_CELASTRUS = "patches_celastrus";

    @ConfigSection(name="Redwood patches", description="Choose the patches to include in your run", position=28, closedByDefault=true)
    String PATCHES_REDWOOD = "patches_redwood";

    @ConfigSection(name="Coral nursery patches", description="Choose the patches to include in your run", position=29, closedByDefault=true)
    String PATCHES_CORAL = "patches_coral";

    @ConfigSection(name="Spirit tree patches", description="Choose the patches to include in your run", position=30, closedByDefault=true)
    String PATCHES_SPIRIT = "patches_spirit";

    @ConfigItem(keyName="patch_LUMBRIDGE_TREE", name="Lumbridge", description="West of Lumbridge Castle. Access: 15 Farming or the level for your sapling", section=PATCHES_TREE, position=0)
    default boolean includeLUMBRIDGE_TREE() { return true; }

    @ConfigItem(keyName="patch_VARROCK_TREE", name="Varrock Castle", description="Castle courtyard. Access: 15 Farming or the level for your sapling", section=PATCHES_TREE, position=1)
    default boolean includeVARROCK_TREE() { return true; }

    @ConfigItem(keyName="patch_FALADOR_TREE", name="Falador Park", description="Inside the park. Access: 15 Farming or the level for your sapling", section=PATCHES_TREE, position=2)
    default boolean includeFALADOR_TREE() { return true; }

    @ConfigItem(keyName="patch_TAVERLEY_TREE", name="Taverley", description="Tree patch near the balloon landing. Access: 15 Farming or the level for your sapling", section=PATCHES_TREE, position=3)
    default boolean includeTAVERLEY_TREE() { return true; }

    @ConfigItem(keyName="patch_STRONGHOLD_TREE", name="Gnome Stronghold", description="South-west of the spirit tree, near the Slayer Cave. Access: 15 Farming or the level for your sapling", section=PATCHES_TREE, position=4)
    default boolean includeSTRONGHOLD_TREE() { return true; }

    @ConfigItem(keyName="patch_STRONGHOLD_FRUIT", name="Gnome Stronghold", description="East of the spirit tree. Access: 27 Farming or the level for your sapling", section=PATCHES_FRUIT, position=5)
    default boolean includeSTRONGHOLD_FRUIT() { return true; }

    @ConfigItem(keyName="patch_VILLAGE_FRUIT", name="Tree Gnome Village", description="Outside, west of the maze. Access: 27 Farming; Tree Gnome Village for spirit tree travel", section=PATCHES_FRUIT, position=6)
    default boolean includeVILLAGE_FRUIT() { return true; }

    @ConfigItem(keyName="patch_LLETYA_FRUIT", name="Lletya", description="Fruit patch in Lletya. Access: 27 Farming; access during Mourning's End Part I", section=PATCHES_FRUIT, position=7)
    default boolean includeLLETYA_FRUIT() { return true; }

    @ConfigItem(keyName="patch_CATHERBY_FRUIT", name="Catherby", description="East along the shore, near the fishing spots. Access: 27 Farming or the level for your sapling", section=PATCHES_FRUIT, position=8)
    default boolean includeCATHERBY_FRUIT() { return true; }

    @ConfigItem(keyName="patch_BRIMHAVEN_FRUIT", name="Brimhaven", description="North of Brimhaven, near the docks. Access: 27 Farming or the level for your sapling", section=PATCHES_FRUIT, position=9)
    default boolean includeBRIMHAVEN_FRUIT() { return true; }

    @ConfigItem(keyName="patch_TAI_BWO_WANNAI_CALQUAT", name="Tai Bwo Wannai", description="North of the village. Access: 72 Farming; Tai Bwo Wannai Trio for the teleport scroll", section=PATCHES_CALQUAT, position=10)
    default boolean includeTAI_BWO_WANNAI_CALQUAT() { return true; }

    @ConfigItem(keyName="patch_FOSSIL_EAST", name="Fossil Island east", description="Eastern hardwood patch in the Mushroom Forest. Access: Bone Voyage; 35 Farming minimum; teak 35, mahogany 55, camphor 66, ironwood 80, rosewood 92", section=PATCHES_HARDWOOD, position=11)
    default boolean includeFOSSIL_EAST() { return true; }

    @ConfigItem(keyName="patch_FOSSIL_MIDDLE", name="Fossil Island middle", description="Middle hardwood patch in the Mushroom Forest. Access: Bone Voyage; 35 Farming minimum; teak 35, mahogany 55, camphor 66, ironwood 80, rosewood 92", section=PATCHES_HARDWOOD, position=12)
    default boolean includeFOSSIL_MIDDLE() { return true; }

    @ConfigItem(keyName="patch_FOSSIL_WEST", name="Fossil Island west", description="Western hardwood patch in the Mushroom Forest. Access: Bone Voyage; 35 Farming minimum; teak 35, mahogany 55, camphor 66, ironwood 80, rosewood 92", section=PATCHES_HARDWOOD, position=13)
    default boolean includeFOSSIL_WEST() { return true; }

    @ConfigItem(keyName="patch_GUILD_TREE", name="Farming Guild", description="West wing tree patch. Access: 65 Farming to enter this wing; sapling's level also applies", section=PATCHES_TREE, position=14)
    default boolean includeGUILD_TREE() { return true; }

    @ConfigItem(keyName="patch_GUILD_FRUIT", name="Farming Guild", description="North wing fruit patch. Access: 85 Farming to enter this wing; sapling's level also applies", section=PATCHES_FRUIT, position=15)
    default boolean includeGUILD_FRUIT() { return true; }

    @ConfigItem(keyName="patch_NEMUS_TREE", name="Nemus Retreat - Normal tree patch", description="Normal tree patch. Access: 15 Farming minimum; Fairy-ring access; pendant destination must be activated if used", section=PATCHES_TREE, position=16)
    default boolean includeNEMUS_TREE() { return false; }

    @ConfigItem(keyName="patch_KASTORI_FRUIT", name="Kastori - Fruit patch north of town", description="Fruit patch north of town. Access: 27 Farming minimum; Build the Kastori quetzal landing site; alternatively activate the Pendant of Ates statue", section=PATCHES_FRUIT, position=17)
    default boolean includeKASTORI_FRUIT() { return false; }

    @ConfigItem(keyName="patch_LOCUS_HARDWOOD", name="Locus Oasis - Hardwood patch", description="Hardwood patch. Access: 35 Farming minimum; The Ribbiting Tale of a Lily Pad Labour Dispute", section=PATCHES_HARDWOOD, position=18)
    default boolean includeLOCUS_HARDWOOD() { return false; }

    @ConfigItem(keyName="patch_ANGLERS_HARDWOOD", name="Anglers' Retreat - Hardwood patch", description="Hardwood patch. Access: 35 Farming minimum; 51 Sailing to moor; first visit to build the return rowboat (50 Construction, 10 swamp paste, 8 camphor planks, hammer and saw)", section=PATCHES_HARDWOOD, position=19)
    default boolean includeANGLERS_HARDWOOD() { return false; }

    @ConfigItem(keyName="patch_KASTORI_CALQUAT", name="Kastori - Calquat patch north-east of landing", description="Calquat patch north-east of landing. Access: 72 Farming; Build the Kastori quetzal landing site; alternatively activate the Pendant of Ates statue", section=PATCHES_CALQUAT, position=20)
    default boolean includeKASTORI_CALQUAT() { return false; }

    @ConfigItem(keyName="patch_CONCH_CALQUAT", name="The Great Conch - Summer Shore calquat patch", description="Summer Shore calquat patch. Access: 72 Farming; 45 Sailing and progress in Troubled Tortugans for island access; ring access must be unlocked", section=PATCHES_CALQUAT, position=21)
    default boolean includeCONCH_CALQUAT() { return false; }

    @ConfigItem(keyName="patch_FALADOR_ALLOTMENT_NW", name="South Falador farm - North-west allotment", description="North-west allotment. Access: 1 Farming minimum; Medium Lumbridge & Draynor diary for ring 2; lower-tier daily charge limits apply", section=PATCHES_ALLOTMENT, position=22)
    default boolean includeFALADOR_ALLOTMENT_NW() { return false; }

    @ConfigItem(keyName="patch_FALADOR_ALLOTMENT_SE", name="South Falador farm - South-east allotment", description="South-east allotment. Access: 1 Farming minimum; Medium Lumbridge & Draynor diary for ring 2; lower-tier daily charge limits apply", section=PATCHES_ALLOTMENT, position=23)
    default boolean includeFALADOR_ALLOTMENT_SE() { return false; }

    @ConfigItem(keyName="patch_FALADOR_FLOWER", name="South Falador farm - Flower patch", description="Flower patch. Access: 2 Farming minimum; Medium Lumbridge & Draynor diary for ring 2; lower-tier daily charge limits apply", section=PATCHES_FLOWER, position=24)
    default boolean includeFALADOR_FLOWER() { return false; }

    @ConfigItem(keyName="patch_FALADOR_HERB", name="South Falador farm - Herb patch", description="Herb patch. Access: 9 Farming minimum; Medium Lumbridge & Draynor diary for ring 2; lower-tier daily charge limits apply", section=PATCHES_HERB, position=25)
    default boolean includeFALADOR_HERB() { return false; }

    @ConfigItem(keyName="patch_PHASMATYS_ALLOTMENT_NW", name="Port Phasmatys farm - North-west allotment", description="North-west allotment. Access: 1 Farming minimum; Ghosts Ahoy for the ectophial, or fairy-ring access", section=PATCHES_ALLOTMENT, position=26)
    default boolean includePHASMATYS_ALLOTMENT_NW() { return false; }

    @ConfigItem(keyName="patch_PHASMATYS_ALLOTMENT_SE", name="Port Phasmatys farm - South-east allotment", description="South-east allotment. Access: 1 Farming minimum; Ghosts Ahoy for the ectophial, or fairy-ring access", section=PATCHES_ALLOTMENT, position=27)
    default boolean includePHASMATYS_ALLOTMENT_SE() { return false; }

    @ConfigItem(keyName="patch_PHASMATYS_FLOWER", name="Port Phasmatys farm - Flower patch", description="Flower patch. Access: 2 Farming minimum; Ghosts Ahoy for the ectophial, or fairy-ring access", section=PATCHES_FLOWER, position=28)
    default boolean includePHASMATYS_FLOWER() { return false; }

    @ConfigItem(keyName="patch_PHASMATYS_HERB", name="Port Phasmatys farm - Herb patch", description="Herb patch. Access: 9 Farming minimum; Ghosts Ahoy for the ectophial, or fairy-ring access", section=PATCHES_HERB, position=29)
    default boolean includePHASMATYS_HERB() { return false; }

    @ConfigItem(keyName="patch_CATHERBY_ALLOTMENT_N", name="North Catherby farm - North allotment", description="North allotment. Access: 1 Farming minimum; Lunar spell/tablet/POH for Catherby, or Camelot access", section=PATCHES_ALLOTMENT, position=30)
    default boolean includeCATHERBY_ALLOTMENT_N() { return false; }

    @ConfigItem(keyName="patch_CATHERBY_ALLOTMENT_S", name="North Catherby farm - South allotment", description="South allotment. Access: 1 Farming minimum; Lunar spell/tablet/POH for Catherby, or Camelot access", section=PATCHES_ALLOTMENT, position=31)
    default boolean includeCATHERBY_ALLOTMENT_S() { return false; }

    @ConfigItem(keyName="patch_CATHERBY_FLOWER", name="North Catherby farm - Flower patch", description="Flower patch. Access: 2 Farming minimum; Lunar spell/tablet/POH for Catherby, or Camelot access", section=PATCHES_FLOWER, position=32)
    default boolean includeCATHERBY_FLOWER() { return false; }

    @ConfigItem(keyName="patch_CATHERBY_HERB", name="North Catherby farm - Herb patch", description="Herb patch. Access: 9 Farming minimum; Lunar spell/tablet/POH for Catherby, or Camelot access", section=PATCHES_HERB, position=33)
    default boolean includeCATHERBY_HERB() { return false; }

    @ConfigItem(keyName="patch_ARDOUGNE_ALLOTMENT_N", name="North Ardougne farm - North allotment", description="North allotment. Access: 1 Farming minimum; Medium Ardougne diary for farm teleport; lower-tier daily charge limits apply", section=PATCHES_ALLOTMENT, position=34)
    default boolean includeARDOUGNE_ALLOTMENT_N() { return false; }

    @ConfigItem(keyName="patch_ARDOUGNE_ALLOTMENT_S", name="North Ardougne farm - South allotment", description="South allotment. Access: 1 Farming minimum; Medium Ardougne diary for farm teleport; lower-tier daily charge limits apply", section=PATCHES_ALLOTMENT, position=35)
    default boolean includeARDOUGNE_ALLOTMENT_S() { return false; }

    @ConfigItem(keyName="patch_ARDOUGNE_FLOWER", name="North Ardougne farm - Flower patch", description="Flower patch. Access: 2 Farming minimum; Medium Ardougne diary for farm teleport; lower-tier daily charge limits apply", section=PATCHES_FLOWER, position=36)
    default boolean includeARDOUGNE_FLOWER() { return false; }

    @ConfigItem(keyName="patch_ARDOUGNE_HERB", name="North Ardougne farm - Herb patch", description="Herb patch. Access: 9 Farming minimum; Medium Ardougne diary for farm teleport; lower-tier daily charge limits apply", section=PATCHES_HERB, position=37)
    default boolean includeARDOUGNE_HERB() { return false; }

    @ConfigItem(keyName="patch_HOSIDIUS_ALLOTMENT_NE", name="Hosidius farm - North-east allotment", description="North-east allotment. Access: 1 Farming minimum; Charged Xeric's talisman, or another unlocked Hosidius route", section=PATCHES_ALLOTMENT, position=38)
    default boolean includeHOSIDIUS_ALLOTMENT_NE() { return false; }

    @ConfigItem(keyName="patch_HOSIDIUS_ALLOTMENT_SW", name="Hosidius farm - South-west allotment", description="South-west allotment. Access: 1 Farming minimum; Charged Xeric's talisman, or another unlocked Hosidius route", section=PATCHES_ALLOTMENT, position=39)
    default boolean includeHOSIDIUS_ALLOTMENT_SW() { return false; }

    @ConfigItem(keyName="patch_HOSIDIUS_FLOWER", name="Hosidius farm - Flower patch", description="Flower patch. Access: 2 Farming minimum; Charged Xeric's talisman, or another unlocked Hosidius route", section=PATCHES_FLOWER, position=40)
    default boolean includeHOSIDIUS_FLOWER() { return false; }

    @ConfigItem(keyName="patch_HOSIDIUS_HERB", name="Hosidius farm - Herb patch", description="Herb patch. Access: 9 Farming minimum; Charged Xeric's talisman, or another unlocked Hosidius route", section=PATCHES_HERB, position=41)
    default boolean includeHOSIDIUS_HERB() { return false; }

    @ConfigItem(keyName="patch_HARMONY_ALLOTMENT", name="Harmony Island - Allotment patch", description="Allotment patch. Access: 1 Farming minimum; Elite diary is only required for the herb patch; Island access from The Great Brain Robbery; herb patch additionally needs Elite Morytania diary", section=PATCHES_ALLOTMENT, position=42)
    default boolean includeHARMONY_ALLOTMENT() { return false; }

    @ConfigItem(keyName="patch_HARMONY_HERB", name="Harmony Island - Herb patch", description="Herb patch. Access: Elite Morytania diary; Island access from The Great Brain Robbery; herb patch additionally needs Elite Morytania diary", section=PATCHES_HERB, position=43)
    default boolean includeHARMONY_HERB() { return false; }

    @ConfigItem(keyName="patch_GUILD_ALLOTMENT_N", name="Farming Guild - North allotment", description="North allotment. Access: 45 Farming guild entrance; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_ALLOTMENT, position=44)
    default boolean includeGUILD_ALLOTMENT_N() { return false; }

    @ConfigItem(keyName="patch_GUILD_ALLOTMENT_S", name="Farming Guild - South allotment", description="South allotment. Access: 45 Farming guild entrance; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_ALLOTMENT, position=45)
    default boolean includeGUILD_ALLOTMENT_S() { return false; }

    @ConfigItem(keyName="patch_GUILD_FLOWER", name="Farming Guild - Flower patch", description="Flower patch. Access: 45 Farming guild entrance; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_FLOWER, position=46)
    default boolean includeGUILD_FLOWER() { return false; }

    @ConfigItem(keyName="patch_GUILD_HERB", name="Farming Guild - Herb patch in west wing", description="Herb patch in west wing. Access: 65 Farming west wing; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_HERB, position=47)
    default boolean includeGUILD_HERB() { return false; }

    @ConfigItem(keyName="patch_PRIF_ALLOTMENT_N", name="Prifddinas - North allotment", description="North allotment. Access: 1 Farming minimum; Song of the Elves", section=PATCHES_ALLOTMENT, position=48)
    default boolean includePRIF_ALLOTMENT_N() { return false; }

    @ConfigItem(keyName="patch_PRIF_ALLOTMENT_S", name="Prifddinas - South allotment", description="South allotment. Access: 1 Farming minimum; Song of the Elves", section=PATCHES_ALLOTMENT, position=49)
    default boolean includePRIF_ALLOTMENT_S() { return false; }

    @ConfigItem(keyName="patch_PRIF_FLOWER", name="Prifddinas - Flower patch", description="Flower patch. Access: 2 Farming minimum; Song of the Elves", section=PATCHES_FLOWER, position=50)
    default boolean includePRIF_FLOWER() { return false; }

    @ConfigItem(keyName="patch_TROLL_HERB", name="Troll Stronghold roof - Rooftop herb patch", description="Rooftop herb patch. Access: 9 Farming minimum; My Arm's Big Adventure for the patch; Making Friends with My Arm for stony basalt", section=PATCHES_HERB, position=51)
    default boolean includeTROLL_HERB() { return false; }

    @ConfigItem(keyName="patch_WEISS_HERB", name="Weiss - Herb patch", description="Herb patch. Access: 9 Farming minimum; Making Friends with My Arm and built Fire of Nourishment", section=PATCHES_HERB, position=52)
    default boolean includeWEISS_HERB() { return false; }

    @ConfigItem(keyName="patch_ORTUS_ALLOTMENT_N", name="Ortus Farm - North allotment", description="North allotment. Access: 1 Farming minimum; Varlamore access; quetzal destination unlocked", section=PATCHES_ALLOTMENT, position=53)
    default boolean includeORTUS_ALLOTMENT_N() { return false; }

    @ConfigItem(keyName="patch_ORTUS_ALLOTMENT_S", name="Ortus Farm - South allotment", description="South allotment. Access: 1 Farming minimum; Varlamore access; quetzal destination unlocked", section=PATCHES_ALLOTMENT, position=54)
    default boolean includeORTUS_ALLOTMENT_S() { return false; }

    @ConfigItem(keyName="patch_ORTUS_FLOWER", name="Ortus Farm - Flower patch", description="Flower patch. Access: 2 Farming minimum; Varlamore access; quetzal destination unlocked", section=PATCHES_FLOWER, position=55)
    default boolean includeORTUS_FLOWER() { return false; }

    @ConfigItem(keyName="patch_ORTUS_HERB", name="Ortus Farm - Herb patch", description="Herb patch. Access: 9 Farming minimum; Varlamore access; quetzal destination unlocked", section=PATCHES_HERB, position=56)
    default boolean includeORTUS_HERB() { return false; }

    @ConfigItem(keyName="patch_KASTORI_FLOWER", name="Kastori - Flower patch south of town", description="Flower patch south of town. Access: 2 Farming minimum; Build the Kastori quetzal landing site; alternatively activate the Pendant of Ates statue", section=PATCHES_FLOWER, position=57)
    default boolean includeKASTORI_FLOWER() { return false; }

    @ConfigItem(keyName="patch_LUMBRIDGE_HOPS", name="Lumbridge farm north - Hops patch", description="Hops patch. Access: 3 Farming minimum; Chronicle charges or Lumbridge teleport access", section=PATCHES_HOPS, position=58)
    default boolean includeLUMBRIDGE_HOPS() { return false; }

    @ConfigItem(keyName="patch_SEERS_HOPS", name="McGrubor's Wood north - Hops patch", description="Hops patch. Access: 3 Farming minimum; Fairy-ring access or music cape", section=PATCHES_HOPS, position=59)
    default boolean includeSEERS_HOPS() { return false; }

    @ConfigItem(keyName="patch_YANILLE_HOPS", name="Yanille - Hops patch", description="Hops patch. Access: 3 Farming minimum; 50 Construction to redirect a Yanille tablet", section=PATCHES_HOPS, position=60)
    default boolean includeYANILLE_HOPS() { return false; }

    @ConfigItem(keyName="patch_ENTRANA_HOPS", name="Entrana - Hops patch", description="Hops patch. Access: 3 Farming minimum; Entrana equipment restrictions; Entrana equipment restrictions apply; bank prohibited weapons and armour before travel", section=PATCHES_HOPS, position=61)
    default boolean includeENTRANA_HOPS() { return false; }

    @ConfigItem(keyName="patch_ALDARIN_HOPS", name="Aldarin - Hops patch", description="Hops patch. Access: 3 Farming minimum; Fairy-ring or unlocked quetzal access; 35 Construction for redirected tablet", section=PATCHES_HOPS, position=62)
    default boolean includeALDARIN_HOPS() { return false; }

    @ConfigItem(keyName="patch_CHAMPIONS_BUSH", name="Champions' Guild west - Bush patch", description="Bush patch. Access: 10 Farming minimum; Charged bracelet or Chronicle", section=PATCHES_BUSH, position=63)
    default boolean includeCHAMPIONS_BUSH() { return false; }

    @ConfigItem(keyName="patch_RIMMINGTON_BUSH", name="Rimmington - Bush patch", description="Bush patch. Access: 10 Farming minimum; Meet your bush's Farming level", section=PATCHES_BUSH, position=64)
    default boolean includeRIMMINGTON_BUSH() { return false; }

    @ConfigItem(keyName="patch_ARDOUGNE_BUSH", name="Ardougne south - Bush patch", description="Bush patch. Access: 10 Farming minimum; Easy Ardougne diary for cloak", section=PATCHES_BUSH, position=65)
    default boolean includeARDOUGNE_BUSH() { return false; }

    @ConfigItem(keyName="patch_ETCETERIA_BUSH", name="Etceteria - South-west bush patch", description="South-west bush patch. Access: 10 Farming minimum; Throne of Miscellania for the wealth teleport, or fairy-ring/boat access", section=PATCHES_BUSH, position=66)
    default boolean includeETCETERIA_BUSH() { return false; }

    @ConfigItem(keyName="patch_GUILD_BUSH", name="Farming Guild - Bush patch", description="Bush patch. Access: 45 Farming guild entrance; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_BUSH, position=67)
    default boolean includeGUILD_BUSH() { return false; }

    @ConfigItem(keyName="patch_AL_KHARID_CACTUS", name="Al Kharid - Cactus patch", description="Cactus patch. Access: 55 Farming for cactus; 64 for potato cactus; Charged jewelry or another Al Kharid route", section=PATCHES_CACTUS, position=68)
    default boolean includeAL_KHARID_CACTUS() { return false; }

    @ConfigItem(keyName="patch_GUILD_CACTUS", name="Farming Guild - Cactus patch", description="Cactus patch. Access: 45 Farming guild entrance; 55 cactus or 64 potato cactus; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_CACTUS, position=69)
    default boolean includeGUILD_CACTUS() { return false; }

    @ConfigItem(keyName="patch_SEAWEED_N", name="Fossil Island underwater - North seaweed patch", description="North seaweed patch. Access: 23 Farming; Bone Voyage; fishbowl helmet and diving apparatus recommended; meet diving equipment/weight restrictions", section=PATCHES_SEAWEED, position=70)
    default boolean includeSEAWEED_N() { return false; }

    @ConfigItem(keyName="patch_SEAWEED_S", name="Fossil Island underwater - South seaweed patch", description="South seaweed patch. Access: 23 Farming; Bone Voyage; fishbowl helmet and diving apparatus recommended; meet diving equipment/weight restrictions", section=PATCHES_SEAWEED, position=71)
    default boolean includeSEAWEED_S() { return false; }

    @ConfigItem(keyName="patch_VINERY_E1", name="Hosidius Vinery - East trellis 1", description="East trellis 1. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=72)
    default boolean includeVINERY_E1() { return false; }

    @ConfigItem(keyName="patch_VINERY_E2", name="Hosidius Vinery - East trellis 2", description="East trellis 2. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=73)
    default boolean includeVINERY_E2() { return false; }

    @ConfigItem(keyName="patch_VINERY_E3", name="Hosidius Vinery - East trellis 3", description="East trellis 3. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=74)
    default boolean includeVINERY_E3() { return false; }

    @ConfigItem(keyName="patch_VINERY_E4", name="Hosidius Vinery - East trellis 4", description="East trellis 4. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=75)
    default boolean includeVINERY_E4() { return false; }

    @ConfigItem(keyName="patch_VINERY_E5", name="Hosidius Vinery - East trellis 5", description="East trellis 5. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=76)
    default boolean includeVINERY_E5() { return false; }

    @ConfigItem(keyName="patch_VINERY_E6", name="Hosidius Vinery - East trellis 6", description="East trellis 6. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=77)
    default boolean includeVINERY_E6() { return false; }

    @ConfigItem(keyName="patch_VINERY_W1", name="Hosidius Vinery - West trellis 1", description="West trellis 1. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=78)
    default boolean includeVINERY_W1() { return false; }

    @ConfigItem(keyName="patch_VINERY_W2", name="Hosidius Vinery - West trellis 2", description="West trellis 2. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=79)
    default boolean includeVINERY_W2() { return false; }

    @ConfigItem(keyName="patch_VINERY_W3", name="Hosidius Vinery - West trellis 3", description="West trellis 3. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=80)
    default boolean includeVINERY_W3() { return false; }

    @ConfigItem(keyName="patch_VINERY_W4", name="Hosidius Vinery - West trellis 4", description="West trellis 4. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=81)
    default boolean includeVINERY_W4() { return false; }

    @ConfigItem(keyName="patch_VINERY_W5", name="Hosidius Vinery - West trellis 5", description="West trellis 5. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=82)
    default boolean includeVINERY_W5() { return false; }

    @ConfigItem(keyName="patch_VINERY_W6", name="Hosidius Vinery - West trellis 6", description="West trellis 6. Access: 36 Farming; 36 Farming; bring grape seeds and saltpetre", section=PATCHES_GRAPE, position=83)
    default boolean includeVINERY_W6() { return false; }

    @ConfigItem(keyName="patch_CANIFIS_MUSHROOM", name="Canifis west - Mushroom patch", description="Mushroom patch. Access: 53 Farming; Morytania and fairy-ring access", section=PATCHES_MUSHROOM, position=84)
    default boolean includeCANIFIS_MUSHROOM() { return false; }

    @ConfigItem(keyName="patch_DRAYNOR_BELLADONNA", name="Draynor Manor - Belladonna patch", description="Belladonna patch. Access: 63 Farming; wear gloves to harvest safely; Gloves to harvest belladonna safely", section=PATCHES_BELLADONNA, position=85)
    default boolean includeDRAYNOR_BELLADONNA() { return false; }

    @ConfigItem(keyName="patch_AUBURNVALE_BELLADONNA", name="Auburnvale - Belladonna patch east of town", description="Belladonna patch east of town. Access: 63 Farming; wear gloves to harvest safely; Fairy-ring access or unlocked quetzal transport", section=PATCHES_BELLADONNA, position=86)
    default boolean includeAUBURNVALE_BELLADONNA() { return false; }

    @ConfigItem(keyName="patch_GUILD_HESPORI", name="Farming Guild - Hespori cave", description="Hespori cave. Access: 65 Farming; bring appropriate combat supplies before entering the fight; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_HESPORI, position=87)
    default boolean includeGUILD_HESPORI() { return false; }

    @ConfigItem(keyName="patch_GUILD_ANIMA", name="Farming Guild - Anima patch in west wing", description="Anima patch in west wing. Access: 76 Farming; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_ANIMA, position=88)
    default boolean includeGUILD_ANIMA() { return false; }

    @ConfigItem(keyName="patch_PRIF_CRYSTAL", name="Prifddinas - Crystal tree patch", description="Crystal tree patch. Access: 74 Farming; Song of the Elves", section=PATCHES_CRYSTAL, position=89)
    default boolean includePRIF_CRYSTAL() { return false; }

    @ConfigItem(keyName="patch_GUILD_CELASTRUS", name="Farming Guild - Celastrus patch in north wing", description="Celastrus patch in north wing. Access: 85 Farming; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_CELASTRUS, position=90)
    default boolean includeGUILD_CELASTRUS() { return false; }

    @ConfigItem(keyName="patch_GUILD_REDWOOD", name="Farming Guild - Redwood patch in north wing", description="Redwood patch in north wing. Access: 90 Farming; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_REDWOOD, position=91)
    default boolean includeGUILD_REDWOOD() { return false; }

    @ConfigItem(keyName="patch_CORAL_E", name="Coral Nurseries - East coral nursery", description="East coral nursery. Access: 28 / 52 / 77 Farming for elkhorn / pillar / umbral; Complete Troubled Tortugans; fairy-ring destination unlocked", section=PATCHES_CORAL, position=92)
    default boolean includeCORAL_E() { return false; }

    @ConfigItem(keyName="patch_CORAL_W", name="Coral Nurseries - West coral nursery", description="West coral nursery. Access: 28 / 52 / 77 Farming for elkhorn / pillar / umbral; Complete Troubled Tortugans; fairy-ring destination unlocked", section=PATCHES_CORAL, position=93)
    default boolean includeCORAL_W() { return false; }

    @ConfigItem(keyName="patch_ETCETERIA_SPIRIT", name="Etceteria - South-east spirit-tree patch", description="South-east spirit-tree patch. Access: 83 Farming; planted spirit-tree limits apply; Throne of Miscellania for the wealth teleport, or fairy-ring/boat access", section=PATCHES_SPIRIT, position=94)
    default boolean includeETCETERIA_SPIRIT() { return false; }

    @ConfigItem(keyName="patch_PORT_SARIM_SPIRIT", name="Port Sarim east - Spirit-tree patch", description="Spirit-tree patch. Access: 83 Farming; planted spirit-tree limits apply; 83 Farming to plant spirit trees; planted-tree limits apply", section=PATCHES_SPIRIT, position=95)
    default boolean includePORT_SARIM_SPIRIT() { return false; }

    @ConfigItem(keyName="patch_BRIMHAVEN_SPIRIT", name="Brimhaven - Spirit-tree patch east of the fruit patch", description="Spirit-tree patch east of the fruit patch. Access: 83 Farming; use an independent arrival method if the tree is not grown; 40 Construction to redirect the tablet", section=PATCHES_SPIRIT, position=96)
    default boolean includeBRIMHAVEN_SPIRIT() { return false; }

    @ConfigItem(keyName="patch_HOSIDIUS_SPIRIT", name="Hosidius south-west - Spirit-tree patch", description="Spirit-tree patch. Access: 83 Farming; planted spirit-tree limits apply; 83 Farming to plant spirit trees; planted-tree limits apply", section=PATCHES_SPIRIT, position=97)
    default boolean includeHOSIDIUS_SPIRIT() { return false; }

    @ConfigItem(keyName="patch_GUILD_SPIRIT", name="Farming Guild - Spirit-tree patch in north wing", description="Spirit-tree patch in north wing. Access: 85 Farming to enter north wing; planted spirit-tree limits apply; 45 Farming to enter; west wing 65; north wing 85", section=PATCHES_SPIRIT, position=98)
    default boolean includeGUILD_SPIRIT() { return false; }

}

