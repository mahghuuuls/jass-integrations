package com.mahghuuuls.jassintegrations.integration.statusworks;

import com.mahghuuuls.jass.api.IContextualStaminaModifierProvider;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaTotalsLog;
import com.mahghuuuls.statusworks.api.ContributionService;
import com.mahghuuuls.statusworks.api.StatusworksApi;
import com.mahghuuuls.statusworks.api.contribution.ContributionGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * The contextual provider {@code jassintegrations:statusworks}. Reads the player's contribution groups from
 * Statusworks each time JASS rebuilds the profile, so Statusworks stays the only owner of the values. A group
 * that is not current counts as nothing; its settled signal marks the profile dirty again. Writes the debug line
 * here, where the complete totals are known, when a player's totals change.
 */
final class StatusworksStaminaProvider implements IContextualStaminaModifierProvider {

    private final StaminaTotalsLog log = new StaminaTotalsLog();

    @Override
    public StaminaContribution getContribution(EntityPlayer player) {
        if (player.world.isRemote) {
            return StaminaContribution.EMPTY;
        }
        StaminaBonusTotals totals = totals(player);
        // A fake player can share its owner's UUID and never joins a world; it must not move the owner's record.
        if (!(player instanceof FakePlayer) && log.changed(player.getUniqueID(), totals)) {
            DebugLog.debug(() -> "Statusworks: " + player.getName() + " stamina bonuses now " + totals);
        }
        return StaminaBonusMapping.map(totals);
    }

    /** The player's current totals; server only. */
    static StaminaBonusTotals totals(EntityPlayer player) {
        ContributionService contributions = StatusworksApi.contributions();
        return StatusworksTypeIds.sum(id -> {
            ContributionGroup group = contributions.group(player, id);
            // An unavailable group already has no values; the check says so where it matters.
            return group.isAvailable() ? group.values() : null;
        });
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        log.forget(event.player.getUniqueID());
    }
}
