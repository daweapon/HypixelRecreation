package dev.daweapon.type.murdermysterylobby.events;

import lombok.SneakyThrows;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import net.minestom.server.entity.GameMode;
import dev.daweapon.type.murdermysterylobby.TypeMurderMysteryLobbyLoader;
import dev.daweapon.type.murdermysterylobby.hologram.LeaderboardHologramManager;

public class ActionPlayerSpawn implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true, phase = EventPhase.SPAWN)
    public void run(PlayerSpawnEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL);

        // Spawn leaderboard holograms for this player
        LeaderboardHologramManager.spawnHologramsForPlayer(player);
        TypeMurderMysteryLobbyLoader.lobbyMap.sendMapData(player);
    }
}
