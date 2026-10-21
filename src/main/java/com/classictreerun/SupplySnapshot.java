package com.classictreerun;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.client.game.ItemManager;

/** Ephemeral inventory/equipment counts; never saved or uploaded. */
final class SupplySnapshot
{
    static final SupplySnapshot UNKNOWN=new SupplySnapshot(false,Map.of(),Map.of());
    final boolean known;
    final Map<Integer,Long> plain, notes;
    final Map<EquipmentInventorySlot,EquipmentSetup.Entry> worn;
    final Map<Integer,Integer> wornAliases;
    final BackpackSetup backpack;
    SupplySnapshot(boolean known,Map<Integer,Long> plain,Map<Integer,Long> notes)
    { this(known,plain,notes,Map.of()); }
    SupplySnapshot(boolean known,Map<Integer,Long> plain,Map<Integer,Long> notes,Map<EquipmentInventorySlot,EquipmentSetup.Entry> worn)
    {this(known,plain,notes,worn,Map.of(),BackpackSetup.NONE);}
    SupplySnapshot(boolean known,Map<Integer,Long> plain,Map<Integer,Long> notes,Map<EquipmentInventorySlot,EquipmentSetup.Entry> worn,BackpackSetup backpack)
    {this(known,plain,notes,worn,Map.of(),backpack);}
    private SupplySnapshot(boolean known,Map<Integer,Long> plain,Map<Integer,Long> notes,Map<EquipmentInventorySlot,EquipmentSetup.Entry> worn,Map<Integer,Integer> wornAliases,BackpackSetup backpack)
    { this.known=known;this.plain=Map.copyOf(plain);this.notes=Map.copyOf(notes);this.worn=Map.copyOf(worn);this.wornAliases=Map.copyOf(wornAliases);this.backpack=backpack; }
    int canonicalWorn(int item){return wornAliases.getOrDefault(item,item);}

    static SupplySnapshot read(ItemManager items,ItemContainer inventory,ItemContainer equipment)
    { return read(items::getItemComposition,items::canonicalize,inventory,equipment); }

    static SupplySnapshot read(java.util.function.IntFunction<ItemComposition> definitions,ItemContainer inventory,ItemContainer equipment)
    {return read(definitions,item->item,inventory,equipment);}

    static SupplySnapshot read(java.util.function.IntFunction<ItemComposition> definitions,java.util.function.IntUnaryOperator canonicalize,ItemContainer inventory,ItemContainer equipment)
    {
        if(inventory==null || equipment==null)return UNKNOWN;
        Map<Integer,Long> plain=new HashMap<>(),notes=new HashMap<>();
        for(ItemContainer container:new ItemContainer[]{inventory,equipment})
            for(Item item:container.getItems())
            {
                if(item.getId()<0 || item.getQuantity()<=0)continue;
                ItemComposition definition=definitions.apply(item.getId());
                if(definition.getPlaceholderTemplateId()!=-1)continue;
                boolean noted=definition.getNote()!=-1;
                int id=canonicalize.applyAsInt(noted?definition.getLinkedNoteId():item.getId());
                (noted?notes:plain).merge(id,(long)item.getQuantity(),Long::sum);
            }
        Map<EquipmentInventorySlot,EquipmentSetup.Entry> worn=new java.util.EnumMap<>(EquipmentInventorySlot.class);
        Map<Integer,Integer> aliases=new HashMap<>();
        Item[] equipped=equipment.getItems();
        for(EquipmentInventorySlot slot:EquipmentSetup.SLOTS)
        {
            int index=slot.getSlotIdx();if(index>=equipped.length)continue;
            Item item=equipped[index];if(item.getId()<=0 || item.getQuantity()<=0)continue;
            ItemComposition definition=definitions.apply(item.getId());
            if(definition.getNote()!=-1 || definition.getPlaceholderTemplateId()!=-1)continue;
            int id=canonicalize.applyAsInt(item.getId());
            if(id!=item.getId())aliases.put(item.getId(),id);
            worn.put(slot,new EquipmentSetup.Entry(id,slot==EquipmentInventorySlot.AMMO || slot==EquipmentInventorySlot.WEAPON?item.getQuantity():1));
        }
        java.util.List<BackpackSetup.Entry> slots=new java.util.ArrayList<>(java.util.Collections.nCopies(28,null));
        Item[] carried=inventory.getItems();
        for(int i=0;i<Math.min(28,carried.length);i++)
        {
            Item item=carried[i];if(item.getId()<=0 || item.getQuantity()<=0)continue;
            ItemComposition definition=definitions.apply(item.getId());if(definition.getPlaceholderTemplateId()!=-1)continue;
            boolean noted=definition.getNote()!=-1;
            slots.set(i,new BackpackSetup.Entry(canonicalize.applyAsInt(noted?definition.getLinkedNoteId():item.getId()),item.getQuantity(),noted));
        }
        return new SupplySnapshot(true,plain,notes,worn,aliases,new BackpackSetup(slots));
    }
}
