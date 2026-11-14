package dev.daweapon.type.bedwarslobby.events;

import lombok.SneakyThrows;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.bedwarslobby.TypeBedWarsLobbyLoader;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;

// Enforces survival gamemode and sends map data when a player spawns in the Bedwars lobby.
public class ActionPlayerSpawn implements HypixelEventClass {

    @SneakyThrows
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true)
    public void run(PlayerSpawnEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        player.setGameMode(GameMode.SURVIVAL); //noticed stayed in Spectator after Bedwars game end. This is Hacky, but I don't see a reason why gamemode shouldn't be enforced on lobby join. -petethepossum

        TypeBedWarsLobbyLoader.bedWarsLobbyMap.sendMapData(player);
    }
}
