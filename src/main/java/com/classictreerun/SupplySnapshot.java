package com.classictreerun;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;

/** Ephemeral inventory/equipment counts; never saved or uploaded. */
final class SupplySnapshot
{
    static final SupplySnapshot UNKNOWN=new SupplySnapshot(false,Map.of(),Map.of());
    final boolean known;
    final Map<Integer,Long> plain, notes;
    SupplySnapshot(boolean known,Map<Integer,Long> plain,Map<Integer,Long> notes)
    { this.known=known;this.plain=Map.copyOf(plain);this.notes=Map.copyOf(notes); }

    static SupplySnapshot read(ItemManager items,ItemContainer inventory,ItemContainer equipment)
    { return read(items::getItemComposition,inventory,equipment); }

    static SupplySnapshot read(java.util.function.IntFunction<ItemComposition> definitions,ItemContainer inventory,ItemContainer equipment)
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
                int id=noted?definition.getLinkedNoteId():item.getId();
                (noted?notes:plain).merge(id,(long)item.getQuantity(),Long::sum);
            }
        return new SupplySnapshot(true,plain,notes);
    }
}
