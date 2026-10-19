# Build results

Version **1.12.1**, tested with **JDK 11** and **RuneLite 1.13.2**.

| Check | Result |
| --- | --- |
| JUnit regression suite | 132 tests passed; no failures, errors or skips |
| Crop decoders | 1,376,256 raw values matched |
| Patch bindings | 99 distinct patches matched |
| Tile and plane boundaries | 6,176,768 comparisons matched |
| Tree stump supplement | All 19 entries matched the tagged RuneLite source |
| Local Plugin Hub packager | Standard build, dependencies, metadata, licenses and API checks passed |
| GitHub workflow | Build, tests and reference audit run on each push; [workflow history](https://github.com/dovermlg/farm-run-planner/actions/workflows/check.yml) |

The suite covers preset editing and migration, local storage failures, profile and character separation, route ordering, travel options, crop decoding, reminders, replant completion and sidebar layout. The 17 backup tests cover saved-only exports, round trips, Unicode names, duplicate imports, conflict renaming, capacity and storage failures, invalid files, bounded filenames and the native cog actions. The 23 supplies tests cover exact crop quantities, optional protection, noted payments, shared stack allocation, bank display filtering, shortage colors, container sampling and version 1/2 backup compatibility. All 23 tree variants are checked against separate sapling, seed, dry-seedling and watered-seedling IDs; seeds, seedlings, other tiers and noted saplings cannot satisfy planting supplies. Headless previews include the green completion bar and its reset, partial and empty states.

## Reproduce

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' test build
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
```

Results: `build/reports/tests/test/index.html`. The build can download dependencies, but these tasks do not launch the game or use an account.

The local packager evidence is in [hub-check-results.json](hub-check-results.json) and [hub-packager.log](hub-packager.log). These record the 1.12.1 local preflight and its artifact hashes. They are not a hosted Plugin Hub result. Source publication is covered in [GITHUB.md](GITHUB.md).

Live bank rendering, crop timing, actual server payload capture and desktop notification delivery have not been validated. Routes use estimated travel costs, and crop countdowns use last-observed growth stages. See [DATA-AUDIT.md](DATA-AUDIT.md) and [HUB-REVIEW.md](HUB-REVIEW.md) for the reference checks and review scope.
