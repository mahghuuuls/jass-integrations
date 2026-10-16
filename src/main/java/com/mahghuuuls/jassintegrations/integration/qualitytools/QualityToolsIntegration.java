package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;
import java.util.List;

/**
 * Entry class of the Quality Tools integration; created by {@link IntegrationGate} by name. Quality
 * Tools finds the attributes by name, so this integration uses no Quality Tools class.
 */
public final class QualityToolsIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "quality_tools");

    @Override
    public List<String> compatibilityProblems() {
        return Collections.emptyList();
    }

    @Override
    public void start() {
        MinecraftForge.EVENT_BUS.register(new QualityAttributes.Installer());
        JassApi.registerContextualProvider(PROVIDER_ID, new QualityStaminaProvider());
        MinecraftForge.EVENT_BUS.register(new QualityChangeTracker());
    }
}
