package dev.daweapon.commons.metrics;

import dev.daweapon.commons.ServerType;

public final class DrainDestination {
    private DrainDestination() {
    }

    public static ServerType of(ServerType draining) {
        if (draining == ServerType.SKYBLOCK_HUB) return ServerType.SKYBLOCK_ISLAND;
        if (draining.isSkyBlock()) return ServerType.SKYBLOCK_HUB;
        if (draining == ServerType.PROTOTYPE_LOBBY) return ServerType.SKYBLOCK_HUB;
        return ServerType.PROTOTYPE_LOBBY;
    }
}
