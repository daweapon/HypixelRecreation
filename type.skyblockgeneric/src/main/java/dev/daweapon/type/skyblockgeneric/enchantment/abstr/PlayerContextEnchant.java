package dev.daweapon.type.skyblockgeneric.enchantment.abstr;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public interface PlayerContextEnchant {
    ItemStatistics getStatistics(SkyBlockPlayer player, int level);
}
