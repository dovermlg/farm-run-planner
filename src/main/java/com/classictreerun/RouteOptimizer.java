package com.classictreerun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** Exact independent travel groups; bounded fallback only for unsupported large cost models. */
final class RouteOptimizer
{
    private RouteOptimizer() { }

    static List<Patch> optimize(List<Patch> patches, int[] entry, int[][] travel)
    { return optimize(patches,entry,travel,()->false); }

    static List<Patch> optimize(List<Patch> patches, int[] entry, int[][] travel, BooleanSupplier cancelled)
    {
        checkCancelled(cancelled);
        int n = patches.size();
        if (entry.length != n || travel.length != n) { throw new IllegalArgumentException("Invalid route dimensions"); }
        if (n == 0) { return new ArrayList<>(); }
        for (int[] row : travel) { if (row.length != n) { throw new IllegalArgumentException("Invalid cost matrix"); } }
        List<List<Integer>> groups = independentGroups(entry,travel,cancelled);
        if(groups!=null && groups.size()>1)
        {
            List<Patch> result=new ArrayList<>();
            for(List<Integer> group:groups)
            {
                checkCancelled(cancelled);
                int m=group.size();int[] localEntry=new int[m];int[][] localTravel=new int[m][m];
                List<Patch> localPatches=new ArrayList<>();
                for(int i=0;i<m;i++)
                {
                    int original=group.get(i);localPatches.add(patches.get(original));localEntry[i]=entry[original];
                    for(int j=0;j<m;j++)localTravel[i][j]=travel[original][group.get(j)];
                }
                result.addAll(exactRoute(localPatches,localEntry,localTravel,cancelled));
            }
            return result;
        }
        if (n > 16) { return largeRoute(patches, entry, travel, cancelled); }
        return exactRoute(patches,entry,travel,cancelled);
    }

    /**
     * Cross-group travel must equal the next patch's entry cost, and all other edges
     * may only save cost. Any route's within-group chains can then be joined without
     * increasing cost. Solving each group exactly and concatenating is globally optimal.
     * Removing patches cannot enlarge these groups. See docs/route-optimality.md.
     */
    private static List<List<Integer>> independentGroups(int[] entry,int[][] travel,BooleanSupplier cancelled)
    {
        int n=entry.length;
        for(int i=0;i<n;i++)for(int j=0;j<n;j++)
            if(i!=j && travel[i][j]>entry[j])return null;
        List<List<Integer>> groups=new ArrayList<>();boolean[] used=new boolean[n];
        for(int start=0;start<n;start++)if(!used[start])
        {
            checkCancelled(cancelled);
            List<Integer> group=new ArrayList<>();group.add(start);used[start]=true;
            for(int at=0;at<group.size();at++)
            {
                int from=group.get(at);
                for(int to=0;to<n;to++)if(!used[to] && (travel[from][to]<entry[to] || travel[to][from]<entry[from]))
                {
                    used[to]=true;group.add(to);
                    if(group.size()>16)return null;
                }
            }
            Collections.sort(group);groups.add(group);
        }
        return groups;
    }

