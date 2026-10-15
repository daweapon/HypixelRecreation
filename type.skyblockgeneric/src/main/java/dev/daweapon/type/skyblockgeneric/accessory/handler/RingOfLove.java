package dev.daweapon.type.skyblockgeneric.accessory.handler;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

/** The Ring of Love line: a chance per hit to deal +100% damage, from 1 in 10^12 up to 1 in 100. */
public final class RingOfLove {
    private static final Map<ItemType, Double> CHANCE_PERCENT = Map.of(
            ItemType.WEDDING_RING_0, 1e-10,
            ItemType.WEDDING_RING_1, 1e-9,
            ItemType.WEDDING_RING_2, 1e-8,
            ItemType.WEDDING_RING_3, 1e-7,
            ItemType.WEDDING_RING_4, 1e-6,
            ItemType.WEDDING_RING_5, 1e-4,
            ItemType.WEDDING_RING_6, 1e-3,
            ItemType.WEDDING_RING_7, 1e-2,
            ItemType.WEDDING_RING_8, 1e-1,
            ItemType.WEDDING_RING_9, 1D
    );

    private RingOfLove() {
    }

    public static double chancePercent(SkyBlockPlayer player) {
        double best = 0;
        for (Map.Entry<ItemType, Double> entry : CHANCE_PERCENT.entrySet()) {
            if (entry.getValue() > best && UsableAccessory.usable(player, entry.getKey())) best = entry.getValue();
        }
        return best;
    }

    public static double damageMultiplier(SkyBlockPlayer player) {
        double chance = chancePercent(player);
        return chance > 0 && LootRoll.roll(player.getUuid(), null, chance) ? 2 : 1;
    }
}
