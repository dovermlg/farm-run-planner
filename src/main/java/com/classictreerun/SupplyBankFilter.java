package com.classictreerun;

import java.util.Set;

/** Client-thread state for the ordinary bank's display callback. */
final class SupplyBankFilter
{
    private Set<Integer> items=Set.of();
    private boolean armed, seenBank;
    void start(SupplyPlan plan) { items=plan.items;armed=!items.isEmpty();seenBank=false; }
    void clear() { items=Set.of();armed=false;seenBank=false; }
    boolean active() { return armed; }
    void bankVisible(boolean visible)
    {
        if(visible)seenBank=true;
        else if(seenBank)clear();
    }
    void filter(String callback,boolean bankOpen,int[] stack,int size,int itemId,boolean placeholder)
    {
        if(!armed || !bankOpen || size<1 || size>stack.length)return;
        if("bankBuildTab".equals(callback)){stack[size-1]=1;return;}
        if(!"bankSearchFilter".equals(callback) || size<2)return;
        // Preserve other search/tag filters. No placeholder stands in for an item the player owns.
        if(placeholder || !items.contains(itemId))stack[size-2]=0;
    }
}
