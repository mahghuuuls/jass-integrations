package com.mahghuuuls.jassintegrations.integration.setworks;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Each player's applied Setworks stamina effects, by effect instance id. Setworks applies an instance
 * when its bonus becomes active and withdraws it when the bonus ends, after committing the new state
 * that {@code isActive} reads. At login, respawn and dimension change it rebuilds without withdrawing
 * and applies the then-active instances again under the same ids, so one tick after a respawn or
 * dimension change the instances whose bonus is no longer active are dropped here. The JASS profile is
 * rebuilt whenever a player's totals change.
 */
final class SetworksStaminaEffects {

    /** One applied effect: the stamina bonus it gives, its amount, and its bonus id ({@code ""} when unknown). */
    static final class Applied {
        final StaminaBonus bonus;
        final double amount;
        final String bonusId;

        Applied(StaminaBonus bonus, double amount, String bonusId) {
            this.bonus = bonus;
            this.amount = amount;
            this.bonusId = bonusId;
        }
    }

    /** Written on the server thread; read by the provider. Removed at logout. */
    private final Map<UUID, Map<String, Applied>> applied = new ConcurrentHashMap<>();
    /** Players whose ended bonuses are dropped at their next tick. */
    private final Set<UUID> dropAtNextTick = ConcurrentHashMap.newKeySet();
    /** Instance ids already warned about, so each is logged once per game. */
    private final Set<String> warnedIds = ConcurrentHashMap.newKeySet();
    /** Whether Setworks reports a bonus active for a player. */
    private final BiPredicate<EntityPlayer, String> bonusActive;

    SetworksStaminaEffects(BiPredicate<EntityPlayer, String> bonusActive) {
        this.bonusActive = bonusActive;
    }

    /**
     * The bonus id in a Setworks instance id {@code <first set>/<bonus>/<index>}; ids cannot contain
     * {@code /}. {@code null} for another shape.
     */
    static String bonusIdOf(String instanceId) {
        String[] parts = instanceId.split("/", -1);
        return parts.length == 3 && !parts[1].isEmpty() ? parts[1] : null;
    }

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

    /**
     * Records an applied instance under its bonus id. An instance id of another shape is kept for the
     * player's whole session and warned about once.
     *
     * @return whether the player's totals changed
     */
    boolean record(UUID player, String instanceId, StaminaBonus bonus, double amount) {
        String bonusId = bonusIdOf(instanceId);
        if (bonusId == null && warnedIds.add(instanceId)) {
            DebugLog.warn("Setworks: effect instance id " + instanceId
                    + " has no bonus id; it counts until withdrawn or logout, also after a rebuild");
        }
        StaminaBonusTotals before = totals(player);
        applied.computeIfAbsent(player, id -> new ConcurrentHashMap<>())
                .put(instanceId, new Applied(bonus, amount, bonusId == null ? "" : bonusId));
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

    /**
     * Drops the instances whose bonus is not active; an instance without a bonus id stays.
     *
     * @return whether the player's totals changed
     */
    boolean dropInactive(UUID player, Predicate<String> active) {
        Map<String, Applied> instances = applied.get(player);
        if (instances == null) {
            return false;
        }
        StaminaBonusTotals before = totals(player);
        for (Iterator<Applied> it = instances.values().iterator(); it.hasNext(); ) {
            Applied effect = it.next();
            if (!effect.bonusId.isEmpty() && !active.test(effect.bonusId)) {
                it.remove();
            }
        }
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
        dropAtNextTick.remove(player);
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

    /** Setworks rebuilt the player's state (respawn or dimension change): drop ended bonuses at the next tick. */
    void rebuilt(UUID player) {
        dropAtNextTick.add(player);
    }

    /**
     * The player's tick ended: drops the ended bonuses when a rebuild is pending.
     *
     * @return whether the player's totals changed
     */
    boolean tickEnded(UUID player, Predicate<String> active) {
        return dropAtNextTick.remove(player) && dropInactive(player, active);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        rebuilt(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        rebuilt(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.world.isRemote
                && tickEnded(player.getUniqueID(), bonusId -> bonusActive.test(player, bonusId))) {
            changed(player, "dropped bonuses that ended across a respawn or dimension change");
        }
    }
}
