# Inventory planning and bank stops

Set these options in the normal RuneLite cog under **Inventory planning**, then click **Save preset**. The run tab uses the saved choices.

| Setting | Effect |
| --- | --- |
| Plan inventory loads | Split the prepared patch order into loads and add bank stops; enabled by default. Disable for a full remaining-run supply list. |
| Reserved inventory slots | Space kept for other gear and harvested produce; 8 by default, adjustable from 0 to 27. Configured travel supplies are counted separately. |
| Pack protection as notes | Plan noteable protection payments as single stacks and require notes in their checklist rows; enabled by default. |
| Bank between inventories | Main resupply bank for this preset; Farming Guild by default. Other banks remain selectable. |
| Custom bank instructions | Directions shown when Custom bank is selected; up to 500 characters. |

The compact list checks carried items only. It never reads bank ownership, withdraws supplies, changes withdrawal mode or opens a bank. Start run packs the first inventory and clears old progress after confirmation. Selecting a saved preset links its current inventory to the bank automatically. **Show setup in bank** reopens that display without resetting progress. See [saved setup banking](setup-banking.md).

If RuneLite item definitions cannot be read, the sidebar shows an error and keeps Start and Filter disabled. Toggle **Plan inventory loads** off and on to request the data again. With planning off, the full-run supply list remains available. The bank layout and grid still wait for item definitions; choose Filter only or Off for a bank display without layout metadata. This does not resolve RuneLite's own network or startup problems.

Finish the current load's patches to advance its packing list. The next route step is the chosen bank. Deposit produce and unneeded supplies, keep tools and travel items, and withdraw the next list yourself. **Banked - continue** acknowledges that stop. Patch progress and bank acknowledgments survive a restart; starting, resetting or loading a saved run clears both. Reopening an earlier patch clears bank acknowledgments after that inventory. Settings changes rebuild the plan and invalidate its old bank acknowledgments.

![Bank stop and second inventory — offline layout fixture with a deliberately small capacity](inventory-run-preview.png)

## How loads are calculated

RuneLite's public item definitions supply inventory stackability and whether a payment can be noted. Definitions are read on the client thread. The sidebar waits for them before enabling Start or claiming a load fits. It then calculates the plan in the background from an immutable settings snapshot.

Seeds and coins use one slot per item stack. Saplings and ordinary compost buckets use one slot each. A filled bottomless compost bucket uses one slot; its charges and type remain a manual check. Noteable protection payments use one slot per payment stack when the notes option is enabled. Loose payment and planting quantities sharing an item ID are added together; noted payments remain separate. The selected tools and coin allowance are included in each load that needs them.

The planner keeps the prepared patch order and chooses contiguous loads. It minimizes the number of loads first, then the estimated bank detour cost, then prefers a fuller first load on a tie. Crop supplies are counted incrementally, sharing tools, the bottomless bucket and coin allowance across a load. Each candidate also includes travel supplies for its actual walking links, departure from the bank and return to the next bank stop. Teleport equipment is counted as a backpack slot even when worn. The bank cost uses relative travel scores, with local walking allowances at Catherby and the Farming Guild. It is not live pathfinding or a measured travel-time estimate. Route optimization still happens before load planning; this is not a joint search over every patch order and bank placement.

The Farming Guild is the default resupply hub. When feasible with the fewest inventories, the detour scoring favors a bank boundary beside Guild patches: bank there, then walk to the next Guild patch. A run that fits in one inventory gets no extra bank stop. Inventory capacity and the prepared route determine the split; the Guild is not forced to a literal halfway position. With **Optimize route** disabled, your saved patch order is also kept.

On the first load of this update, existing local presets and the active draft using Grand Exchange switch to Farming Guild. Other bank choices are kept. This migration runs once per profile, so selecting and saving Grand Exchange afterward is respected. Names, patches, crop choices, captured setups and patch completion marks are preserved. The new bank changes the plan signature, so an old bank acknowledgment cannot incorrectly skip the new resupply stop.

Load boundaries stay fixed as patches are completed. Within a load, completed patches disappear from its supply quantities. Changing patches, crops, supply options, travel or inventory settings recomputes the plan. If one patch's supplies exceed capacity, that patch remains as a separate load and the sidebar flags the shortage of space.

Capacity describes the planned starting supplies. It does not measure unrelated carried items, forecast harvest yield, inspect containers or discount a tool's slot when worn. Set the reserve for other gear, unlisted travel equipment and expected harvest, and note or bank produce manually. Optional protection still takes space when included; turn off its supply recommendation if you do not intend to carry it. A green item row confirms possession, not that the whole run can be completed without further banking.

## Bank choices and travel

These are directions only. With **Travel → Include teleport supplies** enabled, the listed bank's primary teleport is included when needed. Custom bank supplies remain manual. The plugin does not verify travel access, charges or unlocks. See [teleport supplies](teleport-supplies.md).

| Bank | Suggested arrival |
| --- | --- |
| Grand Exchange | Ring of wealth; Varrock's Grand Exchange destination needs the medium Varrock diary. |
| Castle Wars | Ring of dueling to the lobby bank chest on a members world. |
| Ferox Enclave | Ring of dueling, then the bank inside the enclave. |
| Farming Guild (default) | Uses **Travel → Farming Guild**: Skills necklace, Farming cape or your grown Guild spirit tree. Use the central bank chest; guild entrance requires 45 Farming, and individual wings have higher requirements. |
| Catherby | Catherby spell, tablet or POH portal; alternatively Camelot and walk south-east. |
| Seers' Village | Camelot and walk west; direct bank destination requires the hard Kandarin diary. |
| Edgeville | Amulet of glory. |
| Draynor Village | Amulet of glory. |
| Falador west | Falador spell or tablet, then walk to the west bank. |
| Custom bank | The player's own directions; default text asks the player to travel and resupply manually. |

Checked against RuneLite 1.13.2 and OSRS Wiki on 2026-10-18. Public item methods are documented in [ItemComposition](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/ItemComposition.html). Bank access and destinations are described in [Grand Exchange](https://oldschool.runescape.wiki/w/Grand_Exchange), [ring of dueling](https://oldschool.runescape.wiki/w/Ring_of_dueling), [Farming Guild](https://oldschool.runescape.wiki/w/Farming_Guild), [Catherby teleport](https://oldschool.runescape.wiki/w/Catherby_Teleport), [Camelot Teleport](https://oldschool.runescape.wiki/w/Camelot_Teleport), [amulet of glory](https://oldschool.runescape.wiki/w/Amulet_of_glory) and [Falador Teleport](https://oldschool.runescape.wiki/w/Falador_Teleport).

Backup format 7 includes these options, teleport-supply choices and optional worn equipment. Formats 1-6 remain importable without a captured backpack; formats 1-5 use the default bank layout. Formats 1 and 2 retain the historical Grand Exchange bank default and the other inventory defaults above. Backups that contain an explicit bank keep that bank on import. Bank acknowledgments and item metadata are excluded from preset backups. There is no new network connection or player-data service.

A captured backpack is edited and saved under **Inventory setup** on the main tab. It stays fixed at bank stops. The calculated requirements and load plan still advance by inventory; they do not guarantee that a manually captured setup contains enough supplies. Saving only the custom setup preserves existing load acknowledgments. See [setup editor](setup-editor.md).
