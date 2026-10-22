package dev.daweapon.type.skyblockgeneric.torrhus;

import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.List;

public enum StarbornTemple {
    DESERT("Desert", ItemType.ENCHANTED_HELIX_LOG, 0.2,
            new long[]{0, 1, 10, 25, 50, 100, 250, 500, 1_000, 2_500, 5_000, 10_000, 25_000, 50_000, 100_000, 250_000, 500_000, 1_000_000});

    public static final long COINS_PER_DAY = 70_000;
    public static final int ITEMS_PER_DAY = 2;
    public static final List<Integer> DONATION_DAYS = List.of(1, 5, 10, 20);
    public static final long DAY_MILLIS = 24L * 60 * 60 * 1000;
    public static final double BENEFACTOR_WILL_MULTIPLIER = 2;
    public static final double SPEED_BONUS = 0.25;

    private final String displayName;
    private final ItemType donationItem;
    private final double perTier;
    private final long[] thresholds;

    StarbornTemple(String displayName, ItemType donationItem, double perTier, long[] thresholds) {
        this.displayName = displayName;
        this.donationItem = donationItem;
        this.perTier = perTier;
        this.thresholds = thresholds;
    }

    public String displayName() {
        return displayName;
    }

    public ItemType donationItem() {
        return donationItem;
    }

    public String activeKey() {
        return "starborn.active." + name();
    }

    public String benefactorKey() {
        return "starborn.benefactor." + name();
    }

    public String redisKey() {
        return "starborn:benefactors:" + name();
    }

    public double will(long benefactors) {
        int tier = 0;
        for (int i = 0; i < thresholds.length; i++) {
            if (benefactors >= thresholds[i]) tier = i;
        }
        double value = tier * perTier;
        if (tier + 1 < thresholds.length) {
            value += (double) (benefactors - thresholds[tier]) / (thresholds[tier + 1] - thresholds[tier]) * perTier;
        }
        return value;
    }

    public static long coinCost(int days) {
        return days * COINS_PER_DAY;
    }

    public static int itemCost(int days) {
        return days * ITEMS_PER_DAY;
    }

    public static long extend(long now, long currentExpiry, int days) {
        return Math.max(now, currentExpiry) + days * DAY_MILLIS;
    }

    public static long carriedOver(long now, long... otherExpiries) {
        long carried = 0;
        for (long expiry : otherExpiries) carried += Math.max(0, expiry - now);
        return carried;
    }

    public static boolean benefactorActive(long expiry, long now) {
        return expiry > now;
    }
}
