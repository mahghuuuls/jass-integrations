package com.mahghuuuls.jassintegrations.integration.stamina;

import com.mahghuuuls.jass.api.StaminaContribution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaminaBonusMappingTest {

    private static final double EPSILON = 1e-9;

    private static StaminaBonusTotals totals(StaminaBonus kind, double amount) {
        double[] values = new double[StaminaBonus.values().length];
        values[kind.ordinal()] = amount;
        return new StaminaBonusTotals(values);
    }

    private static StaminaContribution map(StaminaBonus kind, double amount) {
        return StaminaBonusMapping.map(totals(kind, amount));
    }

    @Test
    void noTotalsGiveAnEmptyContribution() {
        assertTrue(StaminaBonusMapping.map(StaminaBonusTotals.NONE).isEmpty());
    }

    @Test
    void flatAmountsKeepTheirSign() {
        assertEquals(15.0, map(StaminaBonus.MAX_STAMINA_FLAT, 15).getFlatMaximum(), EPSILON);
        assertEquals(-15.0, map(StaminaBonus.MAX_STAMINA_FLAT, -15).getFlatMaximum(), EPSILON);
        assertEquals(5.0, map(StaminaBonus.REGENERATION_FLAT, 5).getFlatRegeneration(), EPSILON);
        assertEquals(-5.0, map(StaminaBonus.REGENERATION_FLAT, -5).getFlatRegeneration(), EPSILON);
        assertEquals(20.0, map(StaminaBonus.STAMINA_EFFICIENCY, 20).getEfficiency(), EPSILON);
        assertEquals(-20.0, map(StaminaBonus.STAMINA_EFFICIENCY, -20).getEfficiency(), EPSILON);
    }

    @Test
    void aDelayReductionShortensTheDelay() {
        // JASS base delay 0.75 s: a 0.25 s reduction leaves 0.5 s.
        assertEquals(-0.25, map(StaminaBonus.DELAY_REDUCTION_FLAT, 0.25).getFlatDelay(), EPSILON);
        assertEquals(0.25, map(StaminaBonus.DELAY_REDUCTION_FLAT, -0.25).getFlatDelay(), EPSILON);
    }

    @Test
    void positivePercentRaisesAndNegativePercentLowers() {
        StaminaContribution up = map(StaminaBonus.MAX_STAMINA_PERCENT, 10);
        assertEquals(0.10, up.getMaximumIncrease(), EPSILON);
        assertEquals(0.0, up.getMaximumReduction(), EPSILON);
        StaminaContribution down = map(StaminaBonus.MAX_STAMINA_PERCENT, -10);
        assertEquals(0.0, down.getMaximumIncrease(), EPSILON);
        assertEquals(0.10, down.getMaximumReduction(), EPSILON);

        assertEquals(0.25, map(StaminaBonus.REGENERATION_PERCENT, 25).getRegenerationIncrease(), EPSILON);
        assertEquals(0.25, map(StaminaBonus.REGENERATION_PERCENT, -25).getRegenerationReduction(), EPSILON);
    }

    @Test
    void aPositiveDelayPercentShortensTheDelay() {
        assertEquals(0.30, map(StaminaBonus.DELAY_REDUCTION_PERCENT, 30).getDelayReduction(), EPSILON);
        assertEquals(0.30, map(StaminaBonus.DELAY_REDUCTION_PERCENT, -30).getDelayIncrease(), EPSILON);
        // JASS counts a reduction above 1 as 1.
        assertEquals(2.5, map(StaminaBonus.DELAY_REDUCTION_PERCENT, 250).getDelayReduction(), EPSILON);
    }

    @Test
    void percentBelowMinusHundredCountsAsMinusHundred() {
        assertEquals(1.0, map(StaminaBonus.MAX_STAMINA_PERCENT, -250).getMaximumReduction(), EPSILON);
        assertEquals(1.0, map(StaminaBonus.REGENERATION_PERCENT, -250).getRegenerationReduction(), EPSILON);
        assertEquals(1.0, map(StaminaBonus.DELAY_REDUCTION_PERCENT, -250).getDelayIncrease(), EPSILON);
    }

    @Test
    void theMixedCardSetMapsToEveryField() {
        double[] values = new double[StaminaBonus.values().length];
        values[StaminaBonus.REGENERATION_FLAT.ordinal()] = 5;
        values[StaminaBonus.DELAY_REDUCTION_FLAT.ordinal()] = 0.25;
        values[StaminaBonus.STAMINA_EFFICIENCY.ordinal()] = 20;
        StaminaContribution c = StaminaBonusMapping.map(new StaminaBonusTotals(values));
        assertEquals(5.0, c.getFlatRegeneration(), EPSILON);
        assertEquals(-0.25, c.getFlatDelay(), EPSILON);
        assertEquals(20.0, c.getEfficiency(), EPSILON);
        assertEquals(0.0, c.getFlatMaximum(), EPSILON);
    }

    @Test
    void anOverflowedTotalCountsAsNoAmount() {
        assertTrue(map(StaminaBonus.MAX_STAMINA_FLAT, Double.POSITIVE_INFINITY).isEmpty());
        assertTrue(map(StaminaBonus.MAX_STAMINA_PERCENT, Double.NaN).isEmpty());
    }

    @Test
    void totalsCompareByValueAndNameTheirBonuses() {
        assertEquals(totals(StaminaBonus.MAX_STAMINA_PERCENT, 10), totals(StaminaBonus.MAX_STAMINA_PERCENT, 10));
        assertFalse(totals(StaminaBonus.MAX_STAMINA_PERCENT, 10).equals(StaminaBonusTotals.NONE));
        assertEquals("max_stamina_percent=10.0", totals(StaminaBonus.MAX_STAMINA_PERCENT, 10).toString());
        assertEquals("none", StaminaBonusTotals.NONE.toString());
    }

    @Test
    void withReplacesOneBonusAndKeepsTheOthers() {
        StaminaBonusTotals one = StaminaBonusTotals.NONE.with(StaminaBonus.MAX_STAMINA_PERCENT, 10);
        StaminaBonusTotals two = one.with(StaminaBonus.STAMINA_EFFICIENCY, 20);
        assertEquals(10.0, two.get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON);
        assertEquals(20.0, two.get(StaminaBonus.STAMINA_EFFICIENCY), EPSILON);
        assertEquals(0.0, one.get(StaminaBonus.STAMINA_EFFICIENCY), EPSILON, "the original is unchanged");
        assertEquals(StaminaBonusTotals.NONE, two.with(StaminaBonus.MAX_STAMINA_PERCENT, 0).with(StaminaBonus.STAMINA_EFFICIENCY, 0));
    }
}
