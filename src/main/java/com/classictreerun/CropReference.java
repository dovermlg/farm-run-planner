package com.classictreerun;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Versioned, attributed data tables. No runtime use of private RuneLite farming APIs. */
final class CropReference
{
    static final class Crop
    {
        final String produce, state;
        final int stage, stages, tickMinutes;
        Crop(String produce, String state, int stage, int stages, int tickMinutes)
        { this.produce=produce; this.state=state; this.stage=stage; this.stages=stages; this.tickMinutes=tickMinutes; }
        boolean planted() { return !"WEEDS".equals(produce) && !"SCARECROW".equals(produce); }
        boolean healthy() { return planted() && !"DEAD".equals(state) && !"DISEASED".equals(state); }
        int remaining() { return Math.max(0, stages - 1 - stage); }
        boolean ready() { return healthy() && ("HARVESTABLE".equals(state) ? tickMinutes == 0 || remaining() == 0 : remaining() == 0); }
        boolean anima() { return "ATTAS".equals(produce) || "IASOR".equals(produce) || "KRONOS".equals(produce); }
        String label() { return produce.toLowerCase(java.util.Locale.ROOT).replace('_',' '); }
    }
    static final class Range
    {
        final int from, to; final Crop crop;
        Range(int from,int to,Crop crop) { this.from=from;this.to=to;this.crop=crop; }
    }
    static final class Binding
    {
        final Patch patch; final int sourceRegion, varbit; final String implementation; final Set<Integer> regions;
        Binding(Patch patch,int sourceRegion,int varbit,String implementation,Set<Integer> regions)
        { this.patch=patch;this.sourceRegion=sourceRegion;this.varbit=varbit;this.implementation=implementation;this.regions=regions; }
        boolean applies(int x,int y,int plane)
        {
            int region = ((x >> 6) << 8) | (y >> 6);
            if (!regions.contains(region)) { return false; }
            // Distinct payloads share these regions. Boundary rules are attributed in THIRD-PARTY-NOTICES.md.
            if (sourceRegion == 11062 && x >= 2816 && y < 3456) { return x < 2840 && y >= 3440 && plane == 0; }
            if (sourceRegion == 11317) { return x >= 2840 || y < 3440 || plane == 1; }
            if (sourceRegion == 12083) { return y >= 3272; }
            if (sourceRegion == 12082) { return y < 3272; }
            if (sourceRegion == 14651)
            {
                if (plane != 0 || x == 3753 && y >= 3868 && y <= 3870) { return false; }
                if ((x == 3729 || x == 3728 || x == 3747 || x == 3746) && y >= 3830 && y <= 3832) { return false; }
            }
            return true;
        }
    }
    private final Map<String,List<Range>> states = new HashMap<>();
    private final Map<String,Set<Integer>> stumps = new HashMap<>();
    private final Map<Integer,List<Binding>> regions = new HashMap<>();
    final Map<Patch,Binding> bindings = new EnumMap<>(Patch.class);
    CropReference()
    {
        for (String[] row : rows("crop-states.csv"))
        {
            Crop crop = new Crop(row[3],row[4],Integer.parseInt(row[5]),Integer.parseInt(row[6]),Integer.parseInt(row[7]));
            states.computeIfAbsent(row[0], key -> new ArrayList<>()).add(new Range(Integer.parseInt(row[1]),Integer.parseInt(row[2]),crop));
        }
        for (String[] row : rows("patch-bindings.csv"))
        {
            Set<Integer> regions = new HashSet<>(); for (String id : row[4].split(";")) { regions.add(Integer.parseInt(id)); }
            Patch patch=Patch.valueOf(row[0]);
            Binding binding = new Binding(patch,Integer.parseInt(row[1]),Integer.parseInt(row[2]),row[3],regions);
            bindings.put(patch,binding);
            for (int region : regions) { this.regions.computeIfAbsent(region, key -> new ArrayList<>()).add(binding); }
        }
        for (String[] row : rows("crop-stumps.csv"))
        { stumps.computeIfAbsent(row[0], key -> new HashSet<>()).add(Integer.parseInt(row[1])); }
        if (bindings.size() != Patch.values().length) { throw new IllegalStateException("Incomplete crop reference"); }
    }
    Crop decode(Patch patch,int raw)
    {
        Binding binding=bindings.get(patch);
        if (raw < 0 || raw > 65535) { return null; }
        List<Range> ranges = states.get(binding.implementation);
        int low=0, high=ranges.size()-1;
        while (low<=high)
        {
            int middle=(low+high)>>>1; Range range=ranges.get(middle);
            if (raw<range.from) { high=middle-1; }
            else if (raw>range.to) { low=middle+1; }
            else { return range.crop; }
        }
        return null;
    }
    boolean isStump(Patch patch,int raw)
    { return stumps.getOrDefault(bindings.get(patch).implementation,Collections.emptySet()).contains(raw); }
    List<Binding> nearby(int x,int y)
    { return regions.getOrDefault(((x>>6)<<8)|(y>>6),Collections.emptyList()); }
    private static List<String[]> rows(String resource)
    {
        List<String[]> result=new ArrayList<>();
        try (InputStream stream=CropReference.class.getResourceAsStream(resource))
        {
            if (stream==null) { throw new IllegalStateException("Missing farming reference: "+resource); }
            try (BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8)))
            {
                String line; while ((line=reader.readLine())!=null) { if (!line.isEmpty() && !line.startsWith("#")) { result.add(line.split(",",-1)); } }
            }
        }
        catch (IOException error) { throw new IllegalStateException("Couldn't read farming reference",error); }
        return result;
    }
}
