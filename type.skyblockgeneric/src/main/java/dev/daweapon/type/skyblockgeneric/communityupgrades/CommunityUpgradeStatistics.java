package dev.daweapon.type.skyblockgeneric.communityupgrades;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CommunityUpgradeStatistics {
    private CommunityUpgradeStatistics() {
    }

    public static ItemStatistics all(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double magicFind = CommunityUpgrades.value(player, CommunityUpgradeId.MAGIC_FIND);
        double fortune = onGarden() ? CommunityUpgrades.value(player, CommunityUpgradeId.GARDEN_FARMING_FORTUNE) : 0;
        if (magicFind == 0 && fortune == 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        if (magicFind != 0) builder.withBase(ItemStatistic.MAGIC_FIND, magicFind);
        if (fortune != 0) builder.withBase(ItemStatistic.FARMING_FORTUNE, fortune);
        return builder.build();
    }

    private static boolean onGarden() {
        return HypixelConst.getTypeLoader() != null && HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_THE_GARDEN;
    }
}
