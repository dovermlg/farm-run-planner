# Route optimality and combination audit

Version **1.14.3** minimizes the plugin's estimated travel score for **every selection from the current 99-patch catalogue**, under every current travel-setting combination. It computes a globally optimal order for that score; several orders can share the same minimum.

Validation status: direct Java 11 compilation and **179 JUnit tests** pass, along with the standalone combination audit. The standard Gradle build and current-source Plugin Hub preflight are pending. The previous 1.14.2 packaging evidence covers the earlier release, not these working changes.

The score describes relative travel effort. It does not establish the fastest real journey, measure paths or account for the player's current position, unlocks, charges, spellbook or harvested inventory. Bank loads and stops are planned after patch ordering; this is not a joint search over patch order and banking.

## Why large selections can be solved exactly

The previous implementation used an exact whole-route search through 16 patches and a heuristic above that limit. A new constructed 18-stop regression reproduced a heuristic score of **629**, compared with an independently calculated optimum of **603**. This is a synthetic cost-model example, not a measured in-game journey.

The current travel model allows a simpler exact solution:

1. Each patch has a standalone arrival cost. Travelling from another selected patch costs at most that arrival cost, because the same teleport remains available in the model.
2. Connect two patches when either direction is cheaper than standalone arrival. These connections form independent groups. Across different groups, travel costs exactly the destination's standalone arrival cost.
3. Any global route leaves a collection of directed chains inside each group. Join those chains into one group route: a new join costs at most the arrival cost it replaces, so this cannot increase the total score.
4. Solve each group's directed route exactly, including its first arrival, then concatenate the group routes. Each group solution is a lower bound for that part of any global route, and concatenation attains their sum. The resulting order is globally optimal.

Removing patches cannot enlarge a group. Checking all **1,728 travel configurations** found a maximum group size of **13 patches**, below the exact solver's 16-patch limit. Therefore this argument covers every subset, including selections larger than 16 and the full 99-patch run.

Production verifies the decomposition conditions before using it. Arbitrary future models with more expensive cross-group edges or a group above 16 retain the bounded fallback for larger runs. A regression checks the entire current catalogue and every travel configuration, so a catalogue/model change that breaks the current guarantee fails validation.

## Exhaustive and independent checks

| Check | Passed coverage |
| --- | --- |
| Classic 16-patch route | All **65,536 subsets**, including empty and full selections, checked against an independent whole-route suffix-cost solver |
| Every current travel setting | **1,728 configurations**, including full 99-patch routes with shuffled input order |
| Distinct travel-group models | All **29,184 subsets** of **65 profiles**; equivalent profiles are deduplicated across settings |
| Input-order variation | **1,516** group cases with shuffled input |
| Brute-force cross-check | **128 cases**, explicitly enumerating **647,278 permutations**, also checking the independent solver |
| Larger independent global checks | **12 constructed 17-stop models** checked against a whole-route oracle that does not use decomposition |
| Original heuristic counterexample | 18-stop score improves from **629** to the exact **603** |

Every comparison checks minimum cost and exact membership: selected patches appear once, with no missing or added stop. Tests compare costs rather than requiring one particular ordering when multiple routes tie. The oracle solves suffix costs, separately from the production forward route solver; its component discovery uses union-find rather than production's breadth-first search.

There are **2^99** possible catalogue subsets. They were not all individually enumerated. Exhaustive local profiles plus the decomposition proof cover that larger selection space. The literal enumeration counts above describe what was executed. Recorded output is in [route-combinations.json](route-combinations.json).

## Reproduce offline

```powershell
.\gradlew.bat test verifyRouteCombinations
```

The audit writes `build/reports/route-combinations.json`. It runs in the test source set, is excluded from the production JAR and never launches the client or uses an account. The project's GitHub workflow runs it on each push.
