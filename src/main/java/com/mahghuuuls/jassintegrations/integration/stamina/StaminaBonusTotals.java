package com.mahghuuuls.jassintegrations.integration.stamina;

import java.util.Arrays;

/** A player's stamina bonus totals at one moment; immutable and compared by value. */
public final class StaminaBonusTotals {

    public static final StaminaBonusTotals NONE = new StaminaBonusTotals(new double[StaminaBonus.values().length]);

    private final double[] values;

    /** {@code values} is indexed by {@link StaminaBonus#ordinal()}; the array is copied. */
    public StaminaBonusTotals(double[] values) {
        this.values = Arrays.copyOf(values, StaminaBonus.values().length);
    }

    public double get(StaminaBonus bonus) {
        return values[bonus.ordinal()];
    }

    /** These totals with one bonus replaced. */
    public StaminaBonusTotals with(StaminaBonus bonus, double amount) {
        double[] changed = values.clone();
        changed[bonus.ordinal()] = amount;
        return new StaminaBonusTotals(changed);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StaminaBonusTotals && Arrays.equals(values, ((StaminaBonusTotals) other).values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    /** The bonuses that are not 0, for example {@code max_stamina_percent=10.0}, or {@code none}. */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (StaminaBonus bonus : StaminaBonus.values()) {
            if (values[bonus.ordinal()] != 0.0) {
                text.append(text.length() == 0 ? "" : ", ").append(bonus.key).append('=').append(values[bonus.ordinal()]);
            }
        }
        return text.length() == 0 ? "none" : text.toString();
    }
}
