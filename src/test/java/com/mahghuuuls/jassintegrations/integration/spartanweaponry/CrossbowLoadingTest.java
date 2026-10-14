package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrossbowLoadingTest {

    private static Class<?> spartan(String simpleName) throws ClassNotFoundException {
        return Class.forName("com.oblivioussp.spartanweaponry.item." + simpleName, false,
                CrossbowLoadingTest.class.getClassLoader());
    }

    private static NBTTagCompound loaded(boolean value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("isLoaded", value);
        return tag;
    }

    @Test
    void anUnloadedSpartanCrossbowIsLoading() throws ClassNotFoundException {
        assertTrue(CrossbowLoading.isUnloadedCrossbow(spartan("ItemCrossbow"), null));
        assertTrue(CrossbowLoading.isUnloadedCrossbow(spartan("ItemCrossbow"), new NBTTagCompound()));
        assertTrue(CrossbowLoading.isUnloadedCrossbow(spartan("ItemCrossbow"), loaded(false)));
    }

    @Test
    void aLoadedCrossbowOrAnotherItemIsNot() throws ClassNotFoundException {
        assertFalse(CrossbowLoading.isUnloadedCrossbow(spartan("ItemCrossbow"), loaded(true)));
        assertFalse(CrossbowLoading.isUnloadedCrossbow(spartan("ItemLongbow"), null));
        assertFalse(CrossbowLoading.isUnloadedCrossbow(String.class, null));
    }

    @Test
    void startIsCheckedAtZeroOrBelowOnly() {
        assertTrue(CrossbowLoading.needsStartCheck(0.0));
        assertTrue(CrossbowLoading.needsStartCheck(-3.0));
        assertFalse(CrossbowLoading.needsStartCheck(0.5));
    }
}
