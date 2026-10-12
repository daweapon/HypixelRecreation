package net.swofty.type.skyblockgeneric.minion;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.minion.minions.mining.MinionGravel;

public final class MinionUpgradeEffects {
    private MinionUpgradeEffects() {
    }

    public static SkyBlockItem convert(SkyBlockItem generated, SkyBlockMinion minion, boolean hasFlintShovel) {
        if (!hasFlintShovel || !(minion instanceof MinionGravel)) return generated;
        if (generated.getAttributeHandler().getPotentialType() != ItemType.GRAVEL) return generated;
        return new SkyBlockItem(ItemType.FLINT, generated.getAmount());
    }
}
