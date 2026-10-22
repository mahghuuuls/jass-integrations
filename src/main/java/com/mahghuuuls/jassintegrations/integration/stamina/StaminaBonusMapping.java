package com.mahghuuuls.jassintegrations.integration.stamina;

import com.mahghuuuls.jass.api.StaminaContribution;

/**
 * The only place that knows the units and signs of the stamina bonuses: turns a player's totals into
 * one JASS contribution, for every integration that gives stamina bonuses. Flat amounts are points
 * (seconds for the delay); percent amounts are percent, and a percent below -100 counts as -100. A
 * positive amount always helps the player.
 */
public final class StaminaBonusMapping {

    private static final double PERCENT_FLOOR = -100.0;

    private StaminaBonusMapping() {
    }

    public static StaminaContribution map(StaminaBonusTotals totals) {
        StaminaContribution.Builder c = StaminaContribution.builder();
        c.flatMaximum(finite(totals.get(StaminaBonus.MAX_STAMINA_FLAT)));
        double maximumPercent = percent(totals.get(StaminaBonus.MAX_STAMINA_PERCENT));
        if (maximumPercent >= 0.0) {
            c.maximumIncrease(maximumPercent / 100.0);
        } else {
            c.maximumReduction(-maximumPercent / 100.0);
        }
        c.flatRegeneration(finite(totals.get(StaminaBonus.REGENERATION_FLAT)));
        double regenerationPercent = percent(totals.get(StaminaBonus.REGENERATION_PERCENT));
        if (regenerationPercent >= 0.0) {
            c.regenerationIncrease(regenerationPercent / 100.0);
        } else {
            c.regenerationReduction(-regenerationPercent / 100.0);
        }
        // A delay reduction shortens the delay, so its sign is the opposite of JASS's delay fields.
        c.flatDelay(-finite(totals.get(StaminaBonus.DELAY_REDUCTION_FLAT)));
        double delayPercent = percent(totals.get(StaminaBonus.DELAY_REDUCTION_PERCENT));
        if (delayPercent >= 0.0) {
            // Above 100 percent JASS counts the reduction as 100 percent.
            c.delayReduction(delayPercent / 100.0);
        } else {
            c.delayIncrease(-delayPercent / 100.0);
        }
        // A total below 0 counts as 0 in JASS, across every source, so it is not clamped here.
        c.efficiency(finite(totals.get(StaminaBonus.STAMINA_EFFICIENCY)));
        return c.build();
    }

    private static double percent(double amount) {
        return Math.max(PERCENT_FLOOR, finite(amount));
    }

    /** A sum that overflowed counts as no amount; JASS refuses values that are not finite. */
    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value;
    }
}
