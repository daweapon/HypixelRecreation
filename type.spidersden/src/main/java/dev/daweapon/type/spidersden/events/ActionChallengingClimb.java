package dev.daweapon.type.spidersden.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMobSpawner;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionChallengingClimb implements HypixelEventClass {
    private static final double SUMMIT_RADIUS = 20;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        var to = event.getNewPosition();
        if (to.y() < SpiderDenMobSpawner.SUMMIT.y() - 4 || to.distance(SpiderDenMobSpawner.SUMMIT) > SUMMIT_RADIUS) return;
        ((SkyBlockPlayer) event.getPlayer()).getAchievementHandler().completeAchievement("skyblock.a_challenging_climb");
    }
}
