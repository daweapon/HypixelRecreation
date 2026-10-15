package dev.daweapon.type.skyblockgeneric.levels.causes;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NewAccessoryLevelCause extends SkyBlockLevelCauseAbstr {
    public ItemType itemTypeLinker;

    public NewAccessoryLevelCause(ItemType itemTypeLinker) {
        this.itemTypeLinker = itemTypeLinker;
    }

    @Override
    public String displayName() {
        return StringUtility.toNormalCase(itemTypeLinker.name());
    }

    /**
     * Records that an accessory was found. The Accessory Bag task pays by current Accessory Power
     * instead ({@code AccessoryBagXP}), so finding one is worth nothing on its own.
     */
    @Override
    public double xpReward() {
        return 0;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return true;
    }
}
