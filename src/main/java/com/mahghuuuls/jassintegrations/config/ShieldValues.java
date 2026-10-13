package com.mahghuuuls.jassintegrations.config;

/** The values of one shield tier and kind. */
public final class ShieldValues {

    public final double stability;
    public final double meleeBlock;
    public final double projectileBlock;
    public final double heldWeight;

    public ShieldValues(double stability, double meleeBlock, double projectileBlock, double heldWeight) {
        this.stability = stability;
        this.meleeBlock = meleeBlock;
        this.projectileBlock = projectileBlock;
        this.heldWeight = heldWeight;
    }
}
