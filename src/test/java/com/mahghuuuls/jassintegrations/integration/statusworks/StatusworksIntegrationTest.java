package com.mahghuuuls.jassintegrations.integration.statusworks;

import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusworksIntegrationTest {

    private static final double EPSILON = 1e-9;

    private static Map<String, Double> group(double... values) {
        Map<String, Double> group = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            group.put("key" + i, values[i]);
        }
        return group;
    }

    @Test
    void theApiIsFoundOnTheDevelopmentStatusworks() {
        assertEquals(Collections.emptyList(), new StatusworksIntegration().compatibilityProblems());
    }

    @Test
    void theTenTypeIdsComeFromTheCatalogue() {
        List<String> expected = Arrays.asList(
                "jassintegrations:max_stamina", "jassintegrations:max_stamina_percent",
                "jassintegrations:stamina_regeneration", "jassintegrations:stamina_regeneration_percent",
                "jassintegrations:regeneration_delay_reduction", "jassintegrations:regeneration_delay_reduction_percent",
                "jassintegrations:stamina_efficiency", "jassintegrations:shield_stability",
                "jassintegrations:weight_reduction", "jassintegrations:weight_reduction_percent");
        assertEquals(new LinkedHashSet<>(expected), StatusworksTypeIds.BY_ID.keySet());
        assertEquals(StaminaBonus.MAX_STAMINA_PERCENT, StatusworksTypeIds.BY_ID.get("jassintegrations:max_stamina_percent"));
        assertEquals(StaminaBonus.DELAY_REDUCTION_FLAT, StatusworksTypeIds.BY_ID.get("jassintegrations:regeneration_delay_reduction"));
        assertEquals(StaminaBonus.WEIGHT_REDUCTION_PERCENT, StatusworksTypeIds.BY_ID.get("jassintegrations:weight_reduction_percent"));
    }

    @Test
    void everyStaminaBonusHasExactlyOneType() {
        Set<StaminaBonus> bonuses = EnumSet.noneOf(StaminaBonus.class);
        bonuses.addAll(StatusworksTypeIds.BY_ID.values());
        assertEquals(EnumSet.allOf(StaminaBonus.class), bonuses);
        assertEquals(StaminaBonus.values().length, StatusworksTypeIds.BY_ID.size());
    }

    @Test
    void aGroupSumsItsFiniteValues() {
        assertEquals(20.0, StatusworksTypeIds.sum(group(10, 10)), EPSILON);
        assertEquals(-5.0, StatusworksTypeIds.sum(group(5, -10)), EPSILON);
        assertEquals(3.0, StatusworksTypeIds.sum(group(3, Double.NaN, Double.POSITIVE_INFINITY)), EPSILON);
        assertEquals(0.0, StatusworksTypeIds.sum(group()), EPSILON);
        assertEquals(0.0, StatusworksTypeIds.sum((Map<String, Double>) null), EPSILON);
    }

    @Test
    void eachTypeFillsItsOwnBonusAndAGroupWithoutValuesCountsAsNothing() {
        Map<String, Map<String, Double>> current = new HashMap<>();
        current.put("jassintegrations:max_stamina_percent", group(10, 10));
        current.put("jassintegrations:max_stamina", group(10));
        current.put("jassintegrations:stamina_efficiency", null);
        StaminaBonusTotals totals = StatusworksTypeIds.sum(current::get);
        assertEquals(20.0, totals.get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON);
        assertEquals(10.0, totals.get(StaminaBonus.MAX_STAMINA_FLAT), EPSILON);
        assertEquals(0.0, totals.get(StaminaBonus.STAMINA_EFFICIENCY), EPSILON);
        assertEquals(0.0, totals.get(StaminaBonus.REGENERATION_FLAT), EPSILON);
    }

    @Test
    void theTotalsMapThroughTheSharedMapping() {
        Map<String, Map<String, Double>> current = new HashMap<>();
        current.put("jassintegrations:weight_reduction", group(2));
        current.put("jassintegrations:weight_reduction_percent", group(50));
        current.put("jassintegrations:regeneration_delay_reduction", group(0.25));
        StaminaContribution c = StaminaBonusMapping.map(StatusworksTypeIds.sum(current::get));
        assertEquals(-2.0, c.getFlatWeight(), EPSILON);
        assertEquals(0.5, c.getWeightReduction(), EPSILON);
        assertEquals(-0.25, c.getFlatDelay(), EPSILON);
    }

    @Test
    void theDebugRecordCountsOnlyRealChangesAndStartsOverAtLogout() {
        StatusworksTotalsLog log = new StatusworksTotalsLog();
        UUID player = UUID.randomUUID();
        StaminaBonusTotals vigor = StaminaBonusTotals.NONE.with(StaminaBonus.MAX_STAMINA_PERCENT, 20);
        assertFalse(log.changed(player, StaminaBonusTotals.NONE), "no bonus at the first rebuild is no change");
        assertTrue(log.changed(player, vigor));
        assertFalse(log.changed(player, vigor), "a rebuild with the same totals (respawn, dimension change)");
        log.forget(player);
        assertTrue(log.changed(player, vigor), "a new login with a bonus counts as a change");
        assertTrue(log.changed(player, StaminaBonusTotals.NONE));
    }

    @Test
    void everyInvalidationIsConfirmed() {
        // A false answer would leave the group failed and the bonuses uncounted until Statusworks retries.
        assertTrue(new StatusworksStaminaType("jassintegrations:max_stamina").invalidate(null));
    }
}
