package com.mahghuuuls.jassintegrations.integration.jafs;

import com.mahghuuuls.jafs.api.PropertyOperation;
import com.mahghuuuls.jafs.api.PropertySpec;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JafsIntegrationTest {

    private static final double EPSILON = 1e-9;

    private static PropertySpec spec(JafsIntegration.Property property) {
        return property.spec();
    }

    @Test
    void unitsAndColorAreTheDisplayedOnes() {
        assertEquals("", spec(JafsIntegration.Property.MAX_STAMINA).unit());
        assertEquals("/s", spec(JafsIntegration.Property.STAMINA_REGENERATION).unit());
        assertEquals("s", spec(JafsIntegration.Property.REGENERATION_DELAY_REDUCTION).unit());
        assertEquals("", spec(JafsIntegration.Property.STAMINA_EFFICIENCY).unit());
        for (JafsIntegration.Property property : JafsIntegration.Property.values()) {
            assertEquals("yellow", spec(property).color(), property.toString());
        }
    }

    @Test
    void storedSumsReachJassThroughTheSharedMapping() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID player = UUID.randomUUID();
        // JAFS adds two +10 percent foods before calling the applier: 60 * 1.2 = 72.
        applier.update(player, JafsIntegration.Property.MAX_STAMINA, 0, 20);
        applier.update(player, JafsIntegration.Property.STAMINA_REGENERATION, 5, 0);
        applier.update(player, JafsIntegration.Property.REGENERATION_DELAY_REDUCTION, 0.25, 0);
        applier.update(player, JafsIntegration.Property.STAMINA_EFFICIENCY, 20, 0);
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
    void theFourPropertiesHaveTheApprovedIds() {
        assertEquals("jassintegrations:max_stamina", spec(JafsIntegration.Property.MAX_STAMINA).id());
        assertEquals("jassintegrations:stamina_regeneration", spec(JafsIntegration.Property.STAMINA_REGENERATION).id());
        assertEquals("jassintegrations:regeneration_delay_reduction",
                spec(JafsIntegration.Property.REGENERATION_DELAY_REDUCTION).id());
        assertEquals("jassintegrations:stamina_efficiency", spec(JafsIntegration.Property.STAMINA_EFFICIENCY).id());
        assertEquals(4, JafsIntegration.Property.values().length);
    }

    @Test
    void operationsAndRangesMatchTheRequirements() {
        assertRange(JafsIntegration.Property.MAX_STAMINA, PropertyOperation.FLAT, -1000, 1000);
        assertRange(JafsIntegration.Property.MAX_STAMINA, PropertyOperation.PERCENT, -100, 1000);
        assertRange(JafsIntegration.Property.STAMINA_REGENERATION, PropertyOperation.FLAT, -1000, 1000);
        assertRange(JafsIntegration.Property.STAMINA_REGENERATION, PropertyOperation.PERCENT, -100, 1000);
        assertRange(JafsIntegration.Property.REGENERATION_DELAY_REDUCTION, PropertyOperation.FLAT, -60, 60);
        assertRange(JafsIntegration.Property.REGENERATION_DELAY_REDUCTION, PropertyOperation.PERCENT, -100, 100);
        assertRange(JafsIntegration.Property.STAMINA_EFFICIENCY, PropertyOperation.FLAT, -1000, 1000);
        assertFalse(spec(JafsIntegration.Property.STAMINA_EFFICIENCY).supports(PropertyOperation.PERCENT));
    }

    private static void assertRange(JafsIntegration.Property property, PropertyOperation operation, double min, double max) {
        PropertySpec.Range range = spec(property).range(operation);
        assertNotNull(range, property + " " + operation);
        assertEquals(min, range.min(), EPSILON, property + " " + operation);
        assertEquals(max, range.max(), EPSILON, property + " " + operation);
    }

    @Test
    void everyBonusHasExactlyOneProperty() {
        Set<StaminaBonus> bonuses = EnumSet.noneOf(StaminaBonus.class);
        for (JafsIntegration.Property property : JafsIntegration.Property.values()) {
            assertTrue(bonuses.add(property.flat), property.toString());
            if (property.percent != null) {
                assertTrue(bonuses.add(property.percent), property.toString());
            }
        }
        assertEquals(EnumSet.allOf(StaminaBonus.class), bonuses);
    }

    @Test
    void everyPropertyHasATooltipNameWithoutAFormatCharacter() throws IOException {
        Properties lang = new Properties();
        try (InputStream in = JafsIntegrationTest.class.getResourceAsStream("/assets/jassintegrations/lang/en_us.lang")) {
            assertNotNull(in, "en_us.lang is missing");
            lang.load(in);
        }
        for (JafsIntegration.Property property : JafsIntegration.Property.values()) {
            String key = spec(property).displayNameKey();
            assertEquals("property.jassintegrations." + property.path, key);
            String name = lang.getProperty(key);
            assertNotNull(name, key);
            assertFalse(name.contains("%"), key + " must not contain a percent sign: " + name);
        }
        assertEquals(JafsIntegration.Property.values().length, lang.stringPropertyNames().stream()
                .filter(key -> key.startsWith("property.jassintegrations.")).count());
    }

    @Test
    void theApplierStoresReplacesAndForgetsSums() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID player = UUID.randomUUID();
        assertEquals(StaminaBonusTotals.NONE, applier.sums(player));

        StaminaBonusTotals one = applier.update(player, JafsIntegration.Property.MAX_STAMINA, 0, 10);
        assertNotNull(one);
        assertEquals(10.0, applier.sums(player).get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON);

        assertNull(applier.update(player, JafsIntegration.Property.MAX_STAMINA, 0, 10), "the same sums are no change");

        applier.update(player, JafsIntegration.Property.STAMINA_EFFICIENCY, 20, 0);
        StaminaBonusTotals two = applier.update(player, JafsIntegration.Property.MAX_STAMINA, 0, 20);
        assertNotNull(two);
        assertEquals(20.0, two.get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON, "a call replaces the sums");
        assertEquals(20.0, two.get(StaminaBonus.STAMINA_EFFICIENCY), EPSILON, "other properties are kept");

        applier.update(player, JafsIntegration.Property.MAX_STAMINA, 0, 0);
        applier.update(player, JafsIntegration.Property.STAMINA_EFFICIENCY, 0, 0);
        assertEquals(StaminaBonusTotals.NONE, applier.sums(player), "zero sums remove the bonus");

        applier.update(player, JafsIntegration.Property.MAX_STAMINA, 5, 0);
        applier.forget(player);
        assertEquals(StaminaBonusTotals.NONE, applier.sums(player), "logout forgets the player");
    }

    @Test
    void aPropertyWithoutPercentIgnoresThePercentSum() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID player = UUID.randomUUID();
        StaminaBonusTotals totals = applier.update(player, JafsIntegration.Property.STAMINA_EFFICIENCY, 20, 50);
        assertEquals(StaminaBonusTotals.NONE.with(StaminaBonus.STAMINA_EFFICIENCY, 20), totals);
    }

    @Test
    void playersAreKeptApart() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        applier.update(first, JafsIntegration.Property.MAX_STAMINA, 0, 10);
        assertEquals(StaminaBonusTotals.NONE, applier.sums(second));
    }

    @Test
    void theApiMethodIsFoundOnTheDevelopmentJafs() {
        assertEquals(Collections.emptyList(), new JafsIntegration().compatibilityProblems());
    }
}
