# Inventory Setups compatibility review

Checked October 20, 2026. Changes remain local and unpublished.

The comparison used Inventory Setups commit `16f2c0a319672c2e19738276a17de63aea7216fa`, the commit named in the [current Plugin Hub marker](https://github.com/runelite/plugin-hub/blob/master/plugins/inventory-setups). Reference files were read for API contracts and behavior. No Inventory Setups source, assets, custom variation tables or tests were copied into this project.

## Comparison

| Area | Reference behavior | Farm Run Planner result |
| --- | --- | --- |
| Core integration | Declares Bank Tags as a plugin dependency and opens layouts through its public service. | Uses the same public service, custom-tag registry and layout manager on the client thread. |
| Layout positions | Equipment on the left of the eight-column bank; a four-column inventory on the right. | Equipment positions and all 28 inventory positions agree. Duplicate saplings and noted-payment bank stock are covered by tests. |
| Item identities | Normalizes item IDs before putting items in a bank layout. | Fixed worn weight-reducing gear, such as graceful and boots of lightness, using RuneLite's public canonicalizer. Both membership and layout IDs are normalized. |
| Equipment checks | Separates inventory possession from the intended worn slot. | New captures save normalized IDs. Existing saved worn IDs still match live worn observations; carrying the item does not satisfy a worn slot. |
| Bank Tags availability | A dependency annotation alone does not mean Bank Tags is enabled. | Fixed missing availability handling. Disabled Bank Tags shows a clear message, leaves the farming checklist usable, and retains the selected setup for re-enabling. |
| Bank lifecycle | Defers bank-close work on the client thread and closes only its own view. | Deferred cleanup now checks that the bank has not already reopened. Other plugins' tags are preserved. |
| Filtering mode | Supports a normal compact filtered bank as an alternative to layouts. | Uses the public no-layout option for the saved Filter only choice. |
| Player actions | Setup presentation does not itself withdraw or equip items. | Banking, equipment, noting and travel remain manual. |

The farming layout is regenerated from the saved preset and current load. Inventory Setups supports editable layouts and broader features such as custom fuzzy mappings, special containers and spellbook capture. Those features are not implied by this comparison. Our generated layout disables dragging, uses curated usable teleport variants, and leaves rune pouches, stored charges and spellbook switching as manual checks.

## Validation

- All **223 tests** passed against RuneLite **1.13.2**, then against the current Plugin Hub target, **1.13.3**. Both Java 11 builds passed.
- New contract tests execute RuneLite's actual `ItemManager.canonicalize`, custom `TagManager` registration and `Layout` classes with synthetic item definitions and containers. Background price jobs are discarded; no game connection or account is used.
- The real item canonicalizer checks worn graceful and boots of lightness, note and placeholder forms, and preservation of charged jewellery IDs. Placeholder bank display does not count as owned inventory.
- Controller and sidebar regressions cover disabled/re-enabled Bank Tags, pending selections, another selected tag, and delayed bank-close callbacks after reopening.
- The unmodified local Plugin Hub packager and external API check passed against its prepared RuneLite **1.13.2** API baseline. The existing unused `support` metadata warning is informational.
- The relevant item, plugin-manager and Bank Tags source files are unchanged between RuneLite 1.13.2 and 1.13.3. The complete plugin was also compiled and tested using the 1.13.3 dependencies.

These checks verify code contracts and offline behavior. They do not establish pixel-perfect bank rendering, compatibility with every other plugin, account safety guarantees or new maintainer approval. No authenticated game session was run, and nothing was committed or pushed.

## Source references

- Inventory Setups: [plugin lifecycle and bank service](https://github.com/dillydill123/inventory-setups/blob/16f2c0a319672c2e19738276a17de63aea7216fa/src/main/java/inventorysetups/InventorySetupsPlugin.java), [layout generation](https://github.com/dillydill123/inventory-setups/blob/16f2c0a319672c2e19738276a17de63aea7216fa/src/main/java/inventorysetups/InventorySetupLayoutUtilities.java).
- RuneLite 1.13.3: [item canonicalization](https://github.com/runelite/runelite/blob/runelite-parent-1.13.3/runelite-client/src/main/java/net/runelite/client/game/ItemManager.java), [Bank Tags service](https://github.com/runelite/runelite/blob/runelite-parent-1.13.3/runelite-client/src/main/java/net/runelite/client/plugins/banktags/BankTagsService.java), [custom tags](https://github.com/runelite/runelite/blob/runelite-parent-1.13.3/runelite-client/src/main/java/net/runelite/client/plugins/banktags/TagManager.java), [layout manager](https://github.com/runelite/runelite/blob/runelite-parent-1.13.3/runelite-client/src/main/java/net/runelite/client/plugins/banktags/tabs/LayoutManager.java), [plugin lifecycle](https://github.com/runelite/runelite/blob/runelite-parent-1.13.3/runelite-client/src/main/java/net/runelite/client/plugins/PluginManager.java).

See [setup banking](setup-banking.md) for the player controls and packing limits.
