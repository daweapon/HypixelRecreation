package net.swofty.type.skyblockgeneric.farmingcontest;

import java.util.List;
import net.swofty.type.skyblockgeneric.accessory.AccessoryOwnership;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Farming Fortune from Anita's permanent upgrades, Personal Bests and Anita's accessories. */
public final class FarmingContestStatistics {
    public static final int FORTUNE_PER_EXTRA_TIER = 4;
    private static final Map<ItemType, Double> ANITA_ACCESSORY_FORTUNE = new LinkedHashMap<>();

    static {
        ANITA_ACCESSORY_FORTUNE.put(ItemType.ANITA_ARTIFACT, 25D);
        ANITA_ACCESSORY_FORTUNE.put(ItemType.ANITA_RING, 15D);
        ANITA_ACCESSORY_FORTUNE.put(ItemType.ANITA_TALISMAN, 5D);
    }

    private FarmingContestStatistics() {}

    public static ItemStatistics all(SkyBlockPlayer player) {
        FarmingContestProfile profile = FarmingContests.profile(player);
        return ItemStatistics.add(ItemStatistics.add(extraFortune(profile), personalBests(profile)), anitaAccessory(player));
    }

    public static ItemStatistics extraFortune(FarmingContestProfile profile) {
        if (profile.getExtraFortuneTier() <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withBase(ItemStatistic.FARMING_FORTUNE, (double) profile.getExtraFortuneTier() * FORTUNE_PER_EXTRA_TIER)
                .build();
    }

    public static ItemStatistics personalBests(FarmingContestProfile profile) {
        if (!profile.isPersonalBestsUnlocked()) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (ContestCrop crop : ContestCrop.values()) {
            double fortune = crop.personalBestFortune(profile.personalBest(crop));
            if (crop.getFortuneStatistic() != null && fortune > 0) builder.withBase(crop.getFortuneStatistic(), fortune);
        }
        return builder.build();
    }

    public static ItemStatistics anitaAccessory(SkyBlockPlayer player) {
        Optional<FarmingContest> contest = FarmingContest.active();
        if (contest.isEmpty()) return ItemStatistics.empty();
        ItemStatistic statistic = contest.get().anitaCrop().getFortuneStatistic();
        if (statistic == null) return ItemStatistics.empty();
        double fortune = anitaAccessoryFortune(player);
        if (fortune <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(statistic, fortune).build();
    }

    public static double anitaAccessoryFortune(SkyBlockPlayer player) {
        List<ItemType> owned = AccessoryOwnership.ownedTypes(player);
        return ANITA_ACCESSORY_FORTUNE.entrySet().stream()
                .filter(entry -> owned.contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .findFirst().orElse(0D);
    }
}
