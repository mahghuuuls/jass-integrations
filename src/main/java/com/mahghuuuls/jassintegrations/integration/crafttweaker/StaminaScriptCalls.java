package com.mahghuuuls.jassintegrations.integration.crafttweaker;

import com.mahghuuuls.jass.api.IStaminaService;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jass.api.StaminaMutationResult;
import com.mahghuuuls.jass.api.StaminaPublicState;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * What the script methods do, without CraftTweaker types. JASS's stamina service works on the logical
 * server only, so on the client, and for a player without a stamina session, every check answers 0 or
 * false and every change does nothing, silently. On the server a value a script must not pass is refused
 * with one warning in the script log, and nothing changes.
 */
final class StaminaScriptCalls {

    /** The cause JASS records for a direct change made by a script. */
    static final ResourceLocation SCRIPT_CAUSE = new ResourceLocation(Tags.MOD_ID, "script");

    private static final Pattern ACTION_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private final Consumer<String> warning;

    StaminaScriptCalls(Consumer<String> warning) {
        this.warning = warning;
    }

    /** The action id a script names, or {@code null} unless it is {@code namespace:path} in lower case. */
    static ResourceLocation actionId(String text) {
        return text != null && ACTION_ID.matcher(text).matches() ? new ResourceLocation(text) : null;
    }

    static boolean finite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /** Why a stamina value cannot be set, or {@code null} when it can. */
    static String valueProblem(double value) {
        return finite(value) ? null : "value " + value + " is not a finite number";
    }

    /** Why an amount or cost cannot be used, or {@code null} when it can; JASS refuses the same ones. */
    static String amountProblem(String name, double amount) {
        return finite(amount) && amount >= 0.0 ? null : name + " " + amount + " is not a finite number of at least 0";
    }

    /** Why an action id cannot be used, or {@code null} when it can. */
    static String actionProblem(String action) {
        return actionId(action) != null ? null : "action '" + action + "' is not a lower-case namespace:path id";
    }

    /** The change a direct mutation made, positive for a gain; 0 for no result. */
    static double change(StaminaMutationResult result) {
        return result == null ? 0.0 : result.getActualDelta();
    }

    /** The service when the player has a stamina session on the server, else {@code null}. */
    private static IStaminaService serviceFor(EntityPlayer player) {
        if (player == null || player.world == null || player.world.isRemote) {
            return null;
        }
        IStaminaService service = JassApi.getStaminaService();
        return service != null && service.getState(player) != null ? service : null;
    }

    private static double read(EntityPlayer player, Function<IStaminaService, Double> question) {
        IStaminaService service = serviceFor(player);
        return service == null ? 0.0 : question.apply(service);
    }

    private static double readState(EntityPlayer player, Function<StaminaPublicState, Double> question) {
        IStaminaService service = serviceFor(player);
        return service == null ? 0.0 : question.apply(service.getState(player));
    }

    double stamina(EntityPlayer player) {
        return readState(player, StaminaPublicState::getStamina);
    }

    double visibleStamina(EntityPlayer player) {
        return readState(player, StaminaPublicState::getVisibleStamina);
    }

    double debt(EntityPlayer player) {
        return readState(player, StaminaPublicState::getDebt);
    }

    double maxStamina(EntityPlayer player) {
        return read(player, s -> s.getMaximumStamina(player));
    }

    double regeneration(EntityPlayer player) {
        return read(player, s -> s.getStaminaRegeneration(player));
    }

    double regenerationDelay(EntityPlayer player) {
        return read(player, s -> s.getRegenerationDelay(player));
    }

    double efficiency(EntityPlayer player) {
        return read(player, s -> s.getStaminaEfficiency(player));
    }

    double weight(EntityPlayer player) {
        return read(player, s -> s.getEffectiveWeight(player));
    }

    boolean spendingPaused(EntityPlayer player) {
        IStaminaService service = serviceFor(player);
        return service != null && service.isSpendingGated(player);
    }

    double set(EntityPlayer player, double value) {
        IStaminaService service = serviceFor(player);
        if (service == null) {
            return 0.0;
        }
        String problem = valueProblem(value);
        if (problem != null) {
            return refused("setStamina", problem, 0.0);
        }
        return changed(player, "setStamina " + value, change(service.set(player, value, SCRIPT_CAUSE)));
    }

    double restore(EntityPlayer player, double amount) {
        IStaminaService service = serviceFor(player);
        if (service == null) {
            return 0.0;
        }
        String problem = amountProblem("amount", amount);
        if (problem != null) {
            return refused("restoreStamina", problem, 0.0);
        }
        return changed(player, "restoreStamina " + amount, change(service.restore(player, amount, SCRIPT_CAUSE)));
    }

    double remove(EntityPlayer player, double amount) {
        IStaminaService service = serviceFor(player);
        if (service == null) {
            return 0.0;
        }
        String problem = amountProblem("amount", amount);
        if (problem != null) {
            return refused("removeStamina", problem, 0.0);
        }
        return changed(player, "removeStamina " + amount, change(service.spend(player, amount, SCRIPT_CAUSE)));
    }

    boolean spend(EntityPlayer player, String action, double cost, boolean movement) {
        IStaminaService service = serviceFor(player);
        if (service == null) {
            return false;
        }
        String problem = actionProblem(action);
        if (problem == null) {
            problem = amountProblem("cost", cost);
        }
        if (problem != null) {
            return refused("spendStamina", problem, false);
        }
        ResourceLocation id = actionId(action);
        boolean allowed = service.tryDiscreteAction(player, id, cost, movement).isSuccessful();
        DebugLog.debug(() -> "CraftTweaker: " + player.getName() + " spendStamina " + id + " " + cost
                + (movement ? " (movement)" : "") + " -> " + allowed);
        return allowed;
    }

    boolean canStart(EntityPlayer player, String action) {
        IStaminaService service = serviceFor(player);
        if (service == null) {
            return false;
        }
        String problem = actionProblem(action);
        if (problem != null) {
            return refused("canStartAction", problem, false);
        }
        ResourceLocation id = actionId(action);
        boolean allowed = service.checkActionStart(player, id, ItemStack.EMPTY, null);
        DebugLog.debug(() -> "CraftTweaker: " + player.getName() + " canStartAction " + id + " -> " + allowed);
        return allowed;
    }

    private static double changed(EntityPlayer player, String call, double delta) {
        DebugLog.debug(() -> "CraftTweaker: " + player.getName() + " " + call + " -> " + delta);
        return delta;
    }

    private <T> T refused(String method, String reason, T answer) {
        warning.accept("mods.jassintegrations.Stamina." + method + ": " + reason + "; nothing changed");
        return answer;
    }
}
