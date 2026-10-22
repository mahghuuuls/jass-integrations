package com.mahghuuuls.jassintegrations.integration.jafs;

import com.mahghuuuls.jafs.api.IPropertyApplier;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps each player's JAFS stamina sums. JAFS calls a property's applier with the current flat and
 * percent sums whenever they may have changed (meal, expiry, clear, reload, login, respawn); each
 * call replaces that property's sums, and a real change asks JASS to rebuild the player's profile.
 * JAFS saves the foods, so nothing here is saved.
 */
final class JafsStaminaApplier {

    /** Written on the server thread; read by the provider. Removed at logout. */
    private final Map<UUID, StaminaBonusTotals> sums = new ConcurrentHashMap<>();

    /** The applier JAFS calls for one property. */
    IPropertyApplier forProperty(JafsIntegration.Property property) {
        return (player, flatSum, percentSum) -> {
            StaminaBonusTotals now = update(player.getUniqueID(), property, flatSum, percentSum);
            if (now == null) {
                return;
            }
            JassApi.markModifierCacheDirty(player);
            DebugLog.debug(() -> "JAFS: " + player.getName() + " " + property.id() + " flat=" + flatSum
                    + " percent=" + percentSum + "; stamina bonuses now " + now);
        };
    }

    /**
     * Replaces one property's sums for a player.
     *
     * @return the player's new totals, or {@code null} when they did not change
     */
    StaminaBonusTotals update(UUID player, JafsIntegration.Property property, double flatSum, double percentSum) {
        StaminaBonusTotals before = sums(player);
        StaminaBonusTotals after = before.with(property.flat, flatSum);
        if (property.percent != null) {
            after = after.with(property.percent, percentSum);
        }
        if (after.equals(before)) {
            return null;
        }
        if (after.equals(StaminaBonusTotals.NONE)) {
            sums.remove(player);
        } else {
            sums.put(player, after);
        }
        return after;
    }

    /** The player's totals; {@link StaminaBonusTotals#NONE} when no food gives a stamina bonus. */
    StaminaBonusTotals sums(UUID player) {
        StaminaBonusTotals totals = sums.get(player);
        return totals == null ? StaminaBonusTotals.NONE : totals;
    }

    void forget(UUID player) {
        sums.remove(player);
    }

    /** JAFS applies the foods again at the next login. */
    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        forget(event.player.getUniqueID());
    }
}
