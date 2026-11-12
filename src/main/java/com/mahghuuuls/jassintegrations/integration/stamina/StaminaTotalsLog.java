package com.mahghuuuls.jassintegrations.integration.stamina;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The totals last given to JASS per player, for the debug line only. Keyed by UUID, which a respawn, an End exit
 * and a dimension change keep; forgotten at logout, so the first rebuild of a new login with any bonus counts as
 * a change. Uses no Minecraft or target-mod class, so it can be tested alone.
 */
public final class StaminaTotalsLog {

    private final Map<UUID, StaminaBonusTotals> last = new ConcurrentHashMap<>();

    /** Records the totals; returns whether they differ from the last ones (none counts as no bonus). */
    public boolean changed(UUID player, StaminaBonusTotals totals) {
        StaminaBonusTotals before = last.put(player, totals);
        return !totals.equals(before == null ? StaminaBonusTotals.NONE : before);
    }

    public void forget(UUID player) {
        last.remove(player);
    }
}
