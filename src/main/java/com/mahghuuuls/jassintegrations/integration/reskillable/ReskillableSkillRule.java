package com.mahghuuuls.jassintegrations.integration.reskillable;

import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusNames;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * One skill rule of the {@code reskillable.skill_rules} config list: a skill, a stamina bonus and an amount for
 * each level above the first, written {@code <skill id>=<bonus name>,<amount>}. Uses no Reskillable class, so it
 * can be tested alone.
 */
final class ReskillableSkillRule {

    static final String OPTION = "reskillable.skill_rules";
    static final String FORMAT = "<skill id>=<stat>,<amount per level>";

    private static final Pattern SKILL_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    final String skillId;
    /** The stat as written, one of {@link StaminaBonusNames#BY_NAME}. */
    final String statName;
    final StaminaBonus bonus;
    final double amountPerLevel;

    /** Rules compare by identity, so two equal config lines stay two rules and both count. */
    ReskillableSkillRule(String skillId, String statName, StaminaBonus bonus, double amountPerLevel) {
        this.skillId = skillId;
        this.statName = statName;
        this.bonus = bonus;
        this.amountPerLevel = amountPerLevel;
    }

    /** The rule as a config line, {@code <skill id>=<stat>,<amount>}. */
    @Override
    public String toString() {
        return skillId + "=" + statName + "," + amountPerLevel;
    }

    /** The rules of every line that can be read; one warning for each line that cannot. */
    static List<ReskillableSkillRule> parseAll(List<String> lines, List<String> warnings) {
        List<ReskillableSkillRule> rules = new ArrayList<>();
        if (lines != null) {
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                ReskillableSkillRule rule = parse(line, warnings);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }
        return Collections.unmodifiableList(rules);
    }

    /** One line's rule, or {@code null} with one warning when the line cannot be read. */
    static ReskillableSkillRule parse(String line, List<String> warnings) {
        String text = line.trim();
        int equals = text.indexOf('=');
        int comma = equals < 0 ? -1 : text.indexOf(',', equals + 1);
        if (equals <= 0 || comma < 0 || text.indexOf(',', comma + 1) >= 0) {
            warnings.add(rejected(line, "expected " + FORMAT));
            return null;
        }
        String skillId = text.substring(0, equals).trim();
        String name = text.substring(equals + 1, comma).trim();
        String amountText = text.substring(comma + 1).trim();
        if (!SKILL_ID.matcher(skillId).matches()) {
            warnings.add(rejected(line, "\"" + skillId + "\" is not a lower-case namespace:path skill id"));
            return null;
        }
        StaminaBonus bonus = StaminaBonusNames.BY_NAME.get(name);
        if (bonus == null) {
            warnings.add(rejected(line, "unknown stat \"" + name + "\"; use one of "
                    + String.join(", ", StaminaBonusNames.BY_NAME.keySet())));
            return null;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            amount = Double.NaN;
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            warnings.add(rejected(line, "\"" + amountText + "\" is not a finite number"));
            return null;
        }
        return new ReskillableSkillRule(skillId, name, bonus, amount);
    }

    /** This rule's amount at a skill level: the amount for each level above the first, nothing at level 1. */
    double amountAt(int level) {
        return level > 1 ? amountPerLevel * (level - 1) : 0.0;
    }

    /**
     * The totals of every rule. {@code levelOf} gives the level of a rule's skill, or {@code null} when it is not
     * known; such a rule counts as nothing. Rules on one skill and rules for one stat add up.
     */
    static StaminaBonusTotals sum(Iterable<ReskillableSkillRule> rules, Function<ReskillableSkillRule, Integer> levelOf) {
        double[] totals = new double[StaminaBonus.values().length];
        for (ReskillableSkillRule rule : rules) {
            Integer level = levelOf.apply(rule);
            if (level != null) {
                totals[rule.bonus.ordinal()] += rule.amountAt(level);
            }
        }
        return new StaminaBonusTotals(totals);
    }

    private static String rejected(String line, String problem) {
        return OPTION + " line \"" + line + "\" ignored (" + problem + ")";
    }
}
