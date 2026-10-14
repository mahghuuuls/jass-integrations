package com.mahghuuuls.jassintegrations.integration.spartanshields;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jass.api.StaminaMutationResult;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

/**
 * The one owner of the shield bash decision. Spartan Shields' server bash handler asks it first
 * (through a Mixin) and runs only when it allows; a refused bash therefore has no effect at all.
 * Off until the Spartan Shields integration activates it, so a disabled or incompatible
 * integration never charges.
 */
public final class ShieldBashCharger {

    static final ResourceLocation ACTION = new ResourceLocation(Tags.MOD_ID, "shield_bash");

    private static volatile boolean active;

    /** What a bash packet is, as far as stamina is concerned. */
    enum Decision {
        /** Spartan Shields would not bash (not blocking, no Spartan shield in that hand, or on cooldown): no charge. */
        NOT_A_BASH,
        /** The bash costs nothing (cost 0): allowed, no charge. */
        FREE,
        /** The bash costs stamina: JASS decides. */
        CHARGE
    }

    private ShieldBashCharger() {
    }

    static void activate() {
        active = true;
    }

    /** True while the Spartan Shields integration runs with a usable bash hook. */
    public static boolean isActive() {
        return active;
    }

    static Decision decide(boolean blocking, boolean spartanShield, boolean onCooldown, double cost) {
        if (!blocking || !spartanShield || onCooldown) {
            return Decision.NOT_A_BASH;
        }
        return cost > 0.0 ? Decision.CHARGE : Decision.FREE;
    }

    /**
     * Called at the start of Spartan Shields' server bash handler.
     *
     * @return {@code false} when the bash must not happen at all
     */
    public static boolean allowBash(EntityPlayerMP player, EnumHand hand) {
        if (!active || player == null || hand == null) {
            return true;
        }
        try {
            ItemStack stack = player.getHeldItem(hand);
            boolean spartanShield = !stack.isEmpty() && ShieldClassifier.isShieldName(stack.getItem().getRegistryName());
            boolean onCooldown = !stack.isEmpty() && player.getCooldownTracker().hasCooldown(stack.getItem());
            double cost = Settings.current().shieldBashCost;
            switch (decide(player.isActiveItemStackBlocking(), spartanShield, onCooldown, cost)) {
                case FREE:
                    DebugLog.debug(() -> "Shield bash by " + player.getName() + ": free (bash_cost 0)");
                    return true;
                case CHARGE:
                    StaminaMutationResult result = JassApi.getStaminaService()
                            .tryDiscreteAction(player, ACTION, cost, false, stack, hand);
                    DebugLog.debug(() -> "Shield bash by " + player.getName() + ": "
                            + (result.isSuccessful() ? "charged, stamina change " + result.getActualDelta() : "refused"));
                    return result.isSuccessful();
                default:
                    return true;
            }
        } catch (RuntimeException | LinkageError e) {
            active = false;
            DebugLog.warn("Spartan Shields: shield bash cost switched off after an error: " + e);
            return true;
        }
    }
}
