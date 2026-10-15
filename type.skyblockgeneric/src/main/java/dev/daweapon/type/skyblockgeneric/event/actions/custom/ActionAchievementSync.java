package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.achievement.SkyBlockAchievementSync;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.dwarvenmines.GoldenGoblin;
import dev.daweapon.type.skyblockgeneric.event.custom.CollectionUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.SkillUpdateEvent;
import dev.daweapon.type.skyblockgeneric.event.custom.SkyBlockXPModificationEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

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
