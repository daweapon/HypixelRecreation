package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.commons.YamlFileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MiningEventConfig {
    private static final File FILE = new File("./configuration/skyblock/mining_events.yml");
    private static volatile MiningEventConfig instance;

    private final Map<String, Object> root;

    public record Site(String name, double x, double y, double z, double radius) {
    }

    public record RewardTier(long miningXp, long hotmXp, long mithrilPowder, long perKillPowder, int bits, int items) {
    }

    private MiningEventConfig(Map<String, Object> root) {
        this.root = root;
    }

    public static MiningEventConfig get() {
        MiningEventConfig current = instance;
        if (current == null) {
            synchronized (MiningEventConfig.class) {
                if (instance == null) instance = load(FILE);
                current = instance;
            }
        }
        return current;
    }

    public static MiningEventConfig load(File file) {
        try {
            return new MiningEventConfig(YamlFileUtils.loadYaml(file));
        } catch (Exception exception) {
            Logger.error(exception, "Failed to load mining_events.yml");
            return new MiningEventConfig(Map.of());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> section(String... path) {
        Object node = root;
        for (String key : path) {
            if (!(node instanceof Map<?, ?> map)) return Map.of();
            node = map.get(key);
        }
        return node instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    public double number(double fallback, String... path) {
        Map<String, Object> parent = path.length == 1 ? root : section(java.util.Arrays.copyOf(path, path.length - 1));
        return parent.get(path[path.length - 1]) instanceof Number number ? number.doubleValue() : fallback;
    }

    public int integer(int fallback, String... path) {
        return (int) number(fallback, path);
    }

    public Site site(Map<String, Object> raw) {
        return new Site(String.valueOf(raw.get("name")), num(raw.get("x")), num(raw.get("y")), num(raw.get("z")), num(raw.get("radius")));
    }

    @SuppressWarnings("unchecked")
    public List<Site> sites(String... path) {
        Map<String, Object> parent = section(java.util.Arrays.copyOf(path, path.length - 1));
        Object raw = path.length == 1 ? root.get(path[0]) : parent.get(path[path.length - 1]);
        List<Site> sites = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object entry : list) sites.add(site((Map<String, Object>) entry));
        }
        return sites;
    }

    public Site singleSite(String... path) {
        return site(section(path));
    }

    public RewardTier tier(String... path) {
        Map<String, Object> raw = section(path);
        return new RewardTier(
                (long) num(raw.get("mining_xp")), (long) num(raw.get("hotm_xp")), (long) num(raw.get("mithril_powder")),
                (long) num(raw.get("per_kill_powder")), (int) num(raw.get("bits")), (int) num(raw.get("items")));
    }

    public List<String> strings(String... path) {
        Map<String, Object> parent = path.length == 1 ? root : section(java.util.Arrays.copyOf(path, path.length - 1));
        return parent.get(path[path.length - 1]) instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

    public int weight(MiningEventType type) {
        return integer(1, "events", type.name(), "weight");
    }

    public long durationMillis(MiningEventType type) {
        return (long) (number(10, "events", type.name(), "duration_minutes") * 60_000L);
    }

    private static double num(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }
}
