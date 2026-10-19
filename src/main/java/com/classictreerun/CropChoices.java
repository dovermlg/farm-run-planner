package com.classictreerun;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

/** Planned supplies, separate from crops actually observed in the game. See docs/supplies.md. */
public final class CropChoices
{
    private CropChoices() { }
    public interface Choice { Crop crop(); }
    static final class Payment
    {
        final int item, quantity; final String label;
        Payment(int item, int quantity, String label) { this.item=item; this.quantity=quantity; this.label=label; }
    }
    static final class Crop
    {
        final String label; final int level, item, quantity; final List<Payment> payments;
        Crop(String label, int level, int item, int quantity, Payment... payments)
        { this.label=label; this.level=level; this.item=item; this.quantity=quantity; this.payments=List.of(payments); }
    }
    private static final Crop NONE = new Crop("Harvest / check only", 0, -1, 0);

    public enum Tree implements Choice
    {
        NONE(CropChoices.NONE),
        OAK(new Crop("Oak", 15, ItemID.PLANTPOT_OAK_SAPLING, 1, new Payment(ItemID.BASKET_TOMATO_5, 1, "Tomatoes(5)"))),
        WILLOW(new Crop("Willow", 30, ItemID.PLANTPOT_WILLOW_SAPLING, 1, new Payment(ItemID.BASKET_APPLE_5, 1, "Apples(5)"))),
        MAPLE(new Crop("Maple", 45, ItemID.PLANTPOT_MAPLE_SAPLING, 1, new Payment(ItemID.BASKET_ORANGE_5, 1, "Oranges(5)"))),
        YEW(new Crop("Yew", 60, ItemID.PLANTPOT_YEW_SAPLING, 1, new Payment(ItemID.CACTUS_SPINE, 10, "Cactus spine"))),
        MAGIC(new Crop("Magic", 75, ItemID.PLANTPOT_MAGIC_TREE_SAPLING, 1, new Payment(ItemID.COCONUT, 25, "Coconut")));
        private final Crop crop;
        Tree(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Fruit implements Choice
    {
        NONE(CropChoices.NONE),
        APPLE(new Crop("Apple", 27, ItemID.PLANTPOT_APPLE_SAPLING, 1, new Payment(ItemID.SWEETCORN, 9, "Sweetcorn"))),
        BANANA(new Crop("Banana", 33, ItemID.PLANTPOT_BANANA_SAPLING, 1, new Payment(ItemID.BASKET_APPLE_5, 4, "Apples(5)"))),
        ORANGE(new Crop("Orange", 39, ItemID.PLANTPOT_ORANGE_SAPLING, 1, new Payment(ItemID.BASKET_STRAWBERRY_5, 3, "Strawberries(5)"))),
        CURRY(new Crop("Curry", 42, ItemID.PLANTPOT_CURRY_SAPLING, 1, new Payment(ItemID.BASKET_BANANA_5, 5, "Bananas(5)"))),
        PINEAPPLE(new Crop("Pineapple", 51, ItemID.PLANTPOT_PINEAPPLE_SAPLING, 1, new Payment(ItemID.WATERMELON, 10, "Watermelon"))),
        PAPAYA(new Crop("Papaya", 57, ItemID.PLANTPOT_PAPAYA_SAPLING, 1, new Payment(ItemID.PINEAPPLE, 10, "Pineapple"))),
        PALM(new Crop("Palm", 68, ItemID.PLANTPOT_PALM_SAPLING, 1, new Payment(ItemID.PAPAYA, 15, "Papaya fruit"))),
        DRAGONFRUIT(new Crop("Dragonfruit", 81, ItemID.PLANTPOT_DRAGONFRUIT_SAPLING, 1, new Payment(ItemID.COCONUT, 15, "Coconut")));
        private final Crop crop;
        Fruit(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Hardwood implements Choice
    {
        NONE(CropChoices.NONE),
        TEAK(new Crop("Teak", 35, ItemID.PLANTPOT_TEAK_SAPLING, 1, new Payment(ItemID.LIMPWURT_ROOT, 15, "Limpwurt root"))),
        MAHOGANY(new Crop("Mahogany", 55, ItemID.PLANTPOT_MAHOGANY_SAPLING, 1, new Payment(ItemID.YANILLIAN_HOPS, 25, "Yanillian hops"))),
        CAMPHOR(new Crop("Camphor", 66, ItemID.PLANTPOT_CAMPHOR_SAPLING, 1, new Payment(ItemID.WHITE_BERRIES, 10, "White berries"))),
        IRONWOOD(new Crop("Ironwood", 80, ItemID.PLANTPOT_IRONWOOD_SAPLING, 1, new Payment(ItemID.CURRY_LEAF, 10, "Curry leaf"))),
        ROSEWOOD(new Crop("Rosewood", 92, ItemID.PLANTPOT_ROSEWOOD_SAPLING, 1, new Payment(ItemID.DRAGONFRUIT, 8, "Dragonfruit")));
        private final Crop crop;
        Hardwood(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Allotment implements Choice
    {
        NONE(CropChoices.NONE),
        POTATO(new Crop("Potato", 1, ItemID.POTATO_SEED, 3, new Payment(ItemID.BUCKET_COMPOST, 2, "Compost"))),
        ONION(new Crop("Onion", 5, ItemID.ONION_SEED, 3, new Payment(ItemID.SACK_POTATO_10, 1, "Potatoes(10)"))),
        CABBAGE(new Crop("Cabbage", 7, ItemID.CABBAGE_SEED, 3, new Payment(ItemID.SACK_ONION_10, 1, "Onions(10)"))),
        TOMATO(new Crop("Tomato", 12, ItemID.TOMATO_SEED, 3, new Payment(ItemID.SACK_CABBAGE_10, 2, "Cabbages(10)"))),
        SWEETCORN(new Crop("Sweetcorn", 20, ItemID.SWEETCORN_SEED, 3, new Payment(ItemID.JUTE_FIBRE, 10, "Jute fibre"))),
        STRAWBERRY(new Crop("Strawberry", 31, ItemID.STRAWBERRY_SEED, 3, new Payment(ItemID.BASKET_APPLE_5, 1, "Apples(5)"))),
        WATERMELON(new Crop("Watermelon", 47, ItemID.WATERMELON_SEED, 3, new Payment(ItemID.CURRY_LEAF, 10, "Curry leaf"))),
        SNAPE_GRASS(new Crop("Snape grass", 61, ItemID.SNAPE_GRASS_SEED, 3, new Payment(ItemID.JANGERBERRIES, 5, "Jangerberries")));
        private final Crop crop;
        Allotment(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Flower implements Choice
    {
        NONE(CropChoices.NONE),
        MARIGOLD(new Crop("Marigold", 2, ItemID.MARIGOLD_SEED, 1)),
        ROSEMARY(new Crop("Rosemary", 11, ItemID.ROSEMARY_SEED, 1)),
        NASTURTIUM(new Crop("Nasturtium", 24, ItemID.NASTURTIUM_SEED, 1)),
        WOAD(new Crop("Woad", 25, ItemID.WOAD_SEED, 1)),
        LIMPWURT(new Crop("Limpwurt", 26, ItemID.LIMPWURT_SEED, 1)),
        WHITE_LILY(new Crop("White lily", 58, ItemID.WHITE_LILY_SEED, 1));
        private final Crop crop;
        Flower(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Herb implements Choice
    {
        NONE(CropChoices.NONE),
        GUAM(new Crop("Guam", 9, ItemID.GUAM_SEED, 1)),
        MARRENTILL(new Crop("Marrentill", 14, ItemID.MARRENTILL_SEED, 1)),
        TARROMIN(new Crop("Tarromin", 19, ItemID.TARROMIN_SEED, 1)),
        HARRALANDER(new Crop("Harralander", 26, ItemID.HARRALANDER_SEED, 1)),
        GOUTWEED(new Crop("Goutweed", 29, ItemID.VILLAGE_RARE_TUBER, 1)),
        RANARR(new Crop("Ranarr", 32, ItemID.RANARR_SEED, 1)),
        TOADFLAX(new Crop("Toadflax", 38, ItemID.TOADFLAX_SEED, 1)),
        IRIT(new Crop("Irit", 44, ItemID.IRIT_SEED, 1)),
        AVANTOE(new Crop("Avantoe", 50, ItemID.AVANTOE_SEED, 1)),
        KWUARM(new Crop("Kwuarm", 56, ItemID.KWUARM_SEED, 1)),
        SNAPDRAGON(new Crop("Snapdragon", 62, ItemID.SNAPDRAGON_SEED, 1)),
        HUASCA(new Crop("Huasca", 65, ItemID.HUASCA_SEED, 1)),
        CADANTINE(new Crop("Cadantine", 67, ItemID.CADANTINE_SEED, 1)),
        LANTADYME(new Crop("Lantadyme", 73, ItemID.LANTADYME_SEED, 1)),
        DWARF_WEED(new Crop("Dwarf weed", 79, ItemID.DWARF_WEED_SEED, 1)),
        TORSTOL(new Crop("Torstol", 85, ItemID.TORSTOL_SEED, 1));
        private final Crop crop;
        Herb(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Hops implements Choice
    {
        NONE(CropChoices.NONE),
        BARLEY(new Crop("Barley", 3, ItemID.BARLEY_SEED, 4, new Payment(ItemID.BUCKET_COMPOST, 3, "Compost"))),
        HAMMERSTONE(new Crop("Hammerstone", 4, ItemID.HAMMERSTONE_HOP_SEED, 4, new Payment(ItemID.MARIGOLD, 1, "Marigold"))),
        ASGARNIAN(new Crop("Asgarnian", 8, ItemID.ASGARNIAN_HOP_SEED, 4, new Payment(ItemID.SACK_ONION_10, 1, "Onions(10)"))),
        JUTE(new Crop("Jute", 13, ItemID.JUTE_SEED, 3, new Payment(ItemID.BARLEY_MALT, 6, "Barley malt"))),
        YANILLIAN(new Crop("Yanillian", 16, ItemID.YANILLIAN_HOP_SEED, 4, new Payment(ItemID.BASKET_TOMATO_5, 1, "Tomatoes(5)"))),
        FLAX(new Crop("Flax", 18, ItemID.FLAX_SEED, 3, new Payment(ItemID.GRAIN, 6, "Grain"))),
        KRANDORIAN(new Crop("Krandorian", 21, ItemID.KRANDORIAN_HOP_SEED, 4, new Payment(ItemID.SACK_CABBAGE_10, 3, "Cabbages(10)"))),
        WILDBLOOD(new Crop("Wildblood", 28, ItemID.WILDBLOOD_HOP_SEED, 4, new Payment(ItemID.NASTURTIUM, 1, "Nasturtium"))),
        HEMP(new Crop("Hemp", 37, ItemID.HEMP_SEED, 3, new Payment(ItemID.FLAX, 6, "Flax"))),
        COTTON(new Crop("Cotton", 71, ItemID.COTTON_SEED, 3, new Payment(ItemID.HEMP, 6, "Hemp")));
        private final Crop crop;
        Hops(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Bush implements Choice
    {
        NONE(CropChoices.NONE),
        REDBERRIES(new Crop("Redberries", 10, ItemID.REDBERRY_BUSH_SEED, 1, new Payment(ItemID.SACK_CABBAGE_10, 4, "Cabbages(10)"))),
        CADAVABERRIES(new Crop("Cadavaberries", 22, ItemID.CADAVABERRY_BUSH_SEED, 1, new Payment(ItemID.BASKET_TOMATO_5, 3, "Tomatoes(5)"))),
        DWELLBERRIES(new Crop("Dwellberries", 36, ItemID.DWELLBERRY_BUSH_SEED, 1, new Payment(ItemID.BASKET_STRAWBERRY_5, 3, "Strawberries(5)"))),
        JANGERBERRIES(new Crop("Jangerberries", 48, ItemID.JANGERBERRY_BUSH_SEED, 1, new Payment(ItemID.WATERMELON, 6, "Watermelon"))),
        WHITEBERRIES(new Crop("White berries", 59, ItemID.WHITEBERRY_BUSH_SEED, 1, new Payment(ItemID.BITTERCAP_MUSHROOM, 8, "Bittercap mushroom"))),
        POISON_IVY(new Crop("Poison ivy", 70, ItemID.POISONIVY_BUSH_SEED, 1));
        private final Crop crop;
        Bush(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Cactus implements Choice
    {
        NONE(CropChoices.NONE),
        CACTUS(new Crop("Cactus", 55, ItemID.CACTUS_SEED, 1, new Payment(ItemID.CADAVABERRIES, 6, "Cadava berries"))),
        POTATO_CACTUS(new Crop("Potato cactus", 64, ItemID.POTATO_CACTUS_SEED, 1, new Payment(ItemID.SNAPE_GRASS, 8, "Snape grass")));
        private final Crop crop;
        Cactus(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Calquat implements Choice
    {
        NONE(CropChoices.NONE),
        CALQUAT(new Crop("Calquat", 72, ItemID.PLANTPOT_CALQUAT_SAPLING, 1, new Payment(ItemID.POISONIVY_BERRIES, 8, "Poison ivy berries")));
        private final Crop crop;
        Calquat(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Seaweed implements Choice
    {
        NONE(CropChoices.NONE),
        GIANT_SEAWEED(new Crop("Giant seaweed", 23, ItemID.SEAWEED_SEED, 1, new Payment(ItemID.FOSSIL_NUMULITE, 200, "Numulite")));
        private final Crop crop;
        Seaweed(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Grape implements Choice
    {
        NONE(CropChoices.NONE),
        GRAPE(new Crop("Grape", 36, ItemID.GRAPE_SEED, 1));
        private final Crop crop;
        Grape(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Mushroom implements Choice
    {
        NONE(CropChoices.NONE),
        BITTERCAP(new Crop("Bittercap mushroom", 53, ItemID.MUSHROOM_SEED, 1));
        private final Crop crop;
        Mushroom(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Belladonna implements Choice
    {
        NONE(CropChoices.NONE),
        BELLADONNA(new Crop("Belladonna", 63, ItemID.BELLADONNA_SEED, 1));
        private final Crop crop;
        Belladonna(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Hespori implements Choice
    {
        NONE(CropChoices.NONE),
        HESPORI(new Crop("Hespori", 65, ItemID.HESPORI_SEED, 1));
        private final Crop crop;
        Hespori(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Anima implements Choice
    {
        NONE(CropChoices.NONE),
        ATTAS(new Crop("Attas", 76, ItemID.ATTAS_SEED, 1)),
        IASOR(new Crop("Iasor", 76, ItemID.IASOR_SEED, 1)),
        KRONOS(new Crop("Kronos", 76, ItemID.KRONOS_SEED, 1));
        private final Crop crop;
        Anima(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Crystal implements Choice
    {
        NONE(CropChoices.NONE),
        CRYSTAL(new Crop("Crystal tree", 74, ItemID.PLANTPOT_CRYSTAL_TREE_SAPLING, 1));
        private final Crop crop;
        Crystal(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Celastrus implements Choice
    {
        NONE(CropChoices.NONE),
        CELASTRUS(new Crop("Celastrus", 85, ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING, 1, new Payment(ItemID.CACTUS_POTATO, 8, "Potato cactus")));
        private final Crop crop;
        Celastrus(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Redwood implements Choice
    {
        NONE(CropChoices.NONE),
        REDWOOD(new Crop("Redwood", 90, ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, 1, new Payment(ItemID.DRAGONFRUIT, 6, "Dragonfruit")));
        private final Crop crop;
        Redwood(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Coral implements Choice
    {
        NONE(CropChoices.NONE),
        ELKHORN(new Crop("Elkhorn", 28, ItemID.CORAL_ELKHORN_FRAG, 1, new Payment(ItemID.GIANT_SEAWEED, 5, "Giant seaweed"))),
        PILLAR(new Crop("Pillar", 52, ItemID.CORAL_PILLAR_FRAG, 1, new Payment(ItemID.CORAL_ELKHORN, 5, "Elkhorn coral"))),
        UMBRAL(new Crop("Umbral", 77, ItemID.CORAL_UMBRAL_FRAG, 1, new Payment(ItemID.CORAL_PILLAR, 5, "Pillar coral")));
        private final Crop crop;
        Coral(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    public enum Spirit implements Choice
    {
        NONE(CropChoices.NONE),
        SPIRIT(new Crop("Spirit tree", 83, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING, 1, new Payment(ItemID.MM_MONKEY_NUTS, 5, "Monkey nuts"), new Payment(ItemID.MM_MONKEY_BAR, 1, "Monkey bar"), new Payment(ItemID.LUNAR_GROUNDTOOTH, 1, "Ground tooth")));
        private final Crop crop;
        Spirit(Crop crop) { this.crop=crop; }
        public Crop crop() { return crop; }
        @Override public String toString() { return this==NONE ? crop.label : crop.label+" ("+crop.level+")"; }
    }

    static Crop forType(Patch.Type type, ClassicTreeRunConfig config)
    {
        switch(type)
        {
            case TREE: return config.cropTree().crop();
            case FRUIT: return config.cropFruit().crop();
            case HARDWOOD: return config.cropHardwood().crop();
            case ALLOTMENT: return config.cropAllotment().crop();
            case FLOWER: return config.cropFlower().crop();
            case HERB: return config.cropHerb().crop();
            case HOPS: return config.cropHops().crop();
            case BUSH: return config.cropBush().crop();
            case CACTUS: return config.cropCactus().crop();
            case CALQUAT: return config.cropCalquat().crop();
            case SEAWEED: return config.cropSeaweed().crop();
            case GRAPE: return config.cropGrape().crop();
            case MUSHROOM: return config.cropMushroom().crop();
            case BELLADONNA: return config.cropBelladonna().crop();
            case HESPORI: return config.cropHespori().crop();
            case ANIMA: return config.cropAnima().crop();
            case CRYSTAL: return config.cropCrystal().crop();
            case CELASTRUS: return config.cropCelastrus().crop();
            case REDWOOD: return config.cropRedwood().crop();
            case CORAL: return config.cropCoral().crop();
            case SPIRIT: return config.cropSpirit().crop();
            default: throw new IllegalArgumentException("Unknown crop type");
        }
    }

    static Map<String,String> settings(ClassicTreeRunConfig config)
    {
        Map<String,String> values=new LinkedHashMap<>();
        values.put("cropTree",config.cropTree().name());
        values.put("cropFruit",config.cropFruit().name());
        values.put("cropHardwood",config.cropHardwood().name());
        values.put("cropAllotment",config.cropAllotment().name());
        values.put("cropFlower",config.cropFlower().name());
        values.put("cropHerb",config.cropHerb().name());
        values.put("cropHops",config.cropHops().name());
        values.put("cropBush",config.cropBush().name());
        values.put("cropCactus",config.cropCactus().name());
        values.put("cropCalquat",config.cropCalquat().name());
        values.put("cropSeaweed",config.cropSeaweed().name());
        values.put("cropGrape",config.cropGrape().name());
        values.put("cropMushroom",config.cropMushroom().name());
        values.put("cropBelladonna",config.cropBelladonna().name());
        values.put("cropHespori",config.cropHespori().name());
        values.put("cropAnima",config.cropAnima().name());
        values.put("cropCrystal",config.cropCrystal().name());
        values.put("cropCelastrus",config.cropCelastrus().name());
        values.put("cropRedwood",config.cropRedwood().name());
        values.put("cropCoral",config.cropCoral().name());
        values.put("cropSpirit",config.cropSpirit().name());
        values.put("supplyProtection",Boolean.toString(config.supplyProtection()));
        values.put("supplyTools",Boolean.toString(config.supplyTools()));
        values.put("supplyRake",Boolean.toString(config.supplyRake()));
        values.put("supplyDibber",Boolean.toString(config.supplyDibber()));
        values.put("supplyCoins",Integer.toString(config.supplyCoins()));
        values.put("supplyCompost",config.supplyCompost().name());
        return values;
    }

    static void validate(String key, String value)
    {
        switch(key)
        {
            case "cropTree": Tree.valueOf(value); return;
            case "cropFruit": Fruit.valueOf(value); return;
            case "cropHardwood": Hardwood.valueOf(value); return;
            case "cropAllotment": Allotment.valueOf(value); return;
            case "cropFlower": Flower.valueOf(value); return;
            case "cropHerb": Herb.valueOf(value); return;
            case "cropHops": Hops.valueOf(value); return;
            case "cropBush": Bush.valueOf(value); return;
            case "cropCactus": Cactus.valueOf(value); return;
            case "cropCalquat": Calquat.valueOf(value); return;
            case "cropSeaweed": Seaweed.valueOf(value); return;
            case "cropGrape": Grape.valueOf(value); return;
            case "cropMushroom": Mushroom.valueOf(value); return;
            case "cropBelladonna": Belladonna.valueOf(value); return;
            case "cropHespori": Hespori.valueOf(value); return;
            case "cropAnima": Anima.valueOf(value); return;
            case "cropCrystal": Crystal.valueOf(value); return;
            case "cropCelastrus": Celastrus.valueOf(value); return;
            case "cropRedwood": Redwood.valueOf(value); return;
            case "cropCoral": Coral.valueOf(value); return;
            case "cropSpirit": Spirit.valueOf(value); return;
            case "supplyCompost": SupplyPlan.Compost.valueOf(value); return;
            case "supplyCoins": if(!value.matches("[0-9]{1,10}") || Long.parseLong(value)>Integer.MAX_VALUE)throw new IllegalArgumentException("Invalid coin budget"); return;
            default: if(!List.of("supplyProtection","supplyTools","supplyRake","supplyDibber").contains(key) || !("true".equals(value)||"false".equals(value)))throw new IllegalArgumentException("Invalid supply option");
        }
    }
}
