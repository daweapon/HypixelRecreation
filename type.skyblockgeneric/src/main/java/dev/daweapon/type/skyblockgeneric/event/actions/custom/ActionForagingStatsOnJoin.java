package dev.daweapon.type.skyblockgeneric.event.actions.custom;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.foraging.ForagingConditionalStats;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.player.PlayerSpawnEvent;

public class ActionForagingStatsOnJoin implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ForagingConditionalStats.ensure(player);
        MoongladeBeacon.syncSkyBlockXp(player);
        if (MoongladeBeacon.onBeaconServer()) MoongladeBeacon.start();
    }
}
