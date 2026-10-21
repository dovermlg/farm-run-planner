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
    @Test public void capturePreservesBackpackPositionsAndNoteFormsWithoutTreatingPlaceholdersAsItems()
    {
        Map<Integer,ItemComposition> definitions=Map.of(10,definition(-1,-1,-1),11,definition(799,10,-1),12,definition(-1,-1,14401));
        SupplySnapshot counts=SupplySnapshot.read(definitions::get,container(new Item(-1,0),new Item(10,2),new Item(11,7),new Item(12,20)),container());
        assertTrue(counts.backpack.captured);assertEquals(28,counts.backpack.slots.size());assertNull(counts.backpack.slots.get(0));
        assertEquals(10,counts.backpack.slots.get(1).item);assertFalse(counts.backpack.slots.get(1).noted);assertEquals(2,counts.backpack.slots.get(1).quantity);
        assertEquals(10,counts.backpack.slots.get(2).item);assertTrue(counts.backpack.slots.get(2).noted);assertEquals(7,counts.backpack.slots.get(2).quantity);
        assertNull(counts.backpack.slots.get(3));assertFalse(SupplySnapshot.UNKNOWN.backpack.captured);
        assertTrue(SupplySnapshot.read(definitions::get,container(),container()).backpack.captured);
    }
    @Test public void preservesWornSlotsAndDistinguishesMovingAnItemFromInventoryToEquipment()
    {
        Item[] slots=new Item[14];java.util.Arrays.fill(slots,new Item(-1,0));
        slots[net.runelite.api.EquipmentInventorySlot.RING.getSlotIdx()]=new Item(10,1);
        slots[net.runelite.api.EquipmentInventorySlot.AMMO.getSlotIdx()]=new Item(20,100);
        slots[net.runelite.api.EquipmentInventorySlot.WEAPON.getSlotIdx()]=new Item(30,250);
        SupplySnapshot equipped=SupplySnapshot.read(id->definition(-1,-1,-1),container(),container(slots));
        SupplySnapshot carried=SupplySnapshot.read(id->definition(-1,-1,-1),container(new Item(10,1),new Item(20,100),new Item(30,250)),container());
        assertEquals(carried.plain,equipped.plain);assertFalse(carried.worn.equals(equipped.worn));
        assertEquals(10,equipped.worn.get(net.runelite.api.EquipmentInventorySlot.RING).item);
        assertEquals(100,equipped.worn.get(net.runelite.api.EquipmentInventorySlot.AMMO).quantity);
        assertEquals(250,equipped.worn.get(net.runelite.api.EquipmentInventorySlot.WEAPON).quantity);
    }
}
