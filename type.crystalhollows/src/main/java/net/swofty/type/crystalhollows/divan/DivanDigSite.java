package net.swofty.type.crystalhollows.divan;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.crystalhollows.layout.StructurePlacement;
import net.swofty.type.crystalhollows.layout.StructureSite;
import net.swofty.type.generic.HypixelConst;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crystalhollows.config.ConfigReader;
import net.swofty.type.crystalhollows.layout.CrystalHollowsLayout;
import net.swofty.type.crystalhollows.quest.CrystalHollowsProgress;
import net.swofty.type.skyblockgeneric.crystalhollows.MetalDetector;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class DivanDigSite implements MetalDetector.DigSite {
    public static final Path FILE = Path.of("./configuration/skyblock/crystal_hollows/metal_detector.yml");
    public static final int DEFAULT_LOBBY_SPOTS = 16;
    private static final int FLOOR_STEP = 2;
    private static final double MIN_SPOT_DISTANCE = 12;

    public record Config(String structure, int lobbySpots, List<Pos> offsets) {
    }

    private record Cache(long seed, Pos origin, List<Point> spots) {
    }

    private static volatile Config config;
    private static volatile Cache cache;

    public static Config config() {
        Config loaded = config;
        if (loaded == null) config = loaded = read(FILE);
        return loaded;
    }

    public static void reload() {
        config = null;
        cache = null;
    }

    public static Config read(Path file) {
        Map<String, Object> root = ConfigReader.read(file);
        List<Pos> offsets = new ArrayList<>();
        for (Map<String, Object> spot : ConfigReader.list(root, "spots")) offsets.add(ConfigReader.pos(spot));
        int count = root.get("lobby_spots") instanceof Number number ? number.intValue() : DEFAULT_LOBBY_SPOTS;
        return new Config((String) root.getOrDefault("structure", "mines_of_divan"), count, List.copyOf(offsets));
    }

    public static List<Pos> select(List<Pos> offsets, int count, long seed) {
        List<Pos> shuffled = new ArrayList<>(offsets);
        Collections.shuffle(shuffled, new Random(seed));
        return List.copyOf(shuffled.subList(0, Math.min(Math.max(0, count), shuffled.size())));
    }

    @Override
    public List<Point> spots() {
        Config current = config();
        Pos origin = CrystalHollowsLayout.origin(current.structure());
        if (origin == null) return List.of();
        long seed = CrystalHollowsLayout.seed();
        if (current.offsets().isEmpty()) return scannedSpots(current, origin, seed);
        Cache cached = cache;
        if (cached != null && cached.seed() == seed && cached.origin().equals(origin)) return cached.spots();
        List<Point> absolute = new ArrayList<>();
        for (Pos offset : select(current.offsets(), current.lobbySpots(), seed ^ 0x4D44L)) {
            absolute.add(new Pos(origin.x() + offset.x(), origin.y() + offset.y(), origin.z() + offset.z()));
        }
        cache = new Cache(seed, origin, List.copyOf(absolute));
        return cache.spots();
    }

    private List<Point> scannedSpots(Config current, Pos origin, long seed) {
        Cache cached = cache;
        if (cached != null && cached.seed() == seed && cached.origin().equals(origin)) return cached.spots();
        StructurePlacement placement = CrystalHollowsLayout.placement(current.structure());
        Instance instance = HypixelConst.getInstanceContainer();
        if (placement == null || placement.spec().site() == null || instance == null) return List.of();
        List<Pos> floor = goldFloor(instance, placement.spec().site(), origin);
        List<Point> chosen = new ArrayList<>(spread(floor, current.lobbySpots(), seed ^ 0x4D44L));
        cache = new Cache(seed, origin, List.copyOf(chosen));
        return cache.spots();
    }

    public static List<Pos> goldFloor(Instance instance, StructureSite site, Pos origin) {
        int shiftX = origin.blockX() - site.minX();
        int shiftY = origin.blockY() - site.minY();
        int shiftZ = origin.blockZ() - site.minZ();
        List<Pos> floor = new ArrayList<>();
        for (int x = site.minX(); x <= site.maxX(); x += FLOOR_STEP) {
            for (int z = site.minZ(); z <= site.maxZ(); z += FLOOR_STEP) {
                instance.loadChunk(x >> 4, z >> 4).join();
                for (int y = site.maxY(); y >= site.minY(); y--) {
                    if (instance.getBlock(x, y, z).compare(Block.GOLD_BLOCK) && instance.getBlock(x, y + 1, z).isAir()) {
                        floor.add(new Pos(x + shiftX + 0.5, y + shiftY + 1, z + shiftZ + 0.5));
                        break;
                    }
                }
            }
        }
        return floor;
    }

    public static List<Pos> spread(List<Pos> candidates, int count, long seed) {
        List<Pos> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, new Random(seed));
        List<Pos> chosen = new ArrayList<>();
        for (Pos candidate : shuffled) {
            if (chosen.size() >= count) break;
            boolean apart = true;
            for (Pos other : chosen) {
                if (candidate.distanceSquared(other) < MIN_SPOT_DISTANCE * MIN_SPOT_DISTANCE) {
                    apart = false;
                    break;
                }
            }
            if (apart) chosen.add(candidate);
        }
        return chosen;
    }

    @Override
    public boolean delivered(UUID player, ItemType scavenged) {
        for (CrystalHollowsProgress.Keeper keeper : CrystalHollowsProgress.Keeper.values()) {
            if (keeper.scavengedItemId.equals(scavenged.name())) return CrystalHollowsProgress.turnedIn(player, keeper);
        }
        return false;
    }
}
