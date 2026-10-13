package com.mahghuuuls.jassintegrations.integration.spartanshields;

import net.minecraft.util.ResourceLocation;

/**
 * The one place that knows Spartan Shields' item names. Shields of one material share a class with
 * both kinds, so the kind and the material come from the registry path:
 * {@code shield_basic_<material>}, {@code shield_tower_<material>}, and the basic-only families
 * {@code shield_botania_}, {@code shield_abyssalcraft_}, {@code shield_tc_}, {@code shield_riot_},
 * and {@code shield_flux_ra}.
 */
final class ShieldClassifier {

    static final String NAMESPACE = "spartanshields";
    static final String BASIC = "basic";
    static final String TOWER = "tower";

    /** A shield's kind ({@link #BASIC} or {@link #TOWER}) and material. */
    static final class Shield {
        final String kind;
        final String material;

        Shield(String kind, String material) {
            this.kind = kind;
            this.material = material;
        }
    }

    private ShieldClassifier() {
    }

    /** Whether an item id is named like a Spartan Shields shield (whether or not its pattern is known). */
    static boolean isShieldName(ResourceLocation id) {
        return id != null && NAMESPACE.equals(id.getNamespace()) && id.getPath().startsWith("shield_");
    }

    /** The kind and material of an item id, or {@code null} when it is not a Spartan shield of a known pattern. */
    static Shield classify(ResourceLocation id) {
        return isShieldName(id) ? classify(id.getPath()) : null;
    }

    /** The kind and material of a Spartan Shields registry path, or {@code null} when the name is not a known pattern. */
    static Shield classify(String path) {
        if (path.startsWith("shield_tower_")) {
            return shield(TOWER, path.substring("shield_tower_".length()));
        }
        if (path.startsWith("shield_basic_")) {
            return shield(BASIC, path.substring("shield_basic_".length()));
        }
        for (String family : new String[] {"shield_botania_", "shield_abyssalcraft_", "shield_tc_"}) {
            if (path.startsWith(family)) {
                return shield(BASIC, path.substring(family.length()));
            }
        }
        if ((path.startsWith("shield_riot_") && path.length() > "shield_riot_".length()) || path.equals("shield_flux_ra")) {
            return shield(BASIC, path.substring("shield_".length()));
        }
        return null;
    }

    private static Shield shield(String kind, String material) {
        return material.isEmpty() ? null : new Shield(kind, material);
    }
}
