package net.swofty.type.skyblockgeneric.mythological.burrow;

import net.swofty.commons.skyblock.item.ItemType;
import org.jetbrains.annotations.Nullable;

/**
 * One row of a spade's treasure table: either an item or a pile of coins, with its weight in that
 * table. Myth the Fish and the Mythological Dye are rolled on their own before the table.
 */
public record BurrowTreasure(int weight, @Nullable ItemType item, int coins) {
    public static final BurrowTreasure MYTH_THE_FISH = item(0, ItemType.MYTH_THE_FISH);
    public static final BurrowTreasure MYTHOLOGICAL_DYE = item(0, ItemType.DYE_MYTHOLOGICAL);

    public static BurrowTreasure item(int weight, ItemType item) {
        return new BurrowTreasure(weight, item, 0);
    }

    public static BurrowTreasure coins(int weight, int coins) {
        return new BurrowTreasure(weight, null, coins);
    }
}
