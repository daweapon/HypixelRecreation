package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.ArrayList;
import java.util.List;

public enum TreasurePool {
    LOW(build(false)),
    HIGH(build(true)),
    STRUCTURE(structure()),
    JUNGLE_TEMPLE(jungleTemple()),
    SLUDGE(sludge()),
    FAIRY_GROTTO(grotto());

    public record Entry(double weight, ItemType item, int min, int max, boolean powder) {
        static Entry of(double weight, ItemType item, int min, int max) {
            return new Entry(weight, item, min, max, false);
        }

        static Entry cut(double weight, int min, int max) {
            return new Entry(weight, null, min, max, true);
        }
    }

    private final List<Entry> entries;

    TreasurePool(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Entry> entries() {
        return entries;
    }

    private static List<Entry> build(boolean high) {
        List<Entry> list = new ArrayList<>();
        list.add(Entry.cut(high ? 30 : 35, high ? 500 : 100, high ? 2_500 : 500));
        for (String gem : gems()) {
            if (high) {
                list.add(Entry.of(4, gem(gem, "FLAWED"), 8, 24));
                list.add(Entry.of(1.5, gem(gem, "FINE"), 1, 2));
            } else {
                list.add(Entry.of(4, gem(gem, "ROUGH"), 20, 60));
                list.add(Entry.of(2, gem(gem, "FLAWED"), 4, 12));
            }
        }
        list.add(Entry.of(3, ItemType.ASCENSION_ROPE, 1, 1));
        list.add(Entry.of(high ? 4 : 2, ItemType.WISHING_COMPASS, 1, 1));
        list.add(Entry.of(high ? 4 : 3, ItemType.OIL_BARREL, 1, 1));
        list.add(Entry.of(high ? 2 : 0.5, ItemType.PREHISTORIC_EGG, 1, 1));
        list.add(Entry.of(high ? 1.5 : 0.3, ItemType.PICKONIMBUS_2000, 1, 1));
        return List.copyOf(list);
    }

    private static List<Entry> structure() {
        List<Entry> list = new ArrayList<>(build(true));
        list.add(Entry.cut(30, 1_000, 4_000));
        list.add(Entry.of(0.5, ItemType.RUBY_CRYSTAL, 1, 1));
        return List.copyOf(list);
    }

    private static List<Entry> jungleTemple() {
        List<Entry> list = new ArrayList<>(structure());
        list.add(Entry.of(3, ItemType.JUNGLE_HEART, 1, 1));
        return List.copyOf(list);
    }

    private static List<Entry> sludge() {
        List<Entry> list = new ArrayList<>(build(false));
        list.add(Entry.cut(120, 2_000, 5_000));
        return List.copyOf(list);
    }

    private static List<Entry> grotto() {
        return List.of(
                Entry.of(40, ItemType.ROUGH_JASPER_GEM, 20, 64),
                Entry.of(20, ItemType.FLAWED_JASPER_GEM, 4, 16),
                Entry.of(5, ItemType.FINE_JASPER_GEM, 1, 2),
                Entry.cut(25, 500, 2_000),
                Entry.of(1, ItemType.JASPER_CRYSTAL, 1, 1));
    }

    private static List<String> gems() {
        return List.of("RUBY", "AMETHYST", "JADE", "AMBER", "SAPPHIRE", "TOPAZ", "JASPER");
    }

    private static ItemType gem(String gem, String grade) {
        return ItemType.valueOf(grade + "_" + gem + "_GEM");
    }
}
