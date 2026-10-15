package dev.daweapon.type.skyblockgeneric.event.actions.custom.levels;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.event.actions.player.ActionAddSkyBlockXPToNametag;
import dev.daweapon.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelUnlock;
import dev.daweapon.type.skyblockgeneric.levels.causes.LevelCause;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

// Announces SkyBlock level-ups and grants their unlock rewards when a player's XP changes
public class ActionChangeHypixelXP implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(SkyBlockXPModificationEvent event) {
        SkyBlockPlayer player = event.getPlayer();

        SkyBlockLevelRequirement oldLevel = SkyBlockLevelRequirement.getFromTotalXP(event.getOldXP());
        SkyBlockLevelRequirement newLevel = SkyBlockLevelRequirement.getFromTotalXP(event.getNewXP());

        // XP can now be taken away by staff commands; keep the nametag honest but stay quiet
        // rather than announcing a "level up" that went backwards.
        if (event.getNewXP() <= event.getOldXP()) {
            if (oldLevel != newLevel) ActionAddSkyBlockXPToNametag.updatePlayerNametag(player);
            return;
        }

        if (oldLevel == newLevel) {
            if (event.getCause() != null && event.getCause().shouldDisplayMessage(player))
                player.sendMessage("<b>+<3>{} <b>SkyBlock XP", event.getNewXP() - event.getOldXP());
        } else {
            ActionAddSkyBlockXPToNametag.updatePlayerNametag(player);
            if (!(event.getCause() instanceof LevelCause)) {
                LevelCause levelCause = SkyBlockLevelCause.getLevelCause(newLevel.asInt());
                if (levelCause != null) player.getSkyBlockExperience().addExperience(levelCause);
            }
            List<SkyBlockLevelUnlock> unlocks = newLevel.getUnlocks();

            player.sendMessage("<3><m>---------------------------------");
            player.sendMessage("  <b><l>SKYBLOCK LEVEL UP! </l><7>{} <8><l>-> </l><3>{}", oldLevel.asInt(), newLevel.asInt());
            if (!unlocks.isEmpty()) {
                player.sendMessage(" ");
                player.sendMessage("  <a><l>REWARDS");
                unlocks.forEach(unlock -> {
                    unlock.getDisplay(player, newLevel.asInt()).forEach(line -> {
                        player.sendMessage(Text.of("  <8>").append(line));
                    });
                });
            }
            player.sendMessage("<3><m>---------------------------------");
        }
    }
}
