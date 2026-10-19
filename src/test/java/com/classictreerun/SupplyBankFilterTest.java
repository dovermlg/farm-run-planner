package com.classictreerun;

import java.util.List;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class SupplyBankFilterTest
{
    private SupplyBankFilter filter()
    {
        SupplyBankFilter filter=new SupplyBankFilter();
        filter.start(SupplyPlan.build(new ClassicTreeRunConfig() { },List.of(Patch.LUMBRIDGE_TREE)));
        return filter;
    }
    @Test public void compactLayoutEnablesFilteringWithAnEmptyBankSearch()
    {
        SupplyBankFilter filter=filter();int[] stack={123,0};
        filter.filter("bankBuildTab",true,stack,2,-1,false);assertArrayEquals(new int[]{123,1},stack);
        int[] wanted={-1,ItemID.PLANTPOT_OAK_SAPLING};
        filter.filter("bankSearchFilter",true,wanted,2,ItemID.PLANTPOT_OAK_SAPLING,false);
        // RuneLite's -1 means defer to vanilla filtering, which accepts an empty search.
        assertArrayEquals(new int[]{-1,ItemID.PLANTPOT_OAK_SAPLING},wanted);
    }
    @Test public void unrelatedItemsAndPlaceholdersAreHiddenAndOtherFiltersArePreserved()
    {
        SupplyBankFilter filter=filter();
        int[] unrelated={-1,ItemID.RANARR_SEED};filter.filter("bankSearchFilter",true,unrelated,2,ItemID.RANARR_SEED,false);assertEquals(0,unrelated[0]);
        int[] placeholder={-1,ItemID.PLANTPOT_OAK_SAPLING};filter.filter("bankSearchFilter",true,placeholder,2,ItemID.PLANTPOT_OAK_SAPLING,true);assertEquals(0,placeholder[0]);
        int[] rejectedByOtherFilter={0,ItemID.PLANTPOT_OAK_SAPLING};filter.filter("bankSearchFilter",true,rejectedByOtherFilter,2,ItemID.PLANTPOT_OAK_SAPLING,false);assertEquals(0,rejectedByOtherFilter[0]);
    }
    @Test public void neverFiltersSharedStorageOrUnrelatedCallbacks()
    {
        SupplyBankFilter filter=filter();int[] stack={-1,ItemID.RANARR_SEED};
        filter.filter("bankSearchFilter",false,stack,2,ItemID.RANARR_SEED,false);assertEquals(-1,stack[0]);
        filter.filter("otherCallback",true,stack,2,ItemID.RANARR_SEED,false);assertEquals(-1,stack[0]);
        filter.filter("bankSearchFilter",true,stack,0,ItemID.RANARR_SEED,false);assertEquals(-1,stack[0]);
    }
    @Test public void waitsForBankAndClearsWhenItClosesOrWhenExplicitlyCleared()
    {
        SupplyBankFilter filter=filter();filter.bankVisible(false);assertTrue(filter.active());
        filter.bankVisible(true);filter.bankVisible(false);assertFalse(filter.active());
        filter=filter();filter.clear();int[] stack={-1,ItemID.RANARR_SEED};
        filter.filter("bankSearchFilter",true,stack,2,ItemID.RANARR_SEED,false);assertEquals(-1,stack[0]);
        filter.start(SupplyPlan.build(new ClassicTreeRunConfig() { },List.of()));assertFalse(filter.active());
    }
}
