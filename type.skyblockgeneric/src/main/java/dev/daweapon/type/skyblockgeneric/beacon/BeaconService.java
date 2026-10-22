package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.data.monogdb.IslandDatabase;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BeaconService {
    public static final long CACHE_MILLIS = 5_000;

    private static final Map<UUID, Cached> CACHE = new ConcurrentHashMap<>();

    private BeaconService() {
    }

    private record Cached(@Nullable BeaconData data, long loadedAt) {
    }

    public static boolean isFuel(@Nullable ItemType type) {
        return type == ItemType.POWER_CRYSTAL || type == ItemType.SCORCHED_POWER_CRYSTAL;
    }

    public static @Nullable ItemType beaconItem(int tier) {
        return tier < 1 || tier > BeaconStat.MAX_TIER ? null : ItemType.get("BEACON_" + tier);
    }

    public static int tierOf(@Nullable ItemType type) {
        if (type == null || !type.name().startsWith("BEACON_")) return 0;
        try {
            int tier = Integer.parseInt(type.name().substring("BEACON_".length()));
            return tier >= 1 && tier <= BeaconStat.MAX_TIER ? tier : 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static @Nullable BeaconData get(UUID islandId) {
        long now = System.currentTimeMillis();
        Cached cached = CACHE.get(islandId);
        if (cached != null && now - cached.loadedAt() < CACHE_MILLIS) return cached.data();
        BeaconData loaded = BeaconData.deserialize(String.valueOf(new IslandDatabase(islandId.toString()).get(BeaconData.DATABASE_KEY, "")));
        CACHE.put(islandId, new Cached(loaded, now));
        return loaded;
    }

    public static @Nullable BeaconData get(@Nullable SkyBlockIsland island) {
        return island == null ? null : get(island.getIslandID());
    }

    public static void save(UUID islandId, @Nullable BeaconData data) {
        new IslandDatabase(islandId.toString()).insertOrUpdate(BeaconData.DATABASE_KEY, data == null ? "" : data.serialize());
        CACHE.put(islandId, new Cached(data, System.currentTimeMillis()));
    }

    public static void insertCrystals(SkyBlockIsland island, int amount, boolean scorched) {
        BeaconData data = get(island);
        if (data == null || amount <= 0) return;
        save(island.getIslandID(), data.withCrystals(amount, scorched, System.currentTimeMillis()));
    }

    public static double minionSpeedPercent(@Nullable Instance instance) {
        BeaconData data = get(SkyBlockIsland.forInstance(instance));
        return data == null ? 0 : data.minionSpeedPercent(System.currentTimeMillis());
    }

    public static String formatRemaining(long millis) {
        long totalMinutes = millis / 60_000;
        long days = totalMinutes / (60 * 24);
        long hours = totalMinutes / 60 % 24;
        long minutes = totalMinutes % 60;
        if (days > 0) return days + "d " + hours + "h " + minutes + "m";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}
