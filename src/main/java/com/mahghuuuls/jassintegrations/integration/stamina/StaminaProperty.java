package com.mahghuuuls.jassintegrations.integration.stamina;

import com.mahghuuuls.jassintegrations.Tags;

/**
 * The stamina properties that other mods' data can name, for example JAFS foods and Setworks
 * bonuses: each one's id, the stamina bonus and per-entry range of each operation, and how it is
 * shown. The only owner of these facts; a property without a percent bonus supports flat only.
 */
public enum StaminaProperty {
    MAX_STAMINA("max_stamina", "", StaminaBonus.MAX_STAMINA_FLAT, -1000, 1000,
            StaminaBonus.MAX_STAMINA_PERCENT, -100, 1000),
    STAMINA_REGENERATION("stamina_regeneration", "/s", StaminaBonus.REGENERATION_FLAT, -1000, 1000,
            StaminaBonus.REGENERATION_PERCENT, -100, 1000),
    REGENERATION_DELAY_REDUCTION("regeneration_delay_reduction", "s", StaminaBonus.DELAY_REDUCTION_FLAT, -60, 60,
            StaminaBonus.DELAY_REDUCTION_PERCENT, -100, 100),
    STAMINA_EFFICIENCY("stamina_efficiency", "", StaminaBonus.STAMINA_EFFICIENCY, -1000, 1000);

    /** Id path, for example {@code max_stamina}. */
    public final String path;
    /** Unit shown after a flat amount, for example {@code s}; may be empty. */
    public final String unit;
    public final StaminaBonus flat;
    public final double flatMin;
    public final double flatMax;
    /** The percent bonus, or {@code null} when the property supports flat only. */
    public final StaminaBonus percent;
    public final double percentMin;
    public final double percentMax;

    /** A property with a flat operation only. */
    StaminaProperty(String path, String unit, StaminaBonus flat, double flatMin, double flatMax) {
        this(path, unit, flat, flatMin, flatMax, null, 0, 0);
    }

    StaminaProperty(String path, String unit, StaminaBonus flat, double flatMin, double flatMax,
            StaminaBonus percent, double percentMin, double percentMax) {
        this.path = path;
        this.unit = unit;
        this.flat = flat;
        this.flatMin = flatMin;
        this.flatMax = flatMax;
        this.percent = percent;
        this.percentMin = percentMin;
        this.percentMax = percentMax;
    }

    /** Namespaced id, for example {@code jassintegrations:max_stamina}. */
    public String id() {
        return Tags.MOD_ID + ":" + path;
    }

    public boolean supportsPercent() {
        return percent != null;
    }

    /** The stamina bonus of an operation; {@code null} for percent on a flat-only property. */
    public StaminaBonus bonus(boolean percentOperation) {
        return percentOperation ? percent : flat;
    }

    /** The lowest amount one entry may have with an operation. */
    public double minimum(boolean percentOperation) {
        return percentOperation ? percentMin : flatMin;
    }

    /** The highest amount one entry may have with an operation. */
    public double maximum(boolean percentOperation) {
        return percentOperation ? percentMax : flatMax;
    }

    /** Language key of the display name, {@code property.jassintegrations.<path>}. */
    public String languageKey() {
        return "property." + Tags.MOD_ID + "." + path;
    }
}
