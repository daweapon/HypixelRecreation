package net.swofty.type.skyblockgeneric.fishing.winter;

import net.swofty.commons.skyblock.item.ItemType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * One row of a Winter Sea Creature's drop table. A row whose amount can roll zero (the wiki's
 * "0-1") drops nothing on that roll even when its chance succeeds.
 */
public record WinterDrop(ItemType item, int min, int max, double chancePercent) {
    public static WinterDrop always(ItemType item, int min, int max) {
        return new WinterDrop(item, min, max, 100);
    }

    public static WinterDrop always(ItemType item, int amount) {
        return always(item, amount, amount);
    }

    public static WinterDrop chance(ItemType item, double chancePercent) {
        return new WinterDrop(item, 1, 1, chancePercent);
    }

    public static WinterDrop oneIn(ItemType item, double odds) {
        return chance(item, 100D / odds);
    }

    public int rollAmount() {
        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
