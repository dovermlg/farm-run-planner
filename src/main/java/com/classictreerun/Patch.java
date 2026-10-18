package com.classictreerun;

/** Stable identifiers are persisted; never use ordinal values as storage keys. */
public enum Patch
{
    LUMBRIDGE_TREE(Type.TREE, "Lumbridge", "West of Lumbridge Castle", "Fayeth", "15 Farming or the level for your sapling"),
    VARROCK_TREE(Type.TREE, "Varrock Castle", "Castle courtyard", "Treznor", "15 Farming or the level for your sapling"),
    FALADOR_TREE(Type.TREE, "Falador Park", "Inside the park", "Heskel", "15 Farming or the level for your sapling"),
    TAVERLEY_TREE(Type.TREE, "Taverley", "Tree patch near the balloon landing", "Alain", "15 Farming or the level for your sapling"),
    STRONGHOLD_TREE(Type.TREE, "Gnome Stronghold", "South-west of the spirit tree, near the Slayer Cave", "Prissy Scilla", "15 Farming or the level for your sapling"),
    STRONGHOLD_FRUIT(Type.FRUIT, "Gnome Stronghold", "East of the spirit tree", "Bolongo", "27 Farming or the level for your sapling"),
    VILLAGE_FRUIT(Type.FRUIT, "Tree Gnome Village", "Outside, west of the maze", "Gileth", "27 Farming; Tree Gnome Village for spirit tree travel"),
    LLETYA_FRUIT(Type.FRUIT, "Lletya", "Fruit patch in Lletya", "Liliwen", "27 Farming; access during Mourning's End Part I"),
    CATHERBY_FRUIT(Type.FRUIT, "Catherby", "East along the shore, near the fishing spots", "Ellena", "27 Farming or the level for your sapling"),
    BRIMHAVEN_FRUIT(Type.FRUIT, "Brimhaven", "North of Brimhaven, near the docks", "Garth", "27 Farming or the level for your sapling"),
    TAI_BWO_WANNAI_CALQUAT(Type.CALQUAT, "Tai Bwo Wannai", "North of the village", "Imiago", "72 Farming; Tai Bwo Wannai Trio for the teleport scroll"),
    FOSSIL_EAST(Type.HARDWOOD, "Fossil Island east", "Eastern hardwood patch in the Mushroom Forest", "Squirrel (east)", "Bone Voyage; 35 Farming minimum; teak 35, mahogany 55, camphor 66, ironwood 80, rosewood 92"),
    FOSSIL_MIDDLE(Type.HARDWOOD, "Fossil Island middle", "Middle hardwood patch in the Mushroom Forest", "Squirrel (middle)", "Bone Voyage; 35 Farming minimum; teak 35, mahogany 55, camphor 66, ironwood 80, rosewood 92"),
    FOSSIL_WEST(Type.HARDWOOD, "Fossil Island west", "Western hardwood patch in the Mushroom Forest", "Squirrel (west)", "Bone Voyage; 35 Farming minimum; teak 35, mahogany 55, camphor 66, ironwood 80, rosewood 92"),
    GUILD_TREE(Type.TREE, "Farming Guild", "West wing tree patch", "Rosie", "65 Farming to enter this wing; sapling's level also applies"),
    GUILD_FRUIT(Type.FRUIT, "Farming Guild", "North wing fruit patch", "Nikkie", "85 Farming to enter this wing; sapling's level also applies"),

    NEMUS_TREE(Type.TREE, PatchSite.NEMUS, "Normal tree patch", "Aub", "15 Farming minimum"),
    KASTORI_FRUIT(Type.FRUIT, PatchSite.KASTORI, "Fruit patch north of town", "Ehecatl", "27 Farming minimum"),
    LOCUS_HARDWOOD(Type.HARDWOOD, PatchSite.LOCUS, "Hardwood patch", "Marcellus", "35 Farming minimum"),
    ANGLERS_HARDWOOD(Type.HARDWOOD, PatchSite.ANGLERS, "Hardwood patch", "Argo", "35 Farming minimum"),
    KASTORI_CALQUAT(Type.CALQUAT, PatchSite.KASTORI, "Calquat patch north-east of landing", "Tziuhtla", "72 Farming"),
    CONCH_CALQUAT(Type.CALQUAT, PatchSite.CONCH, "Summer Shore calquat patch", "Guppa", "72 Farming"),

