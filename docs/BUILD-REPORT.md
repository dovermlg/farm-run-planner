# Build verification — 1.10.1

Date: 2026-10-18 (America/Chicago).

* Windows, Eclipse Temurin JDK 11.0.31, Gradle 8.10; compiled against RuneLite client **1.13.2**.
* **92 JUnit tests passed**, with no failures or skipped tests. Coverage includes the prior preset/cog/storage/route behavior, crop timers, full-run reminders, sampler boundaries, character isolation and optional replant completion.
* New regressions cover stump handling and old records, anima replacement, revising expired estimates after a fresh observation, periodic unchanged payloads, same-fruit replanting, pending writes across character switches, edited preset cache invalidation, retaining UI controls during countdowns and directed relocation moves checked against complete rescoring.
* The offline compatibility audit passed **1,376,256 decoder values**, **99 unique patch bindings** and **6,176,768 tile/plane boundary comparisons** against the actual RuneLite dependency. All 19 stump supplements match the tagged source's object/action comments.
* Sidebar previews rendered at 225 pixels and were visually inspected. Countdown refreshes retain existing selector/checklist controls.
* Official RuneLite Plugin Hub packager local check passed: standard build, verified dependencies, metadata, descriptor/class checks, license and deprecated API checks. Recorded API usage matches RuneLite 1.13.2. Public repository download, hosted CI and maintainer review remain pending.
* Added 5 readiness regressions for the direct config registry, approved-directory/migration descriptor, license resources, restricted production facilities and literal custom names.
* No additional production dependency, network client, telemetry, developer backend or gameplay input was added. Reflection was removed from all production code. Local writes use the supported bounded Filepath API.
* Source provenance, data-vs-estimate limits, fixes, primary references and reproduction instructions: [DATA-AUDIT.md](DATA-AUDIT.md).
* No authenticated game session, live crop timing measurement, live desktop notification or Plugin Hub approval was performed. Local development testing remains necessary.

Reproduce from the project folder:

```powershell
.\gradlew.bat '-PruneliteVersion=1.13.2' clean test build
.\gradlew.bat -I tools/reference-export.gradle '-PruneliteVersion=1.13.2' verifyFarmingReference
```

Build success does not establish a globally fastest in-game route or guaranteed crop readiness. The route uses estimated relative travel costs; the clock uses last-observed crop stages.

Full submission review and evidence: [HUB-REVIEW.md](HUB-REVIEW.md). Author: Astral. No account login or publication occurred.

Version 1.10.1: completed nonempty runs render the progress bar in RuneLite’s completion green. Partial, reset and empty runs keep the normal color. A separate offline visual check verified the rendered fill and transitions; no new test was added for this small visual change.
