package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The one definition of "loading a Spartan crossbow": using a crossbow whose {@code isLoaded} tag
 * is not set. Spartan Weaponry loads a crossbow by holding the use key on an unloaded one, then
 * aims a loaded one as a bow; JASS drains the aiming, this mod drains the loading.
 */
public final class CrossbowLoading {

    /** Spartan Weaponry's item tag that marks a loaded crossbow. */
    static final String LOADED_TAG = "isLoaded";

    private CrossbowLoading() {
    }

    /** True when the stack is a Spartan crossbow that is not loaded. */
    public static boolean isUnloadedCrossbow(ItemStack stack) {
        return !stack.isEmpty() && isUnloadedCrossbow(stack.getItem().getClass(), stack.getTagCompound());
    }

    static boolean isUnloadedCrossbow(Class<?> itemClass, NBTTagCompound tag) {
        return "crossbow".equals(WeaponTypeClassifier.SPARTAN.classify(itemClass))
                && (tag == null || !tag.getBoolean(LOADED_TAG));
    }

    /**
     * Whether a loading attempt must be checked against JASS before it starts: stamina is 0 or
     * below. This holds whatever the loading drain, as for a JASS bow (REQ-033); JASS then decides
     * (Creative and gated players are exempt) and records the refusal.
     */
    static boolean needsStartCheck(double stamina) {
        return stamina <= 0.0;
    }
}