    FALADOR_ALLOTMENT_NW(Type.ALLOTMENT, PatchSite.FALADOR_FARM, "North-west allotment", "Elstan", "1 Farming minimum"),
    FALADOR_ALLOTMENT_SE(Type.ALLOTMENT, PatchSite.FALADOR_FARM, "South-east allotment", "Elstan", "1 Farming minimum"),
    FALADOR_FLOWER(Type.FLOWER, PatchSite.FALADOR_FARM, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    FALADOR_HERB(Type.HERB, PatchSite.FALADOR_FARM, "Herb patch", "No paid crop protection", "9 Farming minimum"),
    PHASMATYS_ALLOTMENT_NW(Type.ALLOTMENT, PatchSite.PHASMATYS, "North-west allotment", "Lyra", "1 Farming minimum"),
    PHASMATYS_ALLOTMENT_SE(Type.ALLOTMENT, PatchSite.PHASMATYS, "South-east allotment", "Lyra", "1 Farming minimum"),
    PHASMATYS_FLOWER(Type.FLOWER, PatchSite.PHASMATYS, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    PHASMATYS_HERB(Type.HERB, PatchSite.PHASMATYS, "Herb patch", "No paid crop protection", "9 Farming minimum"),
    CATHERBY_ALLOTMENT_N(Type.ALLOTMENT, PatchSite.CATHERBY_FARM, "North allotment", "Dantaera", "1 Farming minimum"),
    CATHERBY_ALLOTMENT_S(Type.ALLOTMENT, PatchSite.CATHERBY_FARM, "South allotment", "Dantaera", "1 Farming minimum"),
    CATHERBY_FLOWER(Type.FLOWER, PatchSite.CATHERBY_FARM, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    CATHERBY_HERB(Type.HERB, PatchSite.CATHERBY_FARM, "Herb patch", "No paid crop protection", "9 Farming minimum"),
    ARDOUGNE_ALLOTMENT_N(Type.ALLOTMENT, PatchSite.ARDOUGNE_FARM, "North allotment", "Kragen", "1 Farming minimum"),
    ARDOUGNE_ALLOTMENT_S(Type.ALLOTMENT, PatchSite.ARDOUGNE_FARM, "South allotment", "Kragen", "1 Farming minimum"),
    ARDOUGNE_FLOWER(Type.FLOWER, PatchSite.ARDOUGNE_FARM, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    ARDOUGNE_HERB(Type.HERB, PatchSite.ARDOUGNE_FARM, "Herb patch", "No paid crop protection", "9 Farming minimum"),
    HOSIDIUS_ALLOTMENT_NE(Type.ALLOTMENT, PatchSite.HOSIDIUS_FARM, "North-east allotment", "Marisi", "1 Farming minimum"),
    HOSIDIUS_ALLOTMENT_SW(Type.ALLOTMENT, PatchSite.HOSIDIUS_FARM, "South-west allotment", "Marisi", "1 Farming minimum"),
    HOSIDIUS_FLOWER(Type.FLOWER, PatchSite.HOSIDIUS_FARM, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    HOSIDIUS_HERB(Type.HERB, PatchSite.HOSIDIUS_FARM, "Herb patch", "No paid crop protection", "9 Farming minimum"),
    HARMONY_ALLOTMENT(Type.ALLOTMENT, PatchSite.HARMONY, "Allotment patch", "No gardener", "1 Farming minimum; Elite diary is only required for the herb patch"),
    HARMONY_HERB(Type.HERB, PatchSite.HARMONY, "Herb patch", "No paid crop protection", "Elite Morytania diary"),
    GUILD_ALLOTMENT_N(Type.ALLOTMENT, PatchSite.GUILD, "North allotment", "Alan", "45 Farming guild entrance"),
    GUILD_ALLOTMENT_S(Type.ALLOTMENT, PatchSite.GUILD, "South allotment", "Alan", "45 Farming guild entrance"),
    GUILD_FLOWER(Type.FLOWER, PatchSite.GUILD, "Flower patch", "No paid crop protection", "45 Farming guild entrance"),
    GUILD_HERB(Type.HERB, PatchSite.GUILD, "Herb patch in west wing", "No paid crop protection", "65 Farming west wing"),
    PRIF_ALLOTMENT_N(Type.ALLOTMENT, PatchSite.PRIF, "North allotment", "Oswallt", "1 Farming minimum"),
    PRIF_ALLOTMENT_S(Type.ALLOTMENT, PatchSite.PRIF, "South allotment", "Oswallt", "1 Farming minimum"),
    PRIF_FLOWER(Type.FLOWER, PatchSite.PRIF, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    TROLL_HERB(Type.HERB, PatchSite.TROLL, "Rooftop herb patch", "My Arm / Drunken Dwarf's Leg", "9 Farming minimum"),
    WEISS_HERB(Type.HERB, PatchSite.WEISS, "Herb patch", "Boulder", "9 Farming minimum"),
    ORTUS_ALLOTMENT_N(Type.ALLOTMENT, PatchSite.ORTUS, "North allotment", "Harminia", "1 Farming minimum"),
    ORTUS_ALLOTMENT_S(Type.ALLOTMENT, PatchSite.ORTUS, "South allotment", "Harminia", "1 Farming minimum"),
    ORTUS_FLOWER(Type.FLOWER, PatchSite.ORTUS, "Flower patch", "No paid crop protection", "2 Farming minimum"),
    ORTUS_HERB(Type.HERB, PatchSite.ORTUS, "Herb patch", "No paid crop protection", "9 Farming minimum"),
    KASTORI_FLOWER(Type.FLOWER, PatchSite.KASTORI, "Flower patch south of town", "No paid crop protection", "2 Farming minimum"),

    LUMBRIDGE_HOPS(Type.HOPS, PatchSite.LUMBRIDGE_HOPS, "Hops patch", "Vasquen", "3 Farming minimum"),
    SEERS_HOPS(Type.HOPS, PatchSite.SEERS_HOPS, "Hops patch", "Rhonen", "3 Farming minimum"),
    YANILLE_HOPS(Type.HOPS, PatchSite.YANILLE, "Hops patch", "Selena", "3 Farming minimum"),
    ENTRANA_HOPS(Type.HOPS, PatchSite.ENTRANA, "Hops patch", "Francis", "3 Farming minimum; Entrana equipment restrictions"),
    ALDARIN_HOPS(Type.HOPS, PatchSite.ALDARIN, "Hops patch", "Ercos", "3 Farming minimum"),
    CHAMPIONS_BUSH(Type.BUSH, PatchSite.CHAMPIONS, "Bush patch", "Dreven", "10 Farming minimum"),
    RIMMINGTON_BUSH(Type.BUSH, PatchSite.RIMMINGTON, "Bush patch", "Taria", "10 Farming minimum"),
    ARDOUGNE_BUSH(Type.BUSH, PatchSite.ARDOUGNE_BUSH, "Bush patch", "Torrell", "10 Farming minimum"),
    ETCETERIA_BUSH(Type.BUSH, PatchSite.ETCETERIA, "South-west bush patch", "Rhazien", "10 Farming minimum"),
    GUILD_BUSH(Type.BUSH, PatchSite.GUILD, "Bush patch", "Alan", "45 Farming guild entrance"),
    AL_KHARID_CACTUS(Type.CACTUS, PatchSite.AL_KHARID, "Cactus patch", "Ayesha", "55 Farming for cactus; 64 for potato cactus"),
    GUILD_CACTUS(Type.CACTUS, PatchSite.GUILD, "Cactus patch", "Alan", "45 Farming guild entrance; 55 cactus or 64 potato cactus"),
    SEAWEED_N(Type.SEAWEED, PatchSite.UNDERWATER, "North seaweed patch", "Mernia", "23 Farming"),
    SEAWEED_S(Type.SEAWEED, PatchSite.UNDERWATER, "South seaweed patch", "Mernia", "23 Farming"),
    VINERY_E1(Type.GRAPE, PatchSite.VINERY, "East trellis 1", "Gallow", "36 Farming"),
    VINERY_E2(Type.GRAPE, PatchSite.VINERY, "East trellis 2", "Gallow", "36 Farming"),
    VINERY_E3(Type.GRAPE, PatchSite.VINERY, "East trellis 3", "Gallow", "36 Farming"),
    VINERY_E4(Type.GRAPE, PatchSite.VINERY, "East trellis 4", "Gallow", "36 Farming"),
    VINERY_E5(Type.GRAPE, PatchSite.VINERY, "East trellis 5", "Gallow", "36 Farming"),
    VINERY_E6(Type.GRAPE, PatchSite.VINERY, "East trellis 6", "Gallow", "36 Farming"),
    VINERY_W1(Type.GRAPE, PatchSite.VINERY, "West trellis 1", "Gallow", "36 Farming"),
    VINERY_W2(Type.GRAPE, PatchSite.VINERY, "West trellis 2", "Gallow", "36 Farming"),
    VINERY_W3(Type.GRAPE, PatchSite.VINERY, "West trellis 3", "Gallow", "36 Farming"),
    VINERY_W4(Type.GRAPE, PatchSite.VINERY, "West trellis 4", "Gallow", "36 Farming"),
    VINERY_W5(Type.GRAPE, PatchSite.VINERY, "West trellis 5", "Gallow", "36 Farming"),
    VINERY_W6(Type.GRAPE, PatchSite.VINERY, "West trellis 6", "Gallow", "36 Farming"),
    CANIFIS_MUSHROOM(Type.MUSHROOM, PatchSite.CANIFIS, "Mushroom patch", "No gardener", "53 Farming"),
    DRAYNOR_BELLADONNA(Type.BELLADONNA, PatchSite.DRAYNOR, "Belladonna patch", "No gardener", "63 Farming; wear gloves to harvest safely"),
    AUBURNVALE_BELLADONNA(Type.BELLADONNA, PatchSite.AUBURNVALE, "Belladonna patch east of town", "No gardener", "63 Farming; wear gloves to harvest safely"),
    GUILD_HESPORI(Type.HESPORI, PatchSite.GUILD, "Hespori cave", "Arno", "65 Farming; bring appropriate combat supplies before entering the fight"),
    GUILD_ANIMA(Type.ANIMA, PatchSite.GUILD, "Anima patch in west wing", "Felfiz Yaryus", "76 Farming"),
    PRIF_CRYSTAL(Type.CRYSTAL, PatchSite.PRIF, "Crystal tree patch", "Pennant", "74 Farming"),
    GUILD_CELASTRUS(Type.CELASTRUS, PatchSite.GUILD, "Celastrus patch in north wing", "Taylor", "85 Farming"),
    GUILD_REDWOOD(Type.REDWOOD, PatchSite.GUILD, "Redwood patch in north wing", "Alexandra", "90 Farming"),
    CORAL_E(Type.CORAL, PatchSite.CORAL, "East coral nursery", "Tortugan farmer", "28 / 52 / 77 Farming for elkhorn / pillar / umbral"),
    CORAL_W(Type.CORAL, PatchSite.CORAL, "West coral nursery", "Tortugan farmer", "28 / 52 / 77 Farming for elkhorn / pillar / umbral"),
    ETCETERIA_SPIRIT(Type.SPIRIT, PatchSite.ETCETERIA, "South-east spirit-tree patch", "Yulf Squecks", "83 Farming; planted spirit-tree limits apply"),
    PORT_SARIM_SPIRIT(Type.SPIRIT, PatchSite.PORT_SARIM, "Spirit-tree patch", "Frizzy Skernip", "83 Farming; planted spirit-tree limits apply"),
    BRIMHAVEN_SPIRIT(Type.SPIRIT, PatchSite.BRIMHAVEN, "Spirit-tree patch east of the fruit patch", "Praistan Ebola", "83 Farming; use an independent arrival method if the tree is not grown"),
    HOSIDIUS_SPIRIT(Type.SPIRIT, PatchSite.HOSIDIUS_SPIRIT, "Spirit-tree patch", "Lammy Langle", "83 Farming; planted spirit-tree limits apply"),
    GUILD_SPIRIT(Type.SPIRIT, PatchSite.GUILD, "Spirit-tree patch in north wing", "Latlink Fastbell", "85 Farming to enter north wing; planted spirit-tree limits apply");

    public enum Type
    {
        TREE("Normal tree"), FRUIT("Fruit tree"), HARDWOOD("Hardwood"), CALQUAT("Calquat"),
        ALLOTMENT("Allotment"), FLOWER("Flower"), HERB("Herb"), HOPS("Hops"), BUSH("Bush"),
        CACTUS("Cactus"), SEAWEED("Seaweed"), GRAPE("Grape"), MUSHROOM("Mushroom"),
        BELLADONNA("Belladonna"), HESPORI("Hespori"), ANIMA("Anima"), CRYSTAL("Crystal tree"),
        CELASTRUS("Celastrus"), REDWOOD("Redwood"), CORAL("Coral nursery"), SPIRIT("Spirit tree");
        private final String label;
        Type(String label) { this.label = label; }
        public String toString() { return label; }
    }

    private final Type type;
    private final String location;
    private final String directions;
    private final String gardener;
    private final String requirements;
    private final PatchSite site;

    Patch(Type type, String location, String directions, String gardener, String requirements)
    {
        this.type = type;
        this.location = location;
        this.directions = directions;
        this.gardener = gardener;
        this.requirements = requirements;
        this.site = null;
    }

    Patch(Type type, PatchSite site, String directions, String gardener, String requirements)
    {
        this.type = type;
        this.site = site;
        this.location = site.label;
        this.directions = directions;
        this.gardener = gardener;
        this.requirements = requirements + "; " + site.requirements;
    }

    public boolean isClassic() { return ordinal() < 16; }

    public static java.util.List<Patch> classic()
    {
        return java.util.Arrays.asList(java.util.Arrays.copyOf(values(), 16));
    }

    PatchSite getSite()
    {
        if (site != null) { return site; }
        switch (this)
        {
            case LUMBRIDGE_TREE: return PatchSite.LUMBRIDGE;
            case VARROCK_TREE: return PatchSite.VARROCK;
            case FALADOR_TREE: return PatchSite.FALADOR_TREE;
            case TAVERLEY_TREE: return PatchSite.TAVERLEY;
            case STRONGHOLD_TREE: case STRONGHOLD_FRUIT: return PatchSite.STRONGHOLD;
            case VILLAGE_FRUIT: return PatchSite.VILLAGE;
            case LLETYA_FRUIT: return PatchSite.LLETYA;
            case CATHERBY_FRUIT: return PatchSite.CATHERBY_FRUIT;
            case BRIMHAVEN_FRUIT: return PatchSite.BRIMHAVEN;
            case TAI_BWO_WANNAI_CALQUAT: return PatchSite.CALQUAT;
            case FOSSIL_EAST: case FOSSIL_MIDDLE: case FOSSIL_WEST: return PatchSite.FOSSIL;
            case GUILD_TREE: case GUILD_FRUIT: return PatchSite.GUILD;
            default: throw new IllegalStateException("Missing site: " + this);
        }
    }

    public Type getType() { return type; }
    public String getLocation() { return location; }
    public String getDirections() { return directions; }
    public String getGardener() { return gardener; }
    public String getRequirements() { return requirements; }
    public String getLabel() { return location + " - " + (isClassic() ? type.toString() : directions); }
}
