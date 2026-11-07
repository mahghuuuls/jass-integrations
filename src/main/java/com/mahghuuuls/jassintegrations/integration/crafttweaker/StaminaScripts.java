package com.mahghuuuls.jassintegrations.integration.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.player.IPlayer;
import net.minecraft.entity.player.EntityPlayer;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * The CraftTweaker class {@code mods.jassintegrations.Stamina}: static methods that take the player first.
 * Only translates between script types and {@link StaminaScriptCalls}, which owns the behavior.
 */
@ZenClass("mods.jassintegrations.Stamina")
public final class StaminaScripts {

    private static final StaminaScriptCalls CALLS = new StaminaScriptCalls(CraftTweakerAPI::logWarning);

    private StaminaScripts() {
    }

    private static EntityPlayer unwrap(IPlayer player) {
        Object internal = player == null ? null : player.getInternal();
        return internal instanceof EntityPlayer ? (EntityPlayer) internal : null;
    }

    @ZenMethod
    public static double getStamina(IPlayer player) {
        return CALLS.stamina(unwrap(player));
    }

    @ZenMethod
    public static double getVisibleStamina(IPlayer player) {
        return CALLS.visibleStamina(unwrap(player));
    }

    @ZenMethod
    public static double getDebt(IPlayer player) {
        return CALLS.debt(unwrap(player));
    }

    @ZenMethod
    public static double getMaxStamina(IPlayer player) {
        return CALLS.maxStamina(unwrap(player));
    }

    @ZenMethod
    public static double getRegeneration(IPlayer player) {
        return CALLS.regeneration(unwrap(player));
    }

    @ZenMethod
    public static double getRegenerationDelay(IPlayer player) {
        return CALLS.regenerationDelay(unwrap(player));
    }

    @ZenMethod
    public static double getEfficiency(IPlayer player) {
        return CALLS.efficiency(unwrap(player));
    }

    @ZenMethod
    public static double getWeight(IPlayer player) {
        return CALLS.weight(unwrap(player));
    }

    @ZenMethod
    public static boolean isSpendingPaused(IPlayer player) {
        return CALLS.spendingPaused(unwrap(player));
    }

    @ZenMethod
    public static double setStamina(IPlayer player, double value) {
        return CALLS.set(unwrap(player), value);
    }

    @ZenMethod
    public static double restoreStamina(IPlayer player, double amount) {
        return CALLS.restore(unwrap(player), amount);
    }

    @ZenMethod
    public static double removeStamina(IPlayer player, double amount) {
        return CALLS.remove(unwrap(player), amount);
    }

    @ZenMethod
    public static boolean spendStamina(IPlayer player, String action, double cost, @Optional boolean movement) {
        return CALLS.spend(unwrap(player), action, cost, movement);
    }

    @ZenMethod
    public static boolean canStartAction(IPlayer player, String action) {
        return CALLS.canStart(unwrap(player), action);
    }
}
