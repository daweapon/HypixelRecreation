package dev.daweapon.type.skyblockgeneric.beacon;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;

public final class BeaconStatistics {
    private BeaconStatistics() {
    }

    public static ItemStatistics all(SkyBlockPlayer player) {
        return ItemStatistics.add(profileStat(player), islandEffects(player));
    }

    public static ItemStatistics profileStat(SkyBlockPlayer player) {
        BeaconData own = BeaconService.get(player.getSkyBlockIsland());
        if (own == null || own.stat() == null) return ItemStatistics.empty();
        double value = own.profileStatValue(System.currentTimeMillis());
        if (value == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(own.stat().statistic(), value).build();
    }

    public static ItemStatistics islandEffects(SkyBlockPlayer player) {
        if (!HypixelConst.isIslandServer() || player.getInstance() == null) return ItemStatistics.empty();
        BeaconData local = BeaconService.get(SkyBlockIsland.forInstance(player.getInstance()));
        if (local == null) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.empty();
        for (BeaconEffect effect : local.activeEffects(System.currentTimeMillis())) {
            if (effect.statistic() == null) continue;
            total = ItemStatistics.add(total, ItemStatistics.builder().withBase(effect.statistic(), effect.statValue()).build());
        }
        return total;
    }
}
