package com.mahghuuuls.jassintegrations.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {

    private static Settings parse(List<String> warnings, String... lines) {
        return Settings.fromConfig(false, true, lines, true, BalanceDefaults.SHIELD_TIERS,
                BalanceDefaults.SHIELD_MATERIALS, warnings);
    }

    private static Settings shields(List<String> warnings, String[] tiers, String... materials) {
        return Settings.fromConfig(false, true, BalanceDefaults.WEAPON_TYPES, true, tiers, materials, warnings);
    }

    @Test
    void shippedDefaultsParseWithoutWarnings() {
        List<String> warnings = new ArrayList<>();
        Settings settings = parse(warnings, BalanceDefaults.WEAPON_TYPES);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertEquals(26, Settings.weaponTypeKeys().size());
        assertEquals(18.0, settings.weaponType("greatsword").swingCost);
        assertEquals(6.0, settings.weaponType("greatsword").heldWeight);
    }

    @Test
    void emptyFieldMeansNotSupplied() {
        Settings settings = parse(new ArrayList<>(), BalanceDefaults.WEAPON_TYPES);
        assertTrue(Double.isNaN(settings.weaponType("longbow").swingCost));
        assertEquals(10.0, settings.weaponType("longbow").drawPerSecond);
        assertTrue(Double.isNaN(settings.weaponType("parrying_dagger").drawPerSecond));
    }

    @Test
    void changedLineReplacesOnlyItsType() {
        Settings settings = parse(new ArrayList<>(), "dagger=4,1,6");
        assertEquals(4.0, settings.weaponType("dagger").swingCost);
        assertEquals(18.0, settings.weaponType("greatsword").swingCost);
    }

    @Test
    void missingLineKeepsDefault() {
        Settings settings = parse(new ArrayList<>());
        assertEquals(6.0, settings.weaponType("dagger").swingCost);
    }

    @Test
    void notANumberIsRejectedAndDefaultKept() {
        List<String> warnings = new ArrayList<>();
        Settings settings = parse(warnings, "dagger=abc");
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("dagger=abc"), warnings.get(0));
        assertEquals(6.0, settings.weaponType("dagger").swingCost);
    }

    @Test
    void notANumberInAFullLineIsRejected() {
        List<String> warnings = new ArrayList<>();
        Settings settings = parse(warnings, "dagger=abc,1,6");
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("is not a number"), warnings.get(0));
        assertEquals(6.0, settings.weaponType("dagger").swingCost);
    }

    @Test
    void negativeWrongCountUnknownTypeAndMissingEqualsAreRejected() {
        List<String> warnings = new ArrayList<>();
        Settings settings = parse(warnings, "dagger=-1,1,6", "rapier=8,2", "sword=12,2,", "greatsword");
        assertEquals(4, warnings.size(), warnings.toString());
        assertEquals(6.0, settings.weaponType("dagger").swingCost);
        assertEquals(8.0, settings.weaponType("rapier").swingCost);
        assertNull(settings.weaponType("sword"));
    }

    @Test
    void secondLineForATypeIsRejected() {
        List<String> warnings = new ArrayList<>();
        Settings settings = parse(warnings, "dagger=4,1,6", "dagger=9,1,6");
        assertEquals(1, warnings.size());
        assertEquals(4.0, settings.weaponType("dagger").swingCost);
    }

    @Test
    void shippedShieldDefaultsGiveTierValues() {
        List<String> warnings = new ArrayList<>();
        Settings settings = shields(warnings, BalanceDefaults.SHIELD_TIERS, BalanceDefaults.SHIELD_MATERIALS);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertEquals(50.0, settings.shield("basic", "iron").stability);
        assertEquals(4.0, settings.shield("basic", "iron").heldWeight);
        assertEquals(80.0, settings.shield("tower", "iron").stability);
        assertEquals(8.0, settings.shield("tower", "iron").heldWeight);
        assertEquals(100.0, settings.shield("tower", "diamond").stability);
        assertEquals(30.0, settings.shield("basic", "wood").stability);
        assertEquals("tier_3_tower", settings.shieldTierKey("tower", "iron"));
        assertNull(settings.shield("basic", "unobtainium"));
    }

    @Test
    void materialMovedToAnotherTier() {
        Settings settings = shields(new ArrayList<>(), BalanceDefaults.SHIELD_TIERS, "iron=5");
        assertEquals(100.0, settings.shield("tower", "iron").stability);
        assertEquals(70.0, settings.shield("basic", "iron").stability);
        assertEquals(100.0, settings.shield("tower", "diamond").stability);
    }

    @Test
    void badTierNumbersAreRejected() {
        List<String> warnings = new ArrayList<>();
        Settings settings = shields(warnings, BalanceDefaults.SHIELD_TIERS, "iron=6", "gold=2.5", "wood=0", "mithril=3");
        assertEquals(4, warnings.size(), warnings.toString());
        assertEquals("tier_3_basic", settings.shieldTierKey("basic", "iron"));
        assertEquals("tier_2_basic", settings.shieldTierKey("basic", "gold"));
        assertEquals("tier_1_basic", settings.shieldTierKey("basic", "wood"));
        assertTrue(warnings.get(0).startsWith("spartan_shields.shield_materials line \"iron=6\""), warnings.get(0));
    }

    @Test
    void badShieldTierLinesAreRejected() {
        List<String> warnings = new ArrayList<>();
        Settings settings = shields(warnings, new String[] {"tier_3_tower=80,1.5,1.0,8", "tier_3_basic=50,1.0,,4",
                "tier_9_basic=10,1,1,1", "tier_1_tower=65,0.5,0.25,7"}, BalanceDefaults.SHIELD_MATERIALS);
        assertEquals(3, warnings.size(), warnings.toString());
        assertEquals(1.0, settings.shield("tower", "iron").meleeBlock);
        assertEquals(50.0, settings.shield("basic", "iron").stability);
        assertEquals(65.0, settings.shield("tower", "wood").stability);
        assertEquals(0.5, settings.shield("tower", "wood").meleeBlock);
        assertEquals(0.25, settings.shield("tower", "wood").projectileBlock);
        assertEquals(7.0, settings.shield("tower", "wood").heldWeight);
    }

    @Test
    void zeroIsAccepted() {
        List<String> warnings = new ArrayList<>();
        Settings settings = parse(warnings, "dagger=0,0,0");
        assertTrue(warnings.isEmpty());
        assertEquals(0.0, settings.weaponType("dagger").swingCost);
    }
}
