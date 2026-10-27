package dev.daweapon.type.skyblockgeneric.hunting;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Which Shards a Huntrap can catch where, loaded from hunting/trap_pools.yml.
public final class TrapPools {
    private static final List<Pool> POOLS = load();

    private TrapPools() {
    }

    public record Entry(AttributeId shard, double weight) {
    }

    public record Pool(String id, ServerType world, HuntrapMedium medium, Set<RegionType> regions, List<Entry> entries) {
        boolean matches(ServerType type, @Nullable RegionType region, HuntrapMedium actual) {
            return world == type && medium.accepts(actual) && (regions.isEmpty() || region != null && regions.contains(region));
        }
    }

    public static List<Pool> pools() {
        return POOLS;
    }

    public static List<Entry> candidates(ServerType type, @Nullable RegionType region, HuntrapMedium actual) {
        List<Entry> result = new ArrayList<>();
        for (Pool pool : POOLS) if (pool.matches(type, region, actual)) result.addAll(pool.entries());
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Pool> load() {
        Path path = Files.exists(Path.of("./configuration/skyblock/hunting/trap_pools.yml"))
                ? Path.of("./configuration/skyblock/hunting/trap_pools.yml")
                : Path.of("../configuration/skyblock/hunting/trap_pools.yml");
        try (InputStream stream = Files.newInputStream(path)) {
            Map<String, Object> root = new Yaml().load(stream);
            List<Pool> pools = new ArrayList<>();
            for (Map<String, Object> value : (List<Map<String, Object>>) root.get("pools")) {
                List<Entry> entries = new ArrayList<>();
                for (Map<String, Object> entry : (List<Map<String, Object>>) value.get("shards"))
                    entries.add(new Entry(AttributeId.parse(entry.get("shard").toString()), ((Number) entry.get("weight")).doubleValue()));
                Set<RegionType> regions = Set.copyOf(((List<Object>) value.getOrDefault("regions", List.of())).stream()
                        .map(name -> RegionType.valueOf(name.toString())).toList());
                pools.add(new Pool(value.get("id").toString(), ServerType.valueOf(value.get("world").toString()),
                        HuntrapMedium.valueOf(value.get("medium").toString()), regions, List.copyOf(entries)));
            }
            return List.copyOf(pools);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read trap pools: " + path.toAbsolutePath(), exception);
        }
    }
}
