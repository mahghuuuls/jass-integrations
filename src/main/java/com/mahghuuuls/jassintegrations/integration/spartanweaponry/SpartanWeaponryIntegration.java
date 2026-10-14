package com.mahghuuuls.jassintegrations.integration.spartanweaponry;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Entry class of the Spartan Weaponry integration; created by {@link IntegrationGate} by name. */
public final class SpartanWeaponryIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "spartan_weaponry");

    private final WeaponTypeClassifier classifier = WeaponTypeClassifier.SPARTAN;

    @Override
    public List<String> compatibilityProblems() {
        List<String> missing = IntegrationGate.missingClasses(WeaponTypeClassifier.SPARTAN_CLASS_TYPES.keySet());
        if (missing.size() == WeaponTypeClassifier.SPARTAN_CLASS_TYPES.size()) {
            return Collections.singletonList("no Spartan weapon class found");
        }
        if (!missing.isEmpty()) {
            DebugLog.warn("Spartan Weaponry: weapon classes not found, these types get no values: " + missing);
        }
        return Collections.emptyList();
    }

    @Override
    public void start() {
        JassApi.registerItemValueProvider(PROVIDER_ID, new SpartanWeaponValueProvider(classifier));
        MinecraftForge.EVENT_BUS.register(new CrossbowLoadingHandler());
    }

    @Override
    public String clientEntryClass() {
        return "com.mahghuuuls.jassintegrations.client.CrossbowLoadingGuard";
    }

    /** With {@code debug_logging} on, lists the registered items of each weapon type once. */
    @Override
    public void postInit() {
        if (!DebugLog.enabled()) {
            return;
        }
        Map<String, List<String>> byType = new TreeMap<>();
        for (Item item : ForgeRegistries.ITEMS) {
            String type = classifier.classify(item.getClass());
            if (type != null) {
                byType.computeIfAbsent(type, k -> new ArrayList<>()).add(String.valueOf(item.getRegistryName()));
            }
        }
        for (Map.Entry<String, List<String>> entry : byType.entrySet()) {
            DebugLog.debug(() -> "Spartan Weaponry type " + entry.getKey() + ": " + entry.getValue());
        }
    }
}
