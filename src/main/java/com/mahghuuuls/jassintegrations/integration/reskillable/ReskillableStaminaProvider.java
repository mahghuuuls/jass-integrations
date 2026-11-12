package com.mahghuuuls.jassintegrations.integration.reskillable;

import codersafterdark.reskillable.api.data.PlayerData;
import codersafterdark.reskillable.api.data.PlayerDataHandler;
import codersafterdark.reskillable.api.data.PlayerSkillInfo;
import codersafterdark.reskillable.api.event.LevelUpEvent;
import codersafterdark.reskillable.api.skill.Skill;
import com.mahghuuuls.jass.api.IContextualStaminaModifierProvider;
import com.mahghuuuls.jass.api.JassApi;
import com.mahghuuuls.jass.api.StaminaContribution;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusMapping;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaBonusTotals;
import com.mahghuuuls.jassintegrations.integration.stamina.StaminaTotalsLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Collections;
import java.util.Map;

/**
 * The contextual provider {@code jassintegrations:reskillable}. Reads the levels of the skills the rules name
 * each time JASS rebuilds the profile, so Reskillable stays the only owner of the levels; a level change marks
 * the profile dirty. Writes the debug line when a player's totals change.
 */
final class ReskillableStaminaProvider implements IContextualStaminaModifierProvider {

    private final StaminaTotalsLog log = new StaminaTotalsLog();
    /** Each usable rule with its skill, set once Reskillable has registered its skills; none before that. */
    private volatile Map<ReskillableSkillRule, Skill> rules = Collections.emptyMap();

    void setRules(Map<ReskillableSkillRule, Skill> usable) {
        rules = usable;
    }

    @Override
    public StaminaContribution getContribution(EntityPlayer player) {
        if (player.world.isRemote) {
            return StaminaContribution.EMPTY;
        }
        StaminaBonusTotals totals = totals(player);
        // A fake player can share its owner's UUID and never joins a world; it must not move the owner's record.
        if (!(player instanceof FakePlayer) && log.changed(player.getUniqueID(), totals)) {
            DebugLog.debug(() -> "Reskillable: " + player.getName() + " stamina bonuses now " + totals);
        }
        return StaminaBonusMapping.map(totals);
    }

    /** The player's current totals; server only. */
    StaminaBonusTotals totals(EntityPlayer player) {
        Map<ReskillableSkillRule, Skill> current = rules;
        if (current.isEmpty()) {
            return StaminaBonusTotals.NONE;
        }
        PlayerData data = PlayerDataHandler.get(player);
        if (data == null) {
            return StaminaBonusTotals.NONE;
        }
        return ReskillableSkillRule.sum(current.keySet(), rule -> {
            PlayerSkillInfo info = data.getSkillInfo(current.get(rule));
            return info == null ? null : info.getLevel();
        });
    }

    @SubscribeEvent
    public void onLevelUp(LevelUpEvent.Post event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player != null && !player.world.isRemote) {
            JassApi.markModifierCacheDirty(player);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        log.forget(event.player.getUniqueID());
    }
}
