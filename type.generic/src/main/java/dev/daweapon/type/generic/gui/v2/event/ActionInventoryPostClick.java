package dev.daweapon.type.generic.gui.v2.event;

import net.minestom.server.event.inventory.InventoryClickEvent;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.gui.v2.ViewNavigator;
import dev.daweapon.type.generic.user.HypixelPlayer;

public class ActionInventoryPostClick implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.INVENTORY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onInventoryPostClick(InventoryClickEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        ViewNavigator.find(player).ifPresent(navigator -> {
            if (navigator.getCurrentSession() == null) {
                return;
            }
            navigator.getCurrentSession().onPostClickEvent(event);
        });
    }

}
