package com.mahghuuuls.jassintegrations.integration.setworks;

import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaProperty;
import com.mahghuuuls.setworks.api.CombinationRule;
import com.mahghuuuls.setworks.api.IEffectKind;
import net.minecraft.entity.player.EntityPlayerMP;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The Setworks effect kind of one {@link StaminaProperty}, for example {@code jassintegrations:max_stamina}
 * with the parameters {@code operation} ({@code flat} or {@code percent}) and {@code amount}. Checks the
 * parameters when Setworks validates the definitions and hands applied effects to
 * {@link SetworksStaminaEffects}.
 */
final class SetworksStaminaEffect implements IEffectKind {

    static final String OPERATION = "operation";
    static final String AMOUNT = "amount";

    /** A plain decimal number such as {@code 10}, {@code -0.25} or {@code .5}; no exponent, suffix or hex. */
    private static final Pattern NUMBER = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    private final StaminaProperty property;
    private final SetworksStaminaEffects effects;

    SetworksStaminaEffect(StaminaProperty property, SetworksStaminaEffects effects) {
        this.property = property;
        this.effects = effects;
    }

    /** Parsed, checked parameters: the stamina bonus they give and its amount. */
    static final class Parsed {
        final boolean percent;
        final StaminaBonus bonus;
        final double amount;

        Parsed(boolean percent, StaminaBonus bonus, double amount) {
            this.percent = percent;
            this.bonus = bonus;
            this.amount = amount;
        }
    }

    /**
     * The parameters as a stamina bonus.
     *
     * @throws IllegalArgumentException with a short problem text when they are not usable
     */
    static Parsed parse(StaminaProperty property, Map<String, String> params) {
        for (String key : params.keySet()) {
            if (!OPERATION.equals(key) && !AMOUNT.equals(key)) {
                throw new IllegalArgumentException("unknown parameter '" + key + "' (use operation and amount)");
            }
        }
        String operation = params.get(OPERATION);
        boolean percent;
        if ("flat".equalsIgnoreCase(operation)) {
            percent = false;
        } else if ("percent".equalsIgnoreCase(operation) && property.supportsPercent()) {
            percent = true;
        } else {
            throw new IllegalArgumentException("operation must be " + (property.supportsPercent() ? "flat or percent" : "flat")
                    + " for " + property.id() + ", not '" + operation + "'");
        }
        String text = params.get(AMOUNT);
        String trimmed = text == null ? "" : text.trim();
        if (!NUMBER.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("amount must be a number, not '" + text + "'");
        }
        double amount = Double.parseDouble(trimmed);
        double min = property.minimum(percent);
        double max = property.maximum(percent);
        if (amount < min || amount > max) {
            throw new IllegalArgumentException("amount " + text + " is outside " + format(min) + " to " + format(max)
                    + " for " + property.id() + " " + (percent ? "percent" : "flat"));
        }
        return new Parsed(percent, property.bonus(percent), amount);
    }

    /** {@code +10%} or {@code +0.25 s} and the translatable name, for example {@code +10% ${property.jassintegrations.max_stamina}}. */
    static String describe(StaminaProperty property, Parsed parsed) {
        String number = (parsed.amount >= 0 ? "+" : "") + format(parsed.amount);
        String amount = parsed.percent ? number + "%" : property.unit.isEmpty() ? number : number + " " + property.unit;
        return amount + " ${" + property.languageKey() + "}";
    }

    /** A number without a trailing {@code .0}: {@code 10}, {@code 0.25}. */
    static String format(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().toLowerCase(Locale.ROOT);
    }

    @Override
    public String id() {
        return property.id();
    }

    @Override
    public CombinationRule combinationRule() {
        return CombinationRule.SUM;
    }

    @Override
    public String validate(Map<String, String> params) {
        try {
            parse(property, params);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @Override
    public String describe(Map<String, String> params) {
        try {
            return describe(property, parse(property, params));
        } catch (IllegalArgumentException e) {
            return property.id();
        }
    }

    @Override
    public void apply(EntityPlayerMP player, String instanceId, Map<String, String> params) {
        Parsed parsed = parse(property, params);
        effects.apply(player, instanceId, parsed.bonus, parsed.amount);
    }

    @Override
    public void withdraw(EntityPlayerMP player, String instanceId, Map<String, String> params) {
        effects.withdraw(player, instanceId);
    }
}
