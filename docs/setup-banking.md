# One saved setup for the run and bank

Create or edit a farming preset in the normal cog, then select it in the run tab. Open **Inventory setup** on that main tab to capture and save your backpack and worn gear. The bank and sidebar share that saved capture. Presets without a captured backpack use calculated supplies for the current inventory.

In the main tab's setup editor, choose the bank display before clicking **Save setup**:

| Choice | Bank display |
| --- | --- |
| Equipment and inventory layout | Saved gear on the left, the captured backpack on the right. Presets without a capture use the generated current inventory. This is the default. |
| Filter only | Show matching supplies in the ordinary compact bank display. |
| Off | Keep sidebar requirements without a farming bank view. |

RuneLite's built-in **Bank Tags** plugin is a dependency and must remain enabled. The separate **Inventory Setups** plugin is not needed. There is no setup export step between plugins, second saved loadout, custom settings window or external database.

If Bank Tags is disabled, the sidebar explains how to enable it and keeps the farming checklist usable. The selected setup is retained for re-enabling it. Saved worn gear is normalized through RuneLite's public item API, including gear whose bank and worn item IDs differ.

## Using the setup

1. Save the farming preset in the cog and select it in the sidebar.
2. Open your bank yourself. The linked setup is displayed when the bank opens. If it is already open, selecting a saved preset focuses the setup there.
3. Withdraw supplies and equip saved gear manually. For protection marked as notes, choose the bank's note withdrawal mode yourself, then restore your preferred mode. The plugin never changes it.
4. Complete patches as usual. Calculated requirements decrease and advance to the next inventory. A captured setup stays fixed; a calculated bank view follows the current load.
5. Use **Banked - continue** to acknowledge each resupply stop. This does not reset patch progress.

**Show setup in bank** focuses the current setup again without changing progress. It also allows explicitly previewing the current draft. **Start run** starts fresh, asking before clearing existing completion marks. Neither action opens a bank, withdraws supplies, equips items or teleports.

**Close setup bank view** pauses the link until you select a saved preset or use Show/Start again. Closing the bank or logging out keeps the selected setup ready for the next bank visit. Profile changes and plugin shutdown release it. Editing a preset pauses the old view; saving links the edited setup again. Unsaved crop or route changes do not silently overwrite a saved preset.

## Display details and limits

The equipment layout uses worn slots on the left of the eight-column bank. The right side follows the saved backpack's exact 28 positions, including gaps, or the generated supply grid for presets without a capture. Saplings and other unstackable supplies repeat; stacks occupy one cell. A reusable tool or teleport device already planned as worn equipment appears in its equipment slot rather than requiring a second inventory copy. Noted payment cells refer to the ordinary bank item; the sidebar's note sprite shows how to withdraw it.

Bank Tags may show ghost items where a layout item is absent. These are display placeholders, not proof that the item is owned. Other matching items, such as usable jewellery charge variants or an empty bottomless bucket to refill, may appear below the generated layout. The sidebar checks carried and worn items only; its required quantities remain the authority for packing. A bank cell shows bank stock, not the quantity to withdraw. An oversized inventory still has the sidebar's explicit overflow warning and full List view.

Opening the setup uses Bank Tags' normal search reset. It leaves saved player tags intact. Once you choose another tag or bank view, background progress updates do not focus the farming setup over it. Selecting a saved farming preset, pressing Show/Start, or opening the bank again explicitly restores the farming view. Dragging the generated layout is disabled; its positions follow the farming plan. Potion storage, shared storage, seed vaults and deposit boxes are not targeted.

## Storage and verification

Preset backup format 7 includes the bank-display choice. Formats 1-6 import without a captured backpack; formats 1-5 use the default layout; older crop, teleport and equipment migrations still apply. No inventory observations or bank contents are added to backups. The plugin keeps its own transient custom tag and generated layout named `_farm_run_planner_setup_`; it cleans them up through the core API on clear, profile change and shutdown. Native RuneLite configuration and optional sync behave normally. No new networking or data service is added.

Offline checks cover layout positions, noted payments, sapling duplicates, worn tools, overflow, preset migration, save/edit/close behavior, progress-driven updates and bank close/reopen behavior. The source and public API were checked against RuneLite 1.13.2. Actual in-game bank rendering remains untested; no account was used for these checks.

References: [Inventory Setups bank layouts](https://github.com/dillydill123/inventory-setups), RuneLite's [Bank Tags API](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/banktags/BankTagsService.java), [custom tag registration](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/banktags/TagManager.java), and [layout implementation](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/banktags/tabs/LayoutManager.java). Checked October 20, 2026.

The subsequent [Inventory Setups comparison and regression review](inventory-setups-verification.md) records the compatibility fixes and passing tests against RuneLite 1.13.2 and 1.13.3.

The [setup editor guide](setup-editor.md) covers Capture, Save setup, unsaved drafts, per-slot checks and returning to calculated supplies. Gear capture and bank-display controls have moved out of the cog into that editor.
