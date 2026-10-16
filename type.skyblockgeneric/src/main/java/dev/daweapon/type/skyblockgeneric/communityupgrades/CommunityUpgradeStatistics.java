package dev.daweapon.type.skyblockgeneric.communityupgrades;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CommunityUpgradeStatistics {
    private CommunityUpgradeStatistics() {
    }

    public static ItemStatistics all(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double magicFind = CommunityUpgrades.value(player, CommunityUpgradeId.MAGIC_FIND);
        if (magicFind == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, magicFind).build();
    }
}
