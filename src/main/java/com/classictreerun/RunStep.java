package com.classictreerun;

import java.util.List;

/** Immutable sidebar-to-render-thread snapshot. No Swing or game actions on the render thread. */
final class RunStep
{
    enum Kind { PATCH, BANK, PREPARING, WAITING, COMPLETE }
    final Kind kind;
    final String runName,title,instructions,crop,notes,progress;

    private RunStep(Kind kind,String runName,String title,String instructions,String crop,String notes,String progress)
    {
        this.kind=kind;this.runName=runName;this.title=title;this.instructions=instructions;
        this.crop=crop;this.notes=notes;this.progress=progress;
    }

    static RunStep current(ClassicTreeRunConfig config,List<Patch> route,RouteState state,
        InventoryLoadPlanner.Plan inventories,String banks,CropGrowth growth,long now,boolean preparing,boolean skipped)
    {
        String progress=state.count(route)+" / "+route.size()+" patches complete";
        if(route.isEmpty())return skipped
            ?new RunStep(Kind.WAITING,config.runName(),"Hardwoods still growing",
                "No stops to plan right now. Check the sidebar for estimated ready times.","","",progress):null;
        if(preparing || config.loadPlanning() && inventories==null)
            return new RunStep(Kind.PREPARING,config.runName(),"Preparing your run",
                "Waiting for the route and inventory plan. Check the sidebar if this does not finish.","","",progress);
        Patch next=state.next(route);
        if(next==null)return new RunStep(Kind.COMPLETE,config.runName(),"Run complete",
            "Reset or start a fresh run in the sidebar when you are ready.","","",progress);
        int current=inventories==null?-1:inventories.current(state);
        if(config.loadPlanning() && current>0 && !inventories.bankConfirmed(banks,current))
        {
            List<Patch> before=inventories.loads.get(current-1).patches;
            return new RunStep(Kind.BANK,config.runName(),"Bank: "+config.loadBank(),
                config.loadBank().instructions(before.get(before.size()-1),config),"",
                "Prepare inventory "+(current+1)+" of "+inventories.loads.size()
                    +". Deposit produce and unneeded supplies; mark the bank stop complete in the sidebar.",progress);
        }
        int index=route.indexOf(next),starts=inventories==null?-1:inventories.startsAt(next);
        Patch previous=index>0 && starts<=0?route.get(index-1):null;
        String walking=starts>0?config.loadBank().departureInstructions(next,config):TravelCosts.walkingInstruction(previous,next,config);
        return new RunStep(Kind.PATCH,config.runName(),(index+1)+". "+next.getLabel(),
            walking==null?TravelGuide.instructions(next,config):walking,
            !config.trackGrowth()?"":growth==null?"Growth unknown - visit this patch to track it.":growth.describe(next,now),
            config.showDetails()?next.getDirections()+". Gardener: "+next.getGardener()+". Access: "+next.getRequirements()+".":"",progress);
    }
}
