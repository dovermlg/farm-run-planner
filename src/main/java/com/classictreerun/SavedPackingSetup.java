package com.classictreerun;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Only the three setup fields are changed by the sidebar editor. */
final class SavedPackingSetup
{
    final BackpackSetup backpack;
    final EquipmentSetup equipment;
    final ClassicTreeRunConfig.SetupBankView bank;
    SavedPackingSetup(BackpackSetup backpack,EquipmentSetup equipment,ClassicTreeRunConfig.SetupBankView bank)
    {this.backpack=backpack;this.equipment=equipment;this.bank=bank;}
    static SavedPackingSetup read(ClassicTreeRunConfig config)
    {return new SavedPackingSetup(BackpackSetup.read(config),EquipmentSetup.read(config),config.supplyBankView());}
    Map<String,String> settings()
    {
        Map<String,String> values=new LinkedHashMap<>();values.put("supplyInventory",backpack.encode());
        values.put("supplyEquipment",equipment.encode());values.put("supplyBankView",bank.name());return values;
    }
    boolean same(SavedPackingSetup other){return settings().equals(other.settings());}
    Set<Integer> items(){Set<Integer> ids=new java.util.HashSet<>(backpack.items());ids.addAll(equipment.items());return ids;}
}
