package com.classictreerun;

import java.util.*;
import java.util.function.IntUnaryOperator;

/** Game-thread sampler: wait for region payloads to settle and require two matching reads. */
final class GrowthSampler
{
    static final class Sample
    {
        final Patch patch; final int raw; final boolean transition;
        Sample(Patch patch,int raw,boolean transition){this.patch=patch;this.raw=raw;this.transition=transition;}
    }
    private Set<Patch> area=EnumSet.noneOf(Patch.class);
    private final Map<Patch,Integer> previous=new EnumMap<>(Patch.class), committed=new EnumMap<>(Patch.class);
    private final Map<Patch,Integer> sent=new EnumMap<>(Patch.class);
    private int settle;
    private int reads;
    void reset(){area.clear();previous.clear();committed.clear();sent.clear();settle=0;reads=0;}
    List<Sample> read(CropReference reference,int x,int y,int plane,IntUnaryOperator varbits)
    {
        Set<Patch> current=EnumSet.noneOf(Patch.class);
        for(CropReference.Binding binding:reference.nearby(x,y))
        {if(binding.applies(x,y,plane))current.add(binding.patch);}
        if(!current.equals(area)){reset();area.addAll(current);}
        if(area.isEmpty() || settle++<3)return Collections.emptyList();
        reads++;
        List<Sample> samples=new ArrayList<>();
        for(Patch patch:area)
        {
            int raw=varbits.applyAsInt(reference.bindings.get(patch).varbit);
            Integer last=previous.put(patch,raw);
            if(last==null || last!=raw)continue;
            Integer old=committed.put(patch,raw);
            // Changed payloads immediately; unchanged observations at most once per ~5 minutes.
            if(old!=null && old==raw && reads-sent.get(patch)<500)continue;
            sent.put(patch,reads);
            samples.add(new Sample(patch,raw,old!=null && old!=raw));
        }
        return samples;
    }
}
