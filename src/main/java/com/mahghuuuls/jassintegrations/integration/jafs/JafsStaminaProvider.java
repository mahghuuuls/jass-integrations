package com.mahghuuuls.jassintegrations.integration.jafs;

import com.mahghuuuls.jass.api.IContextualStaminaModifierProvider;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import net.minecraft.entity.player.EntityPlayer;

/**
 * The contextual provider {@code jassintegrations:jafs}. Answers only on the server, where JAFS calls
 * the appliers; the client never has the sums.
 */
final class JafsStaminaProvider implements IContextualStaminaModifierProvider {

    private final JafsStaminaApplier applier;

    JafsStaminaProvider(JafsStaminaApplier applier) {
        this.applier = applier;
    }

    @Override
    public StaminaContribution getContribution(EntityPlayer player) {
        if (player.world.isRemote) {
            return StaminaContribution.EMPTY;
        }
        return StaminaBonusMapping.map(applier.sums(player.getUniqueID()));
    }
}
