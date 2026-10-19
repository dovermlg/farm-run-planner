# Build results

Version **1.13.0**, tested with **JDK 11** and **RuneLite 1.13.2**.

| Check | Result |
| --- | --- |
| JUnit regression suite | 152 tests passed; no failures, errors or skips |
| Crop decoders | 1,376,256 raw values matched |
| Patch bindings | 99 distinct patches matched |
| Tile and plane boundaries | 6,176,768 comparisons matched |
| Tree stump supplement | All 19 entries matched the tagged RuneLite source |
| Local Plugin Hub packager | Standard build, dependencies, metadata, licenses and API checks passed |
| GitHub workflow | Build, tests and reference audit run on each push; [workflow history](https://github.com/dovermlg/farm-run-planner/actions/workflows/check.yml) |

The suite covers preset editing and migration, local storage failures, profile and character separation, route ordering, travel options, crop decoding, reminders, replant completion and sidebar layout. Backup tests cover saved-only exports, round trips, Unicode names, duplicate imports, conflict renaming, capacity and storage failures, invalid files, bounded filenames and native cog actions. Supply tests cover exact crop quantities, optional protection, noted payments, shared stack allocation, bank display filtering, shortage colors and container sampling. All 23 tree variants are checked against separate sapling, seed, dry-seedling and watered-seedling IDs; seeds, seedlings, other tiers and noted saplings cannot satisfy planting supplies. The compact item panel is checked at sidebar width with icon alignment, one-line requirements and detailed shortage tooltips. Headless previews include the green completion bar and its reset, partial and empty states.

The 19 new inventory tests cover real-definition flag reading, stackable seeds, individual saplings and compost buckets, bottomless buckets, noted-payment enforcement, shared items, tools and coins in each load, oversized single stops, empty and harvest-only runs, bank-dependent boundaries and all 99 patches without loss or duplication. Sidebar tests cover waiting for metadata, changing inventories, filter refresh without progress reset, bank acknowledgment persistence, reset, undo and independent travel instructions after banking. Preset tests cover bank choices, custom instructions, version 3 round trips, version 2 defaults and invalid inventory settings; existing version 1 import checks also pass. Test fixtures supply explicit item flags; production reads them from RuneLite. No game account was used.

## Reproduce

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' test build
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
```

Results: `build/reports/tests/test/index.html`. The build can download dependencies, but these tasks do not launch the game or use an account.

The local packager evidence is in [hub-check-results.json](hub-check-results.json) and [hub-packager.log](hub-packager.log). These record the 1.13.0 local preflight and its artifact hashes. They are not a hosted Plugin Hub result. Source publication is covered in [GITHUB.md](GITHUB.md).

Live bank rendering, crop timing, actual server payload capture and desktop notification delivery have not been validated. Routes use estimated travel costs, and crop countdowns use last-observed growth stages. See [DATA-AUDIT.md](DATA-AUDIT.md) and [HUB-REVIEW.md](HUB-REVIEW.md) for the reference checks and review scope.
