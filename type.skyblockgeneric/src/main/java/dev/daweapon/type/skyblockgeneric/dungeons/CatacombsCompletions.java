package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;

import java.util.Map;
import java.util.TreeMap;

/** Completed runs per floor and mode, stored as {@code MODE:FLOOR=count,...} in one string datapoint. */
public final class CatacombsCompletions {
    private final Map<String, Long> counts;

    private CatacombsCompletions(Map<String, Long> counts) {
        this.counts = counts;
    }

    public static CatacombsCompletions parse(String stored) {
        Map<String, Long> counts = new TreeMap<>();
        if (stored != null && !stored.isBlank()) {
            for (String entry : stored.split(",")) {
                String[] parts = entry.split("=", 2);
                if (parts.length != 2) continue;
                try {
                    counts.put(parts[0], Long.parseLong(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return new CatacombsCompletions(counts);
    }

    public static String key(CatacombsFloor floor, CatacombsMode mode) {
        return mode.name() + ":" + floor.name();
    }

    public long count(CatacombsFloor floor, CatacombsMode mode) {
        return counts.getOrDefault(key(floor, mode), 0L);
    }

    public long total() {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    public CatacombsCompletions with(CatacombsFloor floor, CatacombsMode mode) {
        Map<String, Long> next = new TreeMap<>(counts);
        next.merge(key(floor, mode), 1L, Long::sum);
        return new CatacombsCompletions(next);
    }

    public String encode() {
        StringBuilder builder = new StringBuilder();
        counts.forEach((key, value) -> builder.append(builder.isEmpty() ? "" : ",").append(key).append('=').append(value));
        return builder.toString();
    }
}
