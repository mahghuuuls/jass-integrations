package com.mahghuuuls.jassintegrations.integration.jafs;

import com.mahghuuuls.jafs.api.JafsApi;
import com.mahghuuuls.jafs.api.PropertyOperation;
import com.mahghuuuls.jafs.api.PropertySpec;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonus;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.List;

/**
 * Entry class of the JAFS integration; created by {@link IntegrationGate} by name. Registers this
 * mod's stamina properties with JAFS, so food definitions can name them, and gives JASS the players'
 * sums. Only this class knows the property ids, ranges, display units and display keys.
 */
public final class JafsIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "jafs");

    /** Tooltip color of every property, a vanilla text formatting name. */
    static final String COLOR = "yellow";

    /** The JAFS API major version this integration was written for. */
    static final String SUPPORTED_API_MAJOR = "1.";

    /**
     * One JAFS property: its id path, the unit JAFS shows after a flat amount, and the stamina bonus
     * and per-entry range of each operation. A property without a percent bonus supports flat only.
     */
    enum Property {
        MAX_STAMINA("max_stamina", "", StaminaBonus.MAX_STAMINA_FLAT, -1000, 1000,
                StaminaBonus.MAX_STAMINA_PERCENT, -100, 1000),
        STAMINA_REGENERATION("stamina_regeneration", "/s", StaminaBonus.REGENERATION_FLAT, -1000, 1000,
                StaminaBonus.REGENERATION_PERCENT, -100, 1000),
        REGENERATION_DELAY_REDUCTION("regeneration_delay_reduction", "s", StaminaBonus.DELAY_REDUCTION_FLAT, -60, 60,
                StaminaBonus.DELAY_REDUCTION_PERCENT, -100, 100),
        STAMINA_EFFICIENCY("stamina_efficiency", "", StaminaBonus.STAMINA_EFFICIENCY, -1000, 1000);

        final String path;
        final String unit;
        final StaminaBonus flat;
        final double flatMin;
        final double flatMax;
        final StaminaBonus percent;
        final double percentMin;
        final double percentMax;

        /** A property with a flat operation only. */
        Property(String path, String unit, StaminaBonus flat, double flatMin, double flatMax) {
            this(path, unit, flat, flatMin, flatMax, null, 0, 0);
        }

        Property(String path, String unit, StaminaBonus flat, double flatMin, double flatMax,
                StaminaBonus percent, double percentMin, double percentMax) {
            this.path = path;
            this.unit = unit;
            this.flat = flat;
            this.flatMin = flatMin;
            this.flatMax = flatMax;
            this.percent = percent;
            this.percentMin = percentMin;
            this.percentMax = percentMax;
        }

        /** Namespaced id used in food definitions, for example {@code jassintegrations:max_stamina}. */
        String id() {
            return Tags.MOD_ID + ":" + path;
        }

        PropertySpec spec() {
            // The display key is JAFS's default, property.jassintegrations.<path>.
            PropertySpec.Builder spec = PropertySpec.builder(id()).unit(unit).color(COLOR)
                    .operation(PropertyOperation.FLAT, flatMin, flatMax);
            if (percent != null) {
                spec.operation(PropertyOperation.PERCENT, percentMin, percentMax);
            }
            return spec.build();
        }
    }

    @Override
    public List<String> compatibilityProblems() {
        if (!IntegrationGate.hasMethod("com.mahghuuuls.jafs.api.JafsApi", "registerProperty",
                "com.mahghuuuls.jafs.api.PropertySpec", "com.mahghuuuls.jafs.api.IPropertyApplier")) {
            return Collections.singletonList("JafsApi.registerProperty(PropertySpec, IPropertyApplier) not found");
        }
        String version = apiVersion();
        if (version == null || !version.startsWith(SUPPORTED_API_MAJOR)) {
            return Collections.singletonList("JAFS API version " + version + " (supported: " + SUPPORTED_API_MAJOR + "x)");
        }
        return Collections.emptyList();
    }

    /** JAFS's {@code JafsApi.API_VERSION}, read at run time (a compiled-in constant would be this build's copy), or {@code null}. */
    static String apiVersion() {
        try {
            Object value = Class.forName("com.mahghuuuls.jafs.api.JafsApi").getField("API_VERSION").get(null);
            return value instanceof String ? (String) value : null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /** Runs during pre-initialization, after JAFS's own ({@code after:jafs}), while JAFS takes registrations. */
    @Override
    public void start() {
        JafsStaminaApplier applier = new JafsStaminaApplier();
        for (Property property : Property.values()) {
            JafsApi.registerProperty(property.spec(), applier.forProperty(property));
        }
        JassApi.registerContextualProvider(PROVIDER_ID, new JafsStaminaProvider(applier));
        MinecraftForge.EVENT_BUS.register(applier);
    }
}
