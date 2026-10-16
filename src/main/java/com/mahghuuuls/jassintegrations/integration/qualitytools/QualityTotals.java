package com.mahghuuuls.jassintegrations.integration.qualitytools;

import java.util.Arrays;

/** A player's quality attribute totals at one moment; compared by value. */
final class QualityTotals {

    static final QualityTotals NONE = new QualityTotals(new double[QualityAttributes.Kind.values().length]);

    private final double[] values;

    /** {@code values} is indexed by {@link QualityAttributes.Kind#ordinal()}; the array is copied. */
    QualityTotals(double[] values) {
        this.values = values.clone();
    }

    double get(QualityAttributes.Kind kind) {
        return values[kind.ordinal()];
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof QualityTotals && Arrays.equals(values, ((QualityTotals) other).values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    /** The attributes that are not 0, for example {@code max_stamina_percent=10.0}, or {@code none}. */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (QualityAttributes.Kind kind : QualityAttributes.Kind.values()) {
            if (values[kind.ordinal()] != 0.0) {
                text.append(text.length() == 0 ? "" : ", ").append(kind.key).append('=').append(values[kind.ordinal()]);
            }
        }
        return text.length() == 0 ? "none" : text.toString();
    }
}
