package net.swofty.type.skyblockgeneric.levels.causes;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
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
     * Real Hypixel pays +1 SkyBlock XP per point of Magical Power, not a flat amount per
     * accessory, so a Legendary talisman is worth 16 and a Common one 3.
     */
    @Override
    public double xpReward() {
        Rarity rarity = new SkyBlockItem(itemTypeLinker).getAttributeHandler().getRarity();
        return rarity == null ? 0 : rarity.getMagicalPower();
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return true;
    }
}
