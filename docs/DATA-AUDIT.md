# Farming data and performance audit — 1.9.0

Audited 2026-10-18. The tested release is RuneLite **1.13.2**, confirmed against its published Maven release metadata. This is a local development build.

This document records the data audit introduced in 1.9.0. Current validation and later fixes are recorded in [build results](BUILD-REPORT.md) and the [reliability review](REVIEW.md).

## What the game reports

The plugin reads farming varbits through public `Client.getVarbitValue` on the game thread. These are region-dependent payloads: the same varbit can describe different patches in different places. The observer therefore uses a versioned patch/region binding, RuneLite's special location boundaries, modal-dialog guards, settling after arrival and two matching reads. It reads only patches whose payloads should be available at the player's current location. It does not query a remote farming service or inspect every patch from anywhere.

The bundled decoding table converts those values to RuneLite's crop, growth state, stage, stage count and growth/regrowth interval. It is derived from the tagged [PatchImplementation](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/PatchImplementation.java), [PatchState](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/PatchState.java), [Produce](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/Produce.java) and [FarmingWorld](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/FarmingWorld.java) sources. Those internal classes are used only by the optional offline developer utilities, never by the running plugin. BSD notices accompany the data in source and the JAR.

## What is estimated

The game does **not** supply this plugin with a guaranteed per-patch harvest timestamp. It observes the current stage and estimates remaining time from the crop interval. [Jagex's farming timer explanation](https://secure.runescape.com/m=news/farming-timer-rework--skilling-qol?oldschool=1) describes shared growth ticks rather than a clock starting at the exact moment of planting. RuneLite's own [FarmingTracker](https://github.com/runelite/runelite/blob/runelite-parent-1.13.2/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/FarmingTracker.java) additionally learns tick offsets; this plugin currently does not learn those offsets or read the other plugin's configuration.

Consequently, the displayed time is a conservative stage-based estimate and can be late by up to one growth interval under ordinary healthy growth. Unseen disease, gardener intervention, Kronos skips, special modifiers, another client or changed game data can invalidate it. An unchanged state observed after the predicted deadline revises the estimate. A timer passing is not proof of crop health. All estimated-ready text and notifications retain that distinction. The seasonal fivefold rate follows the audited RuneLite implementation, excluding Deadman; future seasonal mechanics need review.

Travel costs are separate hand-authored relative scores. The game does not provide the cost matrix or a globally fastest farming route. The optimizer is exact for its model at up to 16 stops; larger selections use a heuristic. It does not measure tile paths, current player position, spellbooks, unlocks, banking or teleport charges. See [catalogue/travel verification](VERIFICATION.md) for sources and scope.

## Verification performed

| Check | Result |
| --- | --- |
| All raw values 0–65,535 for each of 21 crop implementations | **1,376,256 decoder comparisons matched** |
| Every recurring crop patch in RuneLite, excluding compost bins | **99 unique bindings; no missing or duplicate bindings** |
| Every tile on planes 0–3 in every mapped region alias | **6,176,768 boundary comparisons matched** |
| Stump values distinguished by the tagged source's object/action comments | **All 19 matched** |
| Live OSRS Wiki regular patch tables | All seven refreshed tables unchanged from the earlier 99-patch audit |
| Original 1.9.0 regression suite | **87 tests passed, 0 failed, 0 skipped** |

The checked Wiki pages cover [allotment/flower/herb patches](https://oldschool.runescape.wiki/w/Allotment_patch/Patches), [normal trees](https://oldschool.runescape.wiki/w/Tree_patch/Patches), [fruit trees](https://oldschool.runescape.wiki/w/Fruit_tree_patch/Patches), [hops](https://oldschool.runescape.wiki/w/Hops_patch/Patches), [bushes](https://oldschool.runescape.wiki/w/Bush_patch/Patches), [spirit trees](https://oldschool.runescape.wiki/w/Spirit_Tree_(Farming)/Patches) and [special patches](https://oldschool.runescape.wiki/w/Special_patches/Patches). The audit also checked Catherby/Brimhaven charter access and Tai Bwo Wannai scroll requirements. Travel scores do not assume a fixed fare or measured journey time.

Exact tagged-source URLs and SHA-256 hashes for the downloaded RuneLite files and bundled tables are recorded in [reference-provenance.json](reference-provenance.json). This checks agreement with the audited client and documents the source of the interpretation; it is not independent verification against Jagex's server code.

## Corrections and optimizations

* Clearable stumps no longer acquire fruit-yield regrowth timers or appear as a ready growing crop. Their last-observed state blocks the full-run alert, with instructions to clear/replant. Their ordinary tree respawn mechanics are not timed. Old 1.8 observations are handled without deleting presets.
* Anima shows active/withering status and estimated expiry/replacement. Its final decoder stage represents an expired plant, not a harvestable yield.
* A confirmed same-fruit replant after a stump now works with optional auto-completion. Arrival alone still never marks a stop.
* Pending observation writes retain their original character keys after a character switch and are retried if local storage recovers. Notifications wait for observations to save.
* Countdown polling changes existing labels instead of reconstructing the preset dropdown and checklist. One time value is used per refresh.
* Unchanged nearby payloads are forwarded at most once per 500 game ticks (about five minutes), with stable changes forwarded promptly. Region candidates are indexed, and crop ranges use binary search.
* The character digest is cached for the current account/world-mode context. Saved-preset selections are reparsed only when the local library changes.
* Large-route relocation checks score only changed edges, preserving directed costs and first-stop entry costs. Selection CSVs are parsed once per route read.

Active cog preferences follow RuneLite's optional configuration sync; saved presets, observations and reminder markers use local files. The runtime has no network client or private farming dependency and generates no gameplay input.

## Reproduce

From the project folder with JDK 11:

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' clean test build
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
```

To check a newer release, substitute its version and review failures before replacing tables. The verifier fails on decoder, coverage, binding, alias or boundary differences. Optional `exportFarmingReference` writes candidate tables under `build/reference/`; exporting alone does not update the reviewed resources or the separate stump supplement.

Headless sidebar previews were rendered and inspected at 225 pixels. No authenticated OSRS run, live growth-timing measurement or desktop-notification test was performed. Plugin Hub review has not been requested. Crop countdowns remain estimates; live timing and notification delivery are unverified.
