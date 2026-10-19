package dev.daweapon.type.skyblockgeneric.rabbits;

import java.util.Map;

public final class ChocolateShopStock {
    private static final Map<String, Integer> ANNUAL_STOCK = Map.ofEntries(
            Map.entry("SUPREME_CHOCOLATE_BAR", 500),
            Map.entry("EGGLOCATOR", 1),
            Map.entry("SHARD_RABBIT_MAFIOSO", 12),
            Map.entry("SHARD_RABBIT_CAT", 9),
            Map.entry("DARK_CACAO_TRUFFLE", 3),
            Map.entry("SHARD_RABBIT_NEIGHBOR", 6),
            Map.entry("DYE_CHOCOLATE", 4),
            Map.entry("CHOCO_RABBIT_PERSONALITY", 2),
            Map.entry("SHARD_RABBIT_GODMOTHER", 3),
            Map.entry("STEAMED_CHOCOLATE_FISH", 1),
            Map.entry("HOT_CHOCOLATE_MIXIN", 4),
            Map.entry("CHOCOLATE_FORTUNE", 25));

    private ChocolateShopStock() {
    }

    public static boolean isLimited(String key) {
        return ANNUAL_STOCK.containsKey(key);
    }

    public static int annualStock(String key) {
        return ANNUAL_STOCK.getOrDefault(key, Integer.MAX_VALUE);
    }

    public static int remaining(String key, int purchasedThisYear) {
        if (!isLimited(key)) return Integer.MAX_VALUE;
        return Math.max(0, annualStock(key) - purchasedThisYear);
    }
}
