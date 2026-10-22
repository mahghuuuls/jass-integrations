package com.mahghuuuls.jassintegrations.integration.stamina;

/**
 * The stamina bonuses an integration can give a player. {@link StaminaBonusMapping} owns their units
 * and signs.
 */
public enum StaminaBonus {
    MAX_STAMINA_FLAT("max_stamina_flat"),
    MAX_STAMINA_PERCENT("max_stamina_percent"),
    REGENERATION_FLAT("regeneration_flat"),
    REGENERATION_PERCENT("regeneration_percent"),
    DELAY_REDUCTION_FLAT("delay_reduction_flat"),
    DELAY_REDUCTION_PERCENT("delay_reduction_percent"),
    STAMINA_EFFICIENCY("stamina_efficiency");

    /** Name used in log lines, for example {@code max_stamina_percent}. */
    public final String key;

    StaminaBonus(String key) {
        this.key = key;
    }
}
