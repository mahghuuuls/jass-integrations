package com.mahghuuuls.jassintegrations.integration.setworks;

import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaProperty;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetworksIntegrationTest {

    private static final double EPSILON = 1e-9;

    private static Map<String, String> params(String operation, String amount) {
        Map<String, String> params = new HashMap<>();
        params.put("operation", operation);
        params.put("amount", amount);
        return params;
    }

    private static SetworksStaminaEffect kind(StaminaProperty property) {
        return new SetworksStaminaEffect(property, new SetworksStaminaEffects());
    }

    @Test
    void theKindsUseThePropertyIds() {
        assertEquals("jassintegrations:max_stamina", kind(StaminaProperty.MAX_STAMINA).id());
        assertEquals("jassintegrations:stamina_regeneration", kind(StaminaProperty.STAMINA_REGENERATION).id());
        assertEquals("jassintegrations:regeneration_delay_reduction", kind(StaminaProperty.REGENERATION_DELAY_REDUCTION).id());
        assertEquals("jassintegrations:stamina_efficiency", kind(StaminaProperty.STAMINA_EFFICIENCY).id());
        assertEquals("jassintegrations:shield_stability", kind(StaminaProperty.SHIELD_STABILITY).id());
        assertEquals("jassintegrations:weight_reduction", kind(StaminaProperty.WEIGHT_REDUCTION).id());
    }

    @Test
    void validParametersGiveTheirBonus() {
        SetworksStaminaEffect.Parsed percent = SetworksStaminaEffect.parse(StaminaProperty.MAX_STAMINA, params("percent", "10"));
        assertEquals(StaminaBonus.MAX_STAMINA_PERCENT, percent.bonus);
        assertEquals(10.0, percent.amount, EPSILON);
        SetworksStaminaEffect.Parsed flat = SetworksStaminaEffect.parse(StaminaProperty.REGENERATION_DELAY_REDUCTION,
                params("FLAT", " 0.25 "));
        assertEquals(StaminaBonus.DELAY_REDUCTION_FLAT, flat.bonus);
        assertEquals(0.25, flat.amount, EPSILON);
        assertNull(kind(StaminaProperty.STAMINA_EFFICIENCY).validate(params("flat", "-1000")));
        assertNull(kind(StaminaProperty.MAX_STAMINA).validate(params("percent", "-100")));
    }

    @Test
    void invalidParametersAreReportedWithAReason() {
        assertTrue(kind(StaminaProperty.MAX_STAMINA).validate(params("percent", "5000")).contains("outside -100 to 1000"));
        assertTrue(kind(StaminaProperty.STAMINA_EFFICIENCY).validate(params("percent", "10")).contains("operation must be flat"));
        assertTrue(kind(StaminaProperty.MAX_STAMINA).validate(params("times", "10")).contains("flat or percent"));
        assertTrue(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", "ten")).contains("must be a number"));
        assertNotNull(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", "10f")), "no Java number suffix");
        assertNotNull(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", "1e2")), "no exponent");
        assertNotNull(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", "0x1p3")), "no hex");
        assertNull(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", ".5")));
        assertNotNull(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", "NaN")));
        assertNotNull(kind(StaminaProperty.MAX_STAMINA).validate(params("flat", "Infinity")));
        assertNotNull(kind(StaminaProperty.REGENERATION_DELAY_REDUCTION).validate(params("flat", "61")));
        Map<String, String> extra = params("flat", "5");
        extra.put("amout", "5");
        assertTrue(kind(StaminaProperty.MAX_STAMINA).validate(extra).contains("unknown parameter 'amout'"));
        assertNotNull(kind(StaminaProperty.MAX_STAMINA).validate(Collections.emptyMap()));
        assertThrows(IllegalArgumentException.class,
                () -> SetworksStaminaEffect.parse(StaminaProperty.MAX_STAMINA, params("percent", "5000")));
    }

    @Test
    void descriptionsUseTheSharedNamesAndUnits() {
        assertEquals("+10% ${property.jassintegrations.max_stamina}",
                kind(StaminaProperty.MAX_STAMINA).describe(params("percent", "10")));
        assertEquals("-10% ${property.jassintegrations.max_stamina}",
                kind(StaminaProperty.MAX_STAMINA).describe(params("percent", "-10.0")));
        assertEquals("+5 /s ${property.jassintegrations.stamina_regeneration}",
                kind(StaminaProperty.STAMINA_REGENERATION).describe(params("flat", "5")));
        assertEquals("+0.25 s ${property.jassintegrations.regeneration_delay_reduction}",
                kind(StaminaProperty.REGENERATION_DELAY_REDUCTION).describe(params("flat", "0.25")));
        assertEquals("+20 ${property.jassintegrations.stamina_efficiency}",
                kind(StaminaProperty.STAMINA_EFFICIENCY).describe(params("flat", "20")));
        assertEquals("+1000 ${property.jassintegrations.max_stamina}",
                kind(StaminaProperty.MAX_STAMINA).describe(params("flat", "1000")));
        assertEquals("+20 ${property.jassintegrations.shield_stability}",
                kind(StaminaProperty.SHIELD_STABILITY).describe(params("flat", "20")));
        assertEquals("+50% ${property.jassintegrations.weight_reduction}",
                kind(StaminaProperty.WEIGHT_REDUCTION).describe(params("percent", "50")));
        assertEquals("+5 ${property.jassintegrations.weight_reduction}",
                kind(StaminaProperty.WEIGHT_REDUCTION).describe(params("flat", "5")));
    }

    @Test
    void anInstanceCountsOnceAndTotalsAddUp() {
        SetworksStaminaEffects effects = new SetworksStaminaEffects();
        UUID player = UUID.randomUUID();
        assertTrue(effects.record(player, "jit:iron/jit:iron_max/0", StaminaBonus.MAX_STAMINA_PERCENT, 10));
        assertFalse(effects.record(player, "jit:iron/jit:iron_max/0", StaminaBonus.MAX_STAMINA_PERCENT, 10),
                "a re-applied instance changes nothing");
        assertTrue(effects.record(player, "jit:gold/jit:gold_max/0", StaminaBonus.MAX_STAMINA_PERCENT, 10));
        assertEquals(20.0, effects.totals(player).get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON, "two bonuses, each once");
        assertFalse(effects.record(player, "jit:iron/jit:zero/0", StaminaBonus.STAMINA_EFFICIENCY, 0),
                "an effect of amount 0 does not change the totals");

        assertTrue(effects.forget(player, "jit:gold/jit:gold_max/0"));
        assertFalse(effects.forget(player, "jit:gold/jit:gold_max/0"), "withdrawing an absent instance changes nothing");
        assertEquals(10.0, effects.totals(player).get(StaminaBonus.MAX_STAMINA_PERCENT), EPSILON);

        effects.forgetPlayer(player);
        assertEquals(StaminaBonusTotals.NONE, effects.totals(player), "logout forgets the player");
    }

    @Test
    void theShieldAndWeightKindsCheckTheirOwnRanges() {
        assertNull(kind(StaminaProperty.SHIELD_STABILITY).validate(params("flat", "-1000")));
        assertTrue(kind(StaminaProperty.SHIELD_STABILITY).validate(params("percent", "10")).contains("operation must be flat"));
        assertNull(kind(StaminaProperty.WEIGHT_REDUCTION).validate(params("flat", "100")));
        assertNotNull(kind(StaminaProperty.WEIGHT_REDUCTION).validate(params("flat", "101")));
        assertNull(kind(StaminaProperty.WEIGHT_REDUCTION).validate(params("percent", "-100")));
        assertTrue(kind(StaminaProperty.WEIGHT_REDUCTION).validate(params("percent", "150")).contains("outside -100 to 100"));
    }

    @Test
    void aWithdrawnInstanceStopsCountingAndCountsAgainWhenReapplied() {
        SetworksStaminaEffects effects = new SetworksStaminaEffects();
        UUID player = UUID.randomUUID();
        effects.record(player, "jit:iron/jit:iron_guard/0", StaminaBonus.SHIELD_STABILITY, 20);
        effects.record(player, "jit:iron/jit:iron_guard/1", StaminaBonus.WEIGHT_REDUCTION_PERCENT, 50);
        assertTrue(effects.forget(player, "jit:iron/jit:iron_guard/0"), "a withdraw removes only its own instance");
        assertEquals(0.0, effects.totals(player).get(StaminaBonus.SHIELD_STABILITY), EPSILON);
        assertEquals(50.0, effects.totals(player).get(StaminaBonus.WEIGHT_REDUCTION_PERCENT), EPSILON);
        assertTrue(effects.record(player, "jit:iron/jit:iron_guard/0", StaminaBonus.SHIELD_STABILITY, 20),
                "applied again, it counts again");
        assertEquals(StaminaBonusTotals.NONE, effects.totals(UUID.randomUUID()), "players are kept apart");
    }

    @Test
    void theMixedBonusReachesJassWithTheSharedMapping() {
        SetworksStaminaEffects effects = new SetworksStaminaEffects();
        UUID player = UUID.randomUUID();
        effects.record(player, "jit:iron/jit:iron_mixed/0", StaminaBonus.REGENERATION_FLAT, 5);
        effects.record(player, "jit:iron/jit:iron_mixed/1", StaminaBonus.DELAY_REDUCTION_FLAT, 0.25);
        effects.record(player, "jit:iron/jit:iron_mixed/2", StaminaBonus.STAMINA_EFFICIENCY, 20);
        StaminaContribution c = StaminaBonusMapping.map(effects.totals(player));
        assertEquals(5.0, c.getFlatRegeneration(), EPSILON);
        assertEquals(-0.25, c.getFlatDelay(), EPSILON);
        assertEquals(20.0, c.getEfficiency(), EPSILON);
    }

    @Test
    void theApiMethodsAreFoundOnTheDevelopmentSetworks() {
        assertEquals(Collections.emptyList(), new SetworksIntegration().compatibilityProblems());
    }
}
