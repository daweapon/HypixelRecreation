package dev.daweapon.type.skyblockgeneric.event.actions.player;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.event.EventNodes;
import dev.daweapon.type.generic.event.HypixelEventClass;
import dev.daweapon.type.generic.event.phase.EventPhase;
import dev.daweapon.type.generic.event.phase.PhasedEvent;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import dev.daweapon.type.skyblockgeneric.user.island.portal.PlacedPortal;
import dev.daweapon.type.skyblockgeneric.user.island.portal.PortalCatalog;
import dev.daweapon.type.skyblockgeneric.user.island.portal.PortalDeleteConfirmView;
import dev.daweapon.type.skyblockgeneric.user.island.portal.PortalDestination;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerBlockInteractEvent;

// Prompts to delete a placed island portal when its frame is clicked
public class ActionPortalInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN || !HypixelConst.isIslandServer()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
        if (island == null || island != player.getSkyBlockIsland() || island.getPortals() == null) return;

        PlacedPortal portal = island.getPortals().portalAt(
                event.getBlockPosition().blockX(), event.getBlockPosition().blockY(), event.getBlockPosition().blockZ());
        if (portal == null) return;

        SkyBlockItem held = new SkyBlockItem(player.getItemInMainHand());
        if (held.getAttributeHandler().getPotentialType() != null
                && PortalCatalog.get(held.getAttributeHandler().getPotentialType().name()) != null) return;

        event.setBlockingItemUse(true);
        PortalDestination destination = PortalCatalog.get(portal.itemId());
        player.openView(new PortalDeleteConfirmView(destination, confirmer -> {
            if (island.getPortals() == null) return;
            island.getPortals().remove(event.getInstance(), portal);
            confirmer.sendMessage("<a>You deleted the portal to {}<a>.", destination.labelText());
        }));
    }
}
