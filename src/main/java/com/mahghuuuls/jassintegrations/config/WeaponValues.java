package com.mahghuuuls.jassintegrations.config;

/** The values of one weapon type. {@code NaN} means "not supplied", so JASS uses its own default. */
public final class WeaponValues {

    public final double swingCost;
    public final double heldWeight;
    public final double drawPerSecond;

    public WeaponValues(double swingCost, double heldWeight, double drawPerSecond) {
        this.swingCost = swingCost;
        this.heldWeight = heldWeight;
        this.drawPerSecond = drawPerSecond;
    }
}
