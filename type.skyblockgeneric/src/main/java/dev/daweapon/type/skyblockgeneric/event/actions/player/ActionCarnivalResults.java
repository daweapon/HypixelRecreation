package dev.daweapon.type.skyblockgeneric.event.actions.player;

import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalLeaderboards;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.event.player.PlayerSpawnEvent;

public class ActionCarnivalResults implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) return;
        CarnivalLeaderboards.announceIfPending((SkyBlockPlayer) event.getPlayer());
    }
}
