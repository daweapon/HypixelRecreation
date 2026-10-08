package net.swofty.type.skyblockgeneric.loottable;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

/**
 * The one place a chance-based item reward is decided. Every RNG drop rolls through here so a
 * staff-forced drop ({@code /forcedrop}) wins the roll, whatever system the drop belongs to.
 */
public final class LootRoll {
    private LootRoll() {
    }

    public static boolean roll(@Nullable UUID player, @Nullable ItemType item, double chancePercent) {
        return roll(player, item, chancePercent, ThreadLocalRandom.current());
    }

    public static boolean roll(@Nullable UUID player, @Nullable ItemType item, double chancePercent, RandomGenerator random) {
        if (player != null && item != null && ForcedDrops.consume(player, item)) return true;
        return random.nextDouble() * 100 < chancePercent;
    }

    /**
     * For systems that pick with their own logic: if one of the candidates is forced, it is claimed
     * and returned so the caller can award it; otherwise null and the caller rolls as usual.
     */
    public static @Nullable ItemType claim(@Nullable UUID player, Collection<@Nullable ItemType> candidates) {
        if (player == null) return null;
        for (ItemType candidate : candidates) {
            if (candidate != null && ForcedDrops.consume(player, candidate)) return candidate;
        }
        return null;
    }

    /**
     * Weighted pick-one: a forced item in the table is picked outright, otherwise each entry wins
     * in proportion to its weight. Entries without an item (coins, nothing) can still be picked.
     */
    public static <T> T pick(@Nullable UUID player, List<T> entries, Function<T, @Nullable ItemType> item,
                             ToDoubleFunction<T> weight, RandomGenerator random) {
        if (player != null) {
            for (T entry : entries) {
                ItemType type = item.apply(entry);
                if (type != null && ForcedDrops.consume(player, type)) return entry;
            }
        }
        double total = entries.stream().mapToDouble(weight).sum();
        double pick = random.nextDouble() * total;
        for (T entry : entries) {
            pick -= weight.applyAsDouble(entry);
            if (pick < 0) return entry;
        }
        return entries.getLast();
    }
}
