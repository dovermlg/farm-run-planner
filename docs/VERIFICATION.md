# Patch and travel references

Catalogue audit: **2026-10-18**, against the OSRS Wiki patch tables and **RuneLite 1.13.2**. The catalogue contains 99 recurring plantable patches across 21 crop types. Decoder and boundary checks are documented in [DATA-AUDIT.md](DATA-AUDIT.md).

## Sources

| Source | Coverage |
| --- | --- |
| [Farming patch locations](https://oldschool.runescape.wiki/w/Farming/Patch_locations) | Regular patches and excluded quest/activity plots |
| [Allotment patches](https://oldschool.runescape.wiki/w/Allotment_patch/Patches) | 17 allotments, 9 flowers and 10 herbs; diary and guild access |
| [Normal tree patches](https://oldschool.runescape.wiki/w/Tree_patch/Patches) | Seven trees, including Nemus Retreat |
| [Fruit tree patches](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Patches) | Seven fruit trees, including Kastori |
| [Hops patches](https://oldschool.runescape.wiki/w/Hops_patch/Patches) | Five patches, including Aldarin |
| [Bush patches](https://oldschool.runescape.wiki/w/Bush_patch/Patches) | Five bushes and entry routes |
| [Spirit tree patches](https://oldschool.runescape.wiki/w/Spirit_Tree_(Farming)/Patches) | Five plantable patches; POH construction excluded |
| [Special patches](https://oldschool.runescape.wiki/w/Special_patches/Patches) | Hardwood, calquat, cactus, seaweed, grapes, coral and other special crops |
| [The Great Conch](https://oldschool.runescape.wiki/w/The_Great_Conch) | CJQ, Summer Shore calquat and separate coral access |
| [Anglers' Retreat](https://oldschool.runescape.wiki/w/Anglers%27_Retreat) | Sailing and built-rowboat access |
| [Kastori](https://oldschool.runescape.wiki/w/Kastori) | Quetzal landing site and Pendant of Ates statue |
| [Auburnvale](https://oldschool.runescape.wiki/w/Auburnvale) | AIS, quetzal access, belladonna and nearby Nemus Retreat |

The audit used live Wiki wikitext and the tagged [RuneLite farming sources](https://github.com/runelite/runelite/tree/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming). Source and table hashes are in [reference-provenance.json](reference-provenance.json). Private farming classes are used only by the offline developer utilities, not the runtime plugin.

## Location and access details

- Catherby has a fruit tree patch; it has no normal tree patch.
- Tree Gnome Village's fruit patch is outside the maze. Follow Elkoy out from the spirit tree.
- The Farming Guild's normal tree requires the west wing at 65 Farming; its fruit tree requires the north wing at 85.
- Harmony's herb patch requires the Elite Morytania diary. Its allotment does not.
- Prifddinas has allotment, flower and crystal-tree entries in this catalogue; it has no herb patch.
- Tai Bwo Wannai teleport scrolls require Tai Bwo Wannai Trio. Catherby Teleport uses the Lunar spellbook.
- Redirected Taverley and Brimhaven house tablets have distinct destinations. Ordinary Teleport to House follows the current house location.
- Lletya and Prifddinas crystal destinations are distinct. Fossil Island's barge entry is separate from the unlocked pendant/House on the Hill route.
- Fossil hardwoods use East/Middle/West labels; seaweed uses North/South; coral uses East/West. Grapes follow RuneLite's East 1–6 and West 1–6 labels.

The Brimhaven **Catherby charter ship** option follows the [fruit-patch travel table](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Patches): walk from Catherby fruit patch to its docks, charter to Brimhaven, then walk to the fruit patch. When Catherby fruit is excluded, the route goes directly to the docks using the selected Catherby/Camelot entry. Fare coins are required; discounted fares are not assumed.

Access notes list the main gates and transport requirements. They do not replace seed-specific levels or a complete quest dependency list. Remove patches you cannot use.

## Route model

`TravelCosts` contains relative travel-effort scores. These are not measured timings, exact tile distances or a navigation graph. Up to 16 selected patches use an exact solver for those scores. Larger runs use bounded nearest-neighbour and directed-relocation searches, retaining the existing order when no improvement is found.

Shared walking legs apply to actual sites. Hardwoods on different islands do not share a walking leg simply because they have the same crop type. Underwater seaweed, the Great Conch surface and coral nurseries remain separate access areas. The model does not infer the player's starting position, banking needs, teleport charges, spellbook or unlocks.

## RuneLite integration

Settings use public [ConfigItem](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/config/ConfigItem.java) and [ConfigPanel](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/config/ConfigPanel.java) controls. The 32 preset slots use stable enum IDs with custom names; the cog must be reopened after selecting a slot to refresh its other fields. Main-tab selection starts a fresh checklist; loading a slot for editing preserves progress.

Native settings follow [ConfigManager](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java) and its optional profile sync. Separate local files store the preset library, progress and crop observations. Compatibility keys and migrations are retained for earlier versions; the current workflow is described in the [README](../README.md).
