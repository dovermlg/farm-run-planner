package com.classictreerun;

import java.util.ArrayList;
import java.util.List;

/** Bounded visual inventory; slots describe supplies, not required positions in the live backpack. */
final class PackingLayout
{
    static final class Cell
    {
        final int requirement,index,item,quantity;
        final boolean stack;
        Cell(int requirement,int index,int item,int quantity,boolean stack)
        {this.requirement=requirement;this.index=index;this.item=item;this.quantity=quantity;this.stack=stack;}
        boolean packed(long carried){return carried>=(stack?quantity:index+1L);}
    }
    final List<Cell> cells;
    final long total;
    private PackingLayout(List<Cell> cells,long total){this.cells=List.copyOf(cells);this.total=total;}
    static PackingLayout build(SupplyPlan plan,InventoryItemData data)
    {
        List<Cell> cells=new ArrayList<>();long total=0;
        for(int r=0;r<plan.requirements.size();r++)
        {
            SupplyPlan.Requirement row=plan.requirements.get(r);
            if(plan.equipment.supplies(row))continue;
            InventoryItemData.ItemInfo info=data.get(row.item);
            boolean stack=row.notedOnly || info.stackable;
            int count=stack?1:row.quantity;
            int icon=row.notedOnly && info.noteItem>0?info.noteItem:row.item;
            int shown=Math.min(count,28-cells.size());
            for(int i=0;i<shown;i++)cells.add(new Cell(r,i,icon,stack?row.quantity:1,stack));
            total+=count;
        }
        return new PackingLayout(cells,total);
    }
}
