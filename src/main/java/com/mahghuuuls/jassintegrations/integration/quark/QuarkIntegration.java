package com.mahghuuuls.jassintegrations.integration.quark;

import com.mahghuuuls.jassintegrations.integration.IntegrationGate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entry class of the Quark row; created by {@link IntegrationGate} by name. The row is client only,
 * so nothing starts here; Quark is reached only by reflection, never through its classes.
 */
public final class QuarkIntegration implements IntegrationGate.Integration {

    /** Quark: RotN Edition's module registry and its Visual Stat Display feature. */
    public static final String MODULE_LOADER_CLASS = "vazkii.quark.base.module.ModuleLoader";
    public static final String VISUAL_STAT_DISPLAY_CLASS = "vazkii.quark.client.feature.VisualStatDisplay";

    @Override
    public List<String> compatibilityProblems() {
        List<String> problems = new ArrayList<>();
        if (!IntegrationGate.hasMethod(MODULE_LOADER_CLASS, "isFeatureEnabled", "java.lang.Class")) {
            problems.add(MODULE_LOADER_CLASS + ".isFeatureEnabled(Class) not found");
        }
        problems.addAll(IntegrationGate.missingClasses(Collections.singletonList(VISUAL_STAT_DISPLAY_CLASS)));
        return problems;
    }

    @Override
    public void start() {
    }

    @Override
    public String clientEntryClass() {
        return "com.mahghuuuls.jassintegrations.client.quark.QuarkStatRow";
    }
}
