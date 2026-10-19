package com.classictreerun;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/** EDT coordinator: item metadata arrives from the client thread; planning runs off the EDT. */
final class InventoryPreparation
{
    /** Identity distinguishes retries even when both requests contain the same items. */
    static final class ItemRequest
    {
        final Set<Integer> items;
        ItemRequest(Set<Integer> items) { this.items=Set.copyOf(items); }
    }
    private final ClassicTreeRunConfig config;
    private final Runnable changed;
    private Consumer<ItemRequest> request=ids->{ };
    private InventoryItemData data=InventoryItemData.EMPTY;
    private ItemRequest pendingData;
    private String requestedPlan;
    private SwingWorker<InventoryLoadPlanner.Plan,Void> worker;
    private InventoryLoadPlanner.Plan plan;
    private boolean disposed;
    private String error;
    InventoryPreparation(ClassicTreeRunConfig config,Runnable changed) { this.config=config;this.changed=changed; }
    void setRequest(Consumer<ItemRequest> request) { this.request=request;pendingData=null; }
    void accept(ItemRequest source,InventoryItemData incoming)
    {
        if(disposed || source!=pendingData)return;
        pendingData=null;data=data.merge(incoming);changed.run();
    }
    void failed(ItemRequest source)
    {
        if(disposed || source!=pendingData)return;
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
        {if(worker!=null)worker.cancel(false);worker=null;plan=null;requestedPlan=null;pendingData=null;error=null;return;}
        Set<Integer> items=SupplyPlan.build(config,route).items;
        if(!data.covers(items))
        {
            if(worker!=null)worker.cancel(false);
            plan=null;requestedPlan=null;
            if(pendingData==null || !items.equals(pendingData.items))
            {error=null;pendingData=new ItemRequest(items);request.accept(pendingData);}
            return;
        }
        pendingData=null;
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
    void dispose() { disposed=true;pendingData=null;if(worker!=null)worker.cancel(false); }
}
