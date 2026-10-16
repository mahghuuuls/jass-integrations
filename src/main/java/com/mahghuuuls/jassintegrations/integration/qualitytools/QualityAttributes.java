package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jassintegrations.Tags;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * The seven quality attributes. Quality Tools finds an attribute by its name on the entity, so each
 * one is installed on every player and needs no Quality Tools class. Only this class knows the names.
 */
public final class QualityAttributes {

    /** One quality attribute; the attribute name is {@code jassintegrations.<key>}. */
    public enum Kind {
        MAX_STAMINA_FLAT("max_stamina_flat"),
        MAX_STAMINA_PERCENT("max_stamina_percent"),
        REGENERATION_FLAT("regeneration_flat"),
        REGENERATION_PERCENT("regeneration_percent"),
        DELAY_REDUCTION_FLAT("delay_reduction_flat"),
        DELAY_REDUCTION_PERCENT("delay_reduction_percent"),
        STAMINA_EFFICIENCY("stamina_efficiency");

        final String key;

        Kind(String key) {
            this.key = key;
        }

        public String attributeName() {
            return Tags.MOD_ID + "." + key;
        }
    }

    private static final Map<Kind, IAttribute> ATTRIBUTES = new EnumMap<>(Kind.class);

    static {
        for (Kind kind : Kind.values()) {
            // Quality Tools adds modifiers on the server and builds its tooltip from the item, so the
            // client never needs the values: not watched.
            ATTRIBUTES.put(kind, new RangedAttribute(null, kind.attributeName(), 0.0, -Double.MAX_VALUE,
                    Double.MAX_VALUE).setShouldWatch(false));
        }
    }


    private QualityAttributes() {
    }

    /** Installs the attributes on a player that does not have them yet. */
    static void install(EntityPlayer player) {
        AbstractAttributeMap map = player.getAttributeMap();
        for (IAttribute attribute : ATTRIBUTES.values()) {
            if (map.getAttributeInstance(attribute) == null) {
                map.registerAttribute(attribute);
            }
        }
    }

    /** The player's totals per attribute, as {@link QualityStaminaMapping#usedTotal} counts them. */
    static QualityTotals totals(EntityPlayer player) {
        AbstractAttributeMap map = player.getAttributeMap();
        double[] values = new double[Kind.values().length];
        for (Kind kind : Kind.values()) {
            IAttributeInstance instance = map.getAttributeInstance(ATTRIBUTES.get(kind));
            if (instance == null) {
                continue;
            }
            values[kind.ordinal()] = QualityStaminaMapping.usedTotal(kind.attributeName(), instance.getModifiers());
        }
        return new QualityTotals(values);
    }

    /** Installs the attributes on every player when the player entity is created, on both sides. */
    static final class Installer {

        @SubscribeEvent
        public void onEntityConstructing(EntityEvent.EntityConstructing event) {
            if (event.getEntity() instanceof EntityPlayer) {
                install((EntityPlayer) event.getEntity());
            }
        }
    }
}
