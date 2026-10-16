package dev.daweapon.commons.skyblock.bazaar;

import dev.daweapon.PlayerField;
import dev.daweapon.codec.Codecs;
import dev.daweapon.commons.data.SharedData;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BazaarFlipper {
    public static final int DEFAULT_ORDERS = 14;
    public static final double DEFAULT_TAX = 0.0125;
    private static final int[] EXTRA_ORDERS = {0, 7, 14};
    private static final double[] TAX_REDUCTION = {0, 0.00125, 0.0025};
    private static final long CACHE_MILLIS = 60_000L;

    private static final PlayerField<String> FIELD = PlayerField.create("hypixel", "community_upgrades", Codecs.STRING, null);
    private static final Map<UUID, long[]> CACHE = new ConcurrentHashMap<>();

    private BazaarFlipper() {
    }

    public static int maxOrders(int tier) {
        return DEFAULT_ORDERS + EXTRA_ORDERS[clamp(tier)];
    }

    public static double taxRate(int tier) {
        return DEFAULT_TAX - TAX_REDUCTION[clamp(tier)];
    }

    public static double taxRateOf(UUID player) {
        return taxRate(tierOf(player));
    }

    public static int tierOf(UUID player) {
        long now = System.currentTimeMillis();
        long[] cached = CACHE.get(player);
        if (cached != null && now - cached[1] < CACHE_MILLIS) return (int) cached[0];
        int tier = read(player);
        CACHE.put(player, new long[]{tier, now});
        return tier;
    }

    private static int read(UUID player) {
        try {
            String raw = SharedData.account().get(player, FIELD);
            if (raw == null || raw.isBlank()) return 0;
            Object parsed = new JSONTokener(raw).nextValue();
            String json = parsed instanceof String text ? text : raw;
            if (json.isBlank()) return 0;
            JSONObject tiers = new JSONObject(json).optJSONObject("tiers");
            return tiers == null ? 0 : tiers.optInt("BAZAAR_FLIPPER", 0);
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    private static int clamp(int tier) {
        return Math.max(0, Math.min(tier, EXTRA_ORDERS.length - 1));
    }
}
