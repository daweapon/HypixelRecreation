package dev.daweapon.type.crystalhollows.layout;

import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalHollowsZones;
import dev.daweapon.type.skyblockgeneric.region.RegionType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HollowsRegions {
    public record Box(int x1, int z1, int x2, int z2) {
        public Box normalised() {
            return new Box(Math.min(x1, x2), Math.min(z1, z2), Math.max(x1, x2), Math.max(z1, z2));
        }
    }

    private static final Map<String, Box> PLACEHOLDERS = Map.of(
            "crystal_hollows", new Box(202, 202, 823, 823),
            "crystal_hollows_jungle", new Box(202, 202, 513, 513),
            "crystal_hollows_mithril_deposits", new Box(202, 513, 513, 823),
            "crystal_hollows_goblin_holdout", new Box(513, 202, 823, 513),
            "crystal_hollows_precursor_remnants", new Box(513, 513, 823, 823),
            "crystal_hollows_magma_fields", new Box(202, 202, 823, 823),
            "crystal_hollows_nucleus", new Box(462, 462, 562, 562));

    private HollowsRegions() {
    }

    public static String idOf(RegionType type) {
        return type == RegionType.CRYSTAL_HOLLOWS ? "crystal_hollows" : "crystal_hollows_" + type.name().toLowerCase(Locale.ROOT);
    }

    public static Map<String, RegionType> zones() {
        Map<String, RegionType> zones = new LinkedHashMap<>();
        Arrays.stream(RegionType.values()).filter(CrystalHollowsZones::isCrystalHollows)
                .forEach(type -> zones.put(idOf(type), type));
        return zones;
    }

    public static boolean isPlaceholder(String regionId, Box box) {
        return box.normalised().equals(PLACEHOLDERS.get(regionId));
    }

    public static List<String> ids() {
        return List.copyOf(zones().keySet());
    }
}
