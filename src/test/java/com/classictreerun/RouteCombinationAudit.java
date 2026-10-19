package com.classictreerun;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Exhaustive local combinations plus independent global oracles. Never launches RuneLite. */
public final class RouteCombinationAudit
{
    private static final class Profile
    {
        final List<Patch> patches;final int[] entry,optimum;final int[][] travel;
        Profile(List<Patch> patches,int[] entry,int[][] travel)
        {this.patches=patches;this.entry=entry;this.travel=travel;optimum=RouteAuditOracle.subsetCosts(entry,travel);}
    }
    private RouteCombinationAudit() { }

    public static void main(String[] args) throws Exception
    {
        long started=System.nanoTime();Random random=new Random(9151728);
        List<Patch> all=List.of(Patch.values());List<ClassicTreeRunConfig> models=RouteAuditOracle.models();
        require(all.size()==99,"Catalogue size changed; review the documented audit scope");
        Map<String,Profile> profiles=new LinkedHashMap<>();int largest=0,fullRuns=0;
        for(ClassicTreeRunConfig config:models)
        {
            TravelCosts costs=new TravelCosts(all,config);int expected=0;
            for(List<Integer> group:RouteAuditOracle.groups(costs.entry,costs.travel))
            {
                largest=Math.max(largest,group.size());require(group.size()<=16,"Exact component limit exceeded");
                List<Patch> patches=new ArrayList<>();for(int index:group)patches.add(all.get(index));
                int[] entry=RouteAuditOracle.entries(costs.entry,group);int[][] travel=RouteAuditOracle.edges(costs.travel,group);
                String key=RouteState.encode(patches)+Arrays.toString(entry)+Arrays.deepToString(travel);
                Profile profile=profiles.computeIfAbsent(key,ignored->new Profile(patches,entry,travel));
                expected+=profile.optimum[profile.optimum.length-1];
            }
            List<Patch> shuffled=new ArrayList<>(all);Collections.shuffle(shuffled,random);
            TravelCosts shuffledCosts=new TravelCosts(shuffled,config);
            verify(shuffled,shuffledCosts.entry,shuffledCosts.travel,expected,"99 patches: "+TravelCosts.settings(config));fullRuns++;
        }
        System.out.println("Checked all "+models.size()+" travel configurations; largest independent group: "+largest+" patches.");

        int componentSubsets=0,permutedInputs=0;
        for(Profile profile:profiles.values())for(int mask=0;mask<profile.optimum.length;mask++)
        {
            List<Integer> indices=indices(mask,profile.patches.size());
            if(mask%19==0 && indices.size()>1){Collections.shuffle(indices,random);permutedInputs++;}
            List<Patch> patches=new ArrayList<>();for(int index:indices)patches.add(profile.patches.get(index));
            verify(patches,RouteAuditOracle.entries(profile.entry,indices),RouteAuditOracle.edges(profile.travel,indices),
                profile.optimum[mask],"component subset "+mask+": "+profile.patches);componentSubsets++;
        }
        System.out.println("Checked every subset of "+profiles.size()+" distinct travel-group profiles: "+componentSubsets+" combinations.");

        List<Patch> classic=Patch.classic();TravelCosts classicCosts=new TravelCosts(classic,new ClassicTreeRunConfig(){});
        int[] classicOracle=RouteAuditOracle.subsetCosts(classicCosts.entry,classicCosts.travel);
        for(int mask=0;mask<classicOracle.length;mask++)
        {
            List<Integer> indices=indices(mask,classic.size());List<Patch> selected=new ArrayList<>();
            for(int index:indices)selected.add(classic.get(index));
            verify(selected,RouteAuditOracle.entries(classicCosts.entry,indices),RouteAuditOracle.edges(classicCosts.travel,indices),
                classicOracle[mask],"classic subset "+mask);
        }
        System.out.println("Checked all "+classicOracle.length+" classic-patch subsets against a whole-route suffix oracle.");

        long permutations=0;
        for(int trial=0;trial<128;trial++)
        {
            List<Patch> selected=new ArrayList<>(all);Collections.shuffle(selected,random);
            selected=new ArrayList<>(selected.subList(0,trial%9));
            TravelCosts costs=new TravelCosts(selected,models.get(random.nextInt(models.size())));
            int brute=selected.isEmpty()?0:brute(costs.entry,costs.travel,0,-1);
            int oracle=RouteAuditOracle.subsetCosts(costs.entry,costs.travel)[(1<<selected.size())-1];
            require(brute==oracle,"Suffix oracle disagrees with exhaustive permutations");
            verify(selected,costs.entry,costs.travel,brute,"brute permutation case "+trial);
            long factorial=1;for(int i=2;i<=selected.size();i++)factorial*=i;permutations+=factorial;
        }
        // Whole-route oracle checks decomposition on >16 stops without assuming group independence.
        for(int trial=0;trial<12;trial++)
        {
            int n=17;int[] entry=new int[n];int[][] travel=new int[n][n];
            for(int i=0;i<n;i++)entry[i]=10+random.nextInt(100);
            for(int i=0;i<n;i++)for(int j=0;j<n;j++)travel[i][j]=i/6==j/6?random.nextInt(entry[j]+1):entry[j];
            int expected=RouteAuditOracle.subsetCosts(entry,travel)[(1<<n)-1];
            verify(all.subList(0,n),entry,travel,expected,"17-stop independent whole-route case "+trial);
        }

        Map<String,Object> report=new LinkedHashMap<>();
        report.put("result","passed");report.put("travelConfigurations",models.size());
        report.put("largestIndependentGroup",largest);report.put("distinctGroupProfiles",profiles.size());
        report.put("exhaustiveGroupSubsets",componentSubsets);report.put("permutedGroupInputs",permutedInputs);
        report.put("exhaustiveClassicSubsets",classicOracle.length);report.put("full99PatchRoutes",fullRuns);
        report.put("bruteForceCases",128);report.put("bruteForcePermutations",permutations);
        report.put("wholeRoute17StopOracleCases",12);
        report.put("all99SubsetsLiterallyEnumerated",false);
        report.put("globalGuarantee","All subsets of the current 99-patch catalogue under all current travel settings, by the independent-group proof; minimum estimated travel score only.");
        report.put("bankPlacementJointlyOptimized",false);report.put("liveTravelTimesVerified",false);
        report.put("gameAccountUsed",false);report.put("elapsedSeconds",(System.nanoTime()-started)/1_000_000_000.0);
        Path output=Path.of("build/reports/route-combinations.json");Files.createDirectories(output.getParent());
        Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");
        System.out.println("PASS: route optimality audit. Report: "+output+". No game client or account used.");
    }

    private static List<Integer> indices(int mask,int n)
    {List<Integer> result=new ArrayList<>();for(int i=0;i<n;i++)if((mask&(1<<i))!=0)result.add(i);return result;}
    private static void verify(List<Patch> patches,int[] entry,int[][] travel,int expected,String context)
    {
        List<Patch> actual=RouteOptimizer.optimize(patches,entry,travel);
        int score=RouteAuditOracle.score(actual,patches,entry,travel);
        require(score==expected,context+": expected "+expected+", got "+score);
    }
    private static int brute(int[] entry,int[][] travel,int used,int previous)
    {
        if(used==(1<<entry.length)-1)return 0;int best=Integer.MAX_VALUE;
        for(int next=0;next<entry.length;next++)if((used&(1<<next))==0)
            best=Math.min(best,(previous<0?entry[next]:travel[previous][next])+brute(entry,travel,used|(1<<next),next));
        return best;
    }
    private static void require(boolean condition,String message)
    {if(!condition)throw new AssertionError(message);}
}
