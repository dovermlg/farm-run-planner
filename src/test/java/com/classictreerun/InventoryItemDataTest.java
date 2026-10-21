package com.classictreerun;

import java.lang.reflect.Proxy;
import java.util.Set;
import net.runelite.api.ItemComposition;
import org.junit.Test;
import static org.junit.Assert.*;

public class InventoryItemDataTest
{
    @Test public void readsActualPublicStackableAndNoteLinkFlagsAndRequiresCoverage()
    {
        InventoryItemData data=InventoryItemData.read(Set.of(1,2),id->(ItemComposition)Proxy.newProxyInstance(
            getClass().getClassLoader(),new Class<?>[]{ItemComposition.class},(p,m,a)->{
                if(m.getName().equals("isStackable"))return id==1;
                if(m.getName().equals("getLinkedNoteId"))return id==2?20:-1;
                if(m.getName().equals("getName"))return "Item "+id;
                throw new AssertionError(m.getName());
            }));
        assertTrue(data.get(1).stackable);assertFalse(data.get(1).noteable);
        assertFalse(data.get(2).stackable);assertTrue(data.get(2).noteable);
        assertEquals("Item 2",data.get(2).name);assertEquals(20,data.get(2).noteItem);
        assertTrue(data.covers(Set.of(1,2)));assertFalse(data.covers(Set.of(3)));
        assertThrows(IllegalArgumentException.class,()->data.get(3));
        assertEquals(data.fingerprint(Set.of(1,2)),InventoryItemData.EMPTY.merge(data).fingerprint(Set.of(2,1)));
    }
}
