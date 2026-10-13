package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maps an item class to its Spartan Weaponry weapon type. Each weapon type is one Spartan item
 * class; add-on weapons either subclass those classes or are built from them, so the nearest
 * mapped class in an item's superclass chain gives its type. Classes are matched by name, so this
 * class never loads a Spartan class itself.
 */
final class WeaponTypeClassifier {

    private static final String ITEM_PACKAGE = "com.oblivioussp.spartanweaponry.item.";

    /** Spartan Weaponry 1.6.1 class name to weapon type (the keys of the weapon_types config). */
    static final Map<String, String> SPARTAN_CLASS_TYPES;

    static {
        Map<String, String> types = new LinkedHashMap<>();
        put(types, "ItemDagger", "dagger");
        put(types, "ItemParryingDagger", "parrying_dagger");
        put(types, "ItemCaestus", "caestus");
        put(types, "ItemThrowingKnife", "throwing_knife");
        put(types, "ItemBoomerang", "boomerang");
        put(types, "ItemRapier", "rapier");
        put(types, "ItemSaber", "saber");
        put(types, "ItemThrowingAxe", "throwing_axe");
        put(types, "ItemLongsword", "longsword");
        put(types, "ItemKatana", "katana");
        put(types, "ItemSpear", "spear");
        put(types, "ItemJavelin", "javelin");
        put(types, "ItemQuarterstaff", "quarterstaff");
        put(types, "ItemMace", "mace");
        put(types, "ItemClub", "club");
        put(types, "ItemHammer", "hammer");
        put(types, "ItemBattleaxe", "battleaxe");
        put(types, "ItemGlaive", "glaive");
        put(types, "ItemHalberd", "halberd");
        put(types, "ItemPike", "pike");
        put(types, "ItemLance", "lance");
        put(types, "ItemScythe", "scythe");
        put(types, "ItemGreatsword", "greatsword");
        put(types, "ItemWarhammer", "warhammer");
        put(types, "ItemLongbow", "longbow");
        put(types, "ItemCrossbow", "crossbow");
        SPARTAN_CLASS_TYPES = Collections.unmodifiableMap(types);
    }

    private static void put(Map<String, String> types, String simpleName, String type) {
        types.put(ITEM_PACKAGE + simpleName, type);
    }

    private static final String NONE = "";

    private final Map<String, String> classTypes;
    private final ConcurrentMap<Class<?>, String> cache = new ConcurrentHashMap<>();

    WeaponTypeClassifier(Map<String, String> classTypes) {
        this.classTypes = classTypes;
    }

    /** The weapon type of an item class, or {@code null} when it is not a Spartan weapon. */
    String classify(Class<?> itemClass) {
        String type = cache.get(itemClass);
        if (type == null) {
            type = cache.computeIfAbsent(itemClass, this::nearestMappedType);
        }
        return type.isEmpty() ? null : type;
    }

    private String nearestMappedType(Class<?> itemClass) {
        for (Class<?> c = itemClass; c != null; c = c.getSuperclass()) {
            String type = classTypes.get(c.getName());
            if (type != null) {
                return type;
            }
        }
        return NONE;
    }
}
