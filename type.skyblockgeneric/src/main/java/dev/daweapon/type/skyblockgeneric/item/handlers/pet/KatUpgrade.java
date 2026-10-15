package dev.daweapon.type.skyblockgeneric.item.handlers.pet;

import dev.daweapon.commons.skyblock.item.ItemType;

import java.util.List;

/**
 * Kat's price to raise a pet to the next rarity: coins, items and real time.
 */
public record KatUpgrade(long timeMillis, long coins, List<ItemCost> items) {

    public record ItemCost(ItemType item, int amount) {
    }

    public long discountedCoins(int petLevel) {
        return Math.round(coins * (1 - 0.003 * Math.min(100, Math.max(0, petLevel))));
    }
}
