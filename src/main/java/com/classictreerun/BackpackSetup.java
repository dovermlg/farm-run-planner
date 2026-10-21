package com.classictreerun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** An explicitly captured backpack. Empty slots and noted forms are preserved. */
final class BackpackSetup
{
    static final BackpackSetup NONE=new BackpackSetup(false,Collections.nCopies(28,null));
    static final class Entry
    {
        final int item,quantity;
        final boolean noted;
        Entry(int item,int quantity,boolean noted)
        {
            if(item<=0 || item>1_000_000 || quantity<=0)throw new IllegalArgumentException("Invalid backpack item or quantity.");
            this.item=item;this.quantity=quantity;this.noted=noted;
        }
        boolean matches(Entry actual)
        {return actual!=null && item==actual.item && noted==actual.noted && actual.quantity>=quantity;}
        @Override public boolean equals(Object value)
        {if(!(value instanceof Entry))return false;Entry other=(Entry)value;return item==other.item && quantity==other.quantity && noted==other.noted;}
        @Override public int hashCode(){return java.util.Objects.hash(item,quantity,noted);}
    }
    final boolean captured;
    final List<Entry> slots;
    BackpackSetup(List<Entry> slots){this(true,slots);}
    private BackpackSetup(boolean captured,List<Entry> slots)
    {
        if(slots.size()!=28)throw new IllegalArgumentException("A backpack setup must have 28 slots.");
        this.captured=captured;this.slots=Collections.unmodifiableList(new ArrayList<>(slots));
    }
    static BackpackSetup read(ClassicTreeRunConfig config)
    {try{return parse(config.supplyInventory());}catch(IllegalArgumentException error){return NONE;}}
    static BackpackSetup parse(String text)
    {
        if(text==null || text.isEmpty())return NONE;
        if(text.length()>2048 || !text.startsWith("v1|"))throw new IllegalArgumentException("Invalid saved backpack format.");
        List<Entry> slots=new ArrayList<>(Collections.nCopies(28,null));
        if(text.length()>3)for(String row:text.substring(3).split(";",-1))
        {
            if(!row.matches("[0-9]{1,2}:[0-9]{1,7}:[0-9]{1,10}:[PN]"))throw new IllegalArgumentException("Invalid saved backpack slot.");
            String[] parts=row.split(":");
            try
            {
                int slot=Integer.parseInt(parts[0]);
                if(slot>=28 || slots.get(slot)!=null)throw new IllegalArgumentException("Invalid or duplicate backpack slot.");
                slots.set(slot,new Entry(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),"N".equals(parts[3])));
            }
            catch(NumberFormatException error){throw new IllegalArgumentException("Invalid backpack quantity.",error);}
        }
        return new BackpackSetup(slots);
    }
    String encode()
    {
        if(!captured)return "";
        List<String> values=new ArrayList<>();
        for(int i=0;i<slots.size();i++)
        {Entry entry=slots.get(i);if(entry!=null)values.add(i+":"+entry.item+":"+entry.quantity+":"+(entry.noted?"N":"P"));}
        return "v1|"+String.join(";",values);
    }
    Set<Integer> items()
    {Set<Integer> ids=new HashSet<>();for(Entry entry:slots)if(entry!=null)ids.add(entry.item);return ids;}
    int occupied(){return (int)slots.stream().filter(java.util.Objects::nonNull).count();}
}
