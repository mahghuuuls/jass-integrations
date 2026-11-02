package com.mahghuuuls.jassintegrations.integration.jafs;

import com.mahghuuuls.jafs.api.JafsApi;
import com.mahghuuuls.jafs.api.PropertyOperation;
import com.mahghuuuls.jafs.api.PropertySpec;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.List;

/**
 * Entry class of the JAFS integration; created by {@link IntegrationGate} by name. Registers this
 * mod's stamina properties with JAFS, so food definitions can name them, and gives JASS the players'
 * sums. The properties come from {@link StaminaProperty}; this class owns only how JAFS sees them.
 */
public final class JafsIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "jafs");

    /** Tooltip color of every property, a vanilla text formatting name. */
    static final String COLOR = "yellow";

    /** The JAFS API major version this integration was written for. */
    static final String SUPPORTED_API_MAJOR = "1.";

    /** The JAFS property spec of a stamina property: its id, display key and unit, and per-entry ranges. */
    static PropertySpec spec(StaminaProperty property) {
        PropertySpec.Builder spec = PropertySpec.builder(property.id()).displayNameKey(property.languageKey())
                .unit(property.unit).color(COLOR)
                .operation(PropertyOperation.FLAT, property.minimum(false), property.maximum(false));
        if (property.supportsPercent()) {
            spec.operation(PropertyOperation.PERCENT, property.minimum(true), property.maximum(true));
        }
        return spec.build();
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
        for (StaminaProperty property : StaminaProperty.values()) {
            JafsApi.registerProperty(spec(property), applier.forProperty(property));
        }
        JassApi.registerContextualProvider(PROVIDER_ID, new JafsStaminaProvider(applier));
        MinecraftForge.EVENT_BUS.register(applier);
    }
}
