package dev.daweapon.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.crystalhollows.quest.KingsScentWater;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionKingsScentWater implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        KingsScentWater.check((SkyBlockPlayer) event.getPlayer());
    }
}
