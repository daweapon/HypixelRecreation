package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * One row of a lava Sea Creature's drop table. Items are named by id so a row whose item this server
 * doesn't have yet stays catalogued and simply never drops. {@code enchantment} marks an Enchanted Book row.
 */
public record LavaDrop(String itemId, int min, int max, double chancePercent,
                       @Nullable String enchantment, int enchantmentLevel) {

    public static LavaDrop always(String itemId, int min, int max) {
        return new LavaDrop(itemId, min, max, 100, null, 0);
    }

    public static LavaDrop always(String itemId, int amount) {
        return always(itemId, amount, amount);
    }

    public static LavaDrop chance(String itemId, double chancePercent) {
        return new LavaDrop(itemId, 1, 1, chancePercent, null, 0);
    }

    public static LavaDrop oneIn(String itemId, double odds) {
        return chance(itemId, 100D / odds);
    }

    public static LavaDrop book(String enchantment, int level, double chancePercent) {
        return new LavaDrop("ENCHANTED_BOOK", 1, 1, chancePercent, enchantment, level);
    }

    public static LavaDrop carmine(double odds) {
        return oneIn("DYE_CARMINE", odds);
    }

    public @Nullable ItemType itemType() {
        return ItemType.get(itemId);
    }

    public int rollAmount() {
        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
