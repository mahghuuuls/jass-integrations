package com.mahghuuuls.jassintegrations.integration.setworks;

import com.mahghuuuls.jass.api.IContextualStaminaModifierProvider;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import net.minecraft.entity.player.EntityPlayer;

/**
 * The contextual provider {@code jassintegrations:setworks}. Answers only on the server, where Setworks
 * applies the effects; the client never has them.
 */
final class SetworksStaminaProvider implements IContextualStaminaModifierProvider {

    private final SetworksStaminaEffects effects;

    SetworksStaminaProvider(SetworksStaminaEffects effects) {
        this.effects = effects;
    }

    @Override
    public StaminaContribution getContribution(EntityPlayer player) {
        if (player.world.isRemote) {
            return StaminaContribution.EMPTY;
        }
        return StaminaBonusMapping.map(effects.totals(player.getUniqueID()));
    }
}
