package com.classictreerun;

import java.util.*;

/** Independent suffix-cost oracle and exhaustive travel configurations; test source only. */
final class RouteAuditOracle
{
    private RouteAuditOracle() { }

    static int[] subsetCosts(int[] entry,int[][] travel)
    {
        int n=entry.length,size=1<<n;
        int[][] tail=new int[n][size];int[] optimum=new int[size];
        for(int mask=1;mask<size;mask++)
        {
            optimum[mask]=Integer.MAX_VALUE;
            for(int first=0;first<n;first++)if((mask&(1<<first))!=0)
            {
                int remaining=mask^(1<<first),best=remaining==0?0:Integer.MAX_VALUE;
                for(int next=0;next<n;next++)if((remaining&(1<<next))!=0)
                    best=Math.min(best,travel[first][next]+tail[next][remaining]);
                tail[first][mask]=best;
                optimum[mask]=Math.min(optimum[mask],entry[first]+best);
            }
        }
        return optimum;
    }

    static int score(List<Patch> result,List<Patch> input,int[] entry,int[][] travel)
    {
        if(result.size()!=input.size() || !new HashSet<>(result).equals(new HashSet<>(input)))
            throw new AssertionError("Selected patches were lost, duplicated or added");
        if(result.isEmpty())return 0;
        int total=entry[input.indexOf(result.get(0))];
        for(int i=1;i<result.size();i++)total+=travel[input.indexOf(result.get(i-1))][input.indexOf(result.get(i))];
        return total;
    }

    /** Union-find is separate from the production breadth-first component construction. */
    static List<List<Integer>> groups(int[] entry,int[][] travel)
    {
        int n=entry.length;int[] parent=new int[n];for(int i=0;i<n;i++)parent[i]=i;
        for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)
        {
            if(travel[i][j]>entry[j] || travel[j][i]>entry[i])throw new AssertionError("Travel model has a penalty edge");
            if(travel[i][j]<entry[j] || travel[j][i]<entry[i])parent[root(parent,i)]=root(parent,j);
        }
        Map<Integer,List<Integer>> groups=new LinkedHashMap<>();
        for(int i=0;i<n;i++)groups.computeIfAbsent(root(parent,i),key->new ArrayList<>()).add(i);
        return new ArrayList<>(groups.values());
    }
    private static int root(int[] parent,int at)
    {while(parent[at]!=at)at=parent[at];return at;}

    static int[] entries(int[] input,List<Integer> indices)
    {int[] result=new int[indices.size()];for(int i=0;i<result.length;i++)result[i]=input[indices.get(i)];return result;}
    static int[][] edges(int[][] input,List<Integer> indices)
    {
        int n=indices.size();int[][] result=new int[n][n];
        for(int i=0;i<n;i++)for(int j=0;j<n;j++)result[i][j]=input[indices.get(i)][indices.get(j)];
        return result;
    }
    static List<ClassicTreeRunConfig> models()
    {
        List<ClassicTreeRunConfig> result=new ArrayList<>();
        for(ClassicTreeRunConfig.FaladorTravel a:ClassicTreeRunConfig.FaladorTravel.values())
        for(ClassicTreeRunConfig.TaverleyTravel b:ClassicTreeRunConfig.TaverleyTravel.values())
        for(ClassicTreeRunConfig.StrongholdTravel c:ClassicTreeRunConfig.StrongholdTravel.values())
        for(ClassicTreeRunConfig.CatherbyTravel d:ClassicTreeRunConfig.CatherbyTravel.values())
        for(ClassicTreeRunConfig.BrimhavenTravel e:ClassicTreeRunConfig.BrimhavenTravel.values())
        for(ClassicTreeRunConfig.CalquatTravel f:ClassicTreeRunConfig.CalquatTravel.values())
        for(ClassicTreeRunConfig.FossilTravel g:ClassicTreeRunConfig.FossilTravel.values())
        for(ClassicTreeRunConfig.GuildTravel h:ClassicTreeRunConfig.GuildTravel.values())
            result.add(new ClassicTreeRunConfig(){
                public FaladorTravel faladorTravel(){return a;} public TaverleyTravel taverleyTravel(){return b;}
                public StrongholdTravel strongholdTravel(){return c;} public CatherbyTravel catherbyTravel(){return d;}
                public BrimhavenTravel brimhavenTravel(){return e;} public CalquatTravel calquatTravel(){return f;}
                public FossilTravel fossilTravel(){return g;} public GuildTravel guildTravel(){return h;}
            });
        return result;
    }
}
