package dev.daweapon.type.skyblockgeneric.loottable;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class RareDropAward {
    private RareDropAward() {
    }

    public static boolean tryAward(SkyBlockPlayer player, ItemType item, double chancePercent) {
        if (!LootRoll.roll(player.getUuid(), item, chancePercent)) return false;
        SkyBlockItem reward = new SkyBlockItem(item, 1);
        player.addAndUpdateItem(reward);
        player.sendMessage(Text.of("<6><l>RARE DROP!</l> {}", reward.getDisplayNameText()));
        return true;
    }
}
