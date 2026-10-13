package com.mahghuuuls.jassintegrations.config;

import com.mahghuuuls.jassintegrations.Tags;
import net.minecraftforge.common.config.Config;

/**
 * The configuration file {@code config/jassintegrations.cfg}. Read only by {@link Settings}, which
 * parses and checks these fields into the snapshot the rest of the mod uses.
 */
@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID, category = "")
public final class JassIntegrationsConfig {

    private JassIntegrationsConfig() {
    }

    @Config.Name("general")
    public static final General general = new General();

    @Config.Name("spartan_weaponry")
    public static final SpartanWeaponry spartanWeaponry = new SpartanWeaponry();

    @Config.Name("spartan_shields")
    public static final SpartanShields spartanShields = new SpartanShields();

    public static final class General {

        @Config.Name("debug_logging")
        @Config.Comment("If true, logs each integration decision this mod makes (charges, refusals, changes). Startup lines and warnings are always logged.")
        public boolean debugLogging = false;
    }

    public static final class SpartanWeaponry {

        @Config.Name("enabled")
        @Config.Comment("If false, Spartan Weaponry weapons get no values from this mod (JASS defaults apply). Needs a restart.")
        @Config.RequiresMcRestart
        public boolean enabled = true;

        @Config.Name("weapon_types")
        @Config.Comment({
                "Values per weapon type, one line each: type=swing_cost,held_weight,draw_per_second",
                "An empty field gives no value, so JASS uses its own default. Values are 0 or more.",
                "Example: dagger=6,1,6   JASS config entries for a single item win over these lines."})
        public String[] weaponTypes = BalanceDefaults.WEAPON_TYPES.clone();
    }

    public static final class SpartanShields {

        @Config.Name("enabled")
        @Config.Comment("If false, Spartan Shields shields get no values from this mod (JASS defaults apply). Needs a restart.")
        @Config.RequiresMcRestart
        public boolean enabled = true;

        @Config.Name("shield_tiers")
        @Config.Comment({
                "Values per shield tier and kind, one line each: tier_<n>_<basic|tower>=stability,melee_block,projectile_block,held_weight",
                "Block values are shares from 0 to 1 (1 blocks the whole hit). Example: tier_3_tower=80,1.0,1.0,8"})
        public String[] shieldTiers = BalanceDefaults.SHIELD_TIERS.clone();

        @Config.Name("shield_materials")
        @Config.Comment({
                "The tier of each shield material, one line each: material=tier (1 to 5). Example: iron=3",
                "JASS config entries for a single shield win over these values."})
        public String[] shieldMaterials = BalanceDefaults.SHIELD_MATERIALS.clone();
    }
}
