package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class HoppityShopStock {
    public static final int SLOTS = 7;
    private static final List<RabbitRarity> SHOP_RARITIES = List.of(RabbitRarity.COMMON, RabbitRarity.UNCOMMON,
            RabbitRarity.RARE, RabbitRarity.EPIC, RabbitRarity.LEGENDARY);
    private static final double[][] SLOT_WEIGHTS = {
            {100}, {100}, {90, 10}, {75, 25}, {50, 40, 10}, {30, 50, 16, 4}, {10, 45, 35, 7, 3}
    };

    private HoppityShopStock() {
    }

    public static double weight(int slot, RabbitRarity rarity) {
        double[] weights = SLOT_WEIGHTS[slot];
        return rarity.ordinal() < weights.length ? weights[rarity.ordinal()] : 0;
    }

    public static long price(RabbitRarity rarity) {
        return switch (rarity) {
            case COMMON -> 200_000L;
            case UNCOMMON -> 500_000L;
            case RARE -> 1_000_000L;
            case EPIC -> 2_500_000L;
            default -> 5_000_000L;
        };
    }

    public static long price(ChocolateRabbit rabbit) {
        return price(rabbit.getRarity());
    }

    public static RabbitRarity rollRarity(UUID player, int slot, RandomGenerator random) {
        List<RabbitRarity> rarities = SHOP_RARITIES.subList(0, SLOT_WEIGHTS[slot].length);
        return LootRoll.pick(player, rarities, ignored -> null, rarity -> weight(slot, rarity), random);
    }

    public static List<ChocolateRabbit> candidates(RabbitRarity rarity, Set<ChocolateRabbit> taken) {
        List<ChocolateRabbit> candidates = new ArrayList<>();
        for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
            if (rabbit.getRarity() == rarity && rabbit.isEggRollable() && !taken.contains(rabbit)) candidates.add(rabbit);
        }
        return candidates;
    }

    public static List<ChocolateRabbit> generate(UUID player, RandomGenerator random) {
        List<ChocolateRabbit> stock = new ArrayList<>();
        Set<ChocolateRabbit> taken = new java.util.HashSet<>();
        for (int slot = 0; slot < SLOTS; slot++) {
            RabbitRarity rarity = rollRarity(player, slot, random);
            List<ChocolateRabbit> candidates = candidates(rarity, taken);
            while (candidates.isEmpty() && rarity.ordinal() > 0) {
                rarity = RabbitRarity.values()[rarity.ordinal() - 1];
                candidates = candidates(rarity, taken);
            }
            ChocolateRabbit rabbit = LootRoll.pick(player, candidates, ignored -> null, ignored -> 1.0, random);
            stock.add(rabbit);
            taken.add(rabbit);
        }
        return stock;
    }
}
