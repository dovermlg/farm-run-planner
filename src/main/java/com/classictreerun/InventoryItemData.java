package com.classictreerun;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.runelite.api.ItemComposition;

/** Immutable public item-definition flags. Contains no bank or character information. */
final class InventoryItemData
{
    static final InventoryItemData EMPTY=new InventoryItemData(Map.of());
    static final class ItemInfo
    {
        final boolean stackable, noteable;
        ItemInfo(boolean stackable,boolean noteable) { this.stackable=stackable;this.noteable=noteable; }
        @Override public String toString() { return (stackable?"s":"u")+(noteable?"n":"-"); }
    }
    private final Map<Integer,ItemInfo> items;
    InventoryItemData(Map<Integer,ItemInfo> items) { this.items=Map.copyOf(items); }
    boolean covers(Set<Integer> ids) { return items.keySet().containsAll(ids); }
    ItemInfo get(int item)
    {
        ItemInfo info=items.get(item);
        if(info==null)throw new IllegalArgumentException("Item data is not ready for "+item);
        return info;
    }
    InventoryItemData merge(InventoryItemData other)
    { Map<Integer,ItemInfo> joined=new LinkedHashMap<>(items);joined.putAll(other.items);return new InventoryItemData(joined); }
    String fingerprint(Set<Integer> ids)
    {
        Map<Integer,ItemInfo> used=new TreeMap<>();for(int id:ids)used.put(id,get(id));return used.toString();
    }
    static InventoryItemData read(Set<Integer> ids,java.util.function.IntFunction<ItemComposition> definitions)
    {
        Map<Integer,ItemInfo> values=new LinkedHashMap<>();
        for(int id:ids)
        {
            ItemComposition item=definitions.apply(id);
            values.put(id,new ItemInfo(item.isStackable(),item.getLinkedNoteId()!=-1));
        }
        return new InventoryItemData(values);
    }
}
