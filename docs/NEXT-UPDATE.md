# Next update

## Approved changes

- Replace the green tree sidebar icon with a small spade and green sprout to represent farming across crop types. Approved on October 20, 2026.
- Keep a transparent background, a clear silhouette, and no text. Check readability at RuneLite's actual sidebar icon size before release.
- Use the same spade-and-sprout design for the Plugin Hub listing picture, replacing the default plug. Approved on October 20, 2026. The prepared repository-root `icon.png` is a transparent 48 x 48 PNG, enlarged exactly 2x from the sidebar asset without smoothing.
- Include the route's teleport supplies in the required-items checklist, carried-item checks, bank filter and inventory planning. Tablets are the approved default. Choices are saved with each preset; [details and limits](teleport-supplies.md).
- Add an automatically generated inventory grid, missing-item highlighting, a Grid/List display choice, and optional worn equipment captured in the cog and saved per preset. Saved gear joins the current bank setup. Approved on October 20, 2026; [usage and limits](packing-view.md).

- Link the player's saved inventory setup to the bank display, using the same current-load supplies and equipment. Equipment appears on the left and inventory on the right through RuneLite's built-in Bank Tags support. Include per-preset Layout / Filter only / Off options. Approved October 20, 2026; [details](setup-banking.md).

The exact [24 x 24 transparent PNG](icon-preview/farm-run-planner-spade-sprout.png) and [size comparison](icon-preview/sidebar-icon-preview.png) are saved here. The plugin loads this same design from `src/main/resources/com/classictreerun/farm-run-planner.png` using RuneLite's image resource loader.

The icons, teleport supplies and visual packing with saved equipment are implemented locally. These changes have not been published or released. On October 20, 2026, the user explicitly requested that these changes stay local until they authorize pushing to GitHub.

The [Inventory Setups verification](inventory-setups-verification.md) found and fixed worn-item ID matching, disabled Bank Tags handling and bank-close event timing. It records 223 passing tests against RuneLite 1.13.2 and 1.13.3 without an authenticated game session.

Plugin Hub artwork follows the [official icon instructions](https://github.com/runelite/plugin-hub#creating-new-plugins): a repository-root `icon.png` no larger than 48 x 72 pixels.

The latest approved interaction moves backpack and worn-gear capture into a separate editor inside the main plugin tab. It has physical Capture and Save setup buttons, inline discard handling and a bank-display choice. Capture preserves positions, note forms and quantities; saving changes only that preset's setup. Crop, route and travel choices stay in the cog. [Editor guide and preview](setup-editor.md).

Growing hardwoods are temporarily skipped when preparing the next run, using local readiness estimates. Saved patch selections are kept. Route optimization and calculated supplies and bank stops use the included patches; a started run holds its patch choices steady until the next reset or fresh run. Full-preset reminders still wait for every selected patch. Captured inventory setups remain unchanged. [Behavior and testing](hardwood-readiness.md).

Add a movable top-left overlay for the current farming step, travel directions, crop readiness, bank stops and completion progress. A global cog setting controls visibility. Completion and bank acknowledgments remain in the sidebar. [Details and preview](run-step-overlay.md).

Show on-screen directions only after **Start run** and hide them automatically on the last completed patch. Resetting or switching presets requires Start run again. The sidebar retains completion information.

Apply the same started-run requirement to the sidebar's green next-patch and next-bank borders. Route previews stay neutral before Start run, and preparing an updated route temporarily suppresses the highlight.

Toggle the same button between Start run and Stop run. Stop hides directions and step/teleport highlights, closes the linked bank view, and preserves patch and bank completion marks. Stop remains available during planning refreshes. Completion returns the button to Start run.

Collapse the packing section by default behind an eye button. The backpack, equipment, calculated requirements and Grid/List selector expand together. Visibility survives progress refreshes without changing checks, saved setups or bank filtering.

Use Farming Guild as the default main resupply bank. On first loading this update, switch existing Grand Exchange presets and the active Grand Exchange draft once; preserve other chosen banks and allow choosing Grand Exchange again afterward. Guild bank instructions and teleport requirements follow the preset's Guild travel choice. Prefer nearby load boundaries when capacity permits, without forcing an unnecessary bank stop or claiming the Guild is always halfway through the route. [Bank planning and migration](inventory-planning.md).
