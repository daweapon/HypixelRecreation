package net.swofty.type.skyblockgeneric.item.set.impl;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public interface PlayerArmorSet extends ArmorSet {
    ItemStatistics getStatistics(SkyBlockPlayer player);
}
