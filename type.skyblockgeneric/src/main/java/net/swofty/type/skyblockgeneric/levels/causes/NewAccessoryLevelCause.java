package net.swofty.type.skyblockgeneric.levels.causes;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

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
