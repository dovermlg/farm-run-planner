package com.classictreerun;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;

/** One immutable bank presentation derived from the selected farming setup and current load. */
final class BankSetupRequest
{
    final String signature;
    final ClassicTreeRunConfig.SetupBankView mode;
    final Set<Integer> items;
    final int[] layout;
    final boolean focus;
    private BankSetupRequest(String signature,ClassicTreeRunConfig.SetupBankView mode,Set<Integer> items,int[] layout,boolean focus)
    {this.signature=signature;this.mode=mode;this.items=Set.copyOf(items);this.layout=layout.clone();this.focus=focus;}

    static BankSetupRequest create(ClassicTreeRunConfig config,SupplyPlan plan,InventoryItemData data,boolean focus)
    {
        ClassicTreeRunConfig.SetupBankView mode=config.supplyBankView();
        SavedPackingSetup setup=SavedPackingSetup.read(config);
        if(setup.backpack.captured)
        {
            int[] positions=mode==ClassicTreeRunConfig.SetupBankView.LAYOUT?equipmentLayout(setup.equipment):new int[0];
            if(positions.length>0)for(int i=0;i<28;i++)
            {BackpackSetup.Entry entry=setup.backpack.slots.get(i);if(entry!=null)positions[(i/4)*8+4+i%4]=entry.item;}
            String source=config.runName()+"\n"+setup.settings();
            return new BankSetupRequest(Digests.sha256(source),mode,setup.items(),positions,focus);
        }
        int[] layout=mode==ClassicTreeRunConfig.SetupBankView.LAYOUT?layout(plan,data):new int[0];
        StringBuilder source=new StringBuilder(config.runName()).append('\n').append(mode).append('\n').append(plan.equipment.encode());
        for(SupplyPlan.Requirement row:plan.requirements)
            source.append('\n').append(row.item).append(':').append(row.quantity).append(':').append(row.notedOnly)
                .append(':').append(new java.util.TreeSet<>(row.alternatives));
        source.append('\n').append(Arrays.toString(layout));
        return new BankSetupRequest(Digests.sha256(source.toString()),mode,plan.items,layout,focus);
    }

    /** Use RuneLite's item forms on the client thread, including worn weight-reducing gear. */
    BankSetupRequest normalized(java.util.function.IntUnaryOperator canonicalize)
    {
        Set<Integer> ids=new java.util.HashSet<>();
        for(int item:items)ids.add(canonicalize.applyAsInt(item));
        int[] positions=Arrays.stream(layout).map(item->item<0?item:canonicalize.applyAsInt(item)).toArray();
        return new BankSetupRequest(signature,mode,ids,positions,focus);
    }

    private static int[] layout(SupplyPlan plan,InventoryItemData data)
    {
        if(!data.covers(plan.items))throw new IllegalStateException("Waiting for item data before showing the saved bank setup.");
        int[] layout=equipmentLayout(plan.equipment);
        List<PackingLayout.Cell> inventory=PackingLayout.build(plan,data).cells;
        for(int i=0;i<inventory.size();i++)
        {
            // Bank stock uses the unnoted ID even when the player must withdraw the payment as notes.
            layout[(i/4)*8+4+i%4]=plan.requirements.get(inventory.get(i).requirement).item;
        }
        return layout;
    }

    private static int[] equipmentLayout(EquipmentSetup equipment)
    {
        int[] layout=new int[56];Arrays.fill(layout,-1);
        Map<EquipmentInventorySlot,Integer> equipmentPositions=Map.ofEntries(
            Map.entry(EquipmentInventorySlot.HEAD,1),Map.entry(EquipmentInventorySlot.CAPE,8),
            Map.entry(EquipmentInventorySlot.AMULET,9),Map.entry(EquipmentInventorySlot.AMMO,10),
            Map.entry(EquipmentInventorySlot.WEAPON,16),Map.entry(EquipmentInventorySlot.BODY,17),
            Map.entry(EquipmentInventorySlot.SHIELD,18),Map.entry(EquipmentInventorySlot.LEGS,25),
            Map.entry(EquipmentInventorySlot.GLOVES,32),Map.entry(EquipmentInventorySlot.BOOTS,33),Map.entry(EquipmentInventorySlot.RING,34));
        equipment.entries.forEach((slot,item)->layout[equipmentPositions.get(slot)]=item.item);
        return layout;
    }
}
