package dev.daweapon.type.crystalhollows.claim;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crystalhollows.config.Box;
import dev.daweapon.type.crystalhollows.config.ConfigReader;
import dev.daweapon.type.skyblockgeneric.crystalhollows.NucleusCrystal;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ClaimSpots {
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/claims.yml");

    private static volatile Map<NucleusCrystal, ClaimSpot> loaded;

    private ClaimSpots() {
    }

    public static Map<NucleusCrystal, ClaimSpot> all() {
        Map<NucleusCrystal, ClaimSpot> spots = loaded;
        if (spots == null) loaded = spots = read(FILE);
        return spots;
    }

    public static void reload() {
        loaded = null;
    }

    public static ClaimSpot of(NucleusCrystal crystal) {
        return all().get(crystal);
    }

    public static NucleusCrystal crystalAt(net.minestom.server.coordinate.Point block) {
        for (ClaimSpot spot : all().values()) {
            if (spot.containsBlock(block)) return spot.crystal();
        }
        return null;
    }

    public static Map<NucleusCrystal, ClaimSpot> read(Path file) {
        Map<NucleusCrystal, ClaimSpot> spots = new EnumMap<>(NucleusCrystal.class);
        for (Map<String, Object> entry : ConfigReader.list(ConfigReader.read(file), "claims")) {
            NucleusCrystal crystal = NucleusCrystal.valueOf(String.valueOf(entry.get("crystal")));
            Pos at = ConfigReader.pos(ConfigReader.map(entry, "crystal_at"));
            Box cube = ConfigReader.box(ConfigReader.map(entry, "cube"));
            List<Box> walls = new ArrayList<>();
            if (entry.get("walls") instanceof List<?> raw) {
                for (Object wall : raw) {
                    @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) wall;
                    walls.add(ConfigReader.box(map));
                }
            }
            spots.put(crystal, new ClaimSpot(crystal, String.valueOf(entry.get("structure")), at, cube, List.copyOf(walls)));
        }
        return Map.copyOf(spots);
    }
}
