package com.mahghuuuls.jassintegrations.integration.crafttweaker;

import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import crafttweaker.CraftTweakerAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class of the CraftTweaker integration; created by {@link IntegrationGate} by name. Registers the
 * script class {@code mods.jassintegrations.Stamina} itself instead of through CraftTweaker's own scan, so
 * the config switch can leave it out.
 */
public final class CraftTweakerIntegration implements IntegrationGate.Integration {

    @Override
    public List<String> compatibilityProblems() {
        List<String> problems = new ArrayList<>();
        if (!IntegrationGate.hasMethod("crafttweaker.CraftTweakerAPI", "registerClass", "java.lang.Class")) {
            problems.add("CraftTweakerAPI.registerClass(Class) not found");
        }
        if (!IntegrationGate.hasMethod("crafttweaker.api.entity.IEntity", "getInternal")) {
            problems.add("IEntity.getInternal() not found");
        }
        if (!IntegrationGate.hasMethod("crafttweaker.CraftTweakerAPI", "logWarning", "java.lang.String")) {
            problems.add("CraftTweakerAPI.logWarning(String) not found");
        }
        for (String type : new String[] {"crafttweaker.api.player.IPlayer", "stanhebben.zenscript.annotations.ZenClass",
                "stanhebben.zenscript.annotations.ZenMethod", "stanhebben.zenscript.annotations.Optional"}) {
            if (!IntegrationGate.hasClass(type)) {
                problems.add(type + " not found");
            }
        }
        return problems;
    }

    /**
     * Runs during pre-initialization. CraftTweaker compiles its main scripts at the registry events, after
     * every mod's pre-initialization, so the class is there for them; {@code #loader preinit} scripts run
     * earlier and cannot use it.
     */
    @Override
    public void start() {
        CraftTweakerAPI.registerClass(StaminaScripts.class);
    }
}
