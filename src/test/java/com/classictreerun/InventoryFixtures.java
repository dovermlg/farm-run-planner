package com.classictreerun;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/** Deliberate fake item definitions for offline load-planning scenarios. Production reads RuneLite. */
final class InventoryFixtures
{
    static ConfigFixture light()
    {
        ConfigFixture f=new ConfigFixture();
        for(String key:List.of("supplyTools","supplyRake","supplyDibber","supplyProtection"))f.set(key,"false");
        f.set("supplyCoins","0");f.set("supplyCompost","NONE");f.set("supplyTeleports","false");f.set("loadBank","CASTLE_WARS");f.set("packingView","LIST");f.set("supplyBankView","FILTER");
        return f;
    }
    static InventoryItemData data(ClassicTreeRunConfig config,List<Patch> route)
    {
        Map<Integer,InventoryItemData.ItemInfo> values=new LinkedHashMap<>();
        for(int id:InventoryLoadPlanner.requiredItems(config,route))values.put(id,new InventoryItemData.ItemInfo(false,true));
        for(int id:TravelSupplies.requiredItems(config,route))
            values.put(id,new InventoryItemData.ItemInfo(false,false));
        for(Patch patch:route)
            for(SupplyPlan.Requirement row:TravelSupplies.build(config,List.of(patch),false,false).requirements)
                if(!row.reusable)values.put(row.item,new InventoryItemData.ItemInfo(true,false));
        for(SupplyPlan.Requirement row:TravelSupplies.build(config,route,false,true).requirements)
            if(!row.reusable)values.put(row.item,new InventoryItemData.ItemInfo(true,false));
        for(Patch patch:route)
        {
            CropChoices.Crop crop=CropChoices.forType(patch.getType(),config);if(crop.item<0)continue;
            boolean tree=Set.of(Patch.Type.TREE,Patch.Type.FRUIT,Patch.Type.HARDWOOD,Patch.Type.CALQUAT,
                Patch.Type.CRYSTAL,Patch.Type.CELASTRUS,Patch.Type.REDWOOD,Patch.Type.SPIRIT).contains(patch.getType());
            values.put(crop.item,new InventoryItemData.ItemInfo(!tree && patch.getType()!=Patch.Type.CORAL && crop.item!=ItemID.VILLAGE_RARE_TUBER,tree));
        }
        for(int id:List.of(ItemID.COINS,ItemID.FOSSIL_NUMULITE))if(values.containsKey(id))values.put(id,new InventoryItemData.ItemInfo(true,false));
        return new InventoryItemData(values);
    }
}
