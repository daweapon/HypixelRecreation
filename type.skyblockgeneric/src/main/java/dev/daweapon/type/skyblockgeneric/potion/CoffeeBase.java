package dev.daweapon.type.skyblockgeneric.potion;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Set;

public enum CoffeeBase {
    CHEAP_COFFEE(5),
    DECENT_COFFEE(8),
    BLACK_COFFEE(12);

    private static final Set<PotionEffectType> SPEED_POTIONS = Set.of(
            PotionEffectType.ADRENALINE, PotionEffectType.AGILITY, PotionEffectType.RABBIT, PotionEffectType.SPEED);

    private final int speed;

    CoffeeBase(int speed) {
        this.speed = speed;
    }

    public int speed() {
        return speed;
    }

    public static CoffeeBase of(String base) {
        if (base == null) return null;
        for (CoffeeBase coffee : values()) if (coffee.name().equals(base)) return coffee;
        return null;
    }

    public static void apply(SkyBlockPlayer player, PotionEffectType effectType, String base, long durationMs) {
        CoffeeBase coffee = of(base);
        if (coffee == null || !SPEED_POTIONS.contains(effectType)) return;
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(ItemStatistics.builder().withBase(ItemStatistic.SPEED, (double) coffee.speed).build())
                .withExpirationInMs(durationMs)
                .build());
    }
}
