package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The seven quality attributes. Quality Tools finds an attribute by its name on the entity, so each
 * one is installed on every player and needs no Quality Tools class. Only this class knows the names
 * and that only modifiers with operation 0 (add the amount) count.
 */
public final class QualityAttributes {

    /**
     * One quality attribute and the stamina bonus it gives; the attribute name is
     * {@code jassintegrations.<key>}. Worlds save the names, so a key never changes.
     */
    public enum Kind {
        MAX_STAMINA_FLAT("max_stamina_flat", StaminaBonus.MAX_STAMINA_FLAT),
        MAX_STAMINA_PERCENT("max_stamina_percent", StaminaBonus.MAX_STAMINA_PERCENT),
        REGENERATION_FLAT("regeneration_flat", StaminaBonus.REGENERATION_FLAT),
        REGENERATION_PERCENT("regeneration_percent", StaminaBonus.REGENERATION_PERCENT),
        DELAY_REDUCTION_FLAT("delay_reduction_flat", StaminaBonus.DELAY_REDUCTION_FLAT),
        DELAY_REDUCTION_PERCENT("delay_reduction_percent", StaminaBonus.DELAY_REDUCTION_PERCENT),
        STAMINA_EFFICIENCY("stamina_efficiency", StaminaBonus.STAMINA_EFFICIENCY);

        final String key;
        final StaminaBonus bonus;

        Kind(String key, StaminaBonus bonus) {
            this.key = key;
            this.bonus = bonus;
        }

        public String attributeName() {
            return Tags.MOD_ID + "." + key;
        }
    }

    /** Quality attribute entries count only with this operation (add the amount). */
    static final int USED_OPERATION = 0;

    private static final Map<Kind, IAttribute> ATTRIBUTES = new EnumMap<>(Kind.class);

    /** Attribute name and operation pairs already warned about, so each is logged once per game. */
    private static final Set<String> WARNED_OPERATIONS = ConcurrentHashMap.newKeySet();

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

    /** The player's stamina bonus totals, as {@link #usedTotal} counts each attribute. */
    static StaminaBonusTotals totals(EntityPlayer player) {
        AbstractAttributeMap map = player.getAttributeMap();
        double[] values = new double[StaminaBonus.values().length];
        for (Kind kind : Kind.values()) {
            IAttributeInstance instance = map.getAttributeInstance(ATTRIBUTES.get(kind));
            if (instance == null) {
                continue;
            }
            values[kind.bonus.ordinal()] = usedTotal(kind.attributeName(), instance.getModifiers());
        }
        return new StaminaBonusTotals(values);
    }

    /**
     * The sum of the amounts of the modifiers with the used operation. A modifier with another
     * operation is left out and warned about once per attribute and operation.
     */
    static double usedTotal(String attributeName, Collection<AttributeModifier> modifiers) {
        return usedTotal(attributeName, modifiers, WARNED_OPERATIONS, DebugLog::warn);
    }

    static double usedTotal(String attributeName, Collection<AttributeModifier> modifiers, Set<String> warned,
            Consumer<String> warn) {
        double total = 0.0;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == USED_OPERATION) {
                total += modifier.getAmount();
            } else if (warned.add(attributeName + ":" + modifier.getOperation())) {
                warn.accept("Quality Tools: " + attributeName + " with operation " + modifier.getOperation()
                        + " ignored (only operation " + USED_OPERATION + " is used)");
            }
        }
        return total;
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
