package dev.daweapon.type.bedwarslobby.events;

import net.minestom.server.event.player.PlayerMoveEvent;
import dev.daweapon.type.bedwarslobby.gui.cosmetics.ShopkeeperPreviewController;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.user.HypixelPlayer;

// Cancels player movement while they are locked into a shopkeeper skin preview.
public class ActionShopkeeperPreviewMoveLock implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerMoveEvent event) {
        HypixelPlayer player = (HypixelPlayer) event.getPlayer();
        if (ShopkeeperPreviewController.isLocked(player)) {
            event.setCancelled(true);
        }
    }
}
