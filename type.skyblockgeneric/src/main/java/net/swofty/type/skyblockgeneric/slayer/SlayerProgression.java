package net.swofty.type.skyblockgeneric.slayer;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumMap;
import java.util.Map;

/**
 * What slayer levels and first kills give permanently: the per-level stat rewards, Combat Wisdom for
 * each tier's first kill (+1 for Tiers I-III, +2 for Tier IV) and the SkyBlock XP for each level.
 */
public final class SlayerProgression {
    private SlayerProgression() {
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        DatapointSlayer.SlayerData data = player.getSlayerData();
        Map<ItemStatistic, Double> totals = new EnumMap<>(ItemStatistic.class);
        for (SlayerDefinition definition : SlayerRegistry.all()) {
            DatapointSlayer.SlayerProgress progress = data.progress(definition.type());
            int level = definition.levelForXp(progress.getXp());
            for (SlayerLevelReward reward : definition.levels()) {
                if (reward.level() > level) continue;
                for (Map.Entry<ItemStatistic, Double> stat : reward.stats().entrySet()) {
                    totals.merge(stat.getKey(), stat.getValue(), Double::sum);
                }
            }
            double wisdom = firstKillWisdom(progress);
            if (wisdom > 0) totals.merge(ItemStatistic.COMBAT_WISDOM, wisdom, Double::sum);
        }
        ItemStatistics.Builder builder = ItemStatistics.builder();
        totals.forEach(builder::withBase);
        return builder.build();
    }

    private static double firstKillWisdom(DatapointSlayer.SlayerProgress progress) {
        double wisdom = 0;
        for (SlayerTier tier : new SlayerTier[]{SlayerTier.I, SlayerTier.II, SlayerTier.III}) {
            if (progress.completions(tier) > 0) wisdom += 1;
        }
        if (progress.completions(SlayerTier.IV) > 0) wisdom += 2;
        return wisdom;
    }

    public static int skyBlockXp(SkyBlockPlayer player) {
        DatapointSlayer.SlayerData data = player.getSlayerData();
        int total = 0;
        for (SlayerDefinition definition : SlayerRegistry.all()) {
            int level = definition.levelForXp(data.progress(definition.type()).getXp());
            for (SlayerLevelReward reward : definition.levels()) {
                if (reward.level() <= level) total += reward.skyBlockXp();
            }
        }
        return total;
    }

    public static void sync(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.SLAYER_LEVEL_UP, skyBlockXp(player));
    }
}
