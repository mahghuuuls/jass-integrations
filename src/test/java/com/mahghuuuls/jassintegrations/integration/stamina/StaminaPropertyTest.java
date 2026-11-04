package com.mahghuuuls.jassintegrations.integration.stamina;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaminaPropertyTest {

    private static final double EPSILON = 1e-9;

    @Test
    void theSixPropertiesHaveTheApprovedIds() {
        assertEquals("jassintegrations:max_stamina", StaminaProperty.MAX_STAMINA.id());
        assertEquals("jassintegrations:stamina_regeneration", StaminaProperty.STAMINA_REGENERATION.id());
        assertEquals("jassintegrations:regeneration_delay_reduction", StaminaProperty.REGENERATION_DELAY_REDUCTION.id());
        assertEquals("jassintegrations:stamina_efficiency", StaminaProperty.STAMINA_EFFICIENCY.id());
        assertEquals("jassintegrations:shield_stability", StaminaProperty.SHIELD_STABILITY.id());
        assertEquals("jassintegrations:weight_reduction", StaminaProperty.WEIGHT_REDUCTION.id());
        assertEquals(6, StaminaProperty.values().length);
    }

    @Test
    void eachOperationNamesItsBonusAndRange() {
        assertEquals(StaminaBonus.MAX_STAMINA_PERCENT, StaminaProperty.MAX_STAMINA.bonus(true));
        assertEquals(StaminaBonus.MAX_STAMINA_FLAT, StaminaProperty.MAX_STAMINA.bonus(false));
        assertEquals(-100.0, StaminaProperty.MAX_STAMINA.minimum(true), EPSILON);
        assertEquals(1000.0, StaminaProperty.MAX_STAMINA.maximum(true), EPSILON);
        assertEquals(-60.0, StaminaProperty.REGENERATION_DELAY_REDUCTION.minimum(false), EPSILON);
        assertEquals(100.0, StaminaProperty.REGENERATION_DELAY_REDUCTION.maximum(true), EPSILON);
        assertFalse(StaminaProperty.STAMINA_EFFICIENCY.supportsPercent());
        assertNull(StaminaProperty.STAMINA_EFFICIENCY.bonus(true));
        assertTrue(StaminaProperty.STAMINA_REGENERATION.supportsPercent());
        assertFalse(StaminaProperty.SHIELD_STABILITY.supportsPercent());
        assertEquals(StaminaBonus.SHIELD_STABILITY, StaminaProperty.SHIELD_STABILITY.bonus(false));
        assertEquals(-1000.0, StaminaProperty.SHIELD_STABILITY.minimum(false), EPSILON);
        assertEquals(1000.0, StaminaProperty.SHIELD_STABILITY.maximum(false), EPSILON);
        assertEquals(StaminaBonus.WEIGHT_REDUCTION_FLAT, StaminaProperty.WEIGHT_REDUCTION.bonus(false));
        assertEquals(StaminaBonus.WEIGHT_REDUCTION_PERCENT, StaminaProperty.WEIGHT_REDUCTION.bonus(true));
        assertEquals(-100.0, StaminaProperty.WEIGHT_REDUCTION.minimum(false), EPSILON);
        assertEquals(100.0, StaminaProperty.WEIGHT_REDUCTION.maximum(false), EPSILON);
        assertEquals(-100.0, StaminaProperty.WEIGHT_REDUCTION.minimum(true), EPSILON);
        assertEquals(100.0, StaminaProperty.WEIGHT_REDUCTION.maximum(true), EPSILON);
    }

    @Test
    void everyBonusHasExactlyOneProperty() {
        Set<StaminaBonus> bonuses = EnumSet.noneOf(StaminaBonus.class);
        for (StaminaProperty property : StaminaProperty.values()) {
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
        try (InputStream in = StaminaPropertyTest.class.getResourceAsStream("/assets/jassintegrations/lang/en_us.lang")) {
            assertNotNull(in, "en_us.lang is missing");
            lang.load(in);
        }
        for (StaminaProperty property : StaminaProperty.values()) {
            String key = property.languageKey();
            assertEquals("property.jassintegrations." + property.path, key);
            String name = lang.getProperty(key);
            assertNotNull(name, key);
            assertFalse(name.contains("%"), key + " must not contain a percent sign: " + name);
        }
        assertEquals(StaminaProperty.values().length, lang.stringPropertyNames().stream()
                .filter(key -> key.startsWith("property.jassintegrations.")).count());
    }
}
