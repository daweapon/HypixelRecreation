package dev.daweapon.type.skyblockgeneric.enchantment.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VitalityEnchants {
    private static final long DURATION_MS = 10_000;
    private static final Map<UUID, Double> STRONG = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> HARDENED = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> VIVACIOUS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> EXPIRY = new ConcurrentHashMap<>();

    private VitalityEnchants() {
    }

    public static double strongPercent(int level) {
        return level * 2D;
    }

    public static double hardenedPercent(int level) {
        return level * 4D;
    }

    public static double vivaciousPercent(int level) {
        return level;
    }

    public static double vampiricHealing(int level) {
        return 0.5 + level * 0.05;
    }

    public static double gainedForTest(Map<Integer, Integer> piecesByLevel, double used, java.util.function.IntToDoubleFunction percent) {
        return gained(piecesByLevel, used, percent);
    }

    static double gained(Map<Integer, Integer> piecesByLevel, double used, java.util.function.IntToDoubleFunction percent) {
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : piecesByLevel.entrySet()) {
            total += used * percent.applyAsDouble(entry.getKey()) / 100D * entry.getValue();
        }
        return total;
    }

    public static void onManaUsed(SkyBlockPlayer player, double used) {
        if (used <= 0) return;
        var stats = player.getStatistics();
        double strong = gained(stats.getAllOfEnchants(EnchantmentType.STRONG_MANA, false), used, VitalityEnchants::strongPercent);
        double hardened = gained(stats.getAllOfEnchants(EnchantmentType.HARDENED_MANA, false), used, VitalityEnchants::hardenedPercent);
        double vivacious = gained(stats.getAllOfEnchants(EnchantmentType.FEROCIOUS_MANA, false), used, VitalityEnchants::vivaciousPercent);
        double healing = gained(stats.getAllOfEnchants(EnchantmentType.MANA_VAMPIRE, false), used, level -> vampiricHealing(level) * 100D);
        if (healing > 0) player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + healing));
        if (strong + hardened + vivacious <= 0) return;

        UUID id = player.getUuid();
        long now = System.currentTimeMillis();
        boolean live = EXPIRY.getOrDefault(id, 0L) > now;
        double strongTotal = Math.min(15, (live ? STRONG.getOrDefault(id, 0D) : 0) + strong);
        double hardenedTotal = Math.min(25, (live ? HARDENED.getOrDefault(id, 0D) : 0) + hardened);
        double vivaciousTotal = Math.min(5, (live ? VIVACIOUS.getOrDefault(id, 0D) : 0) + vivacious);
        STRONG.put(id, strongTotal);
        HARDENED.put(id, hardenedTotal);
        VIVACIOUS.put(id, vivaciousTotal);
        EXPIRY.put(id, now + DURATION_MS);

        stats.removeTemporaryStatistics(statistic -> "Vitality".equals(statistic.getDisplayName()));
        stats.boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder()
                        .withBase(ItemStatistic.STRENGTH, strongTotal)
                        .withBase(ItemStatistic.DEFENSE, hardenedTotal)
                        .withBase(ItemStatistic.BONUS_ATTACK_SPEED, vivaciousTotal)
                        .build())
                .withExpirationInMs(DURATION_MS)
                .withDisplayName("Vitality")
                .withDisplayColor("<4>")
                .build());
    }
}
