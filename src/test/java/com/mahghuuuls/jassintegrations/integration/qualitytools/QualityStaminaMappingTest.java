package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jass.api.StaminaContribution;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityStaminaMappingTest {

    private static final double EPSILON = 1e-9;

    private static QualityTotals totals(QualityAttributes.Kind kind, double amount) {
        double[] values = new double[QualityAttributes.Kind.values().length];
        values[kind.ordinal()] = amount;
        return new QualityTotals(values);
    }

    private static StaminaContribution map(QualityAttributes.Kind kind, double amount) {
        return QualityStaminaMapping.map(totals(kind, amount));
    }

    @Test
    void noTotalsGiveAnEmptyContribution() {
        assertTrue(QualityStaminaMapping.map(QualityTotals.NONE).isEmpty());
    }

    @Test
    void flatAmountsKeepTheirSign() {
        assertEquals(15.0, map(QualityAttributes.Kind.MAX_STAMINA_FLAT, 15).getFlatMaximum(), EPSILON);
        assertEquals(-15.0, map(QualityAttributes.Kind.MAX_STAMINA_FLAT, -15).getFlatMaximum(), EPSILON);
        assertEquals(5.0, map(QualityAttributes.Kind.REGENERATION_FLAT, 5).getFlatRegeneration(), EPSILON);
        assertEquals(-5.0, map(QualityAttributes.Kind.REGENERATION_FLAT, -5).getFlatRegeneration(), EPSILON);
        assertEquals(20.0, map(QualityAttributes.Kind.STAMINA_EFFICIENCY, 20).getEfficiency(), EPSILON);
        assertEquals(-20.0, map(QualityAttributes.Kind.STAMINA_EFFICIENCY, -20).getEfficiency(), EPSILON);
    }

    @Test
    void aDelayReductionShortensTheDelay() {
        // JASS base delay 0.75 s: a 0.25 s reduction leaves 0.5 s.
        assertEquals(-0.25, map(QualityAttributes.Kind.DELAY_REDUCTION_FLAT, 0.25).getFlatDelay(), EPSILON);
        assertEquals(0.25, map(QualityAttributes.Kind.DELAY_REDUCTION_FLAT, -0.25).getFlatDelay(), EPSILON);
    }

    @Test
    void positivePercentRaisesAndNegativePercentLowers() {
        StaminaContribution up = map(QualityAttributes.Kind.MAX_STAMINA_PERCENT, 10);
        assertEquals(0.10, up.getMaximumIncrease(), EPSILON);
        assertEquals(0.0, up.getMaximumReduction(), EPSILON);
        StaminaContribution down = map(QualityAttributes.Kind.MAX_STAMINA_PERCENT, -10);
        assertEquals(0.0, down.getMaximumIncrease(), EPSILON);
        assertEquals(0.10, down.getMaximumReduction(), EPSILON);


        assertEquals(0.25, map(QualityAttributes.Kind.REGENERATION_PERCENT, 25).getRegenerationIncrease(), EPSILON);
        assertEquals(0.25, map(QualityAttributes.Kind.REGENERATION_PERCENT, -25).getRegenerationReduction(), EPSILON);
    }

    @Test
    void aPositiveDelayPercentShortensTheDelay() {
        assertEquals(0.30, map(QualityAttributes.Kind.DELAY_REDUCTION_PERCENT, 30).getDelayReduction(), EPSILON);
        assertEquals(0.30, map(QualityAttributes.Kind.DELAY_REDUCTION_PERCENT, -30).getDelayIncrease(), EPSILON);
        // JASS counts a reduction above 1 as 1.
        assertEquals(2.5, map(QualityAttributes.Kind.DELAY_REDUCTION_PERCENT, 250).getDelayReduction(), EPSILON);
    }

    @Test
    void percentBelowMinusHundredCountsAsMinusHundred() {
        assertEquals(1.0, map(QualityAttributes.Kind.MAX_STAMINA_PERCENT, -250).getMaximumReduction(), EPSILON);
        assertEquals(1.0, map(QualityAttributes.Kind.REGENERATION_PERCENT, -250).getRegenerationReduction(), EPSILON);
        assertEquals(1.0, map(QualityAttributes.Kind.DELAY_REDUCTION_PERCENT, -250).getDelayIncrease(), EPSILON);
    }

    @Test
    void theMixedCardSetMapsToEveryField() {
        double[] values = new double[QualityAttributes.Kind.values().length];
        values[QualityAttributes.Kind.REGENERATION_FLAT.ordinal()] = 5;
        values[QualityAttributes.Kind.DELAY_REDUCTION_FLAT.ordinal()] = 0.25;
        values[QualityAttributes.Kind.STAMINA_EFFICIENCY.ordinal()] = 20;
        StaminaContribution c = QualityStaminaMapping.map(new QualityTotals(values));
        assertEquals(5.0, c.getFlatRegeneration(), EPSILON);
        assertEquals(-0.25, c.getFlatDelay(), EPSILON);
        assertEquals(20.0, c.getEfficiency(), EPSILON);
        assertEquals(0.0, c.getFlatMaximum(), EPSILON);
    }

    @Test
    void anOverflowedTotalCountsAsNoAmount() {
        assertTrue(map(QualityAttributes.Kind.MAX_STAMINA_FLAT, Double.POSITIVE_INFINITY).isEmpty());
        assertTrue(map(QualityAttributes.Kind.MAX_STAMINA_PERCENT, Double.NaN).isEmpty());
    }

    @Test
    void onlyOperationZeroCountsAndEachOtherOperationIsWarnedOnce() {
        List<AttributeModifier> modifiers = Arrays.asList(
                new AttributeModifier("qualitytools", 10, 0),
                new AttributeModifier("qualitytools", 5, 0),
                new AttributeModifier("qualitytools", 0.5, 1),
                new AttributeModifier("qualitytools", 0.25, 1),
                new AttributeModifier("qualitytools", 0.1, 2));
        Set<String> warned = new HashSet<>();
        List<String> warnings = new ArrayList<>();
        assertEquals(15.0, QualityStaminaMapping.usedTotal("jassintegrations.max_stamina_percent", modifiers, warned,
                warnings::add), EPSILON);
        assertEquals(2, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("jassintegrations.max_stamina_percent with operation 1 ignored"), warnings.get(0));
        QualityStaminaMapping.usedTotal("jassintegrations.max_stamina_percent", modifiers, warned, warnings::add);
        assertEquals(2, warnings.size(), "warned again on a later check");
        QualityStaminaMapping.usedTotal("jassintegrations.max_stamina_flat", modifiers, warned, warnings::add);
        assertEquals(4, warnings.size(), "another attribute is warned separately");
    }

    @Test
    void aNewEntityWithTheSameUuidStartsFromNoTotals() {
        Object player = new Object();
        Object respawned = new Object();
        QualityTotals seen = totals(QualityAttributes.Kind.MAX_STAMINA_PERCENT, 10);
        QualityChangeTracker.Seen last = new QualityChangeTracker.Seen(player, seen);
        assertEquals(seen, QualityChangeTracker.Seen.totalsFor(last, player));
        assertEquals(QualityTotals.NONE, QualityChangeTracker.Seen.totalsFor(last, respawned));
        assertEquals(QualityTotals.NONE, QualityChangeTracker.Seen.totalsFor(null, player));
    }

    @Test
    void totalsCompareByValueAndNameTheirAttributes() {
        assertEquals(totals(QualityAttributes.Kind.MAX_STAMINA_PERCENT, 10), totals(QualityAttributes.Kind.MAX_STAMINA_PERCENT, 10));
        assertFalse(totals(QualityAttributes.Kind.MAX_STAMINA_PERCENT, 10).equals(QualityTotals.NONE));
        assertEquals("max_stamina_percent=10.0", totals(QualityAttributes.Kind.MAX_STAMINA_PERCENT, 10).toString());
        assertEquals("none", QualityTotals.NONE.toString());
    }

    @Test
    void percentNamesShowAPercentSign() throws IOException {
        Properties lang = lang();
        assertEquals("% Maximum Stamina", String.format(lang.getProperty("attribute.name.jassintegrations.max_stamina_percent")));
    }

    private static Properties lang() throws IOException {
        Properties lang = new Properties();
        try (InputStream in = QualityStaminaMappingTest.class.getResourceAsStream("/assets/jassintegrations/lang/en_us.lang")) {
            assertNotNull(in, "en_us.lang is missing");
            lang.load(in);
        }
        return lang;
    }

    @Test
    void everyAttributeHasATooltipName() throws IOException {
        Properties lang = lang();

        for (QualityAttributes.Kind kind : QualityAttributes.Kind.values()) {
            assertEquals("jassintegrations." + kind.key, kind.attributeName());
            String name = lang.getProperty("attribute.name." + kind.attributeName());
            assertNotNull(name, kind.attributeName());
            // The game passes the name through String.format; a bare percent sign shows "Format error".
            assertDoesNotThrow(() -> String.format(name), name);
        }
        assertEquals(QualityAttributes.Kind.values().length, lang.stringPropertyNames().stream()
                .filter(key -> key.startsWith("attribute.name.jassintegrations.")).count());
    }
}
