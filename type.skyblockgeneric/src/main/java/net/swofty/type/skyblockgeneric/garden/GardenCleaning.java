package net.swofty.type.skyblockgeneric.garden;

import java.util.Set;

public final class GardenCleaning {
    public static final int GROUND_Y = 70;
    public static final int SCAN_TOP_Y = 160;
    public static final double LEAF_DECAY_AT = 0.70;
    public static final double CLEAN_AT = 0.98;

    private static final Set<String> FLOWERS = Set.of(
            "dandelion", "poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip",
            "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley", "sunflower",
            "lilac", "rose_bush", "peony", "wither_rose", "torchflower", "pink_petals", "open_eyeblossom");
    private static final Set<String> PLANTS = Set.of(
            "short_grass", "tall_grass", "fern", "large_fern", "dead_bush", "sweet_berry_bush", "bush",
            "red_mushroom", "brown_mushroom", "sugar_cane", "bamboo");
    private static final Set<String> GROUND = Set.of(
            "grass_block", "dirt", "coarse_dirt", "podzol", "rooted_dirt", "mycelium", "moss_block",
            "stone", "cobblestone", "mossy_cobblestone", "andesite", "diorite", "granite", "gravel");

    private GardenCleaning() {
    }

    public static boolean isLeaf(String name) {
        return name.endsWith("_leaves");
    }

    public static boolean isScytheTarget(String name) {
        return isLeaf(name) || FLOWERS.contains(name) || name.equals("short_grass") || name.equals("tall_grass")
                || name.equals("fern") || name.equals("large_fern");
    }

    public static boolean isCleanable(String name, int y) {
        if (y <= GROUND_Y) {
            return false;
        }
        return isLeaf(name) || name.endsWith("_log") || name.endsWith("_wood") || name.endsWith("_stem")
                || FLOWERS.contains(name) || PLANTS.contains(name) || GROUND.contains(name);
    }

    public static double progress(int total, int remaining) {
        if (total <= 0) {
            return 1;
        }
        return Math.min(1, Math.max(0, 1 - (double) remaining / total));
    }

    public static boolean isClean(int total, int remaining) {
        return progress(total, remaining) >= CLEAN_AT;
    }

    public static boolean leavesDecay(int total, int remaining) {
        return progress(total, remaining) > LEAF_DECAY_AT;
    }
}
