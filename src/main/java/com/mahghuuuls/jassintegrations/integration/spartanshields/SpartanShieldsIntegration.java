package com.mahghuuuls.jassintegrations.integration.spartanshields;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
        activateBash(bashHookProblems(FMLCommonHandler.instance().getSide().isClient(), PACKET_CLASS, CLIENT_HANDLER_CLASS));
    }

    static final String PACKET_CLASS = "com.oblivioussp.spartanshields.network.PacketShieldBash";
    static final String CLIENT_HANDLER_CLASS = "com.oblivioussp.spartanshields.event.EventHandlerClient";

    /** Activates the bash cost when there is no problem; otherwise warns once and leaves the bash free. */
    static boolean activateBash(List<String> problems) {
        if (!problems.isEmpty()) {
            DebugLog.warn("Spartan Shields: shield bash cost off (this version of Spartan Shields is not supported): " + problems);
            return false;
        }
        ShieldBashCharger.activate();
        return true;
    }

    /**
     * Why the bash hook cannot work: a patched method is missing (another Spartan Shields version),
     * or the Mixin was not applied to its class. The client input handler exists only on a client.
     */
    static List<String> bashHookProblems(boolean client, String packetClass, String clientHandlerClass) {
        List<String> problems = new ArrayList<>();
        if (!IntegrationGate.hasMethod(packetClass, "handleServerSide", packetClass, "net.minecraft.entity.player.EntityPlayerMP")) {
            problems.add("missing " + packetClass + "#handleServerSide");
        } else if (!IntegrationGate.implementsInterface(packetClass, BashHookApplied.class)) {
            problems.add("bash Mixin not applied to " + packetClass);
        }
        if (client) {
            if (!IntegrationGate.hasMethod(clientHandlerClass, "onInputEvent", "net.minecraftforge.fml.common.gameevent.InputEvent")) {
                problems.add("missing " + clientHandlerClass + "#onInputEvent");
            } else if (!IntegrationGate.implementsInterface(clientHandlerClass, BashHookApplied.class)) {
                problems.add("swing Mixin not applied to " + clientHandlerClass);
            }
        }
        return problems;
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
