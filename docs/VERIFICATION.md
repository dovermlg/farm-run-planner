# Catalogue and travel verification

Audited 2026-10-18 (America/Chicago). Latest audit: Farm Run Planner 1.10.0; [Plugin Hub readiness](HUB-REVIEW.md). Farming-data audit: see [DATA-AUDIT.md](DATA-AUDIT.md) for exhaustive data checks and corrections. Version sections below retain the development history.

## Current scope

All **99 recurring crop patches** in the audited patch tables are individually selectable. Counts also match RuneLite's current `FarmingWorld` definitions after excluding its eight compost-bin entries. The catalogue covers 21 crop types. It includes 7 normal trees (the added patch is at **Nemus Retreat**), 7 fruit trees, 5 hardwoods, 3 calquats, all regular allotments/flowers/herbs, hops, bushes, cactus, seaweed, grapes, spirit trees and special crops.

Quest-specific plots, minigame/raid/activity plots, rake-only scenery, compost bins and player-owned-house construction spaces are outside this regular-run catalogue. A POH spirit tree is a construction feature, not one of the five plantable spirit-tree patches. The selection is explicitly stored, so adding catalogue entries does not silently add them to existing runs.

## Primary OSRS sources

The web search cache returned older data for some pages, including an obsolete alpha-only description of coral. To resolve that, the **live Wiki wikitext was fetched directly over HTTPS with `?action=raw`**. The source pages below provided the current patch lists, map locations, transport options and key requirements. A requested Herb patch/Patches subpage returned 404: herb rows are actually in the shared allotment/flower/herb table, and were verified there.

