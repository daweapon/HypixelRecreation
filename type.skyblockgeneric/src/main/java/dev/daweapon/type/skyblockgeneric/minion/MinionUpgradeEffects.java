package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.minion.minions.mining.MinionGravel;

public final class MinionUpgradeEffects {
    private MinionUpgradeEffects() {
    }

    public static SkyBlockItem convert(SkyBlockItem generated, SkyBlockMinion minion, boolean hasFlintShovel) {
        if (!hasFlintShovel || !(minion instanceof MinionGravel)) return generated;
        if (generated.getAttributeHandler().getPotentialType() != ItemType.GRAVEL) return generated;
        return new SkyBlockItem(ItemType.FLINT, generated.getAmount());
    }
}
