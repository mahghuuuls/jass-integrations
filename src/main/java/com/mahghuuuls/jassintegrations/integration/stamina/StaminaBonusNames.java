package com.mahghuuuls.jassintegrations.integration.stamina;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The name of each stamina bonus where an integration has no operation of its own: the property path for its
 * flat bonus, the path with {@code _percent} for its percent bonus (for example {@code max_stamina} and
 * {@code max_stamina_percent}). Statusworks types are these names with the mod id; Reskillable skill rules use
 * them as they are.
 */
public final class StaminaBonusNames {

    private static final String PERCENT_SUFFIX = "_percent";

    /** Every stamina bonus by its name, in catalogue order. */
    public static final Map<String, StaminaBonus> BY_NAME = Collections.unmodifiableMap(build());

    private StaminaBonusNames() {
    }

    private static Map<String, StaminaBonus> build() {
        Map<String, StaminaBonus> names = new LinkedHashMap<>();
        for (StaminaProperty property : StaminaProperty.values()) {
            names.put(property.path, property.flat);
            if (property.supportsPercent()) {
                names.put(property.path + PERCENT_SUFFIX, property.percent);
            }
        }
        return names;
    }
}
