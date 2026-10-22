package com.mahghuuuls.jassintegrations.integration.qualitytools;

import com.mahghuuuls.jass.api.IContextualStaminaModifierProvider;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import net.minecraft.entity.player.EntityPlayer;

/**
 * The contextual provider {@code jassintegrations:quality_tools}. Answers only on the server, where
 * Quality Tools puts its modifiers; the client attributes never carry any.
 */
final class QualityStaminaProvider implements IContextualStaminaModifierProvider {

    @Override
    public StaminaContribution getContribution(EntityPlayer player) {
        if (player.world.isRemote) {
            return StaminaContribution.EMPTY;
        }
        return StaminaBonusMapping.map(QualityAttributes.totals(player));
    }
}
