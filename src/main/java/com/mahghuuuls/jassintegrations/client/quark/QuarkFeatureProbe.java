package com.mahghuuuls.jassintegrations.client.quark;

import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.quark.QuarkIntegration;

import java.lang.reflect.Method;

/**
 * Asks Quark, by reflection, whether its Visual Stat Display feature is on right now. The answer can
 * change in game through Quark's config screen, so it is asked for every tooltip. Any failure turns
 * the row off for the rest of the game with one warning.
 */
final class QuarkFeatureProbe {

    private Method isFeatureEnabled;
    private Class<?> visualStatDisplay;
    private boolean failed;

    boolean visualStatDisplayOn() {
        if (failed) {
            return false;
        }
        try {
            if (isFeatureEnabled == null) {
                ClassLoader loader = QuarkFeatureProbe.class.getClassLoader();
                visualStatDisplay = Class.forName(QuarkIntegration.VISUAL_STAT_DISPLAY_CLASS, false, loader);
                isFeatureEnabled = Class.forName(QuarkIntegration.MODULE_LOADER_CLASS, false, loader)
                        .getMethod("isFeatureEnabled", Class.class);
            }
            return Boolean.TRUE.equals(isFeatureEnabled.invoke(null, visualStatDisplay));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            failed = true;
            DebugLog.warn("Quark row: switched off, Quark's Visual Stat Display could not be checked: " + e);
            return false;
        }
    }
}
