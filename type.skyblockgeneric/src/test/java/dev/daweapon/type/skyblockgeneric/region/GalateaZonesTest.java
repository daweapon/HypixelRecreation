package dev.daweapon.type.skyblockgeneric.region;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.skyblockgeneric.data.regions.RegionCatalog;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GalateaZonesTest {
    private static RegionType zoneAt(double x, double y, double z) {
        return RegionCatalog.getAllRegions().stream()
                .filter(region -> region.getServerType() == ServerType.SKYBLOCK_GALATEA)
                .filter(region -> region.insideRegion(new Pos(x, y, z)))
                .min(Comparator.comparingLong(SkyBlockRegion::getVolume))
                .map(SkyBlockRegion::getType)
                .orElse(null);
    }

    @Test
    void wikiCoordinatesResolveToTheirZone() {
        assertEquals(RegionType.TANGLEBURG, zoneAt(-585, 113, -30));
        assertEquals(RegionType.FUSION_HOUSE, zoneAt(-601, 115, -14));
        assertEquals(RegionType.EVERGREEN_PLATEAU, zoneAt(-562, 111, 44));
        assertEquals(RegionType.SOUTH_REACHES, zoneAt(-690, 120, 70));
        assertEquals(RegionType.MOONGLADES_EDGE, zoneAt(-720, 95, 47));
        assertEquals(RegionType.MOONGLADES_EDGE, zoneAt(-728, 98, 89));
        assertEquals(RegionType.MURKWATER_SHALLOWS, zoneAt(-645, 86, -12));
        assertEquals(RegionType.MURKWATER_DEPTHS, zoneAt(-635, 72, 0));
        assertEquals(RegionType.NORTH_WETLANDS, zoneAt(-625, 95, -53));
        assertEquals(RegionType.RED_HOUSE, zoneAt(-630, 99, -86));
        assertEquals(RegionType.VERDANT_SUMMIT, zoneAt(-740, 119, -22));
        assertEquals(RegionType.MURKWATER_OUTPOST, zoneAt(-702, 162, 2));
        assertEquals(RegionType.SQUID_CAVE, zoneAt(-610, 31, -6));
        assertEquals(RegionType.FOREST_TEMPLE, zoneAt(-636, 60, 75));
    }

    @Test
    void everySurfaceZoneAllowsTreeCutting() {
        for (RegionType zone : new RegionType[]{RegionType.TANGLEBURG, RegionType.EVERGREEN_PLATEAU, RegionType.SOUTH_REACHES,
                RegionType.SOUTH_WETLANDS, RegionType.MOONGLADES_EDGE, RegionType.MURKWATER_LOCH, RegionType.NORTH_WETLANDS,
                RegionType.NORTH_REACHES, RegionType.WEST_REACHES, RegionType.VERDANT_SUMMIT, RegionType.WESTBOUND_WETLANDS,
                RegionType.SIDE_EMBER_WAY, RegionType.RED_HOUSE, RegionType.MURKWATER_OUTPOST}) {
            assertNotNull(zone.getMiningHandler(), zone.name());
        }
    }
}
