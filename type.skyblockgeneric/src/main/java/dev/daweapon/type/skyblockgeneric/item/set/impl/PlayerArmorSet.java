package dev.daweapon.type.skyblockgeneric.item.set.impl;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public interface PlayerArmorSet extends ArmorSet {
    ItemStatistics getStatistics(SkyBlockPlayer player);
}
