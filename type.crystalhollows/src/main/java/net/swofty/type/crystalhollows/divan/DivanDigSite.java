package net.swofty.type.crystalhollows.divan;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
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
        if (origin == null || current.offsets().isEmpty()) return List.of();
        long seed = CrystalHollowsLayout.seed();
        Cache cached = cache;
        if (cached != null && cached.seed() == seed && cached.origin().equals(origin)) return cached.spots();
        List<Point> absolute = new ArrayList<>();
        for (Pos offset : select(current.offsets(), current.lobbySpots(), seed ^ 0x4D44L)) {
            absolute.add(new Pos(origin.x() + offset.x(), origin.y() + offset.y(), origin.z() + offset.z()));
        }
        cache = new Cache(seed, origin, List.copyOf(absolute));
        return cache.spots();
    }

    @Override
    public boolean delivered(UUID player, ItemType scavenged) {
        for (CrystalHollowsProgress.Keeper keeper : CrystalHollowsProgress.Keeper.values()) {
            if (keeper.scavengedItemId.equals(scavenged.name())) return CrystalHollowsProgress.turnedIn(player, keeper);
        }
        return false;
    }
}
