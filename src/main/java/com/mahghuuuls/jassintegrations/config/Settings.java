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

    private static volatile Settings current = fromConfig(false, true, BalanceDefaults.WEAPON_TYPES,
            true, BalanceDefaults.SHIELD_TIERS, BalanceDefaults.SHIELD_MATERIALS, new ArrayList<>());

    public final boolean debugLogging;
    public final boolean spartanWeaponryEnabled;
    public final boolean spartanShieldsEnabled;
    private final Map<String, WeaponValues> weaponTypes;
    private final Map<String, ShieldValues> shieldTiers;
    private final Map<String, Integer> materialTiers;

    private Settings(boolean debugLogging, boolean spartanWeaponryEnabled, Map<String, WeaponValues> weaponTypes,
            boolean spartanShieldsEnabled, Map<String, ShieldValues> shieldTiers, Map<String, Integer> materialTiers) {
        this.debugLogging = debugLogging;
        this.spartanWeaponryEnabled = spartanWeaponryEnabled;
        this.weaponTypes = weaponTypes;
        this.spartanShieldsEnabled = spartanShieldsEnabled;
        this.shieldTiers = shieldTiers;
        this.materialTiers = materialTiers;
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
        List<String> warnings = new ArrayList<>();
        current = fromConfig(JassIntegrationsConfig.general.debugLogging,
                JassIntegrationsConfig.spartanWeaponry.enabled,
                JassIntegrationsConfig.spartanWeaponry.weaponTypes,
                JassIntegrationsConfig.spartanShields.enabled,
                JassIntegrationsConfig.spartanShields.shieldTiers,
                JassIntegrationsConfig.spartanShields.shieldMaterials, warnings);
        return warnings;
    }

    static Settings fromConfig(boolean debugLogging, boolean spartanWeaponryEnabled, String[] weaponTypeLines,
            boolean spartanShieldsEnabled, String[] shieldTierLines, String[] shieldMaterialLines, List<String> warnings) {
        Map<String, WeaponValues> weaponTypes = new LinkedHashMap<>();
        WEAPON_TYPE_LINES.parse(weaponTypeLines, DEFAULT_WEAPON_TYPES, warnings)
                .forEach((type, v) -> weaponTypes.put(type, new WeaponValues(v[0], v[1], v[2])));
        Map<String, ShieldValues> shieldTiers = new LinkedHashMap<>();
        SHIELD_TIER_LINES.parse(shieldTierLines, DEFAULT_SHIELD_TIERS, warnings)
                .forEach((tier, v) -> shieldTiers.put(tier, new ShieldValues(v[0], v[1], v[2], v[3])));
        Map<String, Integer> materialTiers = new LinkedHashMap<>();
        SHIELD_MATERIAL_LINES.parse(shieldMaterialLines, DEFAULT_SHIELD_MATERIALS, warnings)
                .forEach((material, v) -> materialTiers.put(material, (int) v[0]));
        return new Settings(debugLogging, spartanWeaponryEnabled, Collections.unmodifiableMap(weaponTypes),
                spartanShieldsEnabled, Collections.unmodifiableMap(shieldTiers), Collections.unmodifiableMap(materialTiers));
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
