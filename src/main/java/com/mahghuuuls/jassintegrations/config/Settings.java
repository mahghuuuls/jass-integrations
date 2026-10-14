package com.mahghuuuls.jassintegrations.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The parsed, checked configuration. The only reader of {@link JassIntegrationsConfig}: everything
 * else asks {@link #current()}, which a configuration change replaces as a whole.
 */
public final class Settings {

    private static final ValueLines WEAPON_TYPE_LINES = new ValueLines("spartan_weaponry.weapon_types",
            "type=swing_cost,held_weight,draw_per_second", true,
            ValueLines.ZERO_OR_MORE, ValueLines.ZERO_OR_MORE, ValueLines.ZERO_OR_MORE);
    private static final ValueLines SHIELD_TIER_LINES = new ValueLines("spartan_shields.shield_tiers",
            "tier_<n>_<basic|tower>=stability,melee_block,projectile_block,held_weight", false,
            ValueLines.ZERO_OR_MORE, ValueLines.SHARE, ValueLines.SHARE, ValueLines.ZERO_OR_MORE);
    private static final ValueLines SHIELD_MATERIAL_LINES = new ValueLines("spartan_shields.shield_materials",
            "material=tier", false,
            v -> v == Math.rint(v) && v >= 1.0 && v <= BalanceDefaults.SHIELD_TIER_COUNT
                    ? null : "must be a whole tier number from 1 to " + BalanceDefaults.SHIELD_TIER_COUNT);

    private static final Map<String, double[]> DEFAULT_WEAPON_TYPES = defaults(WEAPON_TYPE_LINES, BalanceDefaults.WEAPON_TYPES);
    private static final Map<String, double[]> DEFAULT_SHIELD_TIERS = defaults(SHIELD_TIER_LINES, BalanceDefaults.SHIELD_TIERS);
    private static final Map<String, double[]> DEFAULT_SHIELD_MATERIALS = defaults(SHIELD_MATERIAL_LINES, BalanceDefaults.SHIELD_MATERIALS);

    private static volatile Settings current = fromConfig(new Raw(), new ArrayList<>());

    public final boolean debugLogging;
    public final boolean spartanWeaponryEnabled;
    public final boolean spartanShieldsEnabled;
    private final Map<String, WeaponValues> weaponTypes;
    private final Map<String, ShieldValues> shieldTiers;
    private final Map<String, Integer> materialTiers;
    /** One-time cost of a Spartan Shields shield bash; 0 makes the bash free and never refused. */
    public final double shieldBashCost;
    /** Stamina drained per second while loading a Spartan crossbow; 0 makes loading free. */
    public final double crossbowLoadingDrain;

    /**
     * The raw configuration values, before parsing. Starts at the shipped defaults; {@link #reload()}
     * fills it from the configuration file and tests fill only what they check.
     */
    static final class Raw {
        boolean debugLogging = false;
        boolean spartanWeaponryEnabled = true;
        String[] weaponTypes = BalanceDefaults.WEAPON_TYPES;
        boolean spartanShieldsEnabled = true;
        String[] shieldTiers = BalanceDefaults.SHIELD_TIERS;
        String[] shieldMaterials = BalanceDefaults.SHIELD_MATERIALS;
        double shieldBashCost = BalanceDefaults.SHIELD_BASH_COST;
        double crossbowLoadingDrain = BalanceDefaults.CROSSBOW_LOADING_DRAIN;
    }

    private Settings(Raw raw, Map<String, WeaponValues> weaponTypes, Map<String, ShieldValues> shieldTiers,
            Map<String, Integer> materialTiers, double shieldBashCost, double crossbowLoadingDrain) {
        this.debugLogging = raw.debugLogging;
        this.spartanWeaponryEnabled = raw.spartanWeaponryEnabled;
        this.spartanShieldsEnabled = raw.spartanShieldsEnabled;
        this.weaponTypes = weaponTypes;
        this.shieldTiers = shieldTiers;
        this.materialTiers = materialTiers;
        this.shieldBashCost = shieldBashCost;
        this.crossbowLoadingDrain = crossbowLoadingDrain;
    }

    public static Settings current() {
        return current;
    }

    /**
     * Rebuilds the snapshot from the configuration file fields.
     *
     * @return one warning per rejected line, for the caller to log
     */
    public static List<String> reload() {
        Raw raw = new Raw();
        raw.debugLogging = JassIntegrationsConfig.general.debugLogging;
        raw.spartanWeaponryEnabled = JassIntegrationsConfig.spartanWeaponry.enabled;
        raw.weaponTypes = JassIntegrationsConfig.spartanWeaponry.weaponTypes;
        raw.spartanShieldsEnabled = JassIntegrationsConfig.spartanShields.enabled;
        raw.shieldTiers = JassIntegrationsConfig.spartanShields.shieldTiers;
        raw.shieldMaterials = JassIntegrationsConfig.spartanShields.shieldMaterials;
        raw.shieldBashCost = JassIntegrationsConfig.spartanShields.bashCost;
        raw.crossbowLoadingDrain = JassIntegrationsConfig.crossbow.loadingDrain;
        List<String> warnings = new ArrayList<>();
        current = fromConfig(raw, warnings);
        return warnings;
    }

    static Settings fromConfig(Raw raw, List<String> warnings) {
        Map<String, WeaponValues> weaponTypes = new LinkedHashMap<>();
        WEAPON_TYPE_LINES.parse(raw.weaponTypes, DEFAULT_WEAPON_TYPES, warnings)
                .forEach((type, v) -> weaponTypes.put(type, new WeaponValues(v[0], v[1], v[2])));
        Map<String, ShieldValues> shieldTiers = new LinkedHashMap<>();
        SHIELD_TIER_LINES.parse(raw.shieldTiers, DEFAULT_SHIELD_TIERS, warnings)
                .forEach((tier, v) -> shieldTiers.put(tier, new ShieldValues(v[0], v[1], v[2], v[3])));
        Map<String, Integer> materialTiers = new LinkedHashMap<>();
        SHIELD_MATERIAL_LINES.parse(raw.shieldMaterials, DEFAULT_SHIELD_MATERIALS, warnings)
                .forEach((material, v) -> materialTiers.put(material, (int) v[0]));
        double bashCost = zeroOrMore("spartan_shields.bash_cost", raw.shieldBashCost,
                BalanceDefaults.SHIELD_BASH_COST, warnings);
        double loadingDrain = zeroOrMore("crossbow.loading_drain", raw.crossbowLoadingDrain,
                BalanceDefaults.CROSSBOW_LOADING_DRAIN, warnings);
        return new Settings(raw, Collections.unmodifiableMap(weaponTypes), Collections.unmodifiableMap(shieldTiers),
                Collections.unmodifiableMap(materialTiers), bashCost, loadingDrain);
    }

    private static double zeroOrMore(String option, double value, double fallback, List<String> warnings) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0.0) {
            warnings.add(option + " " + value + " ignored (must be 0 or more); the default is kept");
            return fallback;
        }
        return value;
    }

    private static Map<String, double[]> defaults(ValueLines parser, String[] lines) {
        List<String> warnings = new ArrayList<>();
        Map<String, double[]> parsed = parser.parse(lines, null, warnings);
        if (!warnings.isEmpty()) {
            throw new IllegalStateException("Shipped defaults are invalid: " + warnings);
        }
        return Collections.unmodifiableMap(parsed);
    }

    /** The values of a weapon type, or {@code null} for an unknown type. */
    public WeaponValues weaponType(String type) {
        return weaponTypes.get(type);
    }

    /** The values of a shield kind ({@code basic} or {@code tower}) and material, or {@code null} for an unknown material. */
    public ShieldValues shield(String kind, String material) {
        String tier = shieldTierKey(kind, material);
        return tier == null ? null : shieldTiers.get(tier);
    }

    /** The shield tier line key ({@code tier_<n>_<kind>}) of a kind and material, or {@code null} for an unknown material. */
    public String shieldTierKey(String kind, String material) {
        Integer tier = materialTiers.get(material);
        return tier == null ? null : "tier_" + tier + "_" + kind;
    }

    /** The known weapon types, in default order. */
    public static List<String> weaponTypeKeys() {
        return new ArrayList<>(DEFAULT_WEAPON_TYPES.keySet());
    }

    /** The known shield materials, in default order. */
    public static List<String> shieldMaterialKeys() {
        return new ArrayList<>(DEFAULT_SHIELD_MATERIALS.keySet());
    }

    /** Shipped default lines parsed as {@code key -> values}, for the defaults parity test. */
    static Map<String, double[]> shippedDefaults(String option) {
        switch (option) {
            case "weapon_types":
                return DEFAULT_WEAPON_TYPES;
            case "shield_tiers":
                return DEFAULT_SHIELD_TIERS;
            case "shield_materials":
                return DEFAULT_SHIELD_MATERIALS;
            default:
                throw new IllegalArgumentException(option);
        }
    }
}
