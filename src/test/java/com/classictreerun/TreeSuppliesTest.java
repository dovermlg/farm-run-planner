package com.classictreerun;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class TreeSuppliesTest
{
    /** Concrete item forms from RuneLite 1.13.2 gameval ItemID, independent of the crop configuration. */
    @Test public void everyTreeUsesItsPlantableSaplingAndRejectsSeedsAndBothSeedlingForms() throws Exception
    {
        List<String> cases;
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(
            getClass().getResourceAsStream("/tree-planting-items.csv"),StandardCharsets.UTF_8)))
        { cases=reader.lines().skip(1).collect(Collectors.toList()); }

        Set<CropChoices.Crop> covered=new HashSet<>();
        Set<Integer> saplings=cases.stream().map(line->Integer.parseInt(line.split(",")[3])).collect(Collectors.toSet());
        for(String line:cases)
        {
            String[] values=line.split(",");
            String name=values[0]+" / "+values[2];
            Patch.Type type=Patch.Type.valueOf(values[0]);
            int sapling=Integer.parseInt(values[3]),seed=Integer.parseInt(values[4]);
            int seedling=Integer.parseInt(values[5]),watered=Integer.parseInt(values[6]);
            ConfigFixture fixture=new ConfigFixture();fixture.set(values[1],values[2]);
            covered.add(CropChoices.forType(type,fixture.config));
            Patch patch=Arrays.stream(Patch.values()).filter(p->p.getType()==type).findFirst().orElseThrow();
            SupplyPlan plan=SupplyPlan.build(fixture.config,List.of(patch));
            SupplyPlan.Requirement requirement=plan.requirements.stream()
                .filter(row->row.item==sapling && !row.notes).findFirst().orElseThrow();
            assertEquals(name,1,requirement.quantity);

            SupplyBankFilter filter=new SupplyBankFilter();filter.start(plan);
            for(int id:new int[]{sapling,seed,seedling,watered})
            {
                int[] stack={-1,id};
                filter.filter("bankSearchFilter",true,stack,2,id,false);
                assertEquals(name+" item "+id,id==sapling ? -1 : 0,stack[0]);
                assertEquals("Item identity must be preserved",id,stack[1]);
            }
            for(int other:saplings)if(other!=sapling)assertFalse(name+" must not substitute sapling "+other,plan.items.contains(other));

            int index=plan.requirements.indexOf(requirement);
            SupplySnapshot unplantable=new SupplySnapshot(true,Map.of(seed,10L,seedling,10L,watered,10L),Map.of(sapling,10L));
            assertEquals(name+" seeds, seedlings and notes cannot satisfy the packing row",Long.valueOf(0),plan.carried(unplantable).get(index));
            SupplySnapshot plantable=new SupplySnapshot(true,Map.of(sapling,1L),Map.of());
            assertEquals(name,Long.valueOf(1),plan.carried(plantable).get(index));
        }

        Set<CropChoices.Crop> expected=new HashSet<>();
        for(CropChoices.Choice[] family:List.<CropChoices.Choice[]>of(CropChoices.Tree.values(),CropChoices.Fruit.values(),
            CropChoices.Hardwood.values(),CropChoices.Calquat.values(),CropChoices.Crystal.values(),
            CropChoices.Celastrus.values(),CropChoices.Redwood.values(),CropChoices.Spirit.values()))
            for(CropChoices.Choice choice:family)if(choice.crop().item!=-1)expected.add(choice.crop());
        assertEquals("Every available tree variant must be covered by this item-form fixture",expected,covered);
        assertEquals(23,covered.size());
    }
}
