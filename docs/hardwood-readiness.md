# Growing hardwoods

Keep hardwood patches selected in a mixed preset. With **Track crop readiness** enabled, a healthy hardwood whose observed growth stage gives a future ready estimate is skipped when preparing the next run. No patch toggles or saved presets are changed.

The prepared route is optimized using only its included patches. Calculated saplings, protection payments, compost, teleport requirements, inventory loads, bank stops and the next teleport outline use that same route. A sidebar note lists skipped hardwoods and their individual timers. If no stops remain, the sidebar explains that the hardwoods are still growing; it does not report a completed run or request supplies.

Unknown observations, weeds, empty patches, diseased or dead trees, stumps and trees awaiting a health check stay included. A missing or unreliable timer also leaves the patch included. This applies to every supported hardwood patch and species, including the three Fossil Island patches. Other patch types are unaffected.

Before starting, the readiness timer can add a hardwood back when its estimate expires. **Start run** or the first completion holds that run's included patches steady. Replanting cannot remove an included tree halfway through the run. Trees that become ready during a started run are considered when you reset, start a fresh run or load a preset again. Existing completed trees remain included when resuming a checklist. The active planning snapshot is held in memory; reopening the plugin reconstructs it from the character's observations and existing completion marks.

Full-run estimates and notifications continue to use the entire saved preset, including skipped hardwoods. A mixed preset's full-run alert therefore still waits for its hardwoods. Resetting a checklist never resets crop timers. Disabling crop tracking includes all selected patches again.

A manually captured backpack and gear setup stays exactly as saved, including any spare saplings or teleport items. Its bank layout is not rewritten by readiness. The calculated requirements below it reflect the included stops. Without a captured backpack, the generated bank setup follows those requirements.

Readiness uses the existing local, per-character crop observations. It does not remotely check crops or perform game actions. Estimates cannot detect disease while you are away; visit patches to refresh their state.

The immature, health-check, mature, diseased, dead and stump distinctions were checked against [RuneLite 1.13.3's hardwood decoder](https://github.com/runelite/runelite/blob/runelite-parent-1.13.3/runelite-client/src/main/java/net/runelite/client/plugins/timetracking/farming/PatchImplementation.java). No new runtime dependency or private API is used. Offline tests cover every raw state from -1 through 256 at each of the five hardwood patches, all 32 skip combinations, exact route scores against a permutation oracle, inventory-load reduction, reset/start behavior, character changes and full-preset reminders. No authenticated game session was used.
