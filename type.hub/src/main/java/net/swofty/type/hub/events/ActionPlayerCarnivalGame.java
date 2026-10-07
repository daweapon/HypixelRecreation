package net.swofty.type.hub.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.hub.carnival.StationaryCarnivalGame;
import net.swofty.type.skyblockgeneric.carnival.CarnivalGameSession;
import net.swofty.type.skyblockgeneric.carnival.CarnivalSessions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Keeps Carnival game players at their station and ends a game when its player leaves.
public class ActionPlayerCarnivalGame implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void holdAtStation(PlayerMoveEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        CarnivalGameSession session = CarnivalSessions.get(player);
        if (!(session instanceof StationaryCarnivalGame stationary)) return;
        Pos station = stationary.station();
        Pos moved = event.getNewPosition();
        if (moved.distanceSquared(station) < 0.01) return;
        event.setNewPosition(station.withView(moved.yaw(), moved.pitch()));
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void clearLeftoverTools(PlayerSpawnEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!CarnivalSessions.isPlaying(player)) CarnivalSessions.removeTools(player);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void endOnLeave(PlayerDisconnectEvent event) {
        CarnivalSessions.abandon((SkyBlockPlayer) event.getPlayer());
    }
}
