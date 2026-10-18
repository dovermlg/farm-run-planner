package com.classictreerun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Pure model: no client state, game actions, clocks, or network access. */
final class RouteState
{
    private final Set<Patch> completed = EnumSet.noneOf(Patch.class);

    static List<Patch> ordered(String csv)
    {
        Set<Patch> result = new LinkedHashSet<>(parse(csv));
        java.util.Collections.addAll(result, Patch.values());
        return new ArrayList<>(result);
    }

    static Set<Patch> parse(String csv)
    {
        Set<Patch> result = new LinkedHashSet<>();
        if (csv != null)
        {
            for (String token : csv.split(","))
            {
                try { result.add(Patch.valueOf(token.trim())); }
                catch (IllegalArgumentException ignored) { /* Ignore stale or unknown IDs. */ }
            }
        }
        return result;
    }

    static String encode(Collection<Patch> patches)
    {
        return patches.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    static boolean enabled(Patch patch, ClassicTreeRunConfig config)
    {
        // Legacy profiles/presets never silently opt into new patches after catalogue expansion.
        if (config.selection().isEmpty() ? !patch.isClassic() : !parse(config.selection()).contains(patch)) { return false; }
        if (parse(config.excluded()).contains(patch)) { return false; }
        return categoryEnabled(patch,config);
    }

    private static boolean categoryEnabled(Patch patch, ClassicTreeRunConfig config)
    {
        switch (patch.getType())
        {
            case TREE: return config.trees();
            case FRUIT: return config.fruit();
            case HARDWOOD: return config.hardwood();
            case CALQUAT: return config.calquat();
            default: return config.otherPatches();
        }
    }

    static List<Patch> active(ClassicTreeRunConfig config)
    {
        String selection=config.selection();
        Set<Patch> selected=selection.isEmpty()?EnumSet.copyOf(Patch.classic()):parse(selection);
        selected.removeAll(parse(config.excluded()));
        return ordered(config.order()).stream().filter(p -> selected.contains(p) && categoryEnabled(p,config)).collect(Collectors.toList());
    }

    void load(String csv) { completed.clear(); completed.addAll(parse(csv)); }
    String save() { return encode(completed); }
    boolean isDone(Patch patch) { return completed.contains(patch); }
    void setDone(Patch patch, boolean done)
    {
        if (done) { completed.add(patch); } else { completed.remove(patch); }
    }
    void reset() { completed.clear(); }
    int count(List<Patch> route) { return (int) route.stream().filter(this::isDone).count(); }
    Patch next(List<Patch> route) { return route.stream().filter(p -> !isDone(p)).findFirst().orElse(null); }
}
