package com.mahghuuuls.jassintegrations.integration.statusworks;

import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The Statusworks contribution type id of each stamina bonus, and the sum of a player's values. Statusworks
 * types carry no operation, so a property's percent bonus gets its own id: the property id with
 * {@code _percent}. Uses no Statusworks class, so it can be tested alone.
 */
final class StatusworksTypeIds {

    static final String PERCENT_SUFFIX = "_percent";

    /** Every stamina bonus by its type id, in catalogue order. */
    static final Map<String, StaminaBonus> BY_ID = Collections.unmodifiableMap(build());

    private StatusworksTypeIds() {
    }

    private static Map<String, StaminaBonus> build() {
        Map<String, StaminaBonus> ids = new LinkedHashMap<>();
        for (StaminaProperty property : StaminaProperty.values()) {
            ids.put(property.id(), property.flat);
            if (property.supportsPercent()) {
                ids.put(property.id() + PERCENT_SUFFIX, property.percent);
            }
        }
        return ids;
    }

    /**
     * The totals of every type. {@code values} gives a type's values by contribution key, or {@code null} when
     * they are not current; such a type, and a value that is not a finite number, counts as nothing.
     */
    static StaminaBonusTotals sum(Function<String, Map<String, Double>> values) {
        double[] totals = new double[StaminaBonus.values().length];
        for (Map.Entry<String, StaminaBonus> type : BY_ID.entrySet()) {
            Map<String, Double> group = values.apply(type.getKey());
            totals[type.getValue().ordinal()] = sum(group);
        }
        return new StaminaBonusTotals(totals);
    }

    /** The sum of one group's finite values; 0 for {@code null} or none. */
    static double sum(Map<String, Double> group) {
        if (group == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Double value : group.values()) {
            if (value != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                total += value;
            }
        }
        return total;
    }
}
