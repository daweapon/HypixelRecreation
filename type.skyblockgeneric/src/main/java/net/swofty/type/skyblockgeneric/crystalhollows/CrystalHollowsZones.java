package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.EnumSet;
import java.util.Set;

public final class CrystalHollowsZones {
    private static final Set<RegionType> ZONES = EnumSet.of(
            RegionType.CRYSTAL_HOLLOWS, RegionType.CRYSTAL_NUCLEUS, RegionType.DRAGONS_LAIR, RegionType.FAIRY_GROTTO,
            RegionType.GOBLIN_HOLDOUT, RegionType.GOBLIN_QUEENS_DEN, RegionType.JUNGLE, RegionType.JUNGLE_TEMPLE,
            RegionType.MAGMA_FIELDS, RegionType.KHAZAD_DUM, RegionType.MITHRIL_DEPOSITS, RegionType.MINES_OF_DIVAN,
            RegionType.PRECURSOR_REMNANTS, RegionType.LOST_PRECURSOR_CITY);

    private CrystalHollowsZones() {
    }

    public static boolean isCrystalHollows(RegionType region) {
        return region != null && ZONES.contains(region);
    }
}
