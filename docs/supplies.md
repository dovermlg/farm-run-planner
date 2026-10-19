# Crops and packing supplies

Choose crops in the native **Crops and supplies** section, then save the preset. Each patch type uses one selected crop. **Harvest / check only** omits its planting item, compost and protection payment. There is no fallback to a cheaper crop, level check, automatic purchase or withdrawal.

The [crop matrix](supply-crops.md) lists the 82 available planting choices, Farming levels, item IDs and per-patch payments. Sapling choices use grown saplings rather than seeds or watered seedlings. Allotments use three seeds; hops use three or four depending on the crop. Grapes include saltpetre and a gardening trowel.

## Counts and colors

The sidebar compares the quantities for unfinished patches with inventory and worn equipment. Red means short, green means enough, and gray means the containers are not known. A shared stack is allocated once: two buckets of compost cannot cover one application plus a two-bucket protection payment.

Seeds, saplings, tools and compost applications must be unnoted. Gardener payments may be noted, but must match the requested item: five loose tomatoes do not replace one Tomatoes(5) basket. Protection rows are optional recommendations. If a listed patch has no gardener, its payment is omitted. Diary and flower protection are not detected, so payments may be unnecessary for your unlocks.

The list does not inspect your bank to decide whether you can start. It also does not inspect seed boxes, leprechaun tools, tool storage, bottomless bucket contents or charges. Set **Compost** to **No compost** if you use stored charges and check them yourself. Completed patches are removed only when marked manually or by the existing optional replant observer; the list cannot know about supplies used before a stop is marked.

## Tools and equipment

Spade, rake and seed dibber recommendations can be disabled in the cog. The dibber is omitted for saplings, coral and gout tubers. Rake recommendations do not account for Auto Weed, and stored tools are not counted.

Secateurs are included for herbs, allotments, hops, bushes, grapes and celastrus; magic secateurs are recommended for their yield bonus, while ordinary secateurs also match the tool row. The axe row includes ordinary metal axes, dragon, infernal, active crystal and their supported felling variants. It is optional if you pay gardeners to remove trees. It checks possession, not Woodcutting level, crystal charges, energy or variant-specific requirements. Other cosmetic axe variants may need manual checking.

Travel supplies, belladonna gloves, diving equipment and Hespori combat gear are manual packing decisions. Relevant reminders appear above the route. The coin budget is user-selected, initially 10,000; it is not a total of fares and removal fees. Change it for the run you intend to do.

Paid tree protection suppresses compost recommendations except for celastrus, where compost also improves bark yield. Crystal trees include compost for shard yield despite being immune to disease. Other eligible crops can still use compost. Coral, grapes, anima and Hespori do not get ordinary compost recommendations, and poison ivy is also omitted. No attempt is made to predict yield, crop survival or the contents of a saved farming contract.

## Bank filter

**Start run** arms a temporary filter until the ordinary bank closes. It can be clicked before or after opening the bank. It filters only items already present; placeholders do not count as owned supplies. Other bank searches and Bank Tags can narrow the results further. Clear them if they hide wanted supplies. Potion storage, shared storage, the seed vault and deposit boxes are not filtered.

**Clear bank filter** restores the normal display without removing your saved bank tabs or searches. Changes to the selected run, crops, supplies or patches clear the temporary filter. Profile changes, logout, reset and shutdown also clear it. No filter or inventory snapshot is persisted. Inventory counts never leave the client.

The implementation uses RuneLite's public bank layout and filter callbacks. It does not type a search query, open a bank, move an item, equip gear or send a gameplay action. Offline tests exercise the pinned script contract and stack behavior; actual bank rendering has not been tested in an authenticated session.

## Reference sources

Checked on 2026-10-18. Source hashes are recorded in [supply-provenance.json](supply-provenance.json).

- [Tree seeds and protection](https://oldschool.runescape.wiki/w/Tree_patch/Seeds), [fruit trees](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Seeds), [allotments](https://oldschool.runescape.wiki/w/Allotment_patch/Seeds).
- [Herbs](https://oldschool.runescape.wiki/w/Herb_patch/Seeds), [flowers](https://oldschool.runescape.wiki/w/Flower_patch/Seeds), [hops](https://oldschool.runescape.wiki/w/Hops_patch/Seeds), [bushes](https://oldschool.runescape.wiki/w/Bush_patch/Seeds).
- [Special crops, hardwoods, coral and special trees](https://oldschool.runescape.wiki/w/Special_patches/Seeds), [grape seed](https://oldschool.runescape.wiki/w/Grape_seed), [Chet's separate patch payments](https://oldschool.runescape.wiki/w/Chet).
- [Farming payments and noted items](https://oldschool.runescape.wiki/w/Farming), [coral nursery restrictions](https://oldschool.runescape.wiki/w/Coral_nursery_(patch)).
- RuneLite 1.13.2: [ItemID](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-api/src/main/java/net/runelite/api/gameval/ItemID.java), [BankSearch](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/bank/BankSearch.java), [bank layout script](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/scripts/BankMainBuild.rs2asm), [filter callback script](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/scripts/BankSearchFilter.rs2asm).
