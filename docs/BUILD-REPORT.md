# Build results

Version **1.10.1**, tested with **JDK 11** and **RuneLite 1.13.2**.

| Check | Result |
| --- | --- |
| JUnit regression suite | 92 tests passed; no failures, errors or skips |
| Crop decoders | 1,376,256 raw values matched |
| Patch bindings | 99 distinct patches matched |
| Tile and plane boundaries | 6,176,768 comparisons matched |
| Tree stump supplement | All 19 entries matched the tagged RuneLite source |
| Local Plugin Hub packager | Standard build, dependencies, metadata, licenses and API checks passed |
| GitHub workflow | [Build, tests and farming reference passed](https://github.com/dovermlg/farm-run-planner/actions/runs/39178805099) |

The suite covers preset editing and migration, local storage failures, profile and character separation, route ordering, travel options, crop decoding, reminders, replant completion and sidebar layout. Headless previews were inspected, including the green completion bar and its reset, partial and empty states.

## Reproduce

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' test build
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
```

Results: `build/reports/tests/test/index.html`. The build can download dependencies, but these tasks do not launch the game or use an account.

The local packager evidence is in [hub-check-results.json](hub-check-results.json) and [hub-packager.log](hub-packager.log). Those files record the earlier local preflight and its artifact hashes. They are not a hosted Plugin Hub result. Source publication is covered in [GITHUB.md](GITHUB.md).

Live crop timing, actual server payload capture and desktop notification delivery have not been validated. Routes use estimated travel costs, and crop countdowns use last-observed growth stages. See [DATA-AUDIT.md](DATA-AUDIT.md) and [HUB-REVIEW.md](HUB-REVIEW.md) for the reference checks and review scope.
