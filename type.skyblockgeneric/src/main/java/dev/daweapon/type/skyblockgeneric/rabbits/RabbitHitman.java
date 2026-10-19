package dev.daweapon.type.skyblockgeneric.rabbits;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import dev.daweapon.type.skyblockgeneric.hoppity.EggLocation;
import dev.daweapon.type.skyblockgeneric.hoppity.EggLocationCatalogue;
import dev.daweapon.type.skyblockgeneric.hoppity.HuntEggSchedule;
import dev.daweapon.type.skyblockgeneric.region.IslandType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class RabbitHitman {
    public static final int MAX_SLOTS = 28;
    public static final long BASE_COOLDOWN_MS = 20L * 60_000L;
    public static final int CATCH_UP_SPAWNS = 40;
    private static final long[] SLOT_PRICES = {
            765_000L, 892_000L, 1_151_000L, 1_504_000L, 1_936_000L, 2_438_000L, 3_005_000L, 3_632_000L,
            4_315_000L, 5_051_000L, 5_838_000L, 6_674_000L, 7_557_000L, 8_485_000L, 9_456_000L, 10_471_000L,
            11_527_000L, 12_623_000L, 13_758_000L, 14_933_000L, 16_144_000L, 17_393_000L, 18_678_000L,
            19_998_000L, 21_354_000L, 22_744_000L, 24_167_000L, 25_622_000L
    };

    private RabbitHitman() {
    }

    public static int purchasedSlots(ChocolateFactoryData data) {
        return Math.max(0, Math.min(MAX_SLOTS, data.getHitmanSlots()));
    }

    public static int storedEggs(ChocolateFactoryData data) {
        return data.getHitmanEggs().size();
    }

    public static long slotPrice(int slotIndex) {
        return SLOT_PRICES[slotIndex];
    }

    public static long totalPrice() {
        long total = 0;
        for (long price : SLOT_PRICES) total += price;
        return total;
    }

    public static int unlockedSlots(ChocolateFactoryData data) {
        return Math.min(MAX_SLOTS, ChocolateFactoryLevel.forPrestige(data.getPrestigeLevel()).getMaxHitmanSlots());
    }

    public static int requiredFactoryLevel(int slotIndex) {
        for (ChocolateFactoryLevel level : ChocolateFactoryLevel.values()) {
            if (level.getMaxHitmanSlots() > slotIndex) return level.ordinal() + 1;
        }
        return ChocolateFactoryLevel.values().length;
    }

    public static boolean canBuyNextSlot(ChocolateFactoryData data) {
        int purchased = purchasedSlots(data);
        return purchased < MAX_SLOTS && purchased < unlockedSlots(data);
    }

    public static long cooldownPerSlot(double reductionPercent) {
        double factor = 1.0 - Math.max(0, Math.min(100, reductionPercent)) / 100.0;
        return Math.max(1L, Math.round(BASE_COOLDOWN_MS * factor));
    }

    public static int slotsOnCooldown(ChocolateFactoryData data, long now, double reductionPercent) {
        long remaining = data.getHitmanQueueEnd() - now;
        if (remaining <= 0) return 0;
        long perSlot = cooldownPerSlot(reductionPercent);
        return (int) ((remaining + perSlot - 1) / perSlot);
    }

    public static long cooldownRemaining(ChocolateFactoryData data, long now, double reductionPercent, int cooldownIndex) {
        int onCooldown = slotsOnCooldown(data, now, reductionPercent);
        long remaining = data.getHitmanQueueEnd() - now - (long) (onCooldown - 1 - cooldownIndex) * cooldownPerSlot(reductionPercent);
        return Math.max(0, remaining);
    }

    public static int freeSlots(ChocolateFactoryData data, long now, double reductionPercent) {
        return Math.max(0, purchasedSlots(data) - storedEggs(data) - slotsOnCooldown(data, now, reductionPercent));
    }

    public static boolean canOpen(ChocolateFactoryData data, long now, double reductionPercent) {
        return storedEggs(data) > 0 && slotsOnCooldown(data, now, reductionPercent) < purchasedSlots(data);
    }

    public static void startCooldown(ChocolateFactoryData data, long now, double reductionPercent) {
        data.setHitmanQueueEnd(Math.max(now, data.getHitmanQueueEnd()) + cooldownPerSlot(reductionPercent));
    }

    public static boolean store(ChocolateFactoryData data, String locationKey) {
        data.getHitmanEggs().add(locationKey);
        return true;
    }

    public static EggLocation locationOf(String locationKey, EggLocationCatalogue catalogue) {
        int split = locationKey.lastIndexOf(':');
        if (split < 0) return null;
        try {
            IslandType island = IslandType.valueOf(locationKey.substring(0, split));
            return catalogue.location(island, Integer.parseInt(locationKey.substring(split + 1)));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static String markerOf(int year, int spawnNumber) {
        return HuntEggSchedule.spawnId(year, spawnNumber);
    }

    public static int lastChecked(ChocolateFactoryData data, int year) {
        String marker = data.getHitmanLastCheckedSpawn();
        String prefix = year + ":";
        if (marker == null || !marker.startsWith(prefix)) return -1;
        try {
            return Integer.parseInt(marker.substring(prefix.length()));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public static List<Integer> missedSpawns(int year, int windowStart, Set<String> claimed, int lastChecked, int freeSlots) {
        List<Integer> missed = new ArrayList<>();
        int from = Math.max(Math.max(0, lastChecked + 1), windowStart - CATCH_UP_SPAWNS);
        for (int number = from; number < windowStart && missed.size() < freeSlots; number++) {
            if (!claimed.contains(HuntEggSchedule.spawnId(year, number))) missed.add(number);
        }
        return missed;
    }

    public static EggLocation pickLocation(UUID owner, String seedKey, int year, int spawnNumber, EggLocationCatalogue catalogue) {
        List<IslandType> islands = catalogue.islands().stream().filter(catalogue::hasEggs).toList();
        if (islands.isEmpty()) return null;
        Random random = new Random(owner.getMostSignificantBits() * 31 + owner.getLeastSignificantBits() * 17 + seedKey.hashCode());
        IslandType island = islands.get(random.nextInt(islands.size()));
        var egg = HuntEggSchedule.egg(year, Math.max(0, Math.min(spawnNumber, HuntEggSchedule.SPAWNS_PER_HUNT - 1)), island, catalogue);
        return egg == null ? catalogue.locations(island).getFirst() : egg.location();
    }
}
