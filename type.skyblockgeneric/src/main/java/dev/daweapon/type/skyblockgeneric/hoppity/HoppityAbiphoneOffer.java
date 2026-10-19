package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit;
import dev.daweapon.type.skyblockgeneric.rabbits.ChocolateRabbit.RabbitRarity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class HoppityAbiphoneOffer {
    public static final double CALL_CHANCE_PERCENT = 2.0;

    public record Entry(@Nullable ChocolateRabbit fixed, @Nullable RabbitRarity rarity, long price, double weight) {
    }

    public record Offer(ChocolateRabbit rabbit, long price) {
    }

    private static final Entry ABI = new Entry(ChocolateRabbit.ABI, null, 500_000L, 35);
    private static final Entry COMMON = new Entry(null, RabbitRarity.COMMON, 250_000L, 35);
    private static final Entry UNCOMMON = new Entry(null, RabbitRarity.UNCOMMON, 500_000L, 10);
    private static final Entry RARE = new Entry(null, RabbitRarity.RARE, 1_000_000L, 20);
    private static final Entry EPIC = new Entry(null, RabbitRarity.EPIC, 2_500_000L, 5);

    private HoppityAbiphoneOffer() {
    }

    public static List<Entry> entries(boolean ownsAbi) {
        if (!ownsAbi) return List.of(ABI, COMMON, UNCOMMON, RARE, EPIC);
        return List.of(
                new Entry(null, RabbitRarity.COMMON, COMMON.price(), 7),
                new Entry(null, RabbitRarity.UNCOMMON, UNCOMMON.price(), 2),
                new Entry(null, RabbitRarity.RARE, RARE.price(), 4),
                new Entry(null, RabbitRarity.EPIC, EPIC.price(), 1));
    }

    public static double chancePercent(boolean ownsAbi, Entry entry) {
        double total = entries(ownsAbi).stream().mapToDouble(Entry::weight).sum();
        return entry.weight() / total * 100.0;
    }

    public static Offer roll(UUID player, boolean ownsAbi, RandomGenerator random) {
        Entry entry = LootRoll.pick(player, entries(ownsAbi), ignored -> null, Entry::weight, random);
        if (entry.fixed() != null) return new Offer(entry.fixed(), entry.price());
        List<ChocolateRabbit> candidates = HoppityShopStock.candidates(entry.rarity(), java.util.Set.of());
        ChocolateRabbit rabbit = LootRoll.pick(player, candidates, ignored -> null, ignored -> 1.0, random);
        return new Offer(rabbit, entry.price());
    }
}
