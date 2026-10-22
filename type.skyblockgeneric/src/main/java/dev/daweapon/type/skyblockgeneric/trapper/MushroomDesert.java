package dev.daweapon.type.skyblockgeneric.trapper;

import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class MushroomDesert {
    private static final Set<RegionType> ZONES = Set.of(
            RegionType.MUSHROOM_DESERT, RegionType.DESERT_MOUNTAIN, RegionType.DESERT_SETTLEMENT, RegionType.OASIS,
            RegionType.SHEPHERD_KEEP, RegionType.TRAPPERS_DEN, RegionType.JAKE_HOUSE, RegionType.MUSHROOM_GORGE);

    private MushroomDesert() {
    }

    public static boolean contains(@Nullable RegionType type) {
        return type != null && ZONES.contains(type);
    }

    public static boolean contains(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && contains(region.getType());
    }
}
