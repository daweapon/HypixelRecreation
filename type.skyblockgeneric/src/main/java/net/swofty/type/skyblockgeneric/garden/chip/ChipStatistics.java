package net.swofty.type.skyblockgeneric.garden.chip;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.farmingcontest.ContestCrop;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContest;
import net.swofty.type.skyblockgeneric.farmingcontest.FarmingContests;
import net.swofty.type.skyblockgeneric.garden.Gardens;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Optional;

public final class ChipStatistics {
    private ChipStatistics() {
    }

    public static ItemStatistics sowledge(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double wisdom = ChipService.bonus(Gardens.profile(player).chips(), GardenChip.SOWLEDGE);
        return wisdom <= 0 ? ItemStatistics.empty() : ItemStatistics.builder().withBase(ItemStatistic.FARMING_WISDOM, wisdom).build();
    }

    public static ItemStatistics overdrive(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        Optional<FarmingContest> contest = FarmingContest.active();
        if (contest.isEmpty()) return ItemStatistics.empty();
        double fortune = ChipService.bonus(Gardens.profile(player).chips(), GardenChip.OVERDRIVE);
        if (fortune <= 0) return ItemStatistics.empty();
        List<ContestCrop> crops = FarmingContests.activeEntry(player)
                .map(entry -> List.of(entry.getCrop())).orElse(contest.get().crops());
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ContestCrop crop : crops) {
            if (crop.getFortuneStatistic() != null) builder.withBase(crop.getFortuneStatistic(), fortune);
        }
        return builder.build();
    }

    public static ItemStatistics all(SkyBlockPlayer player) {
        return ItemStatistics.add(sowledge(player), overdrive(player));
    }
}
