package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.region.IslandType;

import java.util.List;
import java.util.Random;

public final class RabbitHotspots {
    public static final double HOTSPOT_SHARE = 0.15;
    public static final double HOTSPOT_WEIGHT_BONUS = 0.5;

    private RabbitHotspots() {
    }

    public static IslandType islandFor(int year, ChocolateRabbit rabbit, List<IslandType> islands) {
        if (rabbit.getLocation() != null || !RabbitRoller.isRollable(rabbit) || islands.isEmpty()) return null;
        Random random = new Random(year * 2_654_435_761L + rabbit.ordinal() * 40_503L);
        if (random.nextDouble() >= HOTSPOT_SHARE) return null;
        List<IslandType> ordered = islands.stream().sorted().toList();
        return ordered.get(random.nextInt(ordered.size()));
    }
}
