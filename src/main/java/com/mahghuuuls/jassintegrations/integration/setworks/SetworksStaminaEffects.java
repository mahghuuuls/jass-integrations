package com.mahghuuuls.jassintegrations.integration.setworks;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Each player's applied Setworks stamina effects, by effect instance id. Setworks applies an instance
 * when its bonus becomes active and withdraws it when the bonus ends, including a bonus that ends
 * across a respawn or dimension change and every bonus at death, so the record follows Setworks' calls.
 * Logout also clears the player, beside Setworks' own withdraws then. The JASS profile is rebuilt
 * whenever a player's totals change.
 */
final class SetworksStaminaEffects {

    /** One applied effect: the stamina bonus it gives and its amount. */
    static final class Applied {
        final StaminaBonus bonus;
        final double amount;

        Applied(StaminaBonus bonus, double amount) {
            this.bonus = bonus;
            this.amount = amount;
        }
    }

    /** Written on the server thread; read by the provider. Removed at logout. */
    private final Map<UUID, Map<String, Applied>> applied = new ConcurrentHashMap<>();

    void apply(EntityPlayer player, String instanceId, StaminaBonus bonus, double amount) {
        if (record(player.getUniqueID(), instanceId, bonus, amount)) {
            changed(player, "applied " + instanceId);
        }
    }

    void withdraw(EntityPlayer player, String instanceId) {
        if (forget(player.getUniqueID(), instanceId)) {
            changed(player, "withdrew " + instanceId);
        }
    }

    /** @return whether the player's totals changed */
    boolean record(UUID player, String instanceId, StaminaBonus bonus, double amount) {
        StaminaBonusTotals before = totals(player);
        applied.computeIfAbsent(player, id -> new ConcurrentHashMap<>()).put(instanceId, new Applied(bonus, amount));
        return !totals(player).equals(before);
    }

    /** @return whether the player's totals changed */
    boolean forget(UUID player, String instanceId) {
        Map<String, Applied> instances = applied.get(player);
        if (instances == null) {
            return false;
        }
        StaminaBonusTotals before = totals(player);
        instances.remove(instanceId);
        if (instances.isEmpty()) {
            applied.remove(player, instances);
        }
        return !totals(player).equals(before);
    }

    /** The sum of the player's recorded effects. */
    StaminaBonusTotals totals(UUID player) {
        Map<String, Applied> instances = applied.get(player);
        if (instances == null) {
            return StaminaBonusTotals.NONE;
        }
        double[] values = new double[StaminaBonus.values().length];
        for (Applied effect : instances.values()) {
            values[effect.bonus.ordinal()] += effect.amount;
        }
        return new StaminaBonusTotals(values);
    }

    void forgetPlayer(UUID player) {
        applied.remove(player);
    }

    private void changed(EntityPlayer player, String what) {
        JassApi.markModifierCacheDirty(player);
        DebugLog.debug(() -> "Setworks: " + player.getName() + " " + what + "; stamina bonuses now "
                + totals(player.getUniqueID()));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        forgetPlayer(event.player.getUniqueID());
    }
}
