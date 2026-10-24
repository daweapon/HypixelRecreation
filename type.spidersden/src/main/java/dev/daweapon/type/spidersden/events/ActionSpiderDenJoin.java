package dev.daweapon.type.spidersden.events;

import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenWeather;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionSpiderDenJoin implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SpiderDenWeather.ensureStatistics((SkyBlockPlayer) event.getPlayer());
    }
}
