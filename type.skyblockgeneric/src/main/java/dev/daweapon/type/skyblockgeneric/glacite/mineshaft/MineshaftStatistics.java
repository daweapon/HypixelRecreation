package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPerk;
import dev.daweapon.type.skyblockgeneric.hotm.HotmRegistry;
import dev.daweapon.type.skyblockgeneric.hotm.HotmScope;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class MineshaftStatistics {
    private MineshaftStatistics() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        MineshaftInstance shaft = MineshaftService.current(player);
        if (shaft == null) return ItemStatistics.empty();
        ItemStatistics total = ItemStatistics.empty();
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        for (Map.Entry<String, Integer> entry : data.getPerkLevels().entrySet()) {
            HotmPerk perk = HotmRegistry.get().perk(entry.getKey());
            if (perk == null || !perk.available() || data.getDisabledPerks().contains(entry.getKey())) continue;
            for (HotmPerk.Effect effect : perk.effects()) {
                if (effect.scope() != HotmScope.MINESHAFT || effect.statistic() == null) continue;
                if (effect.statistic() == ItemStatistic.MINING_SPEED) continue;
                total = total.addBase(effect.statistic(), effect.value(entry.getValue()));
            }
        }
        MayhemBuff buff = shaft.buff(player.getUuid());
        if (buff != null && buff.statistic() != null) total = total.addBase(buff.statistic(), buff.amount());
        return total;
    }
}
