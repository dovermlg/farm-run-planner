# Next teleport item

Enable or disable **Checklist display → Highlight next teleport** in RuneLite's normal plugin cog. It starts enabled and applies across presets. The run tab names the expected item, and a green outline appears on matching carried items in the ordinary inventory, bank-side inventory or worn-equipment tab. Bank stock and trade offers are not outlined.

The next unfinished patch determines the highlight. It refreshes after completion, optional observed replant completion, undo, reset, preset or travel changes. Between inventories, it follows the bank stop until **Banked - continue** is clicked. If the bank is within walking distance of the preceding patch, no teleport is suggested. Leaving a Catherby or Farming Guild bank for its local patches also produces no outline. Catherby charter departures from Catherby use no additional teleport.

The outline remains on the current step's device until progress advances. It does not detect that you have arrived or used a teleport. It is cleared while a route or inventory plan is being prepared, when no patches remain, on plugin shutdown or when highlighting is disabled. It is hidden outside a logged-in game state.

## Supported item forms

| Route entry | Item outlined |
| --- | --- |
| Lumbridge, Varrock | Corresponding teleport tablet. |
| Falador Park | Charged ring of wealth, or Falador tablet when that travel option is selected. |
| Taverley | Redirected Taverley tablet, Falador tablet, or no item for balloon travel, according to the travel setting. |
| Stronghold normal tree | Charged or eternal slayer ring when selected; no item for spirit-tree travel. |
| Lletya, Prifddinas | Charged teleport crystal or eternal teleport crystal. |
| Catherby | Catherby or Camelot tablet, following the travel setting. |
| Brimhaven | Redirected Brimhaven tablet, Ardougne tablet for the boat route, or Catherby/Camelot tablet to reach the charter's departure town. No item when already taking the charter from Catherby. |
| Tai Bwo Wannai calquat | Loose Tai bwo wannai scroll; no item for fairy ring CKR. Walking from Brimhaven uses no additional teleport; an independent arrival first uses the configured Brimhaven route. |
| Fossil Island hardwoods, seaweed | Charged digsite pendant. |
| Farming Guild | Charged skills necklace or ordinary/trimmed Farming cape, according to the travel setting; no item for spirit-tree travel. |
| South Falador farm, Port Sarim spirit patch | Explorer's ring 2, 3 or 4. |
| North Ardougne farm | Ardougne cloak 2, 3 or 4. |
| South Ardougne bush | Ardougne cloak 1, 2, 3 or 4. |
| Port Phasmatys farm | Filled ectophial. |
| Hosidius farm | Charged Xeric's talisman. |
| Harmony Island, Draynor Manor | Corresponding Arceuus tablet. |
| Troll Stronghold, Weiss | Stony basalt, icy basalt, respectively. |
| Ortus Farm | Quetzal whistle: basic, enhanced, perfected or the infinite perfected form. |
| Lumbridge hops | Chronicle. |
| Yanille hops, Rimmington bush | Corresponding redirected house tablet. |
| Entrana | Charged, trimmed charged or eternal amulet of glory, to start the Draynor route. |
| Champions' Guild bush | Charged combat bracelet. |
| Etceteria | Charged ring of wealth. |
| Al Kharid cactus | Ring of dueling with at least one charge. |
| Hosidius spirit patch | Charged skills necklace. |
| Kastori | Civitas illa Fortis tablet, to start the quetzal route. |

Bank stops use ring of wealth for the Grand Exchange; ring of dueling for Castle Wars or Ferox; skills necklace for the Farming Guild; Catherby tablet for Catherby; Camelot tablet for Seers'; glory for Edgeville or Draynor; and Falador tablet for Falador west. These follow the bank card's suggested route independently of patch travel settings. Custom bank directions do not choose an item.

Fairy-ring, spirit-tree, boat, balloon and other journeys without a mapped carried device keep their written directions. Alternative methods mentioned in those directions are not all mapped. Cosmetic or combined items absent from the table, max capes, scroll-book contents, runes and POH devices are not detected. A missing supported item simply has no outline; the plugin does not substitute another destination or withdraw one.

Charged jewellery is matched by explicit usable IDs. Regular and imbued charged wealth rings, trimmed charged glories and eternal glory/slayer/crystal forms are included. Uncharged forms, noted copies and placeholders are excluded rather than normalized into usable items. Daily teleport limits, skill/quest requirements, configured POH destinations, Chronicle/whistle charges and other contents not encoded in the item ID remain manual checks. An outline confirms the item matches the route; it does not guarantee the teleport is currently usable.

## Implementation and checks

The resolver uses the same prepared patch order, completion marks, bank acknowledgments and walking rules as the run tab. An immutable device enum is published from Swing's EDT through a volatile field. RuneLite's public `WidgetItemOverlay` renders the outline with `ItemManager.getItemOutline()`, using the current widget's position, including dragging. It paints only; it does not modify widgets, menus, destinations, input or game actions. The plugin registers and removes its own overlay through `OverlayManager`.

The 13 offline regressions cover selected travel methods, charged/empty and noted forms, diary tiers, walking, charters, bank transitions, global settings, progress, disposal, allowed interfaces, logout and dragging. Item sprites in these render tests are fixtures. No game account or live game rendering was used.

Checked against RuneLite 1.13.2 and OSRS Wiki on 2026-10-18:

- Public API: [WidgetItemOverlay](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/ui/overlay/WidgetItemOverlay.html), [ItemManager](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/game/ItemManager.html), [WidgetItem](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/widgets/WidgetItem.html), [item constants](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java).
- Travel references: [transportation](https://oldschool.runescape.wiki/w/Transportation), [ring of wealth](https://oldschool.runescape.wiki/w/Ring_of_wealth), [skills necklace](https://oldschool.runescape.wiki/w/Skills_necklace), [glory](https://oldschool.runescape.wiki/w/Amulet_of_glory), [slayer ring](https://oldschool.runescape.wiki/w/Slayer_ring), [closest teleports](https://oldschool.runescape.wiki/w/Closest_teleports). Existing patch and bank directions are covered by the project's farming audit and [bank reference](inventory-planning.md).
- Review scope: [RuneLite restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features). Local API checks are a development preflight, not maintainer approval.
