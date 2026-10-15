package dev.daweapon.type.skyblockgeneric.fossil;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * The Fossil Excavator treasure table, with the wiki's weights (total 6,475) and its 10 to 14
 * treasure tiles per excavation.
 */
public final class ExcavatorLoot {
    public enum Kind {ITEM, ESSENCE, POWDER, BOOK}

    public record Entry(Kind kind, @Nullable ItemType item, int min, int max, double weight) {}

    public record Drop(Kind kind, @Nullable ItemType item, int amount) {}

    private static final int[] ROLL_COUNTS = {10, 11, 12, 13, 14};
    private static final double[] ROLL_WEIGHTS = {30, 25, 20, 15, 10};

    public static final List<Entry> TABLE = buildTable();

    private ExcavatorLoot() {}

    private static List<Entry> buildTable() {
        List<Entry> entries = new ArrayList<>();
        for (ItemType[] gem : new ItemType[][]{
                {ItemType.FLAWED_AQUAMARINE_GEM, ItemType.FINE_AQUAMARINE_GEM},
                {ItemType.FLAWED_PERIDOT_GEM, ItemType.FINE_PERIDOT_GEM},
                {ItemType.FLAWED_CITRINE_GEM, ItemType.FINE_CITRINE_GEM},
                {ItemType.FLAWED_ONYX_GEM, ItemType.FINE_ONYX_GEM}}) {
            entries.add(new Entry(Kind.ITEM, gem[0], 20, 20, 450));
            entries.add(new Entry(Kind.ITEM, gem[0], 40, 40, 300));
            entries.add(new Entry(Kind.ITEM, gem[1], 1, 1, 150));
            entries.add(new Entry(Kind.ITEM, gem[1], 2, 2, 75));
        }
        entries.add(new Entry(Kind.ESSENCE, null, 10, 10, 400));
        entries.add(new Entry(Kind.ESSENCE, null, 20, 20, 200));
        entries.add(new Entry(Kind.ESSENCE, null, 40, 40, 100));
        for (ItemType metal : new ItemType[]{ItemType.ENCHANTED_UMBER, ItemType.ENCHANTED_TUNGSTEN, ItemType.ENCHANTED_GLACITE}) {
            entries.add(new Entry(Kind.ITEM, metal, 2, 2, 250));
            entries.add(new Entry(Kind.ITEM, metal, 4, 4, 125));
            entries.add(new Entry(Kind.ITEM, metal, 8, 8, 50));
        }
        entries.add(new Entry(Kind.POWDER, null, 1000, 1999, 250));
        entries.add(new Entry(Kind.POWDER, null, 2000, 2999, 125));
        entries.add(new Entry(Kind.POWDER, null, 3000, 3999, 100));
        entries.add(new Entry(Kind.POWDER, null, 4000, 6000, 40));
        entries.add(new Entry(Kind.POWDER, null, 7000, 10000, 20));
        entries.add(new Entry(Kind.BOOK, null, 1, 1, 50));
        entries.add(new Entry(Kind.ITEM, ItemType.FOSSIL_THE_FISH, 1, 1, 15));
        return List.copyOf(entries);
    }

    public static double totalWeight() {
        return TABLE.stream().mapToDouble(Entry::weight).sum();
    }

    public static int rollCount(RandomGenerator random) {
        double pick = random.nextDouble() * java.util.Arrays.stream(ROLL_WEIGHTS).sum();
        for (int i = 0; i < ROLL_COUNTS.length; i++) {
            pick -= ROLL_WEIGHTS[i];
            if (pick < 0) return ROLL_COUNTS[i];
        }
        return ROLL_COUNTS[ROLL_COUNTS.length - 1];
    }

    public static Drop roll(UUID player, RandomGenerator random) {
        Entry entry = LootRoll.pick(player, TABLE, Entry::item, Entry::weight, random);
        int amount = entry.min() == entry.max() ? entry.min() : random.nextInt(entry.min(), entry.max() + 1);
        return new Drop(entry.kind(), entry.item(), amount);
    }
}
