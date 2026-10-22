package dev.daweapon.type.skyblockgeneric.user.island.furniture;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.item.handlers.place.PlaceEventHandler;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.coordinate.Point;

public final class FurniturePlacement {
    public static final String HANDLER_ID = "FURNITURE_PLACE";

    private FurniturePlacement() {
    }

    public static PlaceEventHandler handler() {
        return (event, player, item) -> {
            event.setCancelled(true);
            ItemType type = item.getAttributeHandler().getPotentialType();
            FurnitureDefinition definition = type == null ? null : FurnitureCatalog.get(type.name());
            SkyBlockIsland island = SkyBlockIsland.forInstance(event.getInstance());
            if (definition == null || !HypixelConst.isIslandServer() || island == null
                    || island != player.getSkyBlockIsland() || island.getFurniture() == null) {
                player.sendMessage("<c>You can only place furniture on your own island!");
                return;
            }

            int level = CarpentryCrafting.level(player);
            if (level < definition.carpentryLevel()) {
                player.sendMessage("<c>You need Carpentry {:roman} to place this furniture!", definition.carpentryLevel());
                return;
            }

            IslandFurniture furniture = island.getFurniture();
            if (furniture.isFull(definition)) {
                player.sendMessage("<c>You can't place any more furniture! The limit is {}.", furniture.limitFor(definition));
                return;
            }
            Point target = event.getBlockPosition();
            if (furniture.at(target.blockX(), target.blockY(), target.blockZ()) != null) {
                player.sendMessage("<c>There is already furniture here!");
                return;
            }

            PlacedFurniture placed = new PlacedFurniture(definition.itemId(), target.blockX(), target.blockY(), target.blockZ(),
                    PlacedFurniture.snapYaw(player.getPosition().yaw() + 180), null);
            furniture.add(event.getInstance(), placed);
            player.takeItem(type, 1);
            player.sendMessage("<a>You placed {}<a>! ({}/{})", StringUtility.toNormalCase(definition.itemId()),
                    furniture.count(definition.isCenturyCake()), furniture.limitFor(definition));
        };
    }
}
