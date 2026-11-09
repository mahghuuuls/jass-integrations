package com.mahghuuuls.jassintegrations.integration.statusworks;

import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The totals last given to JASS per player, for the debug line only. Keyed by UUID, which a respawn, an End exit
 * and a dimension change keep; forgotten at logout, so the first rebuild of a new login with any bonus counts as
 * a change. Uses no Minecraft or Statusworks class, so it can be tested alone.
 */
final class StatusworksTotalsLog {

    private final Map<UUID, StaminaBonusTotals> last = new ConcurrentHashMap<>();

    /** Records the totals; returns whether they differ from the last ones (none counts as no bonus). */
    boolean changed(UUID player, StaminaBonusTotals totals) {
        StaminaBonusTotals before = last.put(player, totals);
        return !totals.equals(before == null ? StaminaBonusTotals.NONE : before);
    }

    void forget(UUID player) {
        last.remove(player);
    }
}
