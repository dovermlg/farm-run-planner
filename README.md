# Farm Run Planner

[Source](https://github.com/dovermlg/farm-run-planner) · [Report an issue](https://github.com/dovermlg/farm-run-planner/issues) · [Offline build checks](https://github.com/dovermlg/farm-run-planner/actions/workflows/check.yml)

Source is published in `dovermlg/farm-run-planner` and linked to GitHub Desktop. The repository is currently private; its links require repository access. GitHub's offline build, tests and farming-reference checks passed. See [GitHub setup and publishing status](docs/GITHUB.md).

A local RuneLite development plugin for choosing patches, saving named farming runs and optimizing estimated travel. **Version 1.10 removes submission blockers, adopts RuneLite’s bounded file storage and passes the official local packager checks**, covering all 99 recurring crop patches across 21 crop types. It includes newer Varlamore and Sailing locations, while keeping the original classic tree run as a starter selection.

Author: **Astral**. See [the Plugin Hub readiness review](docs/HUB-REVIEW.md) for fixes, official local check results and remaining submission steps. The source is on GitHub; the plugin has not been submitted to or approved by Plugin Hub.

## Start locally

Install a **JDK 11**, set `JAVA_HOME` to it, and open a terminal in this project folder:

```powershell
.\gradlew.bat test build
.\gradlew.bat run
```

On macOS/Linux, use `chmod +x gradlew`, then `./gradlew test build` and `./gradlew run`.

For account-free testing, run `test build` and the reference check below. Those tasks never launch RuneLite or log into RuneScape. The separate `run` task launches a real development client; it is not a private game test server.

The first build downloads Gradle and RuneLite dependencies. The supplied official-template Gradle wrapper downloads Gradle 8.10 from `downloads.gradle.org` and verifies its published SHA-256 checksum. `run` starts a separate development client with the plugin loaded through RuneLite's `ExternalPluginManager.loadBuiltin` API. Enable **Farm Run Planner**, then open its green tree sidebar button.

For IntelliJ, import `build.gradle`, select JDK 11 as the Gradle JVM, and run the Gradle `run` task or `ClassicTreeRunLauncher.main()` with `--developer-mode --debug`. For a Jagex account, follow RuneLite's [development-client authentication guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

`build/libs/farm-run-planner-1.10.1.jar` is the compiled plugin, not a standalone application. Use the development launcher rather than copying it into the installed client's directory. The existing Java package name and configuration group are retained for compatibility with earlier Classic Tree Run versions.

## Make and edit presets in the RuneLite cog

1. **Enable Farm Run Planner.** Open RuneLite's Configuration (wrench) tab, find the plugin, and click its normal gear cog.
2. Under **Your run → Preset to edit**, choose an **Empty** slot. The dropdown has 32 stable slots; saved slots show their number and your preset name.
3. **Leave and reopen the cog page** to refresh its native fields. An empty slot starts as a copy of your current patches/travel, with a temporary name such as New preset 4.
4. Enter your own **Preset name** (1-60 characters), choose patches, and set travel/display preferences. All 99 patches remain standard checkboxes across 21 crop sections.
5. Click **Save preset** and confirm. The selected slot now holds that named snapshot.
6. Open the green tree main tab. Select the saved name in **Choose your run** to start its route with a fresh checklist.

### Editing, copying and renaming

Select an occupied slot in the cog, leave and reopen that page, edit, then click Save preset. Selecting a saved slot restores its snapshot but **does not save any edits automatically**. Existing checklist marks are preserved while editing; selecting a run in the main tab starts a fresh checklist. Choosing another slot replaces the current working draft, so save first if you want to keep it.

To make a separate copy, select an empty slot: it inherits the current working settings. Give it a unique name and Save. To rename an existing preset, select its occupied slot, change Preset name and Save; the old name is replaced in that slot, rather than creating an extra copy. Names already used by a different slot are rejected, preserving both saved runs.

**Delete named preset** deletes the selected occupied slot after confirmation, even if you have typed an unsaved different name. It keeps the working run and frees that slot. Choosing New / current run keeps the current draft; saving it resolves an existing matching name or allocates a free slot. The main tab automatically selects the matching editor slot so that returning to the cog makes that run available for editing.

**RuneLite renders Save and Delete as checkbox actions.** Clicking either control in either direction performs that action. The tick is not an auto-save setting. Cancelled confirmations make no changes. These actions require the plugin to be enabled. The main tab has no Save/Delete buttons and no secondary settings window opens.

### Native UI limits and existing data

After changing Preset to edit, leave and reopen the cog before touching its fields or patch checkboxes. RuneLite's standard controls do not refresh themselves when a plugin loads new values. Slot labels can be shortened to fit; Preset name and the main dropdown keep your full name. Existing named presets are assigned stable free slots automatically without changing their snapshots. The three starter presets occupy slots as well.

The cog has **32 editable slots** plus New / current run. If an older local library contains more than 32 names, none are deleted: all remain selectable in the main tab. Free an editor slot and restart the plugin to assign another older unassigned name. New saves need a free slot or an existing assigned slot.

This uses RuneLite's supported enum setting controls and public custom-label behavior: [ConfigPanel renderer](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/config/ConfigPanel.java), [Text.titleCase](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/util/Text.java). No private UI access, enum mutation or detached editor is used.

Working cog settings persist normally and recalculate the route automatically. Saved snapshots capture patches, travel/display choices, optimization preference and route order. Completion marks, slot selection and Save/Delete action flags are excluded. Only explicit Save changes a snapshot. Resetting configuration does not execute management commands.

| Starter preset | Selection |
| --- | --- |
| Herbs | All 10 herb patches |
| Trees | All 25 normal, fruit, hardwood, calquat, crystal, celastrus and redwood patches |
| Herbs + trees | The 35 patches from Herbs and Trees combined |

Starter presets are added once without replacing existing names. Deleting one does not recreate it on restart. The fresh working run starts with the classic 16 patches. Spirit-tree patches are opt-in. Remove locations you cannot access. Starter selections do not check unlocks or crop readiness; the optional tracker reports observed crop readiness separately. With Optimize route disabled, stored stop order is retained. With it enabled, loading a preset recalculates the order using restored travel choices.

Under **Travel → Brimhaven travel**, choose **Catherby charter ship** to walk from the Catherby fruit patch west along the shore to the docks, charter to Brimhaven, and walk to its fruit patch. Bring fare coins. The optimizer accounts for this connection when both fruit patches are selected. If Catherby fruit is excluded, instructions take you independently to the docks using your Catherby/Camelot preference, without adding the patch.

## Crop countdowns and next-run reminders

Under **Your run** in the normal cog:

* **Track crop readiness** (on by default): observes nearby patches and shows each crop's name, estimated time remaining and local ready-at time on its checklist card. The header shows the estimated time until **every selected patch** is ready.
* **Notify when run is ready** (on by default): sends one RuneLite notification per saved preset and observed crop cycle, after all of that preset's selected patches are estimated ready. A mixed herb/tree preset waits for the slower trees; it does not send the full-run alert when only the herbs finish. RuneLite's own notification preferences control display and sound.
* **Auto-mark replanted patches** (off by default): optionally checks a selected stop after a stable observed transition from an empty, harvested or unhealthy patch to a newly planted crop during that visit. It does not mark a stop merely because you arrive. Manual correction stays available. Health-check-only completion is not implemented.

These switches are global within the RuneLite configuration profile and are not overwritten when you load another preset. Reminders cover all locally saved presets, not only the currently selected run. Save the patch selection first if you want it monitored as a named group.

Visit your patches with the plugin enabled to obtain observations. Until then, a patch says **Growth unknown**. Unknown, empty, diseased or dead patches prevent a full-run alert. No specific seed selection is needed in settings: the plugin decodes the crop state sent by the game. A row shows the observed crop, such as ranarr or magic, rather than assuming every tree takes the same time. Regrowing bushes/cacti/fruit trees use an estimate for full yield. Clearable tree stumps have no growth timer and block full-run alerts while last observed as a stump. Anima explicitly shows active/withering status and an estimated replacement point, rather than a harvest time.

**Countdowns are estimates, not guarantees.** They use the last observed growth stage and a conservative remaining-time bound, without learning your precise farming-tick offset. They may be late by a growth tick. Unseen disease, protection changes, growth skips/modifiers or activity in another client can invalidate an estimate. Visiting again updates the observation; confirmed unhealthy crops block readiness. A past estimate is labelled estimated ready, with a reminder to revisit and confirm health. Live play and exact timing have not been validated in this development build.

**RuneLite must remain running and the plugin enabled to deliver notifications.** Records and deduplication markers survive restarts. Log into the character after restarting so the plugin can select the correct local observations; an elapsed armed timer can then notify once. After that character logs out, the running client can still remind them at the login screen. Loading/hopping sessions temporarily suppress alerts. The plugin provides no notification service while RuneLite is closed.

Already mature crops seen on the first visit do not generate a flood of alerts. A group must have at least one observed growing/regrowing crop in its current cycle before its full-run notification is armed. Resetting checklist marks or choosing a preset does not restart its crop timers. Replanting/regrowth starts a new notification cycle. Manual completion marks do not assert that a new crop was planted.

The reference maps all 99 catalogue patches to RuneLite 1.13.2 farming payloads. An exhaustive offline check matched 1,376,256 raw decoder values across 21 families, all 99 unique patch bindings and 6,176,768 tile/plane boundary checks. See [the data audit](docs/DATA-AUDIT.md) for exact evidence, fixes and limits. Region-boundary and two-read settling guards prevent common stale-varbit readings. Growth observations are partitioned locally by a one-way character/world-mode key, preventing another character's crops or seasonal worlds from being combined with the current character. The normal, seasonal (fivefold tick-rate estimate) and Deadman contexts are separate. Values not recognized by the bundled decoder have no reliable timer. New game content requires a reviewed reference update; matching the audited client does not guarantee compatibility with every future update.

The attributed reference tables and their BSD notices are in [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md), also embedded in the JAR. Runtime code uses public RuneLite APIs and bundled data, without reflection into private farming classes. To check the bundled reference against the development dependency (and optionally export replacement candidates):

```powershell
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
# Optional: generate candidate replacement tables for review
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' exportFarmingReference
```

These optional offline utilities use reflection against the development dependency. The verifier fails on a changed decoder, missing/duplicate patch binding, changed region alias or mismatched boundary. The exporter writes `build/reference/`; it is not part of the runtime plugin and does not contact or log into RuneScape. Review the exported tables and location matches before replacing the bundled data after a game/client update.

## Privacy and storage

**No database, hosting, signup, analytics, telemetry or developer backend is needed.** The plugin has no network client or direct network requests. No farming data is sent to the plugin author.

Saved preset snapshots, slot/name mappings, checklist completion, growth observations and readiness notification markers use local files:

```text
<RuneLite data folder>/plugin-data/farm-run-planner/profile-<RuneLite profile ID>.properties
```

Normally on Windows this is `%USERPROFILE%\.runelite\plugin-data\farm-run-planner\`. Version 1.10 obtains this bounded directory through RuneLite’s `Plugin.getPluginDirectory()` and uses `Filepath` for disk operations. RuneLite migrates the old `farm-run-planner` folder when the new destination does not already exist; it does not merge existing old/new folders. Existing property formats are unchanged. There is one file per RuneLite configuration profile, not per OSRS character. Copy these files while the client is closed to back up your presets. Avoid simultaneous clients editing the same profile; the last local writer wins. These are ordinary settings files, not encrypted vaults. An OS backup service can independently copy local files.

**Active cog settings use RuneLite's normal ConfigManager**, including the current preset name, patch choices and travel/display settings. Those active values may sync if the player enables synchronization for their RuneLite profile. Saved preset library entries and completion marks are not written to ConfigManager. Loading a preset copies its values into the active cog settings, which then follow that normal sync behavior. For every active setting to remain local as well, keep RuneLite profile synchronization off. The plugin does not change the player's sync preferences. See [RuneLite ConfigManager](https://github.com/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/config/ConfigManager.java).

RuneLite still performs its normal networking for the game, updates and other features. The growth observer reads the public account hash locally and immediately derives a one-way character/world-mode key to separate observations. The raw hash is not stored or sent anywhere by this plugin. No username, password, bank or inventory data is requested or stored. Active cog settings still follow the normal optional RuneLite synchronization described above; observed crop data and notification markers stay in the separate local file.

Existing settings and named presets are migrated automatically. Version 1.5's local working run is copied back into native settings once for each profile; later native edits take precedence. Earlier named selections and fixed slots are imported into local snapshots. Imported old patch-only presets inherit current travel/display preferences. Existing local library names are kept. Previous RuneLite settings/synced copies are not retroactively erased. Local file writes use temporary files and atomic replacement where supported; write failures are reported and the stored file preserved.

## Catalogue coverage

| Type | Patches | Type | Patches |
| --- | ---: | --- | ---: |
| Normal tree | 7 | Fruit tree | 7 |
| Hardwood | 5 | Calquat | 3 |
| Allotment | 17 | Flower | 9 |
| Herb | 10 | Hops | 5 |
| Bush | 5 | Cactus | 2 |
| Seaweed | 2 | Grape | 12 |
| Spirit tree | 5 | Belladonna | 2 |
| Coral nursery | 2 | Mushroom | 1 |
| Crystal tree | 1 | Celastrus | 1 |
| Redwood | 1 | Hespori | 1 |
| Anima | 1 | **Total** | **99** |

The catalogue covers regular plantable crop patches. Quest-only plots, Tithe Farm/raid/activity plots, rake-only scenery, compost bins and player-owned-house construction spaces are outside its scope. Hespori is selectable but still requires preparing for and manually completing its fight. Seaweed diving and Entrana equipment restrictions appear in access notes.

See [the individual patch catalogue](docs/PATCH-CATALOGUE.md) and [source audit](docs/VERIFICATION.md). Your account's unlocks are not automatically detected. Uncheck patches you cannot access. Minimum crop levels do not replace seed-specific requirements. Access notes describe key gates and selected travel requirements, not a complete quest dependency tree.

## What optimization does

The optimizer visits exactly your selected patches and looks for lower estimated travel effort. It groups patches at the same farm or location and supplies walking instructions between them. Large-run relocation checks compare only the affected edges, instead of repeatedly copying and rescoring the entire route. Countdown polling updates existing labels without rebuilding the preset selector or checklist. It respects the existing configurable travel choices for the classic route, with documented entry routes/alternatives for added locations.

* **Up to 16 selected patches:** exact subset dynamic programming finds the least-cost open route under the estimated cost model.
* **17â€“99 selected patches:** multi-start nearest-neighbour search followed by directed relocate improvements keeps calculation bounded. It retains the current order as a candidate and never returns a route with a higher score under that model. This is a heuristic, not a proof of the globally best large route.

Costs are **hand-authored relative travel-effort scores**, not measured seconds or tile distances. The plugin does not read your current position, check unlocks/charges, enforce spellbook compatibility, choose the best teleport from every item in your bank, or optimize banking. First-stop entry is generic. Island, underwater and other separately accessed areas remain separate travel groups. In particular, hardwoods on different islands do not share a walking leg merely because they have the same crop type.

The result is an estimated route, not a guaranteed fastest in-game run. Choose usable travel options and adjust the route for your account. Optimization runs in the background; changes to relevant settings or configuration profile invalidate a pending result.

## Progress and migration

Check Completed after finishing a patch. The progress bar turns green when every selected patch is complete; resetting or unchecking a patch restores the normal progress color. Uncheck it to correct a mark. NEXT and the green border identify the first unfinished selected stop. Hide completed is optional. Reset run asks for confirmation and clears all completion marks, including excluded patches, while preserving saved runs and route settings.

Working settings persist in RuneLite configuration; saved presets and progress persist in the local profile file. Use separate RuneLite profiles for separate characters. There is no daily/logout checklist reset. Optional growth estimates and confirmed-replant completion are described above. Selecting a preset from the main tab resets completion; loading a slot for editing does not. Reset run clears marks while preserving presets and settings.

## Prepare for the selected run

Bring the right seeds/saplings, seed dibber, spade, rake where needed, gardener payments or compost, coins and selected travel supplies. Bank as needed. Include gloves for belladonna, suitable diving equipment for seaweed, and combat supplies for Hespori. Check health, harvest, clear, plant and protect as appropriate; a patch checkbox does not track these subtasks separately.

## Build and test

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' clean test build
# Refresh dependencies after a RuneLite update:
.\gradlew.bat --refresh-dependencies test build
```

The default dependency version is `latest.release`; the property above reproduces the tested RuneLite client version. Reports: `build/reports/tests/test/index.html`. Headless Swing previews: `build/reports/sidebar-preview.png`, `build/reports/preset-controls-preview.png` and `build/reports/growth-preview.png`. The catalogue test also generates `build/reports/patch-catalogue.md`.

Tests cover named slot creation, copying, rename/delete/collision handling, migration, real RuneLite enum-label rendering, restart/profile isolation, capacity and imported overflow, local write failure, configuration-event dispatch, cog editing versus main-tab selection, no management controls on the main tab, crop decoding and timing, payload boundaries/stabilization, full-preset reminders, deduplication/restart, character isolation, opt-in completion, notification suppression and sidebar layout, plus the existing route, travel and optimizer checks. See [build results](docs/BUILD-REPORT.md).

Native preset/cog UI checks can be performed while logged out. The automated suite covers the account-free behavior with fictional fixtures. No authenticated live game run, actual server payload capture or live timing benchmark was performed. That limitation is disclosed for review; there is no requirement here to test using a main account.

## Optional Plugin Hub submission

The source is published at [dovermlg/farm-run-planner](https://github.com/dovermlg/farm-run-planner), currently a private repository. No Plugin Hub submission or release has been made. The author is Astral and the internal submission name is `farm-run-planner`. The official local packager passed with RuneLite 1.13.2, all 92 offline tests pass, and this repository's GitHub build and reference checks passed. Before submission, make the repository public and follow [Plugin Hub's submission instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin). The Hub manifest references the repository and a full 40-character commit SHA. `python tools/make-submission-manifest.py --repository https://github.com/dovermlg/farm-run-planner.git --commit <full-SHA>` writes the local marker; it does not submit anything or verify public visibility. Never submit the local harness’s fictional repository/commit. See [HUB-REVIEW.md](docs/HUB-REVIEW.md). This project uses `build=standard` and no additional production dependencies. Passing this repository's workflow is separate from Plugin Hub CI and maintainer acceptance.

The plugin only displays guidance and updates user-selected configuration. It does not walk, teleport, click objects, invoke game menus or schedule gameplay input. It imports no private farming-plugin implementation and makes no runtime network requests. Preset-library and completion persistence use local files; active cog settings use RuneLite configuration and its optional sync. This remains a local development build requiring normal Plugin Hub review before any Hub release.

