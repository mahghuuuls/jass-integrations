package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import com.mahghuuuls.jass.api.IItemValueProvider;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.config.WeaponValues;
import net.minecraft.item.ItemStack;

/**
 * Gives every Spartan weapon the values of its weapon type. JASS asks on the server for costs and
 * on the client for tooltips, from several threads; the answers depend only on the item class and
 * the current {@link Settings}.
 */
final class SpartanWeaponValueProvider implements IItemValueProvider {

    private final WeaponTypeClassifier classifier;

    SpartanWeaponValueProvider(WeaponTypeClassifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public double meleeCost(ItemStack stack) {
        WeaponValues values = values(stack);
        return values == null ? Double.NaN : values.swingCost;
    }

    @Override
    public double heldWeight(ItemStack stack) {
        WeaponValues values = values(stack);
        return values == null ? Double.NaN : values.heldWeight;
    }

    @Override
    public double bowDrawCost(ItemStack stack) {
        WeaponValues values = values(stack);
        return values == null ? Double.NaN : values.drawPerSecond;
    }

    private WeaponValues values(ItemStack stack) {
        String type = classifier.classify(stack.getItem().getClass());
        return type == null ? null : Settings.current().weaponType(type);
    }
}
