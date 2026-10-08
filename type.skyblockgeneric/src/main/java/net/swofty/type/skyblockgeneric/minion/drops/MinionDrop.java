package net.swofty.type.skyblockgeneric.minion.drops;

import net.swofty.commons.skyblock.item.ItemType;

public record MinionDrop(ItemType item, int minAmount, int maxAmount, double chancePercent) {
    public static MinionDrop of(ItemType item, int amount) {
        return new MinionDrop(item, amount, amount, 100);
    }

    public static MinionDrop between(ItemType item, int minAmount, int maxAmount) {
        return new MinionDrop(item, minAmount, maxAmount, 100);
    }

    public static MinionDrop chance(ItemType item, int amount, double chancePercent) {
        return new MinionDrop(item, amount, amount, chancePercent);
    }

    public boolean guaranteed() {
        return chancePercent >= 100;
    }
}
