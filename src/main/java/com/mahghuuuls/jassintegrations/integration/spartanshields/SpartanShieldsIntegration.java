package com.mahghuuuls.jassintegrations.integration.spartanshields;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Entry class of the Spartan Shields integration; created by {@link IntegrationGate} by name. */
public final class SpartanShieldsIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "spartan_shields");

    private final SpartanShieldValueProvider provider = new SpartanShieldValueProvider();

    @Override
    public List<String> compatibilityProblems() {
        return Collections.emptyList();
    }

    @Override
    public void start() {
        JassApi.registerItemValueProvider(PROVIDER_ID, provider);
    }

    /**
     * Warns once about Spartan Shields items with no values (an unknown name or material); with
     * {@code debug_logging} on, also lists the shields of each tier and kind.
     */
    @Override
    public void postInit() {
        Map<String, List<String>> byTier = new TreeMap<>();
        List<String> withoutValues = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation id = item.getRegistryName();
            if (!ShieldClassifier.isShieldName(id)) {
                continue;
            }
            Optional<ShieldClassifier.Shield> shield = provider.shield(item);
            if (!shield.isPresent() || Settings.current().shield(shield.get().kind, shield.get().material) == null) {
                withoutValues.add(id.toString());
                continue;
            }
            byTier.computeIfAbsent(Settings.current().shieldTierKey(shield.get().kind, shield.get().material),
                    k -> new ArrayList<>()).add(id.getPath());
        }
        if (!withoutValues.isEmpty()) {
            DebugLog.warn("Spartan Shields: shields without values from this mod (JASS defaults apply): " + withoutValues);
        }
        for (Map.Entry<String, List<String>> entry : byTier.entrySet()) {
            DebugLog.debug(() -> "Spartan Shields " + entry.getKey() + ": " + entry.getValue());
        }
    }
}
