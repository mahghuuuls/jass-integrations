package com.mahghuuuls.jassintegrations.integration.spartanshields;

import com.mahghuuuls.jass.api.IItemValueProvider;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.config.ShieldValues;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Gives every Spartan shield the values of its shield kind and tier. The kind and material of an
 * item never change, so they are cached per item; the tier and its values come from the current
 * {@link Settings}, so a configuration change applies at once.
 */
final class SpartanShieldValueProvider implements IItemValueProvider {

    private final ConcurrentMap<Item, Optional<ShieldClassifier.Shield>> shields = new ConcurrentHashMap<>();

    @Override
    public double shieldStability(ItemStack stack) {
        ShieldValues values = values(stack);
        return values == null ? Double.NaN : values.stability;
    }

    @Override
    public double shieldMeleeBlock(ItemStack stack) {
        ShieldValues values = values(stack);
        return values == null ? Double.NaN : values.meleeBlock;
    }

    @Override
    public double shieldProjectileBlock(ItemStack stack) {
        ShieldValues values = values(stack);
        return values == null ? Double.NaN : values.projectileBlock;
    }

    @Override
    public double heldWeight(ItemStack stack) {
        ShieldValues values = values(stack);
        return values == null ? Double.NaN : values.heldWeight;
    }

    private ShieldValues values(ItemStack stack) {
        Optional<ShieldClassifier.Shield> shield = shield(stack.getItem());
        return shield.map(s -> Settings.current().shield(s.kind, s.material)).orElse(null);
    }

    Optional<ShieldClassifier.Shield> shield(Item item) {
        Optional<ShieldClassifier.Shield> shield = shields.get(item);
        if (shield == null) {
            shield = shields.computeIfAbsent(item, i -> Optional.ofNullable(ShieldClassifier.classify(i.getRegistryName())));
        }
        return shield;
    }
}
