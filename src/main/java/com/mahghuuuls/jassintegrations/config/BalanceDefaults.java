package com.mahghuuuls.jassintegrations.config;

/**
 * Shipped default values, one line per entry in the format the configuration file uses.
 * These lines mirror the project's balance defaults table; a test compares them against a copy
 * of that table, so a changed number must change the table and these lines together.
 */
public final class BalanceDefaults {

    private BalanceDefaults() {
    }

    /** {@code type=swing_cost,held_weight,draw_per_second}; an empty field supplies no value. */
    public static final String[] WEAPON_TYPES = {
            "dagger=6,1,6",
            "parrying_dagger=6,1,",
            "caestus=5,1,",
            "throwing_knife=6,1,6",
            "boomerang=6,1,6",
            "rapier=8,2,",
            "saber=8,2,",
            "throwing_axe=8,2,8",
            "longsword=12,3,",
            "katana=12,3,",
            "spear=10,3,",
            "javelin=10,3,8",
            "quarterstaff=10,3,",
            "mace=14,4,",
            "club=14,4,",
            "hammer=14,4,",
            "battleaxe=16,5,",
            "glaive=16,5,",
            "halberd=16,5,",
            "pike=16,5,",
            "lance=16,5,",
            "scythe=16,5,",
            "greatsword=18,6,",
            "warhammer=18,6,",
            "longbow=,3,10",
            "crossbow=,4,3"
    };

    /** One-time stamina cost of a Spartan Shields shield bash. */
    public static final double SHIELD_BASH_COST = 8.0;

    /** Stamina drained per second while loading a Spartan crossbow. */
    public static final double CROSSBOW_LOADING_DRAIN = 10.0;

    /** Number of shield tiers. */
    public static final int SHIELD_TIER_COUNT = 5;

    /** {@code tier_<n>_<kind>=stability,melee_block,projectile_block,held_weight}, kind basic or tower. */
    public static final String[] SHIELD_TIERS = {
            "tier_1_basic=30,1.0,1.0,4",
            "tier_2_basic=40,1.0,1.0,4",
            "tier_3_basic=50,1.0,1.0,4",
            "tier_4_basic=60,1.0,1.0,5",
            "tier_5_basic=70,1.0,1.0,5",
            "tier_1_tower=60,1.0,1.0,8",
            "tier_2_tower=70,1.0,1.0,8",
            "tier_3_tower=80,1.0,1.0,8",
            "tier_4_tower=90,1.0,1.0,8",
            "tier_5_tower=100,1.0,1.0,8"
    };

    /** {@code material=tier}: the tier of each Spartan Shields material. */
    public static final String[] SHIELD_MATERIALS = {
            "wood=1",
            "stone=2",
            "gold=2",
            "copper=2",
            "tin=2",
            "lead=2",
            "iron=3",
            "bronze=3",
            "silver=3",
            "nickel=3",
            "invar=3",
            "constantan=3",
            "darkstone=3",
            "steel=4",
            "electrum=4",
            "platinum=4",
            "obsidian=4",
            "manasteel=4",
            "thaumium=4",
            "abyssalnite=4",
            "soulforged_steel=4",
            "riot_enderio=4",
            "riot_rftools=4",
            "flux_ra=4",
            "diamond=5",
            "terrasteel=5",
            "elementium=5",
            "enderium=5",
            "signalum=5",
            "lumium=5",
            "void=5",
            "coralium=5",
            "dreadium=5",
            "ethaxium=5"
    };
}
