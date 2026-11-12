package com.mahghuuuls.jassintegrations.integration.reskillable;

import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.config.BalanceDefaults;
import com.mahghuuuls.jassintegrations.config.JassIntegrationsConfig;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusNames;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraftforge.common.config.Config;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReskillableIntegrationTest {

    private static final double EPSILON = 1e-9;

    private static ReskillableSkillRule rule(String line) {
        List<String> warnings = new ArrayList<>();
        ReskillableSkillRule rule = ReskillableSkillRule.parse(line, warnings);
        assertEquals(Collections.emptyList(), warnings, line);
        return rule;
    }

    private static String rejected(String line) {
        List<String> warnings = new ArrayList<>();
        assertNull(ReskillableSkillRule.parse(line, warnings), line);
        assertEquals(1, warnings.size(), line);
        return warnings.get(0);
    }

    @Test
    void theApiIsFoundOnTheDevelopmentReskillable() {
        assertEquals(Collections.emptyList(), new ReskillableIntegration().compatibilityProblems());
    }

    @Test
    void theDefaultRuleGivesAgilityMaximumStamina() {
        List<String> warnings = new ArrayList<>();
        List<ReskillableSkillRule> rules = ReskillableSkillRule.parseAll(Arrays.asList(BalanceDefaults.RESKILLABLE_SKILL_RULES), warnings);
        assertEquals(Collections.emptyList(), warnings);
        assertEquals(1, rules.size());
        assertEquals("reskillable:agility", rules.get(0).skillId);
        assertEquals(StaminaBonus.MAX_STAMINA_FLAT, rules.get(0).bonus);
        assertEquals(1.0, rules.get(0).amountPerLevel, EPSILON);
    }

    @Test
    void theParserAcceptsEveryCatalogueName() {
        for (Map.Entry<String, StaminaBonus> name : StaminaBonusNames.BY_NAME.entrySet()) {
            assertEquals(name.getValue(), rule("reskillable:mining=" + name.getKey() + ",2").bonus, name.getKey());
        }
        assertEquals(StaminaBonus.values().length, StaminaBonusNames.BY_NAME.size());
        assertEquals(StaminaBonus.REGENERATION_PERCENT, rule("reskillable:mining=stamina_regeneration_percent,2").bonus);
    }

    @Test
    void spacesAndNegativeAndDecimalAmountsAreRead() {
        ReskillableSkillRule rule = rule("  compatskills:gathering = weight_reduction_percent , -2.5 ");
        assertEquals("compatskills:gathering", rule.skillId);
        assertEquals(StaminaBonus.WEIGHT_REDUCTION_PERCENT, rule.bonus);
        assertEquals(-2.5, rule.amountPerLevel, EPSILON);
        assertEquals("compatskills:gathering=weight_reduction_percent,-2.5", rule.toString());
    }

    @Test
    void unreadableLinesAreRejectedWithOneWarningEach() {
        assertTrue(rejected("reskillable:agility=speed,1").contains("unknown stat \"speed\""));
        assertTrue(rejected("reskillable:agility max_stamina 1").contains("expected"));
        assertTrue(rejected("reskillable:agility=max_stamina").contains("expected"));
        assertTrue(rejected("reskillable:agility=max_stamina,1,2").contains("expected"));
        assertTrue(rejected("=max_stamina,1").contains("expected"));
        assertTrue(rejected("agility=max_stamina,1").contains("skill id"));
        assertTrue(rejected("Reskillable:Agility=max_stamina,1").contains("skill id"));
        assertTrue(rejected("reskillable:agility=max_stamina,lots").contains("finite"));
        assertTrue(rejected("reskillable:agility=max_stamina,NaN").contains("finite"));
        assertTrue(rejected("reskillable:agility=max_stamina,Infinity").contains("finite"));
        assertTrue(rejected("reskillable:agility=speed,1").startsWith(ReskillableSkillRule.OPTION + " line"));
    }

    @Test
    void parseAllSkipsBlankAndBadLinesAndKeepsTheRest() {
        List<String> warnings = new ArrayList<>();
        List<ReskillableSkillRule> rules = ReskillableSkillRule.parseAll(Arrays.asList(
                "reskillable:agility=max_stamina,1", "", "   ", "reskillable:agility=speed,1",
                "reskillable:mining=stamina_regeneration_percent,2"), warnings);
        assertEquals(1, warnings.size());
        assertEquals(2, rules.size());
        assertEquals(Collections.emptyList(), ReskillableSkillRule.parseAll(null, warnings));
    }

    @Test
    void aRuleCountsTheLevelsAboveTheFirst() {
        ReskillableSkillRule rule = rule("reskillable:agility=max_stamina,1");
        assertEquals(0.0, rule.amountAt(1), EPSILON);
        assertEquals(0.0, rule.amountAt(0), EPSILON);
        assertEquals(1.0, rule.amountAt(2), EPSILON);
        assertEquals(10.0, rule.amountAt(11), EPSILON);
        assertEquals(31.0, rule.amountAt(32), EPSILON);
    }

    @Test
    void rulesAddUpPerBonusAndAnUnknownSkillCountsAsNothing() {
        List<ReskillableSkillRule> rules = Arrays.asList(
                rule("reskillable:agility=max_stamina,1"),
                rule("reskillable:defense=max_stamina,0.5"),
                rule("reskillable:mining=stamina_regeneration_percent,2"),
                rule("compatskills:not_a_skill=max_stamina,100"));
        Map<String, Integer> levels = new HashMap<>();
        levels.put("reskillable:agility", 11);
        levels.put("reskillable:defense", 5);
        levels.put("reskillable:mining", 6);
        StaminaBonusTotals totals = ReskillableSkillRule.sum(rules, rule -> levels.get(rule.skillId));
        assertEquals(12.0, totals.get(StaminaBonus.MAX_STAMINA_FLAT), EPSILON);
        assertEquals(10.0, totals.get(StaminaBonus.REGENERATION_PERCENT), EPSILON);
        assertEquals(0.0, totals.get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON);
    }

    @Test
    void severalRulesOnOneSkillAllCount() {
        List<String> warnings = new ArrayList<>();
        List<ReskillableSkillRule> rules = ReskillableSkillRule.parseAll(Arrays.asList(
                "reskillable:agility=max_stamina,1", "reskillable:agility=stamina_efficiency,3",
                "reskillable:agility=max_stamina,0.5"), warnings);
        assertEquals(Collections.emptyList(), warnings);
        assertEquals(3, rules.size());
        StaminaBonusTotals totals = ReskillableSkillRule.sum(rules, rule -> 11);
        assertEquals(15.0, totals.get(StaminaBonus.MAX_STAMINA_FLAT), EPSILON);
        assertEquals(30.0, totals.get(StaminaBonus.STAMINA_EFFICIENCY), EPSILON);
    }

    @Test
    void theConfigCommentNamesEveryStat() throws NoSuchFieldException {
        Config.Comment comment = JassIntegrationsConfig.Reskillable.class.getField("skillRules").getAnnotation(Config.Comment.class);
        String text = String.join(" ", comment.value());
        for (String name : StaminaBonusNames.BY_NAME.keySet()) {
            assertTrue(text.matches("(?s).*(^|[ ,])" + name + "([ ,.(]|$).*"), name);
        }
    }

    @Test
    void levelOneEverywhereGivesNoBonus() {
        List<ReskillableSkillRule> rules = Arrays.asList(rule("reskillable:agility=max_stamina,1"),
                rule("reskillable:mining=stamina_regeneration_percent,2"));
        assertEquals(StaminaBonusTotals.NONE, ReskillableSkillRule.sum(rules, rule -> 1));
    }

    @Test
    void theTotalsMapThroughTheSharedMapping() {
        StaminaBonusTotals totals = ReskillableSkillRule.sum(
                Collections.singletonList(rule("reskillable:mining=stamina_regeneration_percent,2")), rule -> 6);
        StaminaContribution c = StaminaBonusMapping.map(totals);
        assertEquals(0.1, c.getRegenerationIncrease(), EPSILON);
    }
}
