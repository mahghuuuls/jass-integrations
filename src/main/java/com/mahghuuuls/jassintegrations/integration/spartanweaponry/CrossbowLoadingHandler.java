package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import com.mahghuuuls.jass.api.IStaminaService;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jass.api.StaminaMutationResult;
import com.mahghuuuls.jass.api.StaminaPublicState;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Server side of crossbow loading: drains stamina each tick while a player loads a Spartan
 * crossbow, stops the loading when stamina runs out, and refuses a loading start at 0 stamina.
 * The same pattern JASS uses for drawing a bow. On any error it switches itself off with one warning.
 */
public final class CrossbowLoadingHandler {

    static final ResourceLocation ACTION = new ResourceLocation(Tags.MOD_ID, "crossbow_load");

    /** A held use key retries every few ticks; one refusal per attempt is enough feedback. */
    private static final long HELD_RETRY_GAP_TICKS = 10;

    private final Map<EntityPlayer, Long> lastRefusedTick = new WeakHashMap<>();
    private boolean failed;

    /** Refuses the right click before the use starts, as JASS does for a bow at 0 stamina. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote || !CrossbowLoading.isUnloadedCrossbow(event.getItemStack())) {
            return;
        }
        if (refuseStart(player, event.getItemStack(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
        }
    }

    /** Covers loading started in other ways; logs the start of an allowed loading. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote
                || !CrossbowLoading.isUnloadedCrossbow(event.getItem())) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        // The active hand is set only after this event, so find the hand holding the item.
        EnumHand hand = player.getHeldItem(EnumHand.MAIN_HAND) == event.getItem() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        if (refuseStart(player, event.getItem(), hand)) {
            event.setCanceled(true);
            return;
        }
        DebugLog.debug(() -> "Crossbow loading by " + player.getName() + ": started");
    }

    /** The event's item is the copy taken before Spartan's finish path, so it is still unloaded. */
    @SubscribeEvent
    public void onUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!failed && event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().world.isRemote
                && CrossbowLoading.isUnloadedCrossbow(event.getItem())) {
            DebugLog.debug(() -> "Crossbow loading by " + event.getEntityLiving().getName() + ": completed");
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (failed || event.phase != TickEvent.Phase.START || player.world.isRemote || !player.isHandActive()) {
            return;
        }
        ItemStack stack = player.getActiveItemStack();
        double rate = Settings.current().crossbowLoadingDrain;
        if (rate <= 0.0 || !CrossbowLoading.isUnloadedCrossbow(stack)) {
            return;
        }
        try {
            IStaminaService service = JassApi.getStaminaService();
            if (service.getState(player) == null) {
                // No JASS session (for example a fake player): JASS treats it as exempt.
                return;
            }
            StaminaMutationResult result = service.drainContinuousTick(player, ACTION, rate, false, stack,
                    player.getActiveHand());
            if (!result.isSuccessful()) {
                // Clears the use without Spartan's finish path, so no bolt is loaded or used up.
                player.resetActiveHand();
                lastRefusedTick.put(player, player.world.getTotalWorldTime());
                DebugLog.debug(() -> "Crossbow loading by " + player.getName() + ": stopped (no stamina)");
            }
        } catch (RuntimeException | LinkageError e) {
            fail(e);
        }
    }

    /**
     * Whether a loading start must be refused. Asks JASS only at 0 stamina or below; JASS records
     * the refusal (its feedback) and exempts Creative and gated players.
     */
    private boolean refuseStart(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (failed) {
            return false;
        }
        try {
            IStaminaService service = JassApi.getStaminaService();
            StaminaPublicState state = service.getState(player);
            double rate = Settings.current().crossbowLoadingDrain;
            if (state == null || !CrossbowLoading.needsStartCheck(state.getStamina())) {
                return false;
            }
            long now = player.world.getTotalWorldTime();
            Long last = lastRefusedTick.get(player);
            if (last != null && now - last <= HELD_RETRY_GAP_TICKS) {
                lastRefusedTick.put(player, now);
                return true;
            }
            if (service.drainContinuousTick(player, ACTION, rate, false, stack, hand).isSuccessful()) {
                return false;
            }
            lastRefusedTick.put(player, now);
            DebugLog.debug(() -> "Crossbow loading by " + player.getName() + ": refused (no stamina)");
            return true;
        } catch (RuntimeException | LinkageError e) {
            fail(e);
            return false;
        }
    }

    private void fail(Throwable e) {
        failed = true;
        DebugLog.warn("Spartan Weaponry: crossbow loading cost switched off after an error: " + e);
    }
}
