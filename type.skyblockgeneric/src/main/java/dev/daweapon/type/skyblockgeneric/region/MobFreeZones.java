package dev.daweapon.type.skyblockgeneric.region;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class MobFreeZones {
    private static final Set<RegionType> ZONES = EnumSet.of(RegionType.CRYSTAL_NUCLEUS, RegionType.MINES_OF_DIVAN);

    private MobFreeZones() {
    }

    public static boolean isMobFree(@Nullable RegionType type) {
        return type != null && ZONES.contains(type);
    }

    public static boolean isMobFreeAt(Pos position) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        return region != null && isMobFree(region.getType());
    }
}
