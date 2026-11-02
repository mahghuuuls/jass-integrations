package com.mahghuuuls.jassintegrations.integration.setworks;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaProperty;
import com.mahghuuuls.setworks.api.RegistrationResult;
import com.mahghuuuls.setworks.api.SetworksApi;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class of the Setworks integration; created by {@link IntegrationGate} by name. Registers one
 * Setworks effect kind per {@link StaminaProperty}, so set bonuses can name them, and gives JASS the
 * players' totals.
 */
public final class SetworksIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "setworks");

    private static final String API = "com.mahghuuuls.setworks.api.SetworksApi";

    @Override
    public List<String> compatibilityProblems() {
        List<String> problems = new ArrayList<>();
        if (!IntegrationGate.hasMethod(API, "registerEffectKind", "com.mahghuuuls.setworks.api.IEffectKind")) {
            problems.add("SetworksApi.registerEffectKind(IEffectKind) not found");
        }
        if (!IntegrationGate.hasMethod(API, "query")) {
            problems.add("SetworksApi.query() not found");
        }
        if (!IntegrationGate.hasMethod("com.mahghuuuls.setworks.api.ISetworksQuery", "isActive",
                "net.minecraft.entity.player.EntityPlayer", "java.lang.String")) {
            problems.add("ISetworksQuery.isActive(EntityPlayer, String) not found");
        }
        return problems;
    }

    /**
     * Runs during pre-initialization, after Setworks' construction ({@code after:setworks}), before effect
     * kinds freeze. The provider and the lifecycle handler come first, so every kind Setworks accepts
     * works; a refused kind is warned about and the others stay.
     */
    @Override
    public void start() {
        SetworksStaminaEffects effects = new SetworksStaminaEffects(
                (player, bonusId) -> SetworksApi.query().isActive(player, bonusId));
        JassApi.registerContextualProvider(PROVIDER_ID, new SetworksStaminaProvider(effects));
        MinecraftForge.EVENT_BUS.register(effects);
        for (StaminaProperty property : StaminaProperty.values()) {
            RegistrationResult result = SetworksApi.registerEffectKind(new SetworksStaminaEffect(property, effects));
            if (!result.isOk()) {
                DebugLog.warn("Setworks: effect kind " + property.id() + " refused: " + result.message());
            }
        }
    }
}
