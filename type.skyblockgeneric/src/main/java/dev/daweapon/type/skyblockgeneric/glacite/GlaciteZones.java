package dev.daweapon.type.skyblockgeneric.glacite;

import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.Set;

public final class GlaciteZones {
    private static final Set<RegionType> COLD_ZONES = Set.of(RegionType.GLACITE_TUNNELS, RegionType.GREAT_GLACITE_LAKE, RegionType.GRANDPA_WOLFS_CAVE);

    private GlaciteZones() {}

    public static boolean isColdZone(RegionType region) {
        return region != null && COLD_ZONES.contains(region);
    }

    public static boolean isBaseCamp(RegionType region) {
        return region == RegionType.DWARVEN_BASE_CAMP;
    }

    public static boolean isWater(RegionType region) {
        return region == RegionType.GREAT_GLACITE_LAKE;
    }
}
