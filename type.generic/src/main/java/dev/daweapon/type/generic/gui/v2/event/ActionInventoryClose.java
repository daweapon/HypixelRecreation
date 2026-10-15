package dev.daweapon.type.generic.gui.v2.event;

import net.minestom.server.event.inventory.InventoryCloseEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.gui.v2.ViewNavigator;
import dev.daweapon.type.generic.gui.v2.ViewSession;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class ActionInventoryClose implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.INVENTORY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onPlayerInventoryClose(InventoryCloseEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ViewNavigator.find(player).ifPresent(navigator -> {
            ViewSession<?> session = navigator.getCurrentSession();
            if (session == null) {
                return;
            }
            if (event.getInventory() != session.inventory() || session.isSuppressCloseEvent()) {
                session.setSuppressCloseEvent(false);
                return;
            }
            session.close(ViewSession.CloseReason.PLAYER_EXITED);
        });
    }

}
