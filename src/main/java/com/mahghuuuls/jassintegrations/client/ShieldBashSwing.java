package com.mahghuuuls.jassintegrations.client;

import com.mahghuuuls.jass.api.client.ClientStaminaApi;
import com.mahghuuuls.jass.api.client.ClientStaminaSnapshot;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.spartanshields.ShieldBashCharger;

/**
 * Client side of the shield bash refusal: whether to skip the local arm swing. The bash request is
 * still sent, so the server refuses it and JASS records the refusal and shows its feedback. The
 * client predicts from its own configuration and last JASS snapshot; a wrong prediction only
 * changes the swing animation.
 */
public final class ShieldBashSwing {

    private static volatile boolean failed;

    private ShieldBashSwing() {
    }

    /** True when the bash costs stamina and JASS says the local player cannot start an action. */
    public static boolean suppress() {
        if (failed) {
            return false;
        }
        try {
            ClientStaminaSnapshot snapshot = ClientStaminaApi.getSnapshot();
            return suppress(ShieldBashCharger.isActive(), Settings.current().shieldBashCost,
                    snapshot.isReceived(), snapshot.canSpend());
        } catch (RuntimeException | LinkageError e) {
            failed = true;
            DebugLog.warn("Spartan Shields: shield bash swing check switched off after an error: " + e);
            return false;
        }
    }

    static boolean suppress(boolean active, double cost, boolean snapshotReceived, boolean canSpend) {
        return active && cost > 0.0 && snapshotReceived && !canSpend;
    }
}
