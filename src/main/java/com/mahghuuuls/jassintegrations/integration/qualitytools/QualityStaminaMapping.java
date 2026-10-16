package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The only place that knows the units and signs of the quality attributes: turns a player's totals
 * into one JASS contribution. Flat amounts are points (seconds for the delay); percent amounts are
 * percent, and a percent below -100 counts as -100. A positive amount always helps the player. Only
 * modifiers with operation 0 (add the amount) count.
 */
final class QualityStaminaMapping {

    /** Quality attribute entries count only with this operation (add the amount). */
    static final int USED_OPERATION = 0;

    private static final double PERCENT_FLOOR = -100.0;

    private QualityStaminaMapping() {
    }

    /** Attribute name and operation pairs already warned about, so each is logged once per game. */
    private static final Set<String> WARNED_OPERATIONS = ConcurrentHashMap.newKeySet();

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

    static StaminaContribution map(QualityTotals totals) {
        StaminaContribution.Builder c = StaminaContribution.builder();
        c.flatMaximum(finite(totals.get(QualityAttributes.Kind.MAX_STAMINA_FLAT)));
        double maximumPercent = percent(totals.get(QualityAttributes.Kind.MAX_STAMINA_PERCENT));
        if (maximumPercent >= 0.0) {
            c.maximumIncrease(maximumPercent / 100.0);
        } else {
            c.maximumReduction(-maximumPercent / 100.0);
        }
        c.flatRegeneration(finite(totals.get(QualityAttributes.Kind.REGENERATION_FLAT)));
        double regenerationPercent = percent(totals.get(QualityAttributes.Kind.REGENERATION_PERCENT));
        if (regenerationPercent >= 0.0) {
            c.regenerationIncrease(regenerationPercent / 100.0);
        } else {
            c.regenerationReduction(-regenerationPercent / 100.0);
        }
        // A delay reduction shortens the delay, so its sign is the opposite of JASS's delay fields.
        c.flatDelay(-finite(totals.get(QualityAttributes.Kind.DELAY_REDUCTION_FLAT)));
        double delayPercent = percent(totals.get(QualityAttributes.Kind.DELAY_REDUCTION_PERCENT));
        if (delayPercent >= 0.0) {
            // Above 100 percent JASS counts the reduction as 100 percent.
            c.delayReduction(delayPercent / 100.0);
        } else {
            c.delayIncrease(-delayPercent / 100.0);
        }
        // A total below 0 counts as 0 in JASS, across every source, so it is not clamped here.
        c.efficiency(finite(totals.get(QualityAttributes.Kind.STAMINA_EFFICIENCY)));
        return c.build();
    }

    private static double percent(double amount) {
        return Math.max(PERCENT_FLOOR, finite(amount));
    }

    /** A sum that overflowed counts as no amount; JASS refuses values that are not finite. */
    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value;
    }
}
