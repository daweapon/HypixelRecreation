package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.achievement.SkyBlockAchievementSync;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.GoldenGoblin;
import net.swofty.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import net.swofty.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionAchievementSync implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SkyBlockAchievementSync.syncAll((SkyBlockPlayer) event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onSkill(SkillUpdateEvent event) {
        SkyBlockAchievementSync.syncSkill(event.getPlayer(), event.getSkillCategory());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onExperience(SkyBlockXPModificationEvent event) {
        SkyBlockAchievementSync.syncSkyBlockLevel(event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onCollection(CollectionUpdateEvent event) {
        if (event.getOldValue() == 0) SkyBlockAchievementSync.syncCollections(event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.CUSTOM, phase = EventPhase.GAMEPLAY)
    public void onKill(PlayerKilledSkyBlockMobEvent event) {
        if (event.getKilledMob() instanceof GoldenGoblin) {
            event.getPlayer().getAchievementHandler().addProgress("skyblock.goblin_killer", 1);
        }
    }
}
