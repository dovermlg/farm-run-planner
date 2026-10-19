package com.classictreerun;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/** EDT coordinator: item metadata arrives from the client thread; planning runs off the EDT. */
final class InventoryPreparation
{
    private final ClassicTreeRunConfig config;
    private final Runnable changed;
    private Consumer<Set<Integer>> request=ids->{ };
    private InventoryItemData data=InventoryItemData.EMPTY;
    private Set<Integer> requestedItems=Set.of();
    private String requestedPlan;
    private SwingWorker<InventoryLoadPlanner.Plan,Void> worker;
    private InventoryLoadPlanner.Plan plan;
    private boolean disposed;
    private String error;
    InventoryPreparation(ClassicTreeRunConfig config,Runnable changed) { this.config=config;this.changed=changed; }
    void setRequest(Consumer<Set<Integer>> request) { this.request=request;requestedItems=Set.of(); }
    void accept(InventoryItemData incoming) { if(!disposed){data=data.merge(incoming);changed.run();} }
    void failed()
    {
        if(disposed)return;
        if(worker!=null){worker.cancel(false);worker=null;}
        plan=null;requestedPlan=null;
        error="Couldn't read RuneLite item data. Toggle Plan inventory loads off and on to retry.";
        changed.run();
    }
    InventoryLoadPlanner.Plan get() { return plan; }
    InventoryItemData data() { return data; }
    String error() { return error; }
    void update(List<Patch> route)
    {
        if(disposed)return;
        if(!config.loadPlanning())
        {if(worker!=null)worker.cancel(false);worker=null;plan=null;requestedPlan=null;requestedItems=Set.of();error=null;return;}
        Set<Integer> items=SupplyPlan.build(config,route).items;
        if(!data.covers(items))
        {
            if(worker!=null)worker.cancel(false);
            plan=null;requestedPlan=null;
            if(!items.equals(requestedItems)){requestedItems=items;request.accept(items);}
            return;
        }
        String signature=InventoryLoadPlanner.signature(config,route,data);
        if(signature.equals(requestedPlan))return;
        if(worker!=null)worker.cancel(false);
        requestedPlan=signature;plan=null;error=null;
        ClassicTreeRunConfig snapshot=LocalRunConfig.snapshot(config);
        List<Patch> patches=List.copyOf(route);InventoryItemData definitions=data;
        worker=new SwingWorker<InventoryLoadPlanner.Plan,Void>()
        {
            @Override protected InventoryLoadPlanner.Plan doInBackground()
            { return InventoryLoadPlanner.plan(snapshot,patches,definitions); }
            @Override protected void done()
            {
                if(disposed || isCancelled() || !signature.equals(requestedPlan))return;
                try { plan=get(); }
                catch(Exception failure){error="Couldn't prepare inventory loads. Change an inventory setting to retry.";}
                changed.run();
            }
        };
        worker.execute();
    }
    void dispose() { disposed=true;if(worker!=null)worker.cancel(false); }
}
