package dev.daweapon.type.lobby.events;

import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.lobby.LobbyTypeLoader;
import dev.daweapon.type.lobby.item.LobbyItem;
import dev.daweapon.type.lobby.visibility.PlayerVisibilityManager;

import java.util.Map;

// Sets up a lobby player's hotbar items and visibility rules on spawn.
public class LobbyPlayerSpawnEvents implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerSpawnEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();

        if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
            Map<Integer, LobbyItem> hotbarItems = lobbyLoader.getHotbarItems();
            for (Map.Entry<Integer, LobbyItem> entry : hotbarItems.entrySet()) {
                player.getInventory().setItemStack(
                    entry.getKey(),
                    entry.getValue().getItemStack(player)
                );
            }
        }

        PlayerVisibilityManager.setupViewerRuleFromToggle(player);
        player.setGameMode(GameMode.SURVIVAL);
    }
}