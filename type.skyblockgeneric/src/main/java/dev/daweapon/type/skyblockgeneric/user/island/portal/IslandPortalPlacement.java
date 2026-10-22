package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.handlers.place.PlaceEventHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public final class IslandPortalPlacement {
    public static final String HANDLER_ID = "ISLAND_PORTAL_PLACE";

    private IslandPortalPlacement() {
    }

    public static PlaceEventHandler handler() {
        return (event, player, item) -> {
            event.setCancelled(true);
            ItemType type = item.getAttributeHandler().getPotentialType();
            PortalDestination destination = type == null ? null : PortalCatalog.get(type.name());
            SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
            if (destination == null || !HypixelConst.isIslandServer() || island == null
                    || island != player.getSkyBlockIsland() || island.getPortals() == null) {
                player.sendMessage("<c>You can only place portals on your own island!");
                return;
            }

            Point clicked = event.getBlockPosition().sub(faceNormal(event.getBlockFace()));
            PlacedPortal portal = new PlacedPortal(type.name(), clicked.blockX(), clicked.blockY() + 1, clicked.blockZ(),
                    PortalShape.frontFromYaw(player.getPosition().yaw()));
            if (!PortalShape.hasClearance(event.getInstance(), portal)) {
                player.sendMessage("<c>There is not enough space here to place this portal!");
                return;
            }

            island.getPortals().add(event.getInstance(), portal);
            player.takeItem(type, 1);
            player.sendMessage("<a>You placed a portal to {}<a>!", destination.labelText());
            if (!destination.isAvailable()) {
                player.sendMessage("<7>That destination isn't in SkyBlock yet, so the portal will not take you anywhere.");
            }
        };
    }

    private static Point faceNormal(BlockFace face) {
        return new net.minestom.server.coordinate.Vec(face.toDirection().normalX(), face.toDirection().normalY(), face.toDirection().normalZ());
    }
}
