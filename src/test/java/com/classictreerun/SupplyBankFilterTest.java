package com.classictreerun;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplyBankFilterTest
{
    @Test public void selectedSuppliesIncludePlantableSaplingsAndExcludeUnrelatedItems()
    {
        SupplyBankFilter filter=new SupplyBankFilter();
        filter.start(SupplyPlan.build(new ClassicTreeRunConfig() { },List.of(Patch.LUMBRIDGE_TREE)));
        assertTrue(filter.active());assertTrue(filter.contains(ItemID.PLANTPOT_OAK_SAPLING));
        assertFalse(filter.contains(ItemID.RANARR_SEED));
    }
    @Test public void replacementAndClearCannotRetainItemsFromThePreviousSetup()
    {
        SupplyBankFilter filter=new SupplyBankFilter();Set<Integer> source=new HashSet<>(Set.of(ItemID.RANARR_SEED));
        filter.start(source);source.clear();assertTrue(filter.contains(ItemID.RANARR_SEED));
        filter.start(Set.of(ItemID.COINS));assertFalse(filter.contains(ItemID.RANARR_SEED));
        assertTrue(filter.contains(ItemID.COINS));filter.clear();assertFalse(filter.active());assertFalse(filter.contains(ItemID.COINS));
        filter.start(Set.of());assertFalse(filter.active());
    }
}
