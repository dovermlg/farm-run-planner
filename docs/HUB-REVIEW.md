# Plugin Hub review notes

**Farm Run Planner 1.10.1** · Author: **Astral** · Internal name: `farm-run-planner`

The local Plugin Hub packager and API checks passed with RuneLite 1.13.2, alongside 92 regression tests and the farming-reference audit. This is a development preflight; maintainer review and hosted Plugin Hub CI have not taken place.

## Implementation

| Area | Approach |
| --- | --- |
| Build | Java 11, `build=standard`, no extra production dependencies |
| Metadata | Explicit internal name, legacy data directory and disabled-by-default descriptor |
| License | BSD-2-Clause; license and farming-data notices included as normal JAR resources |
| Configuration | Public RuneLite config controls; direct accessors for all 126 items |
| Storage | Bounded `Filepath` from `Plugin.getPluginDirectory()` |
| UI | Plugin-owned sidebar and settings; no private UI access or other-plugin configuration changes |
| Game interaction | Observes public crop state; generates no gameplay input or menu actions |
| Networking | No plugin network requests, telemetry or backend |

The review used the [submission instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin), [rejected features and API restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features), and [Jagex client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1). Maintainers decide whether a submission is accepted.

## Local packager check

The check used unmodified [plugin-hub-tooling at commit 034eaff](https://github.com/runelite/plugin-hub-tooling/tree/034eaffd5bce7bb678d35287b547874f3dba6b3b). A development harness supplied the local source directory and called `Plugin.build("1.13.2", true)`. The packager performed its standard build, dependency verification, class and metadata checks, license check and deprecated-API check. Recorded external API usage matched RuneLite 1.13.2.

The harness did not run the source downloader or uploader. Its `LOCAL-OFFLINE-CHECK` URL and zero commit are parser fixtures, not submission values. The harness and reference utilities are outside the production source set and are excluded from the plugin JAR.

Evidence: [hub-check-results.json](hub-check-results.json), [hub-packager.log](hub-packager.log), and [harness instructions](../tools/hub-review/README.md). The JSON and log retain the original preflight results and hashes. GitHub publication and the project's own CI are documented separately in [GITHUB.md](GITHUB.md).

## Data and testing limits

The offline audit matched 1,376,256 decoder values, all 99 patch bindings and 6,176,768 tile/plane boundary comparisons. Tests cover the native preset controls, migrations, local write failures, route optimization, crop observations, reminder deduplication and sidebar layout. Reproduction commands are in [BUILD-REPORT.md](BUILD-REPORT.md).

No authenticated game session, actual server payload capture, live crop-timing measurement or desktop notification test was performed. Timers remain estimates, and travel costs are relative scores rather than measured journey times. The plugin keeps gameplay actions with the player.

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
