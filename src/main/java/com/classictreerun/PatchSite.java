package com.classictreerun;

/** Locations and preferred manual entry instructions audited against live OSRS Wiki tables. */
enum PatchSite
{
    LUMBRIDGE("Lumbridge", "lumbridge", "Lumbridge Teleport spell/tablet.", "Meet your crop's Farming level", 3193,3231),
    VARROCK("Varrock Castle", "varrock", "Varrock city-centre teleport; north into the castle.", "Meet your sapling's Farming level",3229,3459),
    FALADOR_TREE("Falador Park", "falador-park", "Ring of wealth to Falador Park.", "Meet your sapling's Farming level",3004,3373),
    TAVERLEY("Taverley", "taverley", "Redirected Taverley teleport tablet, or unlocked balloon route.", "Meet your sapling's Farming level",2936,3438),
    STRONGHOLD("Gnome Stronghold", "stronghold", "Spirit tree to Gnome Stronghold; Slayer ring is convenient for the normal tree.", "Spirit-tree transport requires its quest unlocks",2476,3446),
    VILLAGE("Tree Gnome Village", "village", "Spirit tree to Tree Gnome Village; follow Elkoy out of the maze.", "Tree Gnome Village for spirit-tree travel",2490,3180),
    LLETYA("Lletya", "lletya", "Teleport crystal to Lletya.", "Access during Mourning's End Part I",2347,3162),
    CATHERBY_FRUIT("Catherby shore", "catherby-shore", "Catherby Teleport; east along the shore.", "Lunar spell, tablet or configured POH teleport",2861,3434),
    BRIMHAVEN("Brimhaven", "brimhaven", "Redirected Brimhaven tablet; run north. A grown spirit tree is another option.", "40 Construction to redirect the tablet",2765,3213),
    CALQUAT("Tai Bwo Wannai", "tai-bwo-wannai", "Tai bwo wannai scroll; north to the calquat patch.", "Tai Bwo Wannai Trio for the scroll",2796,3101),
    FOSSIL("Fossil Island hardwoods", "fossil-hardwoods", "Digsite pendant to Digsite; barge quick-travel; north-west from camp.", "Bone Voyage",3709,3835),
    GUILD("Farming Guild", "guild", "Skills necklace to Farming Guild, then enter the appropriate wing.", "45 Farming to enter; west wing 65; north wing 85",1252,3739),
    NEMUS("Nemus Retreat", "nemus", "Fairy ring AIS then south-west; or Pendant of Ates to Nemus Retreat, then north.", "Fairy-ring access; pendant destination must be activated if used",1366,3321),
    AUBURNVALE("Auburnvale", "auburnvale", "Fairy ring AIS then north; or quetzal to Auburnvale then east.", "Fairy-ring access or unlocked quetzal transport",1450,3354),
    KASTORI("Kastori", "kastori", "Civitas illa Fortis Teleport; quetzal to Kastori. Walk north to fruit, north-east to calquat, or to the flower patch south of town.", "Build the Kastori quetzal landing site; alternatively activate the Pendant of Ates statue",1358,3040),
    LOCUS("Locus Oasis", "locus", "Fairy ring AJP then south-east; or quetzal to Colossal Wyrm Remains then north.", "The Ribbiting Tale of a Lily Pad Labour Dispute",1687,2972),
    ANGLERS("Anglers' Retreat", "anglers", "Reach Corsair Cove (e.g. charter ship); use the unlocked rowboat south-west of the cove, or sail to the island.", "51 Sailing to moor; first visit to build the return rowboat (50 Construction, 10 swamp paste, 8 camphor planks, hammer and saw)",2471,2703),
    CONCH("The Great Conch", "conch", "Fairy ring CJQ. Calquat is south-west near the Summer Shore; for coral, enter the Coral Nurseries.", "45 Sailing and progress in Troubled Tortugans for island access; ring access must be unlocked",3148,2445),
    CORAL("Coral Nurseries", "coral", "Fairy ring CJQ on the Great Conch, then enter the Coral Nurseries and find the east/west nursery.", "Complete Troubled Tortugans; fairy-ring destination unlocked",3297,8861),
    FALADOR_FARM("South Falador farm", "falador-farm", "Explorer's ring 2 or higher cabbage-port; walk to the farming patches.", "Medium Lumbridge & Draynor diary for ring 2; lower-tier daily charge limits apply",3055,3308),
    PHASMATYS("Port Phasmatys farm", "phasmatys", "Ectophial; run west to the farming patches. Alternative: fairy ring ALQ.", "Ghosts Ahoy for the ectophial, or fairy-ring access",3602,3526),
    CATHERBY_FARM("North Catherby farm", "catherby-farm", "Catherby Teleport; walk to the farming patches north of town. Alternative: Camelot Teleport and run east.", "Lunar spell/tablet/POH for Catherby, or Camelot access",2810,3464),
    ARDOUGNE_FARM("North Ardougne farm", "ardougne-farm", "Ardougne cloak 2 or higher farm teleport. Alternative: skills necklace to Fishing Guild and run to the farm.", "Medium Ardougne diary for farm teleport; lower-tier daily charge limits apply",2667,3375),
    HOSIDIUS_FARM("Hosidius farm", "hosidius-farm", "Xeric's talisman to Xeric's Glade; walk to the farming patches.", "Charged Xeric's talisman, or another unlocked Hosidius route",1735,3555),
    HARMONY("Harmony Island", "harmony", "Harmony Island Teleport spell/tablet; walk to the allotment/herb patches.", "Island access from The Great Brain Robbery; herb patch additionally needs Elite Morytania diary",3792,2837),
    PRIF("Prifddinas", "prif", "Spirit tree or teleport crystal to Prifddinas; go to the farming area in the Crwys district.", "Song of the Elves",3291,6108),
    TROLL("Troll Stronghold roof", "troll", "Stony basalt to the roof. Alternative: Trollheim teleport then reach the roof via the stronghold or unlocked shortcut.", "My Arm's Big Adventure for the patch; Making Friends with My Arm for stony basalt",2828,3696),
    WEISS("Weiss", "weiss", "Icy basalt teleport; walk to the herb patch.", "Making Friends with My Arm and built Fire of Nourishment",2847,3933),
    ORTUS("Ortus Farm", "ortus", "Quetzal whistle to Hunter Guild then north; or Civitas illa Fortis Teleport and quetzal to Hunter Guild.", "Varlamore access; quetzal destination unlocked",1586,3099),
    LUMBRIDGE_HOPS("Lumbridge farm north", "lumbridge-hops", "Chronicle to Champions' Guild then travel to the hops patch north of Lumbridge farm; or Lumbridge Teleport and run north.", "Chronicle charges or Lumbridge teleport access",3229,3315),
    SEERS_HOPS("McGrubor's Wood north", "seers-hops", "Fairy ring ALS; go north of McGrubor's Wood to the hops patch. Alternative: music cape teleport.", "Fairy-ring access or music cape",2667,3526),
    YANILLE("Yanille", "yanille", "Redirected Yanille house tablet; walk to the hops patch. Alternative: Watchtower teleport to Yanille after Hard Ardougne diary.", "50 Construction to redirect a Yanille tablet",2576,3105),
    ENTRANA("Entrana", "entrana", "Amulet of glory to Draynor, then Port Sarim; take the monks' boat to Entrana and walk to the hops patch. An unlocked balloon route is another option.", "Entrana equipment restrictions apply; bank prohibited weapons and armour before travel",2811,3337),
    ALDARIN("Aldarin", "aldarin", "Fairy ring CKQ or quetzal to Aldarin; walk to the hops patch. Alternative: redirected Aldarin house tablet.", "Fairy-ring or unlocked quetzal access; 35 Construction for redirected tablet",1365,2939),
    CHAMPIONS("Champions' Guild west", "champions", "Combat bracelet to Champions' Guild, or Chronicle; walk west to the bush patch.", "Charged bracelet or Chronicle",3182,3358),
    RIMMINGTON("Rimmington", "rimmington", "Redirected Rimmington house tablet; walk to the bush patch.", "Meet your bush's Farming level",2941,3222),
    ARDOUGNE_BUSH("Ardougne south", "ardougne-bush", "Ardougne cloak 1 or higher to the monastery; walk to the bush patch.", "Easy Ardougne diary for cloak",2618,3226),
    ETCETERIA("Etceteria", "etceteria", "Ring of wealth to Miscellania, or fairy ring CIP; travel east to Etceteria. Bush is south-west; spirit patch is south-east.", "Throne of Miscellania for the wealth teleport, or fairy-ring/boat access",2603,3861),
    AL_KHARID("Al Kharid", "al-kharid", "Ring of dueling to Emir's Arena; walk to the cactus patch. Alternative: glory to Al Kharid.", "Charged jewelry or another Al Kharid route",3316,3203),
    UNDERWATER("Fossil Island underwater", "underwater", "Digsite pendant to Digsite; barge to Fossil Island; rowboat from Museum Camp to the north-east island, then dive to the seaweed patches.", "Bone Voyage; fishbowl helmet and diving apparatus recommended; meet diving equipment/weight restrictions",3734,10271),
    VINERY("Hosidius Vinery", "vinery", "Fairy ring AKR; walk to the grape trellises. Alternative: Tithe Farm grouping teleport after entering that activity once.", "36 Farming; bring grape seeds and saltpetre",1808,3556),
    CANIFIS("Canifis west", "canifis", "Fairy ring CKS; walk to the mushroom patch west of Canifis.", "Morytania and fairy-ring access",3452,3473),
    DRAYNOR("Draynor Manor", "draynor", "Draynor Manor Teleport spell/tablet; walk to the belladonna patch.", "Gloves to harvest belladonna safely",3087,3355),
    PORT_SARIM("Port Sarim east", "port-sarim", "Explorer's ring cabbage-port or glory to Draynor; walk to the spirit-tree patch east of Port Sarim.", "83 Farming to plant spirit trees; planted-tree limits apply",3060,3258),
    HOSIDIUS_SPIRIT("Hosidius south-west", "hosidius-spirit", "Skills necklace to Woodcutting Guild; walk to the spirit-tree patch south-west of Hosidius.", "83 Farming to plant spirit trees; planted-tree limits apply",1693,3542);

    final String label;
    final String group;
    final String travel;
    final String requirements;
    final int x;
    final int y;

    PatchSite(String label, String group, String travel, String requirements, int x, int y)
    {
        this.label = label;
        this.group = group;
        this.travel = travel;
        this.requirements = requirements;
        this.x = x;
        this.y = y;
    }
}
