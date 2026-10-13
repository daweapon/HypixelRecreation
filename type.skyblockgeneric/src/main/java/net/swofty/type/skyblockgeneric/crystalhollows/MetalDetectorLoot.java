package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class MetalDetectorLoot {
    public static final double SCAVENGED_WEIGHT = 20;
    public static final List<ItemType> SCAVENGED = List.of(
            ItemType.DWARVEN_LAPIS_SWORD, ItemType.DWARVEN_GOLD_HAMMER,
            ItemType.DWARVEN_DIAMOND_AXE, ItemType.DWARVEN_EMERALD_HAMMER);

    public record Entry(double weight, ItemType item, int min, int max, boolean powder) {
    }

    private record Gem(String name, int roughOrFlawed, int flawedPair, int fine) {
    }

    private static final List<Gem> GEMS = List.of(
            new Gem("AMETHYST", 1, 2, 2), new Gem("AMBER", 1, 2, 2), new Gem("JADE", 2, 4, 4),
            new Gem("SAPPHIRE", 1, 2, 2), new Gem("TOPAZ", 1, 2, 2), new Gem("RUBY", 2, 4, 4));

    private MetalDetectorLoot() {
    }

    public static List<ItemType> missing(java.util.function.Predicate<ItemType> obtained) {
        return SCAVENGED.stream().filter(item -> !obtained.test(item)).toList();
    }

    public static List<Entry> table(Collection<ItemType> missingScavenged) {
        boolean complete = missingScavenged.isEmpty();
        double single = complete ? 7.44 : 5.95;
        double pair = complete ? 4.46 : 3.57;
        List<Entry> list = new ArrayList<>();
        for (ItemType item : missingScavenged) {
            list.add(new Entry(SCAVENGED_WEIGHT / missingScavenged.size(), item, 1, 1, false));
        }
        for (Gem gem : GEMS) {
            list.add(item(single, "FLAWED_" + gem.name() + "_GEM", gem.roughOrFlawed()));
            list.add(item(pair, "FLAWED_" + gem.name() + "_GEM", gem.flawedPair()));
            list.add(item(0.01, "FINE_" + gem.name() + "_GEM", gem.fine()));
        }
        list.add(item(single, "ROUGH_JASPER_GEM", 40));
        list.add(item(pair, "FLAWED_JASPER_GEM", 1));
        list.add(item(0.01, "FINE_JASPER_GEM", 1));
        list.add(new Entry(complete ? 7.44 : 5.96, null, 100, 500, true));
        list.add(new Entry(complete ? 0.74 : 0.60, null, 500, 2_500, true));
        list.add(new Entry(complete ? 0.06 : 0.05, null, 2_500, 10_000, true));
        list.add(new Entry(complete ? 0.07 : 0.06, ItemType.PICKONIMBUS_2000, 1, 1, false));
        list.add(new Entry(complete ? 0.03 : 0.02, ItemType.ASCENSION_ROPE, 1, 1, false));
        return List.copyOf(list);
    }

    private static Entry item(double weight, String id, int amount) {
        return new Entry(weight, ItemType.valueOf(id), amount, amount, false);
    }

    public static Entry roll(UUID player, List<Entry> table, RandomGenerator random) {
        return LootRoll.pick(player, table, Entry::item, Entry::weight, random);
    }

    public static int amount(Entry entry, RandomGenerator random) {
        return entry.min() + random.nextInt(entry.max() - entry.min() + 1);
    }
}
