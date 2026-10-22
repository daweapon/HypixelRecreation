package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.type.generic.entity.hologram.ServerHolograms;

public record PlacedPortal(String itemId, int x, int y, int z, int front) {
    public String serialize() {
        return itemId + "," + x + "," + y + "," + z + "," + front;
    }

    public static PlacedPortal deserialize(String raw) {
        String[] parts = raw.split(",");
        return new PlacedPortal(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
    }

    public String[] hologramText(PortalDestination destination) {
        return new String[]{"<b>Travel to:", destination.label()};
    }

    public ServerHolograms.ExternalHologram hologram(net.minestom.server.instance.Instance instance, PortalDestination destination) {
        return ServerHolograms.ExternalHologram.builder()
                .instance(instance)
                .pos(PortalShape.hologramPosition(this))
                .text(hologramText(destination))
                .build();
    }
}
