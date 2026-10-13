package com.mahghuuuls.jassintegrations.integration.spartanshields;

import com.mahghuuuls.jassintegrations.config.Settings;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShieldClassifierTest {

    /** Every shield name Spartan Shields 1.5.5 registers (init/ItemRegistrySS), with its expected kind and material. */
    private static Map<String, String> expected() {
        Map<String, String> shields = new LinkedHashMap<>();
        for (String material : Arrays.asList("wood", "stone", "iron", "gold", "diamond", "obsidian", "bronze", "steel",
                "copper", "tin", "invar", "platinum", "electrum", "nickel", "lead", "constantan", "silver")) {
            shields.put("shield_basic_" + material, "basic " + material);
            shields.put("shield_tower_" + material, "tower " + material);
        }
        for (String material : Arrays.asList("enderium", "signalum", "lumium", "soulforged_steel")) {
            shields.put("shield_basic_" + material, "basic " + material);
        }
        for (String material : Arrays.asList("manasteel", "terrasteel", "elementium")) {
            shields.put("shield_botania_" + material, "basic " + material);
        }
        for (String material : Arrays.asList("darkstone", "abyssalnite", "coralium", "dreadium", "ethaxium")) {
            shields.put("shield_abyssalcraft_" + material, "basic " + material);
        }
        shields.put("shield_tc_thaumium", "basic thaumium");
        shields.put("shield_tc_void", "basic void");
        shields.put("shield_riot_enderio", "basic riot_enderio");
        shields.put("shield_riot_rftools", "basic riot_rftools");
        shields.put("shield_flux_ra", "basic flux_ra");
        return shields;
    }

    @Test
    void everySpartanShieldClassifies() {
        Map<String, String> shields = expected();
        assertEquals(51, shields.size());
        for (Map.Entry<String, String> entry : shields.entrySet()) {
            ShieldClassifier.Shield shield = ShieldClassifier.classify(entry.getKey());
            assertNotNull(shield, entry.getKey());
            assertEquals(entry.getValue(), shield.kind + " " + shield.material, entry.getKey());
        }
    }

    @Test
    void everyMaterialHasATierAndEveryTierMaterialIsUsed() {
        Set<String> used = new HashSet<>();
        for (String path : expected().keySet()) {
            ShieldClassifier.Shield shield = ShieldClassifier.classify(path);
            assertNotNull(Settings.current().shield(shield.kind, shield.material), path);
            used.add(shield.material);
        }
        List<String> configured = Settings.shieldMaterialKeys();
        assertEquals(new HashSet<>(configured), used);
    }

    @Test
    void otherNamesGiveNoShield() {
        assertNull(ShieldClassifier.classify("shield_basic_"));
        assertNull(ShieldClassifier.classify("shield_tower_"));
        assertNull(ShieldClassifier.classify("spikes_book"));
        assertNull(ShieldClassifier.classify("shield_unknown_iron"));
        assertNull(ShieldClassifier.classify("shield_riot_"));
    }

    @Test
    void onlySpartanShieldsIdsClassify() {
        assertNotNull(ShieldClassifier.classify(new ResourceLocation("spartanshields", "shield_tower_iron")));
        assertNull(ShieldClassifier.classify(new ResourceLocation("minecraft", "shield_tower_iron")));
        assertNull(ShieldClassifier.classify(new ResourceLocation("spartanshields", "spikes_book")));
        assertNull(ShieldClassifier.classify((ResourceLocation) null));
    }
}
