package net.swofty.type.skyblockgeneric.bazaar;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class BazaarOrderBook {
    public static final int TOP_LEVELS = 7;
    private static final double TICK = 0.1;

    private BazaarOrderBook() {
    }

    public record Level(double price, long amount, int orders) {
    }

    public static List<Level> top(List<BazaarConnector.MarketOrder> orders, boolean highestFirst) {
        Map<Long, long[]> byPrice = new TreeMap<>(highestFirst ? Comparator.<Long>reverseOrder() : Comparator.<Long>naturalOrder());
        for (BazaarConnector.MarketOrder market : orders) {
            if (market.amount() <= 0) {
                continue;
            }
            long key = Math.round(market.price() / TICK);
            long[] level = byPrice.computeIfAbsent(key, k -> new long[2]);
            level[0] += Math.round(market.amount());
            level[1]++;
        }
        return byPrice.entrySet().stream()
                .limit(TOP_LEVELS)
                .map(entry -> new Level(entry.getKey() * TICK, entry.getValue()[0], (int) entry.getValue()[1]))
                .toList();
    }
}