| Source | Used for |
| --- | --- |
| [Farming/Patch locations](https://oldschool.runescape.wiki/w/Farming/Patch_locations) | Catalogue sections and distinction between regular, quest and activity plots |
| [Allotment patch/Patches](https://oldschool.runescape.wiki/w/Allotment_patch/Patches) | 17 allotments, 9 flowers, 10 herbs; diary gates, farm transport and guild wing access |
| [Tree patch/Patches](https://oldschool.runescape.wiki/w/Tree_patch/Patches) | Seven normal trees; Nemus Retreat rather than merely the broad Auburnvale region label |
| [Fruit tree patch/Patches](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Patches) | Seven fruit trees, including Kastori; transport and outside-maze Village location |
| [Hops patch/Patches](https://oldschool.runescape.wiki/w/Hops_patch/Patches) | Five hops patches including Aldarin; entry routes |
| [Bush patch/Patches](https://oldschool.runescape.wiki/w/Bush_patch/Patches) | Five bushes and their entry routes |
| [Spirit Tree (Farming)/Patches](https://oldschool.runescape.wiki/w/Spirit_Tree_(Farming)/Patches) | Five plantable spirit-tree patches; guild north-wing access; POH exclusion |
| [Special patches/Patches](https://oldschool.runescape.wiki/w/Special_patches/Patches) | Hardwood, calquat, cactus, seaweed, grapes, coral nurseries, belladonna, mushroom, Hespori, anima, crystal, celastrus and redwood |
| [The Great Conch](https://oldschool.runescape.wiki/w/The_Great_Conch) | CJQ access after quest progress; Summer Shore calquat and separate Coral Nurseries entry |
| [Anglers' Retreat](https://oldschool.runescape.wiki/w/Anglers%27_Retreat) | 51 Sailing to moor; Corsair Cove rowboat requires first building it on the island |
| [Kastori](https://oldschool.runescape.wiki/w/Kastori) | Built quetzal landing site or activated Pendant of Ates statue |
| [Auburnvale](https://oldschool.runescape.wiki/w/Auburnvale) | AIS and quetzal access; belladonna to the east; nearby Nemus access |

Each allotment is distinct. The 12 grape entries follow RuneLite's East 1–6 / West 1–6 names. Fossil hardwoods follow East/Middle/West, seaweed North/South, and coral East/West. Prifddinas has allotments/flower/crystal in this catalogue, **not an invented herb patch**. Harmony's herb needs Elite Morytania diary; its allotment does not require that diary.

## RuneLite sources and standards

[Current FarmingWorld](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/FarmingWorld.java) independently agrees with all 21 crop-type counts. It labels the broader normal-tree region Auburnvale; the Wiki table refines the physical patch location to Nemus Retreat. FarmingWorld was read as evidence, not imported as a private runtime dependency or copied into plugin code.

The project follows the [official example plugin](https://github.com/runelite/example-plugin) launcher/build pattern and [Plugin Hub standards](https://github.com/runelite/plugin-hub): Java 11, a standard build descriptor, public RuneLite UI/configuration APIs, a BSD-2-Clause license and no additional production dependencies. The tested client version is 1.13.2. Submission/review is separate from local compilation.

## Route accuracy and limits

The added entry instructions are based on the audited transport tables and include alternatives and key unlocks. They are guidance, not account-access detection or exhaustive quest prerequisite lists. Users must exclude inaccessible patches and meet seed-specific levels. Existing travel choices remain available for the classic-route locations. Added locations use documented entry guidance; every possible teleport is not modeled or selectable.

`TravelCosts.java` uses hand-authored relative travel-effort scores. These are **not measured OSRS timings, exact walking distances or a navigation graph**. The solver is exact for these scores when at most 16 patches are selected. Larger selections use bounded heuristic search, retaining the current order if no improvement is found. No global optimality claim applies to larger runs or to real in-game times.

Same-site grouping only links patches at the same actual site. Separate hardwood locations cannot acquire a false shared walking leg merely because their type is Hardwood. Underwater seaweed, the Great Conch surface and Coral Nurseries remain separately accessed areas in the model. The first-stop cost assumes generic teleport access rather than the player's actual location. Banking, charges, spellbooks, readiness and transport unlocks are not inferred.

## Classic-route corrections retained

Catherby contributes fruit, not normal tree. Tree Gnome Village fruit is outside the maze, reached by following Elkoy out. The guild's normal and fruit patches require west/north-wing access at 65/85 Farming respectively. Tai Bwo Wannai scroll requires Tai Bwo Wannai Trio. Taverley and Brimhaven redirected tablets are different destinations; ordinary Teleport to House goes only to the current house location. Catherby Teleport is Lunar. Lletya crystal destination is distinct from Prifddinas. Digsite/barge entry is distinct from the unlocked Fossil Island pendant/House on the Hill route.

The original chat's assistant route could only be retrieved as opaque content references, so no line-by-line comparison is claimed.

## Version 1.3 settings workflow

Patch selection now uses normal RuneLite `ConfigItem` boolean controls in declared `ConfigSection` crop groups. Travel and display preferences are grouped on that same gear-cog page. Internal CSV/order values are hidden. See RuneLite's [ConfigPanel](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/config/ConfigPanel.java), [ConfigItem](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigItem.java) and [ConfigManager](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java).

The tested client uses declared section fields and annotated config getters, so all sections are declared on the primary configuration interface and the selection-reading helper lives outside the config proxy. No production reflection or private UI access is needed. Changes autosave and trigger background route preparation; activating the run tab flushes pending changes. The tab displays only the prepared route and completion/reset controls.

Named runs remain stored in the existing format. The native Run name field saves/loads names; the standard settings page must be reopened to refresh its checkbox widgets after loading a different named selection. This replaces the former custom preset editor and picker controls.

## Version 1.3.1 Catherby charter option

The Brimhaven travel selector includes Catherby charter ship: walk from Catherby fruit patch to the docks, charter to Brimhaven, then reach its nearby fruit patch. This route is documented in the [OSRS Wiki tree-farming guide](https://oldschool.runescape.wiki/w/Farming_training#Tree_runs) and the [fruit-patch travel table](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Patches). Instructions require coins without fixing a potentially discounted fare. Its connection score remains an unmeasured estimate. Tests cover reversed-input reordering, the shared travel instruction, independent arrival with Brimhaven alone, Camelot fallback and preserving the other Brimhaven options.

## Historical version 1.4 preset selection (superseded by 1.5)

The native settings dropdown provides independently editable Herbs, Trees, Herbs + trees and two custom slots, alongside the existing named/current run. First-use defaults come from the audited catalogue: 10 herbs, 25 tree-related patches (excluding spirit trees), or their 35-patch union. This is a deliberate preset policy, not a readiness/unlock check. Slots persist patch selections separately; travel preferences and progress are shared. Empty selections are stored explicitly rather than replaced by defaults. No gameplay input is introduced.

## Historical version 1.5 local editor (superseded by 1.5.1)

RuneLite's [ConfigManager source](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java) contains optional configuration syncing, including whole-profile upload on enabling sync. [RuneLite.java](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/RuneLite.java) exposes `RUNELITE_DIR`. This plugin uses a separate subfolder there and never writes planner data through ConfigManager. Its separate cog editor provides buttons without private config-panel access. Previous OSRS catalogue and travel audits are unchanged by this storage/UI release.

## Historical version 1.5.1 native cog restoration (superseded by 1.6)

RuneLite's current public config annotations and registered config provider restore the actual plugin cog. The [native control renderer](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/config/ConfigPanel.java) supports typed settings but does not expose arbitrary action buttons. Save/Load/Delete therefore live in the existing tree sidebar; no detached settings window remains. Active cog values use normal ConfigManager optional sync. Named snapshot library and completion keys use local files only; loading a snapshot applies its current values to native settings. No database or developer backend is involved. This supersedes version 1.5's separate editor and all-local active-settings policy.

## Historical version 1.6 settings creation and main-tab run selection

Native `ConfigItem` boolean controls inside Your run now dispatch Save preset and Delete named preset. Each toggle direction is an action, with the stock `warning` confirmation mechanism. The plugin uses no private UI methods. Initial/default writes, background changes and profile switching do not intentionally execute commands; the plugin reset callback cancels queued management commands. RuneLite's synchronous [ConfigManager events](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java) and [ConfigPanel checkbox/confirmation implementation](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/config/ConfigPanel.java) were inspected. Action flags are excluded from saved snapshots. The main sidebar contains only the saved-run selector and checklist; choosing a run loads it immediately and resets progress. Privacy/storage behavior remains as documented for 1.5.1.

## Version 1.7 named native editor slots

The native dropdown has 32 stable enum slot IDs plus Current. Their public toString labels reflect profile-local bindings, with long display names shortened. RuneLite [Text.titleCase](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/util/Text.java) explicitly preserves custom enum toString values, and a test uses the actual client TitleCaseListCellRenderer. This supports named labels without adding dynamic enum constants or accessing private config-panel methods. Selecting slots loads a saved snapshot or prepares an unsaved copy; the cog must be reopened to refresh other widgets. Local slot bindings and snapshot updates/renames/deletes share one file update. Native selector synchronization after Save or main-tab selection is guarded against recursive loading. Earlier named snapshots are adopted into slots without deletion; legacy overflow remains available in the main dropdown. No gameplay automation or new networking is introduced.

## Version 1.8 passive crop observation and readiness

Reference: RuneLite client 1.13.2, [farming sources](https://github.com/runelite/runelite/tree/master/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming). The offline exporter records the decoded produce, state, stage, stage count and tick duration for every raw value from 0 to 65535, compressed into 1,774 contiguous ranges across the 21 crop families. It separately matches all 99 planner patches to the current FarmingWorld payloads, recording main and alias region IDs, varbit IDs and decoder types. These derived tables carry their source BSD notices in THIRD-PARTY-NOTICES.md and the JAR. Runtime code accesses only public game state/varbit APIs, not private farming classes.

The estimator uses remaining stages and tick duration from the last stable observation, and intentionally labels future/past inferred readiness as estimated. It does not infer crop health remotely or learn a precise farming-tick offset. Unknown, empty, diseased and dead members block full-preset notifications. Growth stays separate from checklist marks, and full-run notifications wait for all selected patches, not only a faster crop subset. The Notifier callback follows the player's normal RuneLite notification preferences. Full in-game timing and desktop delivery remain unverified locally.

Legal/design reference: [Jagex third-party guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), [macro/client rules](https://legal.jagex.com/docs/rules/macro-and-client-features-not-permitted). This feature only observes and displays data and updates its own checklist. It never walks, plants, harvests, teleports or sends game inputs. Local compilation is not Plugin Hub/Jagex approval.


## Version 1.9 data and performance audit

The current release independently compared every raw value across the 21 bundled crop decoder families against the actual RuneLite 1.13.2 dependency: 1,376,256 comparisons matched. All 99 distinct recurring crop bindings, all region aliases and 6,176,768 tile/plane boundary checks matched FarmingWorld. Nineteen clearable stump states were checked against the tagged decoder's object/action comments. All seven regular patch tables were fetched again as live OSRS Wiki wikitext and were unchanged.

The revised tracker excludes stump timers, labels anima replacement explicitly, handles same-fruit replanting and preserves failed observation writes across character changes. Countdown updates retain the selector/checklist, while indexed payload lookups, cached character/preset data and edge-only relocation scoring reduce repeated work. All 87 regression tests pass. See the [full audit](DATA-AUDIT.md) and [source/table hashes](reference-provenance.json). This does not replace authenticated live-game timing and UI validation.


## Version 1.10 submission readiness

Removed all production configuration reflection in favor of a generated direct getter/key registry, adopted Plugin.getPluginDirectory()/Filepath with RuneLite-managed legacy directory migration, added the matching internal name and disabled-by-default descriptor, and included license notices as ordinary resources so standard builds retain them. Author is Astral. Custom preset names render as literal text. An account-free CI workflow is prepared locally.

The official local Plugin Hub packager and API checks passed. All 92 offline tests and the exhaustive farming reference check passed. The public repository URL, actual submitted commit, hosted CI and maintainer decision remain outstanding. No game account was used, and no publication occurred. See [the review and evidence](HUB-REVIEW.md).
