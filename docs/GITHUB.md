# GitHub setup and publication

Repository: [dovermlg/farm-run-planner](https://github.com/dovermlg/farm-run-planner)

Author displayed in RuneLite: **Astral**. GitHub owner: **dovermlg**.

## Connected components

- This source folder is an independent Git repository with default branch `main` and `origin` set to `https://github.com/dovermlg/farm-run-planner.git`.
- GitHub Desktop has this existing folder added. Future changes appear in its Changes tab; commit and push them from the same folder.
- The plugin's support link points to [GitHub Issues](https://github.com/dovermlg/farm-run-planner/issues), which are enabled.
- Pushes and pull requests run [Offline checks](https://github.com/dovermlg/farm-run-planner/actions/workflows/check.yml): Java 11 build, regression tests and the RuneLite farming-reference audit. This workflow never launches the game or logs into an account.
- Commit identity uses Astral and the GitHub noreply address. Build caches, local runtime data and compiled output are excluded from Git.

## Verified publication

GitHub Desktop published the source. The remote `main` branch was verified to match initial source commit `491a74c99541b5ff5227365ece21f3dd2ab5842b`.

The [initial hosted check](https://github.com/dovermlg/farm-run-planner/actions/runs/39178628649) completed successfully, including the build, tests and farming-reference verification. Later documentation commits run the same workflow automatically; check the workflow page for their current status.

GitHub's repository API reports **private** visibility. Unauthenticated access returns 404, as expected for a private repository. The source and Issues links require access until the owner changes that visibility. Privacy was not changed by the assistant.

## Plugin Hub remains a separate step

Make the repository public before submitting to Plugin Hub. Then verify unauthenticated access to the chosen full commit and generate a marker:

```powershell
git rev-parse HEAD
python tools/make-submission-manifest.py --repository https://github.com/dovermlg/farm-run-planner.git --commit <full-40-character-SHA>
```

The helper writes `build/submission/farm-run-planner`, which is intentionally outside tracked source. A marker using this real repository is still not usable by Plugin Hub while the repository is private. Follow [the official submission instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin) and disclose the live-integration testing limits in [HUB-REVIEW.md](HUB-REVIEW.md). No upstream pull request or release has been created.

GitHub hosts source and runs build checks only. It does not become a database or notification backend for the plugin; players' preset libraries and crop observations remain in their own local RuneLite data folders.
