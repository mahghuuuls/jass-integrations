package com.mahghuuuls.jassintegrations.integration.statusworks;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jassintegrations.Tags;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import com.mahghuuuls.statusworks.api.RegistrationResult;
import com.mahghuuuls.statusworks.api.StatusworksApi;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry class of the Statusworks integration; created by {@link IntegrationGate} by name. Registers one
 * Statusworks contribution type per stamina bonus, so statuses, pools and marks can name them, and gives JASS
 * the players' totals.
 */
public final class StatusworksIntegration implements IntegrationGate.Integration {

    static final ResourceLocation PROVIDER_ID = new ResourceLocation(Tags.MOD_ID, "statusworks");

    private static final String API = "com.mahghuuuls.statusworks.api.StatusworksApi";
    private static final String CONTRIBUTIONS = "com.mahghuuuls.statusworks.api.ContributionService";
    private static final String GROUP_TYPE = "com.mahghuuuls.statusworks.api.contribution.ContributionGroupType";

    @Override
    public List<String> compatibilityProblems() {
        List<String> problems = new ArrayList<>();
        if (!IntegrationGate.hasMethod(API, "registerContributionType",
                "com.mahghuuuls.statusworks.api.contribution.ContributionType")) {
            problems.add("StatusworksApi.registerContributionType(ContributionType) not found");
        }
        // Group signals and group reads arrived in Statusworks 1.2.0; the provider relies on both.
        if (!IntegrationGate.hasClass(GROUP_TYPE)
                || !IntegrationGate.hasMethod(CONTRIBUTIONS, "group", "net.minecraft.entity.Entity", "java.lang.String")) {
            problems.add("Statusworks 1.2.0 or later needed (" + GROUP_TYPE + " or ContributionService.group not found)");
        }
        return problems;
    }

    /**
     * Runs during pre-initialization, after Statusworks' construction ({@code after:statusworks}), before
     * definitions freeze. The provider comes first, so every type Statusworks accepts works; a refused type is
     * warned about and the others stay.
     */
    @Override
    public void start() {
        StatusworksStaminaProvider provider = new StatusworksStaminaProvider();
        JassApi.registerContextualProvider(PROVIDER_ID, provider);
        MinecraftForge.EVENT_BUS.register(provider);
        for (String id : StatusworksTypeIds.BY_ID.keySet()) {
            RegistrationResult result = StatusworksApi.registerContributionType(new StatusworksStaminaType(id));
            if (!result.isAccepted()) {
                DebugLog.warn("Statusworks: contribution type " + id + " refused: " + result.error());
            }
        }
    }
}
