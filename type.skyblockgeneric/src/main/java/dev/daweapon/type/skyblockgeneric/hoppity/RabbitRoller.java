package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import dev.daweapon.type.skyblockgeneric.region.IslandType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class RabbitRoller {
    public static final int MYTHIC_PITY = 250;
    public static final int DIVINE_PITY = 1250;
    public static final double SHRINE_PERCENT_PER_LEVEL = 2.0;
    public static final double NAPOLEON_PERCENT = 10.0;
    private static final double RABBIT_WEIGHT = 100.0;
    private static final double[] BASE_WEIGHTS = {608, 250, 100, 30, 10, 4, 1};

    private RabbitRoller() {
    }

    public record Context(UUID player, int factoryLevel, int shrineLevel, boolean napoleon, double legendaryLuck,
                          double mythicLuck, double divineLuck, double unobtainedBonusPercent, IslandType island,
                          Set<ChocolateRabbit> found, int mythicPity, int divinePity, int hotspotYear,
                          List<IslandType> hotspotIslands) {
    }

    public record Result(ChocolateRabbit rabbit, boolean duplicate, int mythicPity, int divinePity) {
    }

    public static boolean isRollable(ChocolateRabbit rabbit) {
        return rabbit.isEggRollable();
    }

    public static int unlockLevel(RabbitRarity rarity) {
        return switch (rarity) {
            case COMMON, UNCOMMON -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            case LEGENDARY -> 4;
            case MYTHIC -> 5;
            case DIVINE -> 6;
        };
    }

    public static double rarityWeight(Context context, RabbitRarity rarity) {
        double bonus = 0;
        if (rarity.ordinal() >= RabbitRarity.UNCOMMON.ordinal()) bonus += context.shrineLevel() * SHRINE_PERCENT_PER_LEVEL;
        if (rarity.ordinal() >= RabbitRarity.RARE.ordinal() && context.napoleon()) bonus += NAPOLEON_PERCENT;
        bonus += switch (rarity) {
            case LEGENDARY -> context.legendaryLuck();
            case MYTHIC -> context.mythicLuck();
            case DIVINE -> context.divineLuck();
            default -> 0;
        };
        return BASE_WEIGHTS[rarity.ordinal()] * (1 + bonus / 100.0);
    }

    public static List<RabbitRarity> availableRarities(int factoryLevel) {
        List<RabbitRarity> available = new ArrayList<>();
        for (RabbitRarity rarity : RabbitRarity.values()) {
            if (unlockLevel(rarity) <= factoryLevel) available.add(rarity);
        }
        return available;
    }

    public static Result roll(Context context, RandomGenerator random) {
        int mythicPity = context.mythicPity() + 1;
        int divinePity = context.divinePity() + 1;
        RabbitRarity rarity;
        if (context.factoryLevel() >= unlockLevel(RabbitRarity.DIVINE) && divinePity >= DIVINE_PITY) {
            rarity = RabbitRarity.DIVINE;
        } else if (context.factoryLevel() >= unlockLevel(RabbitRarity.MYTHIC) && mythicPity >= MYTHIC_PITY) {
            rarity = RabbitRarity.MYTHIC;
        } else {
            List<RabbitRarity> available = availableRarities(context.factoryLevel());
            rarity = LootRoll.pick(context.player(), available, ignored -> null, r -> rarityWeight(context, r), random);
        }

        ChocolateRabbit rabbit = pickRabbit(context, rarity, random);
        while (rabbit == null && rarity.ordinal() > 0) {
            rarity = RabbitRarity.values()[rarity.ordinal() - 1];
            rabbit = pickRabbit(context, rarity, random);
        }
        if (rabbit == null) return null;

        if (rabbit.getRarity() == RabbitRarity.MYTHIC) mythicPity = 0;
        if (rabbit.getRarity() == RabbitRarity.DIVINE) divinePity = 0;
        return new Result(rabbit, context.found().contains(rabbit), mythicPity, divinePity);
    }

    public static List<ChocolateRabbit> candidates(Context context, RabbitRarity rarity) {
        List<ChocolateRabbit> candidates = new ArrayList<>();
        for (ChocolateRabbit rabbit : ChocolateRabbit.values()) {
            if (rabbit.getRarity() != rarity || !isRollable(rabbit)) continue;
            if (context.island() != null && rabbit.getLocation() != null && rabbit.getLocation() != context.island()) continue;
            candidates.add(rabbit);
        }
        return candidates;
    }

    public static double rabbitWeight(Context context, ChocolateRabbit rabbit) {
        double weight = RABBIT_WEIGHT;
        if (context.island() != null && context.island() == RabbitHotspots.islandFor(context.hotspotYear(), rabbit, context.hotspotIslands())) {
            weight *= 1 + RabbitHotspots.HOTSPOT_WEIGHT_BONUS;
        }
        if (!context.found().contains(rabbit)) weight *= 1 + context.unobtainedBonusPercent() / 100.0;
        return weight;
    }

    private static ChocolateRabbit pickRabbit(Context context, RabbitRarity rarity, RandomGenerator random) {
        List<ChocolateRabbit> candidates = candidates(context, rarity);
        if (candidates.isEmpty()) return null;
        return LootRoll.pick(context.player(), candidates, ignored -> null, rabbit -> rabbitWeight(context, rabbit), random);
    }
}
