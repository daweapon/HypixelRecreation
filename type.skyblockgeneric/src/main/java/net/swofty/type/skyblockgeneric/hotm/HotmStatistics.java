package net.swofty.type.skyblockgeneric.hotm;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

public final class HotmStatistics {
    private HotmStatistics() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        boolean onMiningIsland = HotmService.onMiningIsland();
        ItemStatistics total = ItemStatistics.empty();
        for (Map.Entry<String, Integer> entry : data.getPerkLevels().entrySet()) {
            HotmPerk perk = HotmRegistry.get().perk(entry.getKey());
            if (perk == null || !perk.available() || data.getDisabledPerks().contains(entry.getKey())) continue;
            for (HotmPerk.Effect effect : perk.effects()) {
                if (effect.statistic() == null || !effect.scope().isGlobal()) continue;
                if (effect.scope() == HotmScope.MINING_ISLAND && !onMiningIsland) continue;
                total = total.addBase(effect.statistic(), effect.value(entry.getValue()));
            }
        }
        if (onMiningIsland && HotmService.hasPerk(player, "lonesome_miner")) {
            double percent = HotmService.perkValue(player, "lonesome_miner");
            ItemStatistics.Builder builder = ItemStatistics.builder();
            for (ItemStatistic statistic : List.of(ItemStatistic.STRENGTH, ItemStatistic.CRITICAL_CHANCE,
                    ItemStatistic.CRITICAL_DAMAGE, ItemStatistic.DEFENSE, ItemStatistic.HEALTH)) {
                builder.withMultiplicativePercentage(statistic, percent);
            }
            total = ItemStatistics.add(total, builder.build());
        }
        if (onMiningIsland && FrontLoaded.active(player)) {
            total = total.addBase(ItemStatistic.GEMSTONE_FORTUNE, FrontLoaded.GEMSTONE_FORTUNE)
                    .addBase(ItemStatistic.MINING_SPEED, FrontLoaded.MINING_SPEED);
        }
        HotmSkyMallBuff buff = HotmService.skyMallBuff(player);
        if (buff == HotmSkyMallBuff.MINING_SPEED) total = total.addBase(ItemStatistic.MINING_SPEED, 100D);
        if (buff == HotmSkyMallBuff.MINING_FORTUNE) total = total.addBase(ItemStatistic.MINING_FORTUNE, 50D);
        return ItemStatistics.add(total, HotmAbilities.statistics(player));
    }

    public static double blockSpecific(SkyBlockPlayer player, MineableBlock block, ItemStatistic statistic) {
        double total = 0;
        for (Map.Entry<String, Integer> entry : HotmService.data(player).getPerkLevels().entrySet()) {
            HotmPerk perk = HotmRegistry.get().perk(entry.getKey());
            if (perk == null || !perk.available() || HotmService.data(player).getDisabledPerks().contains(entry.getKey())) continue;
            for (HotmPerk.Effect effect : perk.effects()) {
                if (effect.statistic() != statistic || effect.scope().isGlobal() || !effect.scope().matches(block)) continue;
                total += effect.value(entry.getValue());
            }
        }
        return total;
    }

    public static double miningSpeedFor(SkyBlockPlayer player, MineableBlock block) {
        return blockSpecific(player, block, ItemStatistic.MINING_SPEED);
    }
}
