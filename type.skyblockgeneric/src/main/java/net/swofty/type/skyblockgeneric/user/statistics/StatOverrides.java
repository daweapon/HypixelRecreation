package net.swofty.type.skyblockgeneric.user.statistics;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Staff overrides that pin a player's final total for a statistic to an exact value, whatever
 * their gear, pets and buffs add. Stored per profile as {@code STAT=value;STAT=value} so they
 * survive server switches. Statistics with a game cap (e.g. Speed) still respect it.
 */
public final class StatOverrides {
    public static final double MAX_VALUE = 99_999;

    private static final Map<String, Map<ItemStatistic, Double>> PARSED = new ConcurrentHashMap<>();

    private StatOverrides() {
    }

    public static Map<ItemStatistic, Double> of(SkyBlockPlayer player) {
        DatapointString datapoint = datapoint(player);
        if (datapoint == null) return Map.of();
        return PARSED.computeIfAbsent(datapoint.getValue(), StatOverrides::parse);
    }

    public static ItemStatistics apply(SkyBlockPlayer player, ItemStatistics total) {
        Map<ItemStatistic, Double> overrides = of(player);
        for (Map.Entry<ItemStatistic, Double> override : overrides.entrySet()) {
            total = total.withExactValue(override.getKey(), override.getValue());
        }
        return total;
    }

    public static void set(SkyBlockPlayer player, ItemStatistic stat, double value) {
        Map<ItemStatistic, Double> overrides = new EnumMap<>(ItemStatistic.class);
        overrides.putAll(of(player));
        overrides.put(stat, value);
        save(player, overrides);
    }

    public static boolean clear(SkyBlockPlayer player, ItemStatistic stat) {
        Map<ItemStatistic, Double> overrides = new EnumMap<>(ItemStatistic.class);
        overrides.putAll(of(player));
        if (overrides.remove(stat) == null) return false;
        save(player, overrides);
        return true;
    }

    public static int clearAll(SkyBlockPlayer player) {
        int cleared = of(player).size();
        save(player, Map.of());
        return cleared;
    }

    private static void save(SkyBlockPlayer player, Map<ItemStatistic, Double> overrides) {
        DatapointString datapoint = datapoint(player);
        if (datapoint == null) return;
        datapoint.setValue(overrides.entrySet().stream()
                .map(entry -> entry.getKey().name() + "=" + entry.getValue())
                .collect(Collectors.joining(";")));
        player.getStatistics().invalidateStatistics();
    }

    private static Map<ItemStatistic, Double> parse(String raw) {
        if (raw == null || raw.isBlank()) return Map.of();
        Map<ItemStatistic, Double> overrides = new EnumMap<>(ItemStatistic.class);
        for (String entry : raw.split(";")) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) continue;
            try {
                overrides.put(ItemStatistic.valueOf(parts[0]), Double.parseDouble(parts[1]));
            } catch (IllegalArgumentException ignored) {
                // A statistic removed between versions; drop it rather than fail the profile.
            }
        }
        return Collections.unmodifiableMap(overrides);
    }

    private static DatapointString datapoint(SkyBlockPlayer player) {
        SkyBlockDataHandler handler = player.getSkyblockDataHandler();
        return handler == null ? null : handler.get(SkyBlockDataHandler.Data.STAT_OVERRIDES, DatapointString.class);
    }
}
