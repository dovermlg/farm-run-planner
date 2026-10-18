package com.classictreerun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Exact for <=16 stops; bounded multi-start nearest-neighbour and relocate search for larger runs. */
final class RouteOptimizer
{
    private RouteOptimizer() { }

    static List<Patch> optimize(List<Patch> patches, int[] entry, int[][] travel)
    {
        int n = patches.size();
        if (entry.length != n || travel.length != n) { throw new IllegalArgumentException("Invalid route dimensions"); }
        if (n == 0) { return new ArrayList<>(); }
        for (int[] row : travel) { if (row.length != n) { throw new IllegalArgumentException("Invalid cost matrix"); } }
        if (n > 16) { return largeRoute(patches, entry, travel); }
        int size = 1 << n;
        int[] best = new int[size * n];
        byte[] previous = new byte[size * n];
        Arrays.fill(best, Integer.MAX_VALUE);
        Arrays.fill(previous, (byte) -1);
        for (int i = 0; i < n; i++) { best[(1 << i) * n + i] = entry[i]; }
        for (int mask = 1; mask < size; mask++)
        {
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

    private static List<Patch> largeRoute(List<Patch> patches, int[] entry, int[][] travel)
    {
        int n = patches.size();
        List<Integer> best = new ArrayList<>();
        for (int i = 0; i < n; i++) { best.add(i); }
        int bestScore = score(best, entry, travel); // Never return a worse route than the user's order.
        for (int start = 0; start < n; start++)
        {
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
        // Directed relocate moves: evaluate complete costs, including first-stop entry, after each move.
        for (int pass = 0; pass < Math.min(n, 100); pass++)
        {
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

    private static int score(List<Integer> route, int[] entry, int[][] travel)
    {
        int score = entry[route.get(0)];
        for (int i = 1; i < route.size(); i++) { score += travel[route.get(i - 1)][route.get(i)]; }
        return score;
    }
}
