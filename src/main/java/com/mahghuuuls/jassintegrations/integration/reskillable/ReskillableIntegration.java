package com.mahghuuuls.jassintegrations.integration.reskillable;

import codersafterdark.reskillable.api.ReskillableRegistries;
import codersafterdark.reskillable.api.skill.Skill;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry class of the Reskillable integration; created by {@link IntegrationGate} by name. Gives JASS stamina
 * bonuses from a player's Reskillable skill levels, by the configured skill rules.
 */
public final class ReskillableIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "reskillable");

    private static final String DATA_HANDLER = "codersafterdark.reskillable.api.data.PlayerDataHandler";
    private static final String DATA = "codersafterdark.reskillable.api.data.PlayerData";
    private static final String SKILL_INFO = "codersafterdark.reskillable.api.data.PlayerSkillInfo";
    private static final String SKILL = "codersafterdark.reskillable.api.skill.Skill";
    private static final String REGISTRIES = "codersafterdark.reskillable.api.ReskillableRegistries";
    private static final String LEVEL_UP = "codersafterdark.reskillable.api.event.LevelUpEvent$Post";

    private ReskillableStaminaProvider provider;
    private List<ReskillableSkillRule> rules = Collections.emptyList();

    @Override
    public List<String> compatibilityProblems() {
        List<String> missing = new ArrayList<>();
        if (!IntegrationGate.hasMethod(DATA_HANDLER, "get", "net.minecraft.entity.player.EntityPlayer")) {
            missing.add("PlayerDataHandler.get(EntityPlayer)");
        }
        if (!IntegrationGate.hasMethod(DATA, "getSkillInfo", SKILL)) {
            missing.add("PlayerData.getSkillInfo(Skill)");
        }
        if (!IntegrationGate.hasMethod(SKILL_INFO, "getLevel")) {
            missing.add("PlayerSkillInfo.getLevel()");
        }
        if (!IntegrationGate.hasField(REGISTRIES, "SKILLS")) {
            missing.add("ReskillableRegistries.SKILLS");
        }
        if (!IntegrationGate.hasMethod(SKILL, "isEnabled")) {
            missing.add("Skill.isEnabled()");
        }
        if (!IntegrationGate.hasClass(LEVEL_UP)) {
            missing.add("LevelUpEvent.Post");
        }
        if (missing.isEmpty()) {
            return missing;
        }
        return Collections.singletonList("Reskillable 1.13.0 or later needed (" + String.join(", ", missing) + " not found)");
    }

    /**
     * Runs during pre-initialization. Reskillable registers its skills after that, so the rules are only read here
     * and matched with the skills in {@link #postInit()}; until then the provider gives nothing.
     */
    @Override
    public void start() {
        List<String> warnings = new ArrayList<>();
        rules = ReskillableSkillRule.parseAll(Settings.current().reskillableSkillRules, warnings);
        warnings.forEach(DebugLog::warn);
        provider = new ReskillableStaminaProvider();
        JassApi.registerContextualProvider(PROVIDER_ID, provider);
        MinecraftForge.EVENT_BUS.register(provider);
    }

    /**
     * Matches each rule with its registered skill. A rule whose skill is not registered, or is switched off in
     * Reskillable's config, is skipped with a warning.
     */
    @Override
    public void postInit() {
        Map<ReskillableSkillRule, Skill> usable = new LinkedHashMap<>();
        if (ReskillableRegistries.SKILLS == null) {
            DebugLog.warn("Reskillable: no skill registry found; every skill rule is skipped");
            return;
        }
        for (ReskillableSkillRule rule : rules) {
            Skill skill = ReskillableRegistries.SKILLS.getValue(new ResourceLocation(rule.skillId));
            if (skill == null) {
                DebugLog.warn("Reskillable: skill rule for " + rule.skillId + " skipped (no such skill is registered)");
            } else if (!skill.isEnabled()) {
                DebugLog.warn("Reskillable: skill rule for " + rule.skillId + " skipped (the skill is switched off in Reskillable)");
            } else {
                usable.put(rule, skill);
            }
        }
        provider.setRules(Collections.unmodifiableMap(usable));
        DebugLog.debug(() -> "Reskillable: " + usable.size() + " skill rules in use: " + describe(usable.keySet()));
    }

    private static String describe(Iterable<ReskillableSkillRule> rules) {
        List<String> text = new ArrayList<>();
        for (ReskillableSkillRule rule : rules) {
            text.add(rule.toString());
        }
        return text.isEmpty() ? "none" : String.join(", ", text);
    }
}
