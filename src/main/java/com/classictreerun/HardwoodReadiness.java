package com.classictreerun;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Temporary run exclusions. Saved patch choices are never edited by readiness. */
final class HardwoodReadiness
{
    private final Set<Patch> skipped=EnumSet.noneOf(Patch.class);
    private Set<Patch> selection=EnumSet.noneOf(Patch.class);
    private String name;
    private boolean tracking,locked;

    boolean update(String run,List<Patch> selected,CropGrowth growth,boolean enabled,long now,RouteState progress)
    {
        Set<Patch> nextSelection=EnumSet.noneOf(Patch.class);nextSelection.addAll(selected);
        if(!Objects.equals(name,run) || !selection.equals(nextSelection) || tracking!=enabled)
        {locked=false;name=run;selection=nextSelection;tracking=enabled;}
        if(locked)return false;
        Set<Patch> next=EnumSet.noneOf(Patch.class);
        if(enabled && growth!=null)
            for(Patch patch:selected)
                if(patch.getType()==Patch.Type.HARDWOOD && !progress.isDone(patch) && growth.isKnownGrowing(patch,now))next.add(patch);
        boolean changed=!next.equals(skipped);skipped.clear();skipped.addAll(next);
        // Existing completion marks resume a run instead of dropping its replanted trees.
        if(progress.count(selected)>0)locked=true;
        return changed;
    }

    List<Patch> route(List<Patch> selected)
    {List<Patch> route=new ArrayList<>(selected);route.removeAll(skipped);return route;}
    Set<Patch> skipped() { return Set.copyOf(skipped); }
    void lock() { locked=true; }
    void restart() { locked=false; }
}
