package net.swofty.type.skyblockgeneric.essence;

import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

/**
 * Researcher Beryl's Fossil Essence Shop. Hypixel's leveling menu only gives the shop's SkyBlock XP
 * total (226), so the per-tier split below is an estimate that reproduces it.
 */
public final class FossilEssenceShop {
    private static final Map<FossilEssencePerk, int[]> TIER_XP = Map.of(
            FossilEssencePerk.SLEIGHT_OF_HAND, new int[]{6},
            FossilEssencePerk.CHILLED_TO_THE_BONE, new int[]{3, 3, 4, 4, 5, 5, 6, 6, 7, 7},
            FossilEssencePerk.DWARVEN_EXPERTISE, new int[]{3, 3, 4, 4, 5, 5, 6, 6, 7, 7},
            FossilEssencePerk.CUT_LOOSE, new int[]{6, 7, 8, 9, 10},
            FossilEssencePerk.PREHISTORIAN, new int[]{2, 2, 3, 3, 3, 4, 4, 4, 5, 5},
            FossilEssencePerk.RESOURCEFUL, new int[]{6, 8, 9, 10, 12});

    public static final EssenceShop<FossilEssencePerk> SHOP = new EssenceShop<>(EssenceType.FOSSIL,
            List.of(FossilEssencePerk.values()), TIER_XP, SkyBlockXPSource.FOSSIL_ESSENCE_SHOP);

    private FossilEssenceShop() {}

    public static EssenceShop.Result buy(SkyBlockPlayer player, FossilEssencePerk perk) {
        return SHOP.buy(player, perk);
    }

    public static void maxAvailable(SkyBlockPlayer player) {
        SHOP.maxAvailable(player);
    }

    public static int maxSkyBlockXP() {
        return SHOP.maxSkyBlockXP();
    }

    public static int startingCharges(SkyBlockPlayer player) {
        return (int) EssencePerks.value(player, FossilEssencePerk.SLEIGHT_OF_HAND);
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null || !HotmService.onMiningIsland()) return ItemStatistics.empty();
        double fortune = EssencePerks.value(player, FossilEssencePerk.DWARVEN_EXPERTISE);
        if (fortune == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.DWARVEN_METAL_FORTUNE, fortune).build();
    }
}
