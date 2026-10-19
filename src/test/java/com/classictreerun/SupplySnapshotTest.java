package com.classictreerun;

import java.lang.reflect.Proxy;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplySnapshotTest
{
    private ItemComposition definition(int note,int linked,int placeholder)
    {
        return (ItemComposition)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{ItemComposition.class},(p,m,a)->{
            switch(m.getName())
            {
                case "getNote":return note;case "getLinkedNoteId":return linked;case "getPlaceholderTemplateId":return placeholder;
                default:throw new AssertionError(m.getName());
            }
        });
    }
    private ItemContainer container(Item... items)
    { return (ItemContainer)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{ItemContainer.class},(p,m,a)->{
        if("getItems".equals(m.getName()))return items;throw new AssertionError(m.getName());}); }

    @Test public void countsInventoryAndEquipmentSeparatelyFromNotesAndSkipsPlaceholders()
    {
        Map<Integer,ItemComposition> definitions=Map.of(10,definition(-1,-1,-1),11,definition(799,10,-1),12,definition(-1,-1,14401));
        SupplySnapshot counts=SupplySnapshot.read(definitions::get,container(new Item(10,2),new Item(11,7),new Item(12,20),new Item(-1,1),new Item(10,0)),container(new Item(10,1)));
        assertTrue(counts.known);assertEquals(Map.of(10,3L),counts.plain);assertEquals(Map.of(10,7L),counts.notes);
    }
    @Test public void missingContainersMeanUnknownRatherThanZeroOwned()
    {
        SupplySnapshot counts=SupplySnapshot.read(id->definition(-1,-1,-1),null,container());
        assertFalse(counts.known);assertTrue(counts.plain.isEmpty());
    }
}
