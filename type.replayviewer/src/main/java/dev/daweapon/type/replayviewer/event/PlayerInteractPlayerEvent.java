package dev.daweapon.type.replayviewer.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.replayviewer.entity.ReplayPlayerEntity;
import dev.daweapon.type.replayviewer.view.GUIViewPlayer;

public class PlayerInteractPlayerEvent implements HypixelEventClass {

    // this feels pretty cool
    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false)
    public void run(PlayerEntityInteractEvent event) {
        final HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        final Entity target = event.getTarget();

        if (!(target instanceof ReplayPlayerEntity targetPlayer)) return;

        player.openView(new GUIViewPlayer(), new GUIViewPlayer.State(targetPlayer));
    }

}
