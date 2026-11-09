package com.mahghuuuls.jassintegrations.integration.statusworks;

import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.statusworks.api.contribution.ContributionGroupType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

/**
 * The Statusworks contribution type of one stamina bonus, for example {@code jassintegrations:max_stamina_percent}.
 * Keeps nothing: it marks the JASS profile dirty when a player's group is about to change and again when it has
 * settled, and JASS reads the groups back through {@link StatusworksStaminaProvider}. Players on the server only.
 */
final class StatusworksStaminaType implements ContributionGroupType {

    private final String id;

    StatusworksStaminaType(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean appliesTo(EntityLivingBase entity) {
        return entity instanceof EntityPlayer && !entity.world.isRemote;
    }

    @Override
    public void onValue(EntityLivingBase entity, String key, double value) {
        // The settled signal ends every change; the provider reads the complete group then.
    }

    @Override
    public void onEnd(EntityLivingBase entity, String key) {
        // The settled signal ends every change; the provider reads the complete group then.
    }

    /**
     * The earlier values stop being current, so JASS must not keep a profile built from them: while the group is
     * updating, or failed until Statusworks' retry succeeds, the provider counts it as nothing.
     */
    @Override
    public boolean invalidate(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            JassApi.markModifierCacheDirty((EntityPlayer) entity);
        }
        return true;
    }

    /** Also at a join with unchanged values: JASS may have rebuilt the profile while the group was not current. */
    @Override
    public void settled(EntityLivingBase entity, Map<String, Double> group) {
        if (entity instanceof EntityPlayer) {
            JassApi.markModifierCacheDirty((EntityPlayer) entity);
        }
    }
}
