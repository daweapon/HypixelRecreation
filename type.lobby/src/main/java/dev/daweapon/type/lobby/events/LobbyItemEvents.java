package dev.daweapon.type.lobby.events;

import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.lobby.LobbyTypeLoader;
import dev.daweapon.type.lobby.item.LobbyItemHandler;

// Forwards item-related player events to the active lobby's item handler.
public class LobbyItemEvents implements HypixelEventClass {

    private static LobbyItemHandler getHandler() {
        if (HypixelConst.getTypeLoader() instanceof LobbyTypeLoader lobbyLoader) {
            return lobbyLoader.getItemHandler();
        }
        return null;
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onItemFinishUse(PlayerFinishItemUseEvent event) {
        LobbyItemHandler handler = getHandler();
        if (handler != null) {
            handler.onItemFinishUse(event);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onItemUseOnBlock(PlayerUseItemOnBlockEvent event) {
        LobbyItemHandler handler = getHandler();
        if (handler != null) {
            handler.onItemUseOnBlock(event);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onItemUse(PlayerUseItemEvent event) {
        LobbyItemHandler handler = getHandler();
        if (handler != null) {
            handler.onItemUse(event);
        }
    }

    @PhasedEvent(node = EventNodes.ITEM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onItemDrop(ItemDropEvent event) {
        LobbyItemHandler handler = getHandler();
        if (handler != null) {
            handler.onItemDrop(event);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onBlockPlace(PlayerBlockPlaceEvent event) {
        LobbyItemHandler handler = getHandler();
        if (handler != null) {
            handler.onBlockPlace(event);
        }
    }
}
