package net.swofty.type.crystalhollows.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import net.swofty.type.crystalhollows.quest.KingsScentWater;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionKingsScentWater implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        KingsScentWater.check((SkyBlockPlayer) event.getPlayer());
    }
}
