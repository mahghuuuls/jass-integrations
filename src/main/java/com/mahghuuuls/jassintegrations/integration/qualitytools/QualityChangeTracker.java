package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JASS caches each player's profile and Quality Tools posts no event when it changes a modifier, so
 * every few server ticks this compares each player's totals with the last ones seen and asks JASS
 * to rebuild the profile when they differ.
 */
final class QualityChangeTracker {

    /** Quality Tools updates every 7 ticks; checking every 5 keeps a change under 1 second. */
    static final int CHECK_INTERVAL_TICKS = 5;

    /** Server thread only. Removed at logout; the entity itself is held weakly. */
    private final Map<UUID, Seen> lastSeen = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote
                || player.ticksExisted % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        StaminaBonusTotals totals = QualityAttributes.totals(player);
        Seen previous = lastSeen.put(player.getUniqueID(), new Seen(player, totals));
        if (totals.equals(Seen.totalsFor(previous, player))) {
            return;
        }
        JassApi.markModifierCacheDirty(player);
        DebugLog.debug(() -> "Quality Tools: " + player.getName() + " stamina attributes now " + totals);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastSeen.remove(event.player.getUniqueID());
    }

    /** The totals last seen on one player entity. */
    static final class Seen {
        final WeakReference<Object> entity;
        final StaminaBonusTotals totals;

        Seen(Object entity, StaminaBonusTotals totals) {
            this.entity = new WeakReference<>(entity);
            this.totals = totals;
        }

        /**
         * The totals to compare with: those seen on this same entity, or none. A respawn or a return
         * from the End creates a new entity with the same UUID and entity id, and JASS may have built
         * its profile before Quality Tools gave that entity any modifier, so it starts from none.
         */
        static StaminaBonusTotals totalsFor(Seen seen, Object entity) {
            return seen != null && seen.entity.get() == entity ? seen.totals : StaminaBonusTotals.NONE;
        }
    }
}
