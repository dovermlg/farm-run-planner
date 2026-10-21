package com.classictreerun;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;

/** Explicitly saved worn-item choices. No inventory contents or character identifiers. */
final class EquipmentSetup
{
    static final List<EquipmentInventorySlot> SLOTS=List.of(EquipmentInventorySlot.HEAD,EquipmentInventorySlot.CAPE,
        EquipmentInventorySlot.AMULET,EquipmentInventorySlot.WEAPON,EquipmentInventorySlot.BODY,EquipmentInventorySlot.SHIELD,
        EquipmentInventorySlot.LEGS,EquipmentInventorySlot.GLOVES,EquipmentInventorySlot.BOOTS,EquipmentInventorySlot.RING,EquipmentInventorySlot.AMMO);
    static final EquipmentSetup EMPTY=new EquipmentSetup(Map.of());
    static final class Entry
    {
        final int item,quantity;
        Entry(int item,int quantity)
        {
            if(item<=0 || item>1_000_000 || quantity<=0)throw new IllegalArgumentException("Invalid saved equipment item or quantity.");
            this.item=item;this.quantity=quantity;
        }
        Set<Integer> alternatives()
        {
            for(TeleportDevice device:List.of(TeleportDevice.WEALTH,TeleportDevice.DUELING,TeleportDevice.GLORY,
                TeleportDevice.SKILLS,TeleportDevice.SLAYER,TeleportDevice.DIGSITE,TeleportDevice.COMBAT))
                if(device.matches(item))return device.items();
            return Set.of(item);
        }
        @Override public boolean equals(Object other)
        {return other instanceof Entry && item==((Entry)other).item && quantity==((Entry)other).quantity;}
        @Override public int hashCode(){return 31*item+quantity;}
    }
    final Map<EquipmentInventorySlot,Entry> entries;
    EquipmentSetup(Map<EquipmentInventorySlot,Entry> entries)
    {
        if(!SLOTS.containsAll(entries.keySet()))throw new IllegalArgumentException("Unknown saved equipment slot.");
        for(Map.Entry<EquipmentInventorySlot,Entry> entry:entries.entrySet())
            if(entry.getKey()!=EquipmentInventorySlot.AMMO && entry.getKey()!=EquipmentInventorySlot.WEAPON && entry.getValue().quantity!=1)
                throw new IllegalArgumentException("Only ammunition or thrown weapons can have an equipment stack quantity.");
        this.entries=Map.copyOf(entries);
    }
    static EquipmentSetup parse(String encoded)
    {
        if(encoded==null || encoded.isEmpty())return EMPTY;
        if(encoded.length()>512)throw new IllegalArgumentException("Saved equipment is too long.");
        Map<EquipmentInventorySlot,Entry> entries=new EnumMap<>(EquipmentInventorySlot.class);
        try
        {
            for(String row:encoded.split(",",-1))
            {
                if(!row.matches("[A-Z]+:[0-9]{1,7}:[0-9]{1,10}"))throw new IllegalArgumentException("Invalid saved equipment.");
                String[] parts=row.split(":");EquipmentInventorySlot slot=EquipmentInventorySlot.valueOf(parts[0]);
                if(entries.put(slot,new Entry(Integer.parseInt(parts[1]),Integer.parseInt(parts[2])))!=null)
                    throw new IllegalArgumentException("Duplicate saved equipment slot.");
            }
            return new EquipmentSetup(entries);
        }
        catch(IllegalArgumentException error){throw new IllegalArgumentException("Invalid saved equipment: check slots, item IDs and quantities.",error);}
    }
    static EquipmentSetup read(ClassicTreeRunConfig config)
    {
        try{return parse(config.supplyEquipment());}
        catch(IllegalArgumentException error){return EMPTY;}
    }
    String encode()
    {
        List<String> rows=new java.util.ArrayList<>();
        for(EquipmentInventorySlot slot:SLOTS)
        {Entry entry=entries.get(slot);if(entry!=null)rows.add(slot.name()+":"+entry.item+":"+entry.quantity);}
        return String.join(",",rows);
    }
    Set<Integer> items()
    {Set<Integer> ids=new HashSet<>();for(Entry entry:entries.values())ids.addAll(entry.alternatives());return ids;}
    long worn(EquipmentInventorySlot slot,SupplySnapshot snapshot)
    {
        Entry expected=entries.get(slot),actual=snapshot.worn.get(slot);
        if(expected==null || actual==null)return 0;
        for(int item:expected.alternatives())if(snapshot.canonicalWorn(item)==actual.item)return actual.quantity;
        return 0;
    }
    boolean supplies(SupplyPlan.Requirement requirement)
    {
        if(!requirement.reusable)return false;
        for(Entry entry:entries.values())if(requirement.alternatives.contains(entry.item))return true;
        return false;
    }
    static String label(EquipmentInventorySlot slot)
    {String name=slot.name().toLowerCase(java.util.Locale.ROOT);return Character.toUpperCase(name.charAt(0))+name.substring(1);}
}
