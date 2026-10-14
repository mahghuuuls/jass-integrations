package com.mahghuuuls.jassintegrations.client;

import com.mahghuuuls.jass.api.client.ClientStaminaApi;
import com.mahghuuuls.jass.api.client.ClientStaminaSnapshot;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.spartanweaponry.CrossbowLoading;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * At 0 stamina an unloaded Spartan crossbow does not start loading on the client. Vanilla has
 * already sent the use packet, so the server refuses it too and JASS records the refusal and
 * shows its feedback; without this the client would show a loading the server never runs.
 * Started by the client proxy only while the Spartan Weaponry integration runs.
 */
public final class CrossbowLoadingGuard implements Runnable {

    private volatile boolean failed;

    @Override
    public void run() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (failed || !event.getWorld().isRemote || !CrossbowLoading.isUnloadedCrossbow(event.getItemStack())) {
            return;
        }
        try {
            ClientStaminaSnapshot snapshot = ClientStaminaApi.getSnapshot();
            if (refuse(snapshot.isReceived(), snapshot.canSpend())) {
                event.setCanceled(true);
            }
        } catch (RuntimeException | LinkageError e) {
            failed = true;
            DebugLog.warn("Spartan Weaponry: crossbow loading client check switched off after an error: " + e);
        }
    }

    /** Loading cannot start at 0 stamina, whatever the loading drain (as for a JASS bow). */
    static boolean refuse(boolean snapshotReceived, boolean canSpend) {
        return snapshotReceived && !canSpend;
    }
}
