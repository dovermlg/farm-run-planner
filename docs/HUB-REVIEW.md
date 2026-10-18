# Plugin Hub readiness review — 1.10.1

Reviewed 2026-10-18 for **Farm Run Planner**, author **Astral**, internal name `farm-run-planner`.

**Result: the documented code/build blockers found in this review are fixed, the official local packager check passed, and all 92 offline tests passed.** This is preparation for submission, not RuneLite approval, a complete hosted CI run, or an account-safety guarantee. No account was used.

## Requirements and findings

| Area | Project result |
| --- | --- |
| Java 11 and standard build | Passed the official standard builder; `build=standard` |
| Metadata and plugin entry class | Official packager accepted the descriptor, author, name, version and entry class |
| License and bundled resources | BSD-2-Clause; own license and farming-data notices are normal resources included in both build paths |
| Dependencies | No added production library; official dependency verification passed |
| Reflection | Removed from all production settings/migration code; direct accessors cover all 126 configuration items |
| Files | Bounded `Filepath` from `Plugin.getPluginDirectory()`; no unrestricted file API or `Filepath.Unchecked` in production |
| Game behavior | Reviewed reads of local patch state; no generated game input, server actions, menu changes or combat guidance |
| Other plugins and core UI | Changes stay in this plugin's configuration and panel; no other plugin configuration or core-window manipulation |
| Networking/privacy | No plugin network client, backend, telemetry or credentials; separate local preset/growth files |
| Simulation | No simulation feature in the runtime plugin; fictional states are development test fixtures only |

Requirements were checked against the [Plugin Hub submission/build instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin), RuneLite's [rejected features and language/API restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), and [Jagex's client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1). The feature-scope assessment is our source review; maintainers retain the final decision.

## Changes made

1. Replaced configuration reflection in snapshot capture, native-key routing and legacy migration with a generated, checked-in list of direct Java getter calls. A regression test compares it with every configuration annotation. Regenerate with `python tools/generate-config-values.py` after editing the config interface.
2. Switched local persistence to RuneLite's [Filepath API](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/util/Filepath.html). Added `internalName` and `legacyDataDirectory` to the plugin descriptor. RuneLite performs directory migration; this plugin never obtains unrestricted paths. Installation starts disabled by default.
3. Put license notices under `src/main/resources/META-INF/`. The Hub replaces a standard plugin's custom build scripts, so root-file copy steps cannot be required for runtime resources. Removed that custom copy step and tested source/resource equality.
4. Set the requested author to Astral. Kept the native cog, local named preset library, timers and checklist behavior.
5. Made preset dropdown names literal text, preventing Swing HTML interpretation of user-entered names. No names were rejected or rewritten.
6. Added a read-only GitHub Actions workflow for account-free build, regression and data-reference checks. It passed on GitHub after source publication; see [the publication record](GITHUB.md). This is the project's workflow, separate from hosted Plugin Hub CI.

## Official local tooling evidence

Used the unmodified [plugin-hub-tooling](https://github.com/runelite/plugin-hub-tooling/tree/034eaffd5bce7bb678d35287b547874f3dba6b3b) source at commit `034eaffd5bce7bb678d35287b547874f3dba6b3b`. Built its API recorder and packager under JDK 11. A small **development-only local harness**, outside production sources, supplies the source directory and calls `Plugin.build("1.13.2", true)`. It does not call the repository downloader or uploader. The official packager still performs its standard build, Gradle dependency verification, resource/class/version checks, descriptor/metadata checks, license check and terminally deprecated API check. The recorded external API set was also compared against RuneLite 1.13.2's dependency API set with no missing symbols.

The local harness uses an explicitly fictional `LOCAL-OFFLINE-CHECK` repository URL and a zero commit solely as parser fixtures. They are **not submission values**. That local check did not verify public-repository availability or remote source retrieval, and did not run hosted Plugin Hub CI. The later GitHub publication is recorded separately in [GITHUB.md](GITHUB.md); the repository remains private and cannot yet be retrieved publicly by Plugin Hub. No fixture manifest is supplied as a real submission manifest.

An initial packager attempt could not resolve a Maven hostname in its child process. Caching the standard builder's dependencies through the regular build resolved that environment problem; the successful run retained the official verification metadata and checks.

Evidence: [hub-check-results.json](hub-check-results.json) and [hub-packager.log](hub-packager.log). The production JAR excludes the local harness, tests, source generators, reference exporters and private farming classes. Automated checks supplement the source review; they do not establish every policy decision.

## Offline verification

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' test build
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
```

These commands do not launch RuneLite or connect an account/game world. The build may download dependencies. The farming reference still passes all 1,376,256 decoder comparisons, 99 distinct bindings and 6,176,768 tile/plane boundary comparisons. The regression suite covers presets, native-cog actions, persistence failures, route selection/optimization, crop observations, readiness notifications and layout. It also checks registry completeness, descriptor migration settings, packaged notices, restricted facilities and literal preset-name rendering.

No authenticated run, actual server payload capture, live crop-timing measurement or desktop notification test was performed. That limit should be disclosed to reviewers. Do not describe developer fixtures as recorded gameplay.

## Local data migration

The supported location is now:

```text
<RuneLite data folder>/plugin-data/farm-run-planner/profile-<RuneLite profile ID>.properties
```

The descriptor tells RuneLite to move the previous `farm-run-planner` directory into that location when the new destination does not exist. Existing property formats and config keys are unchanged. If both old and new directories already exist, RuneLite does not merge them; back up both and resolve the files with the client closed. This review did not open or modify anyone's installed RuneLite data.

Snapshots, slot mappings, progress, crop observations and reminder markers remain local. Active native-cog settings still follow RuneLite's optional configuration sync. Nothing is sent to Astral by this plugin.

## Remaining submission steps

1. Make the existing `dovermlg/farm-run-planner` repository public. Its root already contains the source, license, notices and README; GitHub Desktop is linked.
2. Use the full 40-character SHA of the final pushed source commit and verify that commit can be fetched publicly.
3. Generate the marker file using `tools/make-submission-manifest.py` and those real values. It creates a local file only.
4. Follow the official Plugin Hub pull-request process, disclose the untested live integration, resolve hosted checks and wait for maintainer review.

Source publication and this project's GitHub workflow are complete; see [GITHUB.md](GITHUB.md). No upstream Plugin Hub pull request, submission or release has been created. The JSON and packager log remain the historical evidence from the earlier local preflight, rather than a claim of a new hosted Plugin Hub check.

## Version 1.10.1

The completion bar now uses RuneLite’s green when every selected patch is complete. Its own Swing progress component uses a standard paint delegate for consistent fill across themes. The existing 92 tests passed, the offline preview verified green pixels and reset/partial/empty behavior, and the official local packager was rerun. No game account or runtime permissions were added.
