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

    @Config.Name("crossbow")
    public static final Crossbow crossbow = new Crossbow();

    @Config.Name("quality_tools")
    public static final QualityTools qualityTools = new QualityTools();

    @Config.Name("quark")
    public static final Quark quark = new Quark();

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

        @Config.Name("bash_cost")
        @Config.Comment({
                "Stamina cost of one shield bash (hit or miss). A bash is refused at 0 Stamina or below and may go into debt.",
                "0 makes the bash free and never refused."})
        @Config.RangeDouble(min = 0.0, max = 1000.0)
        public double bashCost = BalanceDefaults.SHIELD_BASH_COST;
    }

    public static final class Crossbow {

        @Config.Name("loading_drain")
        @Config.Comment({
                "Stamina drained per second while loading a Spartan Weaponry crossbow. Loading stops when Stamina runs out",
                "and cannot start at 0 Stamina, even when the drain is 0 (as for a JASS bow). 0 makes loading free. Aiming a",
                "loaded crossbow uses the crossbow line of spartan_weaponry.weapon_types (draw_per_second)."})
        @Config.RangeDouble(min = 0.0, max = 1000.0)
        public double loadingDrain = BalanceDefaults.CROSSBOW_LOADING_DRAIN;
    }

    public static final class QualityTools {

        @Config.Name("enabled")
        @Config.Comment({
                "If false, the jassintegrations.* quality attributes do not exist, so Quality Tools qualities that name them",
                "have no effect on Stamina. This mod adds no qualities of its own. Needs a restart."})
        @Config.RequiresMcRestart
        public boolean enabled = true;
    }

    public static final class Quark {

        @Config.Name("quark_row")
        @Config.Comment({
                "Client only. If true and Quark: RotN Edition's Visual Stat Display is on, item tooltips show the JASS values as",
                "one row of icons and numbers (hold Shift for the JASS text). If false, the JASS text lines always show. Needs a restart."})
        @Config.RequiresMcRestart
        public boolean quarkRow = true;
    }
}