package dev.daweapon.type.skyblockgeneric.communityupgrades;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

import java.util.UUID;

public final class IslandBorder {
    public static final int DEFAULT_SIZE = 161;

    private IslandBorder() {
    }

    public static int sizeForProfile(UUID profileId) {
        int upgraded = CommunityUpgrades.profileIntValue(profileId, CommunityUpgradeId.ISLAND_SIZE);
        return upgraded > 0 ? upgraded : DEFAULT_SIZE;
    }

    public static void apply(SkyBlockIsland island, int size) {
        Instance instance = island.getIslandInstance();
        if (instance != null) instance.setWorldBorder(new WorldBorder(size, 0, 0, 0, 0));
    }
}
