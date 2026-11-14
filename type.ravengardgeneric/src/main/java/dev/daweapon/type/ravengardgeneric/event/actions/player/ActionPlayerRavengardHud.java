package dev.daweapon.type.ravengardgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.ravengardgeneric.hud.RavengardHud;

public class ActionPlayerRavengardHud implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.POST_SPAWN)
    public void onSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) {
            return;
        }
        RavengardHud.attach((HypixelPlayer) event.getPlayer());
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.DISCONNECT)
    public void onDisconnect(PlayerDisconnectEvent event) {
        RavengardHud.detach((HypixelPlayer) event.getPlayer());
    }
}
