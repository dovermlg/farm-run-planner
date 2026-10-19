# Plugin Hub review notes

**Farm Run Planner 1.14.1** · Author: **Astral** · Internal name: `farm-run-planner`

The local Plugin Hub packager and API checks passed with RuneLite 1.13.2, alongside 170 regression tests and the farming-reference audit. This is a development preflight; maintainer review and hosted Plugin Hub CI have not taken place.

## Implementation

| Area | Approach |
| --- | --- |
| Build | Java 11, `build=standard`, no extra production dependencies |
| Metadata | Explicit internal name, legacy data directory and disabled-by-default descriptor |
| License | BSD-2-Clause; license and farming-data notices included as normal JAR resources |
| Configuration | Public RuneLite config controls; direct accessors for all 162 items |
| Storage | Bounded `Filepath` from `Plugin.getPluginDirectory()` |
| UI | Plugin-owned sidebar and settings; no private UI access or other-plugin configuration changes |
| Game interaction | Observes public crop state; generates no gameplay input or menu actions |
| Networking | No plugin network requests, telemetry or backend |

The review used the [submission instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin), [rejected features and API restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), and [Jagex client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1). Maintainers decide whether a submission is accepted.

## Local packager check

The check used unmodified [plugin-hub-tooling at commit 034eaff](https://github.com/runelite/plugin-hub-tooling/tree/034eaffd5bce7bb678d35287b547874f3dba6b3b). A development harness supplied the local source directory and called `Plugin.build("1.13.2", true)`. The packager performed its standard build, dependency verification, class and metadata checks, license check and deprecated-API check. Recorded external API usage matched RuneLite 1.13.2.

The harness did not run the source downloader or uploader. Its `LOCAL-OFFLINE-CHECK` URL and zero commit are parser fixtures, not submission values. The harness and reference utilities are outside the production source set and are excluded from the plugin JAR.

Evidence: [hub-check-results.json](hub-check-results.json), [hub-packager.log](hub-packager.log), and [harness instructions](../tools/hub-review/README.md). The JSON and log record the 1.14.1 local preflight. GitHub publication and the project's own CI are documented separately in [GITHUB.md](GITHUB.md).

## Preset backups

Import and export are explicit, confirmed actions in the native settings cog. Filenames are limited to simple `.json` names inside `Plugin.getPluginDirectory()/preset-backups`; no unrestricted paths, file chooser, external program or network client is used. Exports use a temporary file and replacement move through `Filepath`.

The versioned JSON contains only names, ordered patch selections and supported crop, supply, inventory planning and travel/display settings. Version 3 writes the inventory options; versions 1 and 2 remain readable with defaults for absent options. The reader rejects duplicate fields, unknown patch IDs or travel choices, action/tracking settings and malformed data. Files are limited to 1 MB and 256 entries. Import planning resolves conflicts and checks free slots before a single local library/slot update. Existing names are preserved, identical copies are skipped, and changed imports receive separate names. The active run, progress and observations are unchanged.

The codec uses Gson's streaming reader and writer already supplied by RuneLite. It creates no Gson instance and adds no dependency. Imported data does not execute commands or change another plugin's settings.

## Bank supplies display

Start run arms an in-memory filter for the first inventory; Filter this inventory refreshes it for the current load without clearing progress. It uses the public `bankBuildTab` and `bankSearchFilter` script callbacks, scoped to the visible bank and excluding potion storage, plus `BankSearch.layoutBank()` on the client thread. There are no direct client script calls, chatbox writes, withdraw/equip actions, packet-enabled callbacks or menu actions. It preserves other search/tag rejection results and excludes placeholders. The filter clears on bank close, load transitions, logout, preset/patch/crop/inventory changes, profile changes and shutdown.

The compact requirements list uses RuneLite fonts and item sprites from the public `ItemManager`/`AsyncBufferedImage` APIs. Sprites load asynchronously, with icon updates and caching on Swing’s EDT. It adds no dependency on Quest Helper and no bank-availability tracking. Item tooltips retain exact shortages, payment notes and bottomless bucket limitations.

Inventory and worn equipment are read through public item containers. Counts are transient, noted payments are separated from usable planting supplies, and a shared stack is allocated once. Crop choices never replace observed growth data. The [crop matrix](supply-crops.md) records all 82 planting options and their payments; [supply notes](supplies.md) cover assumptions and limits.

## Inventory planning

The saved bank choice and reserve are ordinary native cog fields. Stackability and noteability come from public `ItemComposition.isStackable()` and `getLinkedNoteId()` definitions read through `ItemManager` on the client thread. These flags contain no character or bank data. Load planning runs in a SwingWorker against immutable settings and item flags; stale results are ignored, and workers are disposed with their panel. Start and filter controls wait for metadata and route preparation.

The planner partitions the prepared patch order into contiguous loads, minimizing load count and then relative bank detour costs. It does not navigate or perform banking. A manual Banked - continue button records a local acknowledgment; those records are fingerprinted by the plan, excluded from backups and cleared on a fresh run. Undoing an earlier patch restores later bank stops. Capacity includes only planned starting supplies plus a user-selected reserve; oversized individual stops are retained and flagged. Travel, harvest quantities, stored tools and container charges remain manual decisions. See [inventory planning](inventory-planning.md) for the bank reference and calculation limits.

## Next teleport outline

A public `WidgetItemOverlay` draws a green `ItemManager.getItemOutline()` sprite on matching inventory or worn-equipment items. It is registered only for inventory, bank-side inventory and worn-equipment interfaces, and validates the interface before drawing. It never outlines bank stock, edits widgets, opens menus, changes destinations, swaps entries, equips items or sends input. It uses explicit charged/unnoted item IDs, not name matching or canonicalization that could admit empty or noted forms.

The sidebar publishes an immutable device enum through a volatile field. It follows the next unfinished patch, the selected travel settings and manual bank acknowledgments; walking links, custom travel and unsupported methods have no item outline. The overlay is hidden outside logged-in state, disabled by its global cog toggle and removed on shutdown. It adds no stored character data, network calls or production dependency. [Supported devices and checks](teleport-highlights.md) describe the scope and offline fixtures.

## Data and testing limits

The offline audit matched 1,376,256 decoder values, all 99 patch bindings and 6,176,768 tile/plane boundary comparisons. Tests cover the native preset controls, migrations, local write failures, route optimization, crop observations, reminder deduplication and sidebar layout. Reproduction commands are in [BUILD-REPORT.md](BUILD-REPORT.md).

The [1.14.1 reliability review](REVIEW.md) covers manual-progress precedence after failed writes, cooperative worker cancellation, incremental slot-count equivalence, compatible fingerprints, consistent bank departure guidance and recoverable item-data errors. The benchmark is a test-only Java entry point and cannot launch the game. No additional runtime dependency or network client was introduced.

No authenticated game session, actual server payload capture, live bank rendering, live crop-timing measurement or desktop notification test was performed. Timers remain estimates, and travel costs are relative scores rather than measured journey times. The plugin keeps gameplay actions with the player.

## Local storage

Saved presets, slot bindings, progress and growth observations use:

```text
<RuneLite data folder>/plugin-data/farm-run-planner/profile-<profile ID>.properties
```

RuneLite migrates the legacy directory when the destination does not exist. It does not merge two existing directories. Active cog settings use RuneLite's normal optional sync; the separate preset library and crop records stay local.

## Before submitting

1. Make the repository public and verify unauthenticated access to the selected commit.
2. Generate the marker with its real repository URL and full 40-character SHA.
3. Follow the official pull-request process, include the testing limits above, and resolve hosted checks and maintainer feedback.

The plugin has not been submitted to or approved by Plugin Hub.
