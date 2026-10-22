package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityAttributesTest {

    private static final double EPSILON = 1e-9;

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
        assertEquals(15.0, QualityAttributes.usedTotal("jassintegrations.max_stamina_percent", modifiers, warned,
                warnings::add), EPSILON);
        assertEquals(2, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("jassintegrations.max_stamina_percent with operation 1 ignored"), warnings.get(0));
        QualityAttributes.usedTotal("jassintegrations.max_stamina_percent", modifiers, warned, warnings::add);
        assertEquals(2, warnings.size(), "warned again on a later check");
        QualityAttributes.usedTotal("jassintegrations.max_stamina_flat", modifiers, warned, warnings::add);
        assertEquals(4, warnings.size(), "another attribute is warned separately");
    }

    @Test
    void aNewEntityWithTheSameUuidStartsFromNoTotals() {
        Object player = new Object();
        Object respawned = new Object();
        StaminaBonusTotals seen = StaminaBonusTotals.NONE.with(StaminaBonus.MAX_STAMINA_PERCENT, 10);
        QualityChangeTracker.Seen last = new QualityChangeTracker.Seen(player, seen);
        assertEquals(seen, QualityChangeTracker.Seen.totalsFor(last, player));
        assertEquals(StaminaBonusTotals.NONE, QualityChangeTracker.Seen.totalsFor(last, respawned));
        assertEquals(StaminaBonusTotals.NONE, QualityChangeTracker.Seen.totalsFor(null, player));
    }

    @Test
    void everyAttributeGivesItsOwnBonusUnderItsSavedName() {
        Set<StaminaBonus> bonuses = EnumSet.noneOf(StaminaBonus.class);
        for (QualityAttributes.Kind kind : QualityAttributes.Kind.values()) {
            assertTrue(bonuses.add(kind.bonus), kind.key);
        }
        assertEquals(EnumSet.allOf(StaminaBonus.class), bonuses);
        // Worlds save these names; they are the 1.0.0 names.
        assertEquals("jassintegrations.max_stamina_percent", QualityAttributes.Kind.MAX_STAMINA_PERCENT.attributeName());
        assertEquals("jassintegrations.delay_reduction_flat", QualityAttributes.Kind.DELAY_REDUCTION_FLAT.attributeName());
    }

    @Test
    void percentNamesShowAPercentSign() throws IOException {
        Properties lang = lang();
        assertEquals("% Maximum Stamina", String.format(lang.getProperty("attribute.name.jassintegrations.max_stamina_percent")));
    }

    private static Properties lang() throws IOException {
        Properties lang = new Properties();
        try (InputStream in = QualityAttributesTest.class.getResourceAsStream("/assets/jassintegrations/lang/en_us.lang")) {
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