    private static List<Patch> exactRoute(List<Patch> patches,int[] entry,int[][] travel,BooleanSupplier cancelled)
    {
        int n=patches.size();
        int size = 1 << n;
        int[] best = new int[size * n];
        byte[] previous = new byte[size * n];
        Arrays.fill(best, Integer.MAX_VALUE);
        Arrays.fill(previous, (byte) -1);
        for (int i = 0; i < n; i++) { best[(1 << i) * n + i] = entry[i]; }
        for (int mask = 1; mask < size; mask++)
        {
            if((mask & 127)==1)checkCancelled(cancelled);
            for (int last = 0; last < n; last++)
            {
                int cost = best[mask * n + last];
                if (cost == Integer.MAX_VALUE) { continue; }
                for (int next = 0; next < n; next++)
                {
                    if ((mask & (1 << next)) != 0) { continue; }
                    int index = (mask | (1 << next)) * n + next;
                    int candidate = cost + travel[last][next];
                    if (candidate < best[index])
                    {
                        best[index] = candidate;
                        previous[index] = (byte) last;
                    }
                }
            }
        }
        int mask = size - 1;
        // Prefer the existing later endpoint on ties, preserving the default guild finish where possible.
        int last = n - 1;
        for (int i = n - 2; i >= 0; i--)
        {
            if (best[mask * n + i] < best[mask * n + last]) { last = i; }
        }
        List<Patch> result = new ArrayList<>();
        while (last >= 0)
        {
            result.add(patches.get(last));
            int prev = previous[mask * n + last];
            mask ^= 1 << last;
            last = prev;
        }
        Collections.reverse(result);
        return result;
    }

    private static List<Patch> largeRoute(List<Patch> patches, int[] entry, int[][] travel, BooleanSupplier cancelled)
    {
        int n = patches.size();
        List<Integer> best = new ArrayList<>();
        for (int i = 0; i < n; i++) { best.add(i); }
        int bestScore = score(best, entry, travel); // Never return a worse route than the user's order.
        for (int start = 0; start < n; start++)
        {
            checkCancelled(cancelled);
            boolean[] used = new boolean[n];
            List<Integer> route = new ArrayList<>();
            int last = start;
            used[last] = true;
            route.add(last);
            while (route.size() < n)
            {
                int next = -1;
                for (int j = 0; j < n; j++)
                {
                    if (!used[j] && (next < 0 || travel[last][j] < travel[last][next])) { next = j; }
                }
                used[next] = true;
                route.add(next);
                last = next;
            }
            int candidate = score(route, entry, travel);
            if (candidate < bestScore) { best = route; bestScore = candidate; }
        }
        // Score each directed relocation from its changed edges, including first-stop entry.
        for (int pass = 0; pass < Math.min(n, 100); pass++)
        {
            checkCancelled(cancelled);
            int bestFrom = -1, bestTo = -1;
            int improvedScore = bestScore;
            for (int from = 0; from < n; from++)
            {
                int stop = best.get(from);
                int before = from > 0 ? best.get(from - 1) : -1;
                int after = from + 1 < n ? best.get(from + 1) : -1;
                int removed = before < 0 ? entry[after] - entry[stop] - travel[stop][after]
                    : -travel[before][stop] + (after < 0 ? 0 : travel[before][after] - travel[stop][after]);
                for (int to = 0; to < n; to++)
                {
                    // Index the route with 'from' removed, without copying or rescoring every edge.
                    int left = to > 0 ? best.get(to - 1 < from ? to - 1 : to) : -1;
                    int right = to < n - 1 ? best.get(to < from ? to : to + 1) : -1;
                    int inserted = left < 0 ? entry[stop] + travel[stop][right] - entry[right]
                        : travel[left][stop] + (right < 0 ? 0 : travel[stop][right] - travel[left][right]);
                    int cost = bestScore + removed + inserted;
                    if (cost < improvedScore) { bestFrom = from; bestTo = to; improvedScore = cost; }
                }
            }
            if (bestFrom < 0) { break; }
            Integer stop = best.remove(bestFrom);
            best.add(bestTo, stop);
            bestScore = improvedScore;
        }
        List<Patch> result = new ArrayList<>();
        for (int index : best) { result.add(patches.get(index)); }
        return result;
    }

    private static void checkCancelled(BooleanSupplier cancelled)
    { if(cancelled.getAsBoolean())throw new CancellationException("Route preparation superseded"); }

    private static int score(List<Integer> route, int[] entry, int[][] travel)
    {
        int score = entry[route.get(0)];
        for (int i = 1; i < route.size(); i++) { score += travel[route.get(i - 1)][route.get(i)]; }
        return score;
    }
}
