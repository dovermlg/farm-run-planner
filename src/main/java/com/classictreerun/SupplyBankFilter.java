package com.classictreerun;

import java.util.Set;

/** Current setup membership, supplied to RuneLite's built-in Bank Tags display API. */
final class SupplyBankFilter
{
    private Set<Integer> items=Set.of();
    void start(SupplyPlan plan) { start(plan.items); }
    void start(Set<Integer> items) { this.items=Set.copyOf(items); }
    void clear() { items=Set.of(); }
    boolean active() { return !items.isEmpty(); }
    boolean contains(int item){return items.contains(item);}
}
