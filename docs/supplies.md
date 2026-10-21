# Crops and packing supplies

Choose crops in the native **Crops and supplies** section, then save the preset. Each patch type uses one selected crop. **Harvest / check only** omits its planting item, compost and protection payment. There is no fallback to a cheaper crop, level check, automatic purchase or withdrawal.

The [crop matrix](supply-crops.md) lists the 82 available planting choices, Farming levels, item IDs and per-patch payments. Sapling choices use grown saplings rather than seeds or watered seedlings. Allotments use three seeds; hops use three or four depending on the crop. Grapes include saltpetre and a gardening trowel.

## Counts and colors

The sidebar uses a compact **Item requirements:** panel. Each single-line entry shows the quantity first (for example, `6 x Magic sapling`) with the item sprite on the right. Hover a row or icon for its full name, carried count, exact shortage and optional-payment notes. Hover **Packing notes** for the remaining reminders. Long names are shortened to fit; basket and sack contents such as Tomatoes(5) stay visible.

The sidebar compares the quantities for unfinished patches in the current inventory with inventory and worn equipment. Disable inventory planning for the whole remaining-run list. Red means short, green means enough, and gray means the containers are not known. A shared stack is allocated once: two buckets of compost cannot cover one application plus a two-bucket protection payment.

Seeds, saplings, tools and compost applications must be unnoted. With inventory planning and **Pack protection as notes** enabled, noteable protection rows require notes to match the calculated slot usage. Disable that option to pack loose payments; with inventory planning off, payments accept either form. Payments must match the requested item: five loose tomatoes do not replace one Tomatoes(5) basket. Protection rows are optional recommendations. If a listed patch has no gardener, its payment is omitted. Diary and flower protection are not detected, so payments may be unnecessary for your unlocks.

Item icons identify the item and do not report bank ownership. The list does not inspect your bank to decide whether you can start. It also does not inspect seed boxes, leprechaun tools, tool storage, bottomless bucket contents or charges. Choose **Bottomless compost bucket** under **Compost** to pack one filled bucket instead of separate application buckets. The filter shows both filled and empty buckets so you can find one to refill, but an empty bucket stays short in the checklist. A green bucket row confirms possession only: check its compost type and charges yourself. A reminder gives the number of remaining patches needing compost. Ordinary compost buckets used as gardener payments are still listed separately. Choose **No compost** if you use other stored supplies. Completed patches are removed only when marked manually or by the existing optional replant observer; the list cannot know about supplies used before a stop is marked.

## Tools and equipment

Spade, rake and seed dibber recommendations can be disabled in the cog. The dibber is omitted for saplings, coral and gout tubers. Rake recommendations do not account for Auto Weed, and stored tools are not counted.

Secateurs are included for herbs, allotments, hops, bushes, grapes and celastrus; magic secateurs are recommended for their yield bonus, while ordinary secateurs also match the tool row. The axe row includes ordinary metal axes, dragon, infernal, active crystal and their supported felling variants. It is optional if you pay gardeners to remove trees. It checks possession, not Woodcutting level, crystal charges, energy or variant-specific requirements. Other cosmetic axe variants may need manual checking.

Route teleport supplies are included by default; see [teleport supplies](teleport-supplies.md) for settings and charge limits. Belladonna gloves, diving equipment and Hespori combat gear are manual packing decisions. Relevant reminders appear above the route. The coin budget is user-selected, initially 10,000; it is not a total of fares and removal fees. Change it for the run you intend to do.

Paid tree protection suppresses compost recommendations except for celastrus, where compost also improves bark yield. Crystal trees include compost for shard yield despite being immune to disease. Other eligible crops can still use compost. Coral, grapes, anima and Hespori do not get ordinary compost recommendations, and poison ivy is also omitted. No attempt is made to predict yield, crop survival or the contents of a saved farming contract.

## Saved setup bank display

The saved farming setup provides both the sidebar checklist and bank display. Selecting a preset links its current load automatically. The default layout puts saved equipment on the left and the captured backpack on the right. Presets without a capture keep the generated current-inventory layout. Choose **Filter only** or **Off** in the main tab's **Inventory setup** editor, then click **Save setup**.

**Start run** clears old progress after confirmation and prepares the first inventory. **Show setup in bank** opens the current setup without changing progress. **Close setup bank view** pauses it until a preset is selected or a show/start action is used. Closing the bank retains the setup for the next visit. Crop completion updates calculated requirements and advances loads. A captured setup stays fixed until you explicitly capture and save another one. If you choose another bank tag, background progress updates leave that view alone.

The implementation uses RuneLite's public Bank Tags service and layout API. It maintains only its own generated tag and layout; no separate Inventory Setups installation is needed. Opening a setup clears the bank search through the standard core API. Missing layout items can appear as placeholders; the sidebar still checks carried items only. Potion storage, shared storage, the seed vault and deposit boxes are not targeted.

Withdrawing, equipping, noting payments and travelling remain manual. The plugin does not open a bank or send a gameplay action. Offline tests cover item selection, layout positions, current-load updates and bank-view lifecycle. Actual bank rendering has not been tested in an authenticated session. See [saved setup banking](setup-banking.md) and [inventory planning](inventory-planning.md).

## Reference sources

Checked on 2026-10-18. Source hashes are recorded in [supply-provenance.json](supply-provenance.json).

- [Tree seeds and protection](https://oldschool.runescape.wiki/w/Tree_patch/Seeds), [fruit trees](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Seeds), [allotments](https://oldschool.runescape.wiki/w/Allotment_patch/Seeds).
- [Herbs](https://oldschool.runescape.wiki/w/Herb_patch/Seeds), [flowers](https://oldschool.runescape.wiki/w/Flower_patch/Seeds), [hops](https://oldschool.runescape.wiki/w/Hops_patch/Seeds), [bushes](https://oldschool.runescape.wiki/w/Bush_patch/Seeds).
- [Special crops, hardwoods, coral and special trees](https://oldschool.runescape.wiki/w/Special_patches/Seeds), [grape seed](https://oldschool.runescape.wiki/w/Grape_seed), [Chet's separate patch payments](https://oldschool.runescape.wiki/w/Chet).
- [Farming payments and noted items](https://oldschool.runescape.wiki/w/Farming), [coral nursery restrictions](https://oldschool.runescape.wiki/w/Coral_nursery_(patch)).
- RuneLite 1.13.2: [ItemID](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java), [Bank Tags service](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/banktags/BankTagsService.java), [layout manager](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/banktags/tabs/LayoutManager.java).
