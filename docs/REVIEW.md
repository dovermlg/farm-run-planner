# Optimization and reliability review

Farm Run Planner **1.14.1**, reviewed 2026-10-18 against RuneLite **1.13.2**. The release version was rechecked against [RuneLite's published dependency metadata](https://repo.runelite.net/net/runelite/client/maven-metadata.xml). No game account was used.

The review covered route ordering, inventory boundaries, supplies and noted payments, bank filters, teleport outlines, preset editing and backups, local storage recovery, crop observation, reminders, thread ownership and plugin shutdown. The existing player settings and backup format 3 are unchanged; versions 1 and 2 remain importable.

## Fixes

**Manual progress takes priority after a failed save.** A failed automatic replant-completion write could remain queued and later overwrite a successful manual reset or correction. This was reproduced by failing the local write, resetting the run, restoring storage and polling the observer. Successful Reset, Start, checkbox edits and main-tab preset loads now discard obsolete pending automatic progress. Pending crop observations are retained and retried. Failed manual writes still leave the saved state intact.

**Item-data failures are visible and recoverable.** A client-thread item-definition failure previously left the run waiting for data without explaining why. It now produces a local diagnostic log and a sidebar message. Start and Filter stay disabled until definitions are available. Toggling inventory planning off and on retries the request; disabling planning also clears its old request marker.

**Bank departures and outlines use the same walking instructions.** After banking at Catherby, a charter departure now starts at the bank and walks to the docks. It does not send the player back to the fruit patch or suggest another teleport to Catherby. The same rule handles a charter followed by the walk to Tai Bwo Wannai calquat. Brimhaven-to-calquat walking also uses one shared instruction for the sidebar and outline resolver.

**Release versions agree.** The source descriptor had remained at 1.12.2 while Gradle named later builds 1.14.x. Plugin Hub reads the descriptor's version, so this would have displayed an outdated release. The descriptor now supplies 1.14.1 to both Gradle and the Hub packager. The local packaging harness checks and records the generated manifest's version.

## Performance

Inventory planning previously rebuilt complete supply lists for every candidate slice of the route. It now builds each patch's list once and extends each candidate load incrementally. Tools, the bottomless bucket and the coin allowance are shared per load; consumable quantities add, noted payments remain separate, and individual saplings/buckets retain their slot cost. The load-count, travel-cost and tie-breaking rules are unchanged.

Route workers now cancel obsolete work when settings change or the plugin is disposed. Exact and heuristic searches check for cancellation between bounded portions of work. Cancellation uses `SwingWorker.cancel(false)` and a predicate, without thread interruption or gameplay actions. Results from superseded workers cannot replace the current route.

Plan fingerprints now read only the relevant travel fields rather than every native setting. Hexadecimal hashing also avoids per-byte formatter allocation. An independent SHA-256 comparison verifies that existing plan fingerprints keep the same format and values, preserving compatible bank acknowledgments.

| Offline stress case | 1.14.0 baseline | 1.14.1 |
| --- | --- | --- |
| All 99 patches, harvest/check only, optional tools/coins/compost off | 26.148 ms median | 1.962 ms median |

Both measurements used JDK 11 on the same workspace, three warmups and seven measured plans. This case exercises a long route whose supplies fit in one load. The baseline was commit `7b79abf68a6682b96796467075df1cca3f2454e3`. Times are hardware- and fixture-dependent, not game travel times, rendering measurements or a general speed guarantee. Reproduce the current case without opening RuneLite:

```powershell
.\gradlew.bat benchmarkInventoryPlanning
```

The benchmark and its deliberate item-data fixtures are in the test source set and excluded from the production JAR.

## Validation

- Clean Java 11 build and **170 JUnit tests** pass.
- New regressions cover stale-save recovery through all four manual progress actions, cancellation in exact and heuristic route searches, item-data retry, compatible hashes and incremental slot counts.
- The slot-count regression compares **1,528** mixed-route slices against independently built complete packing lists, including both payment modes and four compost choices.
- The farming audit matches **1,376,256** raw decoder values, **99** patch bindings and **6,176,768** tile/plane comparisons against the current RuneLite dependency. All 19 stump entries retain their source matches.
- The official local Plugin Hub packager checks the build, dependencies, licenses, metadata, public API usage and deprecated APIs. Evidence and artifact hashes are in [hub-check-results.json](hub-check-results.json).
- Preset round trips, old-format imports, storage failures, profile/character separation, timers, reminder deduplication, bank filtering, exact sapling forms, bottomless buckets, completion colors and offline outline rendering remain covered by the suite.

The restriction review used the current [Plugin Hub instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin) and [RuneLite API restrictions](https://github.com/runelite/runelite/wiki/Rejected-or-Rolled-Back-Features). Production code adds no gameplay input, withdrawals, automatic teleports, reflection, developer backend or networking. Bank changes remain display filtering through public callbacks, and outlines remain public overlay drawing.

Actual server payload capture, live bank/outline rendering, measured crop timing and desktop notification delivery remain unverified. Crop times, slot reserves and route costs retain their documented estimates and manual checks. The repository remains private; Plugin Hub submission, hosted Hub CI and maintainer approval have not occurred. See [build results](BUILD-REPORT.md) and [review scope](HUB-REVIEW.md).
