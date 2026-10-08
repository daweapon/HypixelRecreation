package net.swofty.type.skyblockgeneric.fishing.festival;

import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * The Shark Tooth Necklace line and the extra chance each gives Sharks to drop their tooth
 * (wiki: Fishing Festival, Crafting). Only the highest one in the Accessory Bag counts.
 */
public enum SharkToothNecklace {
    RAGGEDY(ItemType.RAGGEDY_SHARK_TOOTH_NECKLACE, 5),
    DULL(ItemType.DULL_SHARK_TOOTH_NECKLACE, 10),
    HONED(ItemType.HONED_SHARK_TOOTH_NECKLACE, 15),
    SHARP(ItemType.SHARP_SHARK_TOOTH_NECKLACE, 20),
    RAZOR_SHARP(ItemType.RAZOR_SHARP_SHARK_TOOTH_NECKLACE, 25),
    ;

    private final ItemType item;
    private final double toothChanceBonus;

    SharkToothNecklace(ItemType item, double toothChanceBonus) {
        this.item = item;
        this.toothChanceBonus = toothChanceBonus;
    }

    public static double toothChanceBonus(SkyBlockPlayer player) {
        double best = 0;
        for (SkyBlockItem accessory : AccessoryOwnership.activeItems(player)) {
            ItemType type = accessory.getAttributeHandler().getPotentialType();
            for (SharkToothNecklace necklace : values()) {
                if (necklace.item == type) best = Math.max(best, necklace.toothChanceBonus);
            }
        }
        return best;
    }
}
