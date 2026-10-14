package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HollowsZoneRelocator {
    public static final Set<RegionType> STRUCTURE_ZONES = EnumSet.of(RegionType.JUNGLE_TEMPLE, RegionType.MINES_OF_DIVAN,
            RegionType.LOST_PRECURSOR_CITY, RegionType.KHAZAD_DUM, RegionType.GOBLIN_QUEENS_DEN);

    private HollowsZoneRelocator() {
    }

    public static synchronized void apply(List<StructureSpec> specs, Map<String, StructurePlacement> placements) {
        for (StructureSpec spec : specs) {
            if (!STRUCTURE_ZONES.contains(spec.region()) || spec.site() == null) continue;
            for (SkyBlockRegion region : SkyBlockRegion.getRegions()) {
                if (region.getType() != spec.region()) continue;
                StructurePlacement placement = placements.get(spec.id());
                if (placement == null) {
                    region.setFirstLocation(null);
                    region.setSecondLocation(null);
                    continue;
                }
                StructureSite site = spec.site();
                Pos origin = placement.origin();
                region.setFirstLocation(new Pos(origin.blockX(), origin.blockY(), origin.blockZ()));
                region.setSecondLocation(new Pos(origin.blockX() + site.width() - 1, origin.blockY() + site.height() - 1, origin.blockZ() + site.length() - 1));
                region.setCachedVolume(-1);
            }
        }
    }
}
