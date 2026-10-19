# GitHub and submission setup

Repository: [dovermlg/farm-run-planner](https://github.com/dovermlg/farm-run-planner). Plugin author: **Astral**.

## Day-to-day development

The project is linked in GitHub Desktop, with `main` as the default branch and `https://github.com/dovermlg/farm-run-planner.git` as `origin`. Edit this source folder, review changes in GitHub Desktop, then commit and push.

[GitHub Issues](https://github.com/dovermlg/farm-run-planner/issues) is enabled and linked from the plugin metadata. Pushes and pull requests run [Offline checks](https://github.com/dovermlg/farm-run-planner/actions/workflows/check.yml): a Java 11 build, regression tests and the farming-reference audit. Build output and local data are excluded from Git. Commits use the GitHub noreply email address.

Reference build: [passed](https://github.com/dovermlg/farm-run-planner/actions/runs/39178805099) at commit `c9c38fdbcf80f9965b96949c9cc28ad0c0e56128` on 2026-10-18. The workflow page shows checks for later commits.

## Plugin Hub submission

The repository was private when publication was verified on 2026-10-18. It must be publicly accessible before Plugin Hub can fetch the source. Source publication and this project's workflow are separate from Plugin Hub CI and maintainer review.

After making the repository public, choose the final pushed commit and generate its marker:

```powershell
git rev-parse HEAD
python tools/make-submission-manifest.py --repository https://github.com/dovermlg/farm-run-planner.git --commit <full-40-character-SHA>
```

The helper writes `build/submission/farm-run-planner`. It does not publish or submit anything. Verify public access to the chosen commit, then follow [Plugin Hub's submission instructions](https://github.com/runelite/plugin-hub#submitting-a-plugin). Include the testing limits from [HUB-REVIEW.md](HUB-REVIEW.md).

No upstream pull request or Plugin Hub release has been created. GitHub hosts source and build checks; it is not a data or notification backend for players.
