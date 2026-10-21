package com.classictreerun;

import java.util.Set;
import net.runelite.api.gameval.ItemID;

/** Explicit usable item forms; no name matching or charge-collapsing canonicalization. */
enum TeleportDevice
{
    NONE("", Set.of()),
    LUMBRIDGE("Lumbridge teleport tablet", Set.of(ItemID.POH_TABLET_LUMBRIDGETELEPORT)),
    VARROCK("Varrock teleport tablet", Set.of(ItemID.POH_TABLET_VARROCKTELEPORT)),
    FALADOR("Falador teleport tablet", Set.of(ItemID.POH_TABLET_FALADORTELEPORT)),
    CAMELOT("Camelot teleport tablet", Set.of(ItemID.POH_TABLET_CAMELOTTELEPORT)),
    CATHERBY("Catherby teleport tablet", Set.of(ItemID.LUNAR_TABLET_CATHERBY_TELEPORT)),
    ARDOUGNE("Ardougne teleport tablet", Set.of(ItemID.POH_TABLET_ARDOUGNETELEPORT)),
    TAVERLEY("Taverley teleport tablet", Set.of(ItemID.NZONE_TELETAB_TAVERLEY)),
    BRIMHAVEN("Brimhaven teleport tablet", Set.of(ItemID.NZONE_TELETAB_BRIMHAVEN)),
    YANILLE("Yanille teleport tablet", Set.of(ItemID.NZONE_TELETAB_YANILLE)),
    RIMMINGTON("Rimmington teleport tablet", Set.of(ItemID.NZONE_TELETAB_RIMMINGTON)),
    HARMONY("Harmony Island teleport tablet", Set.of(ItemID.TELETAB_HARMONY)),
    DRAYNOR_MANOR("Draynor Manor teleport tablet", Set.of(ItemID.TELETAB_DRAYNOR)),
    FORTIS("Civitas illa Fortis teleport tablet", Set.of(ItemID.POH_TABLET_FORTISTELEPORT)),
    WEALTH("Ring of wealth", Set.of(ItemID.RING_OF_WEALTH_1, ItemID.RING_OF_WEALTH_2,
        ItemID.RING_OF_WEALTH_3, ItemID.RING_OF_WEALTH_4, ItemID.RING_OF_WEALTH_5,
        ItemID.RING_OF_WEALTH_I1, ItemID.RING_OF_WEALTH_I2, ItemID.RING_OF_WEALTH_I3,
        ItemID.RING_OF_WEALTH_I4, ItemID.RING_OF_WEALTH_I5)),
    DUELING("Ring of dueling", Set.of(ItemID.RING_OF_DUELING_1, ItemID.RING_OF_DUELING_2,
        ItemID.RING_OF_DUELING_3, ItemID.RING_OF_DUELING_4, ItemID.RING_OF_DUELING_5,
        ItemID.RING_OF_DUELING_6, ItemID.RING_OF_DUELING_7, ItemID.RING_OF_DUELING_8)),
    GLORY("Amulet of glory", Set.of(ItemID.AMULET_OF_GLORY_1, ItemID.AMULET_OF_GLORY_2,
        ItemID.AMULET_OF_GLORY_3, ItemID.AMULET_OF_GLORY_4, ItemID.AMULET_OF_GLORY_5,
        ItemID.AMULET_OF_GLORY_6, ItemID.TRAIL_AMULET_OF_GLORY_1, ItemID.TRAIL_AMULET_OF_GLORY_2,
        ItemID.TRAIL_AMULET_OF_GLORY_3, ItemID.TRAIL_AMULET_OF_GLORY_4,
        ItemID.TRAIL_AMULET_OF_GLORY_5, ItemID.TRAIL_AMULET_OF_GLORY_6, ItemID.AMULET_OF_GLORY_INF)),
    SKILLS("Skills necklace", Set.of(ItemID.JEWL_NECKLACE_OF_SKILLS_1, ItemID.JEWL_NECKLACE_OF_SKILLS_2,
        ItemID.JEWL_NECKLACE_OF_SKILLS_3, ItemID.JEWL_NECKLACE_OF_SKILLS_4,
        ItemID.JEWL_NECKLACE_OF_SKILLS_5, ItemID.JEWL_NECKLACE_OF_SKILLS_6)),
    SLAYER("Slayer ring", Set.of(ItemID.SLAYER_RING_1, ItemID.SLAYER_RING_2, ItemID.SLAYER_RING_3,
        ItemID.SLAYER_RING_4, ItemID.SLAYER_RING_5, ItemID.SLAYER_RING_6, ItemID.SLAYER_RING_7,
        ItemID.SLAYER_RING_8, ItemID.SLAYER_RING_ETERNAL)),
    DIGSITE("Digsite pendant", Set.of(ItemID.NECKLACE_OF_DIGSITE_1, ItemID.NECKLACE_OF_DIGSITE_2,
        ItemID.NECKLACE_OF_DIGSITE_3, ItemID.NECKLACE_OF_DIGSITE_4, ItemID.NECKLACE_OF_DIGSITE_5)),
    CRYSTAL("Teleport crystal", Set.of(ItemID.MOURNING_TELEPORT_CRYSTAL_1,
        ItemID.MOURNING_TELEPORT_CRYSTAL_2, ItemID.MOURNING_TELEPORT_CRYSTAL_3,
        ItemID.MOURNING_TELEPORT_CRYSTAL_4, ItemID.MOURNING_TELEPORT_CRYSTAL_5, ItemID.PRIF_TELEPORT_CRYSTAL)),
    FARMING_CAPE("Farming cape", Set.of(ItemID.SKILLCAPE_FARMING, ItemID.SKILLCAPE_FARMING_TRIMMED)),
    TAI_BWO_WANNAI("Tai bwo wannai teleport scroll", Set.of(ItemID.TELEPORTSCROLL_TAIBWO)),
    EXPLORER("Explorer's ring 2 or higher", Set.of(ItemID.LUMBRIDGE_RING_MEDIUM,
        ItemID.LUMBRIDGE_RING_HARD, ItemID.LUMBRIDGE_RING_ELITE)),
    ARDOUGNE_FARM("Ardougne cloak 2 or higher", Set.of(ItemID.ARDY_CAPE_MEDIUM,
        ItemID.ARDY_CAPE_HARD, ItemID.ARDY_CAPE_ELITE)),
    ARDOUGNE_MONASTERY("Ardougne cloak", Set.of(ItemID.ARDY_CAPE_EASY, ItemID.ARDY_CAPE_MEDIUM,
        ItemID.ARDY_CAPE_HARD, ItemID.ARDY_CAPE_ELITE)),
    ECTOPHIAL("Ectophial", Set.of(ItemID.ECTOPHIAL)),
    XERIC("Xeric's talisman", Set.of(ItemID.XERIC_TALISMAN)),
    STONY_BASALT("Stony basalt", Set.of(ItemID.STRONGHOLD_TELEPORT_BASALT)),
    ICY_BASALT("Icy basalt", Set.of(ItemID.WEISS_TELEPORT_BASALT)),
    QUETZAL("Quetzal whistle", Set.of(ItemID.HG_QUETZALWHISTLE_BASIC,
        ItemID.HG_QUETZALWHISTLE_ENHANCED, ItemID.HG_QUETZALWHISTLE_PERFECTED,
        ItemID.HG_QUETZALWHISTLE_PERFECTED_INFINITE)),
    CHRONICLE("Chronicle", Set.of(ItemID.CHRONICLE)),
    COMBAT("Combat bracelet", Set.of(ItemID.JEWL_BRACELET_OF_COMBAT_1,
        ItemID.JEWL_BRACELET_OF_COMBAT_2, ItemID.JEWL_BRACELET_OF_COMBAT_3,
        ItemID.JEWL_BRACELET_OF_COMBAT_4, ItemID.JEWL_BRACELET_OF_COMBAT_5,
        ItemID.JEWL_BRACELET_OF_COMBAT_6));

    final String label;
    private final Set<Integer> items;

    TeleportDevice(String label, Set<Integer> items)
    {
        this.label = label;
        this.items = items;
    }

    boolean matches(int itemId) { return items.contains(itemId); }
    Set<Integer> items() { return items; }
    int representative() { return items.stream().mapToInt(Integer::intValue).min().orElseThrow(); }
    boolean consumable()
    {
        switch(this)
        {
            case LUMBRIDGE: case VARROCK: case FALADOR: case CAMELOT: case CATHERBY:
            case ARDOUGNE: case TAVERLEY: case BRIMHAVEN: case YANILLE: case RIMMINGTON:
            case HARMONY: case DRAYNOR_MANOR: case FORTIS: case TAI_BWO_WANNAI:
            case STONY_BASALT: case ICY_BASALT: return true;
            default: return false;
        }
    }
}
