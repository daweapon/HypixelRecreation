package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HollowsZoneRelocator {
    public static final Set<RegionType> STRUCTURE_ZONES = EnumSet.of(RegionType.JUNGLE_TEMPLE, RegionType.MINES_OF_DIVAN,
            RegionType.LOST_PRECURSOR_CITY, RegionType.KHAZAD_DUM, RegionType.GOBLIN_QUEENS_DEN);

    private static final Map<String, Pos[]> ORIGINALS = new HashMap<>();

    private HollowsZoneRelocator() {
    }

    public static synchronized void apply(List<StructureSpec> specs, Map<String, StructurePlacement> placements) {
        for (StructureSpec spec : specs) {
            if (!STRUCTURE_ZONES.contains(spec.region()) || spec.site() == null) continue;
            for (SkyBlockRegion region : SkyBlockRegion.getRegions()) {
                if (region.getType() != spec.region()) continue;
                Pos[] original = ORIGINALS.computeIfAbsent(region.getName(), _ -> new Pos[]{region.getFirstLocation(), region.getSecondLocation()});
                StructurePlacement placement = placements.get(spec.id());
                if (placement == null) {
                    region.setFirstLocation(null);
                    region.setSecondLocation(null);
                    continue;
                }
                double dx = placement.origin().x() - spec.site().minX();
                double dy = placement.origin().y() - spec.site().minY();
                double dz = placement.origin().z() - spec.site().minZ();
                region.setFirstLocation(original[0].add(dx, dy, dz));
                region.setSecondLocation(original[1].add(dx, dy, dz));
                region.setCachedVolume(-1);
            }
        }
    }
}
