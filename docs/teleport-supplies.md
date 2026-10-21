# Teleport supplies

In the RuneLite settings cog, expand **Travel**. **Include teleport supplies** starts enabled. **Teleport supplies** defaults to **Teleport tablets**; choose **Spell runes** if you want ordinary loose runes for supported spell destinations instead. Save the preset to keep these choices. **Include fairy ring staff** adds a dramen or lunar staff for fairy-ring routes; disable it if your account is exempt.

The item requirements use the selected route's primary travel methods. Tablets, scrolls and basalt are counted per journey. Walking between patches does not consume another teleport: the Fossil Island hardwood cluster needs one pendant arrival, and walking from Catherby fruit to the charter does not add a Catherby tablet. Removing patches, changing travel choices or marking patches complete updates the list. Completed earlier patches retain their walking context, so checking the first hardwood patch does not request another arrival pendant for the next patch.

Reusable jewellery, capes, crystals and devices are listed once per load. All supported charged variants count in inventory or worn equipment. An uncharged ring or noted item does not satisfy a travel row. Where both Ardougne farm and monastery travel are needed, one cloak of tier 2 or higher satisfies both.

**Green equipment rows confirm presence only.** Hover for the number of planned teleports. Check item charges, stored charges, diary limits and destination unlocks yourself. A ring with one remaining charge can still show green for possession when the run needs two uses. The list does not inspect Chronicle cards, talisman/whistle charges or daily teleport counters. Stored transport resources are not added as loose supplies.

Inventory planning includes travel slots and the return teleport for the next bank stop. The last load does not request a return trip. Walking from or to a local Catherby/Farming Guild bank avoids an unnecessary teleport. While a bank stop is pending, its arrival item remains in the packing list until **Banked - continue**. Travel items are also included by **Start run** and **Show setup in bank**; withdrawing, equipping and teleporting remain manual.

Spell-rune mode totals the base rune costs for Lumbridge, Varrock, Falador, Camelot, Catherby, Ardougne, Harmony Island, Draynor Manor and Civitas illa Fortis. Redirected house tablets stay destination-specific tablets. Rune pouches, combination runes, elemental staves and other rune-saving equipment are not counted; listed costs are ordinary loose runes. The packing notes identify required spellbooks, and mixed spellbooks require the player's own switching method. Spell mode suppresses tablet highlighting for those destinations.

Access to the starting fairy ring, spirit tree or balloon remains the player's choice. Balloon fuel depends on the departure point and remains manual, as do custom-bank travel supplies and POH alternatives. The coin budget still covers player-chosen fares/removal fees rather than an exact calculated fare total. There is no live access detection or route automation.

Backup format 7 saves the travel-supply options and optional worn equipment. Formats 1-6 remain readable without a captured backpack; formats 1-5 use the default bank layout; formats 1–3 import using tablet supplies and the default fairy-ring staff recommendation. Review these settings for your account. Older plugin versions cannot import format 7.

## References and validation

Reference pages consulted October 20, 2026: [teleportation spells](https://oldschool.runescape.wiki/w/Teleportation_spells), [standard spellbook](https://oldschool.runescape.wiki/w/Standard_spellbook), [Catherby Teleport](https://oldschool.runescape.wiki/w/Catherby_Teleport), [Ardougne tablet](https://oldschool.runescape.wiki/w/Ardougne_teleport_(tablet)), [Arceuus spells](https://oldschool.runescape.wiki/w/Arceuus_spellbook), [dramen staff](https://oldschool.runescape.wiki/w/Dramen_staff), [teleport crystal](https://oldschool.runescape.wiki/w/Teleport_crystal). Item constants compile against RuneLite 1.13.2's public `gameval.ItemID`; inventory stackability comes from RuneLite item definitions at runtime.

Offline checks cover tablet/rune counts, walking links, progress, item alternatives, bank travel, inventory partition capacity, named presets, backup migration, sidebar colors and bank-filter inclusion. These checks do not log in or perform game actions.
