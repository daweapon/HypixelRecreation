package dev.daweapon.type.skyblockgeneric.theend;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ender Node weighted tables from the wiki (Sept 2026 staff correction). The node's original block picks the table;
 * a Stonk removes the Nest Endermite outcome and the rest keep their weights.
 */
public final class EnderNodeLoot {
    public record Entry(@Nullable ItemType item, int amount, int weight) {
        public boolean endermites() {
            return item == null;
        }
    }

    public static final int ENDERMITE_WEIGHT = 350;

    public static final List<Entry> END_STONE = List.of(
            new Entry(null, 0, ENDERMITE_WEIGHT),
            new Entry(ItemType.ENCHANTED_END_STONE, 1, 400),
            new Entry(ItemType.ENCHANTED_ENDER_PEARL, 5, 110),
            new Entry(ItemType.GRAND_EXP_BOTTLE, 1, 120),
            new Entry(ItemType.ENDER_GAUNTLET, 1, 35),
            new Entry(ItemType.END_STONE_SHULKER, 1, 20),
            new Entry(ItemType.ENDSTONE_GEODE, 1, 15),
            new Entry(ItemType.SHRIMP_THE_FISH, 1, 1));

    public static final List<Entry> OBSIDIAN = List.of(
            new Entry(null, 0, ENDERMITE_WEIGHT),
            new Entry(ItemType.ENCHANTED_OBSIDIAN, 1, 400),
            new Entry(ItemType.ENCHANTED_ENDER_PEARL, 5, 80),
            new Entry(ItemType.GRAND_EXP_BOTTLE, 2, 160),
            new Entry(ItemType.GRAND_EXP_BOTTLE, 5, 70),
            new Entry(ItemType.TITANIC_EXP_BOTTLE, 1, 15),
            new Entry(ItemType.ENDER_GAUNTLET, 1, 35),
            new Entry(ItemType.MAGIC_RUNE, 1, 30),
            new Entry(ItemType.SHRIMP_THE_FISH, 1, 2));

    private EnderNodeLoot() {
    }

    public static List<Entry> table(EnderNodeType type, boolean stonk) {
        List<Entry> table = type == EnderNodeType.OBSIDIAN ? OBSIDIAN : END_STONE;
        return stonk ? table.stream().filter(entry -> !entry.endermites()).toList() : table;
    }

    public static Entry roll(@Nullable UUID player, EnderNodeType type, boolean stonk) {
        return LootRoll.pick(player, table(type, stonk), Entry::item, Entry::weight, ThreadLocalRandom.current());
    }
}
