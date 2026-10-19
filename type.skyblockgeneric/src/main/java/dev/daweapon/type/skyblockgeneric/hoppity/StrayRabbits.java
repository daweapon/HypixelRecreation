package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

public final class StrayRabbits {
    public static final double SPAWN_CHANCE_PERCENT = 20;
    public static final long SPAWN_INTERVAL_MS = 30_000L;
    public static final long VISIBLE_MS = 15_000L;
    public static final double FISH_REPLACEMENT_PERCENT = 1;
    public static final double GOLDEN_CHANCE_PERCENT = 10;
    public static final double GOLDEN_CHANCE_OMEGA_PERCENT = 20;
    public static final int STAMPEDE_STRAYS = 10;
    private static final int[] RARITY_SECONDS = {5, 7, 10, 15, 20, 30, 50};

    private StrayRabbits() {
    }

    public enum Golden {
        JACKPOT(35, 1),
        SIDE_DISH(20, 1),
        CHOCOLATE_MOUNTAIN(10, 2),
        EL_DORADO(10, 3),
        STAMPEDE(10, 1),
        GOLDEN_CLICK(10, 4);

        private final double weight;
        private final int factoryLevel;

        Golden(double weight, int factoryLevel) {
            this.weight = weight;
            this.factoryLevel = factoryLevel;
        }

        public double weight() {
            return weight;
        }

        public int factoryLevel() {
            return factoryLevel;
        }
    }

    public static int rewardSeconds(RabbitRarity rarity, boolean huntActive) {
        return RARITY_SECONDS[rarity.ordinal()] * (huntActive ? 2 : 1);
    }

    public static ChocolateRabbit pickRabbit(UUID player, Set<ChocolateRabbit> found, int factoryLevel,
                                             ToDoubleFunction<RabbitRarity> weight, RandomGenerator random) {
        List<RabbitRarity> present = new ArrayList<>();
        for (RabbitRarity rarity : RabbitRarity.values()) {
            if (RabbitRoller.unlockLevel(rarity) > factoryLevel) continue;
            if (found.stream().anyMatch(rabbit -> rabbit.getRarity() == rarity)) present.add(rarity);
        }
        if (present.isEmpty()) return null;
        RabbitRarity rarity = LootRoll.pick(player, present, ignored -> null, weight, random);
        List<ChocolateRabbit> pool = found.stream().filter(rabbit -> rabbit.getRarity() == rarity).sorted().toList();
        return pool.get(random.nextInt(pool.size()));
    }

    public static Golden pickGolden(UUID player, int factoryLevel, RandomGenerator random) {
        List<Golden> available = new ArrayList<>();
        for (Golden golden : Golden.values()) {
            if (golden.factoryLevel() <= factoryLevel) available.add(golden);
        }
        return LootRoll.pick(player, available, ignored -> null, Golden::weight, random);
    }

    public static boolean spawnsThisCheck(UUID player, RandomGenerator random) {
        return LootRoll.roll(player, null, SPAWN_CHANCE_PERCENT, random);
    }

    public static boolean isGolden(UUID player, boolean omega, RandomGenerator random) {
        return LootRoll.roll(player, null, omega ? GOLDEN_CHANCE_OMEGA_PERCENT : GOLDEN_CHANCE_PERCENT, random);
    }

    public static boolean isFish(UUID player, RandomGenerator random) {
        return LootRoll.roll(player, null, FISH_REPLACEMENT_PERCENT, random);
    }
}
