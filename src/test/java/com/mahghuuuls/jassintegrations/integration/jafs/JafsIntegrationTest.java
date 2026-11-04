package com.mahghuuuls.jassintegrations.integration.jafs;

import com.mahghuuuls.jafs.api.PropertyOperation;
import com.mahghuuuls.jafs.api.PropertySpec;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaProperty;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JafsIntegrationTest {

    private static final double EPSILON = 1e-9;

    private static PropertySpec spec(StaminaProperty property) {
        return JafsIntegration.spec(property);
    }

    @Test
    void theSpecTakesItsIdAndDisplayKeyFromTheCatalogue() {
        for (StaminaProperty property : StaminaProperty.values()) {
            assertEquals(property.id(), spec(property).id());
            assertEquals(property.languageKey(), spec(property).displayNameKey());
        }
    }

    @Test
    void unitsAndColorAreTheDisplayedOnes() {
        assertEquals("", spec(StaminaProperty.MAX_STAMINA).unit());
        assertEquals("/s", spec(StaminaProperty.STAMINA_REGENERATION).unit());
        assertEquals("s", spec(StaminaProperty.REGENERATION_DELAY_REDUCTION).unit());
        assertEquals("", spec(StaminaProperty.STAMINA_EFFICIENCY).unit());
        assertEquals("", spec(StaminaProperty.SHIELD_STABILITY).unit());
        assertEquals("", spec(StaminaProperty.WEIGHT_REDUCTION).unit());
        for (StaminaProperty property : StaminaProperty.values()) {
            assertEquals("yellow", spec(property).color(), property.toString());
        }
    }

    @Test
    void storedSumsReachJassThroughTheSharedMapping() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID player = UUID.randomUUID();
        // JAFS adds two +10 percent foods before calling the applier: 60 * 1.2 = 72.
        applier.update(player, StaminaProperty.MAX_STAMINA, 0, 20);
        applier.update(player, StaminaProperty.STAMINA_REGENERATION, 5, 0);
        applier.update(player, StaminaProperty.REGENERATION_DELAY_REDUCTION, 0.25, 0);
        applier.update(player, StaminaProperty.STAMINA_EFFICIENCY, 20, 0);
        StaminaContribution c = StaminaBonusMapping.map(applier.sums(player));
        assertEquals(0.20, c.getMaximumIncrease(), EPSILON);
        assertEquals(5.0, c.getFlatRegeneration(), EPSILON);
        assertEquals(-0.25, c.getFlatDelay(), EPSILON);
        assertEquals(20.0, c.getEfficiency(), EPSILON);
    }

    @Test
    void theDevelopmentJafsReportsApiVersionOne() {
        assertTrue(JafsIntegration.apiVersion().startsWith(JafsIntegration.SUPPORTED_API_MAJOR), JafsIntegration.apiVersion());
    }

    @Test
    void theSixPropertiesHaveTheApprovedIds() {
        assertEquals("jassintegrations:max_stamina", spec(StaminaProperty.MAX_STAMINA).id());
        assertEquals("jassintegrations:stamina_regeneration", spec(StaminaProperty.STAMINA_REGENERATION).id());
        assertEquals("jassintegrations:regeneration_delay_reduction",
                spec(StaminaProperty.REGENERATION_DELAY_REDUCTION).id());
        assertEquals("jassintegrations:stamina_efficiency", spec(StaminaProperty.STAMINA_EFFICIENCY).id());
        assertEquals("jassintegrations:shield_stability", spec(StaminaProperty.SHIELD_STABILITY).id());
        assertEquals("jassintegrations:weight_reduction", spec(StaminaProperty.WEIGHT_REDUCTION).id());
        assertEquals(6, StaminaProperty.values().length);
    }

    @Test
    void operationsAndRangesMatchTheRequirements() {
        assertRange(StaminaProperty.MAX_STAMINA, PropertyOperation.FLAT, -1000, 1000);
        assertRange(StaminaProperty.MAX_STAMINA, PropertyOperation.PERCENT, -100, 1000);
        assertRange(StaminaProperty.STAMINA_REGENERATION, PropertyOperation.FLAT, -1000, 1000);
        assertRange(StaminaProperty.STAMINA_REGENERATION, PropertyOperation.PERCENT, -100, 1000);
        assertRange(StaminaProperty.REGENERATION_DELAY_REDUCTION, PropertyOperation.FLAT, -60, 60);
        assertRange(StaminaProperty.REGENERATION_DELAY_REDUCTION, PropertyOperation.PERCENT, -100, 100);
        assertRange(StaminaProperty.STAMINA_EFFICIENCY, PropertyOperation.FLAT, -1000, 1000);
        assertFalse(spec(StaminaProperty.STAMINA_EFFICIENCY).supports(PropertyOperation.PERCENT));
        assertRange(StaminaProperty.SHIELD_STABILITY, PropertyOperation.FLAT, -1000, 1000);
        assertFalse(spec(StaminaProperty.SHIELD_STABILITY).supports(PropertyOperation.PERCENT));
        assertRange(StaminaProperty.WEIGHT_REDUCTION, PropertyOperation.FLAT, -100, 100);
        assertRange(StaminaProperty.WEIGHT_REDUCTION, PropertyOperation.PERCENT, -100, 100);
    }

    private static void assertRange(StaminaProperty property, PropertyOperation operation, double min, double max) {
        PropertySpec.Range range = spec(property).range(operation);
        assertNotNull(range, property + " " + operation);
        assertEquals(min, range.min(), EPSILON, property + " " + operation);
        assertEquals(max, range.max(), EPSILON, property + " " + operation);
    }

    @Test
    void theApplierStoresReplacesAndForgetsSums() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID player = UUID.randomUUID();
        assertEquals(StaminaBonusTotals.NONE, applier.sums(player));

        StaminaBonusTotals one = applier.update(player, StaminaProperty.MAX_STAMINA, 0, 10);
        assertNotNull(one);
        assertEquals(10.0, applier.sums(player).get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON);

        assertNull(applier.update(player, StaminaProperty.MAX_STAMINA, 0, 10), "the same sums are no change");

        applier.update(player, StaminaProperty.STAMINA_EFFICIENCY, 20, 0);
        StaminaBonusTotals two = applier.update(player, StaminaProperty.MAX_STAMINA, 0, 20);
        assertNotNull(two);
        assertEquals(20.0, two.get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON, "a call replaces the sums");
        assertEquals(20.0, two.get(StaminaBonus.STAMINA_EFFICIENCY), EPSILON, "other properties are kept");

        applier.update(player, StaminaProperty.MAX_STAMINA, 0, 0);
        applier.update(player, StaminaProperty.STAMINA_EFFICIENCY, 0, 0);
        assertEquals(StaminaBonusTotals.NONE, applier.sums(player), "zero sums remove the bonus");

        applier.update(player, StaminaProperty.MAX_STAMINA, 5, 0);
        applier.forget(player);
        assertEquals(StaminaBonusTotals.NONE, applier.sums(player), "logout forgets the player");
    }

    @Test
    void aPropertyWithoutPercentIgnoresThePercentSum() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID player = UUID.randomUUID();
        StaminaBonusTotals totals = applier.update(player, StaminaProperty.STAMINA_EFFICIENCY, 20, 50);
        assertEquals(StaminaBonusTotals.NONE.with(StaminaBonus.STAMINA_EFFICIENCY, 20), totals);
    }

    @Test
    void playersAreKeptApart() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        applier.update(first, StaminaProperty.MAX_STAMINA, 0, 10);
        assertEquals(StaminaBonusTotals.NONE, applier.sums(second));
    }

    @Test
    void theApiMethodIsFoundOnTheDevelopmentJafs() {
        assertEquals(Collections.emptyList(), new JafsIntegration().compatibilityProblems());
    }
}
