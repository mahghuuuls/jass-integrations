package com.mahghuuuls.jassintegrations.config;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The shipped defaults must equal the balance defaults table. The table under test resources is a
 * byte-identical copy of the project's table; a changed number changes the table first.
 */
class BalanceDefaultsParityTest {

    private static final List<String> COLUMNS = Arrays.asList("group", "key", "swing_cost", "held_weight",
            "draw_per_second", "stability", "melee_block", "projectile_block", "tier", "notes");

    /** {@code key -> values} of one table group, in the order of {@code columns}; an empty cell is NaN. */
    private static Map<String, double[]> table(String group, String... columns) throws IOException {
        InputStream in = BalanceDefaultsParityTest.class.getResourceAsStream("/balance-defaults.csv");
        assertNotNull(in, "balance-defaults.csv missing from test resources");
        Map<String, double[]> rows = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            assertEquals(String.join(",", COLUMNS), reader.readLine());
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (!fields[0].equals(group)) {
                    continue;
                }
                double[] values = new double[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    String cell = fields[COLUMNS.indexOf(columns[i])];
                    values[i] = cell.isEmpty() ? Double.NaN : Double.parseDouble(cell);
                }
                rows.put(fields[1], values);
            }
        }
        return rows;
    }

    private static void assertSame(Map<String, double[]> table, Map<String, double[]> shipped) {
        assertEquals(new ArrayList<>(table.keySet()), new ArrayList<>(shipped.keySet()));
        for (Map.Entry<String, double[]> entry : table.entrySet()) {
            assertArrayEquals(entry.getValue(), shipped.get(entry.getKey()), entry.getKey());
        }
    }

    @Test
    void weaponTypeDefaultsMatchTable() throws IOException {
        assertSame(table("weapon_type", "swing_cost", "held_weight", "draw_per_second"),
                Settings.shippedDefaults("weapon_types"));
    }

    @Test
    void shieldTierDefaultsMatchTable() throws IOException {
        assertSame(table("shield_tier", "stability", "melee_block", "projectile_block", "held_weight"),
                Settings.shippedDefaults("shield_tiers"));
    }

    @Test
    void shieldBashCostMatchesTable() throws IOException {
        assertEquals(table("action", "swing_cost").get("shield_bash")[0], BalanceDefaults.SHIELD_BASH_COST);
    }

    @Test
    void crossbowLoadingDrainMatchesTable() throws IOException {
        assertEquals(table("action", "draw_per_second").get("crossbow_loading")[0], BalanceDefaults.CROSSBOW_LOADING_DRAIN);
    }

    @Test
    void shieldMaterialDefaultsMatchTable() throws IOException {
        assertSame(table("shield_material", "tier"), Settings.shippedDefaults("shield_materials"));
    }
}
