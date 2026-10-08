package net.swofty.type.skyblockgeneric.essence;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * Frozen Alex's Ice Essence Shop rules. Hypixel's leveling menu only gives each perk's SkyBlock XP
 * total (19 per five-tier perk, 62 for Season of Joy, Drake Piper is no task there), so the per-tier
 * split below is an estimate that reproduces those totals; Drake Piper's 2 XP makes up the 121 the
 * Skyblock Things Spreadsheet lists for the whole shop.
 */
public final class IceEssenceShop {
    public static final long EXPERIENCE_BOOK_COST = 15_000;

    private static final int[] FIVE_TIER_XP = {3, 3, 4, 4, 5};
    private static final Map<IceEssencePerk, int[]> TIER_XP = Map.of(
            IceEssencePerk.COLD_EFFICIENCY, FIVE_TIER_XP,
            IceEssencePerk.COOLED_FORGES, FIVE_TIER_XP,
            IceEssencePerk.FROZEN_SKIN, FIVE_TIER_XP,
            IceEssencePerk.SEASON_OF_JOY, new int[]{3, 3, 4, 4, 5, 7, 8, 9, 9, 10},
            IceEssencePerk.DRAKE_PIPER, new int[]{2});

    private IceEssenceShop() {
    }

    public enum Result {BOUGHT, UNAVAILABLE, TOO_POOR, MAXED}

    public static Result buy(SkyBlockPlayer player, IceEssencePerk perk) {
        int owned = EssencePerks.tier(player, perk);
        Result check = check(owned, perk, Essences.get(player, EssenceType.ICE));
        if (check != Result.BOUGHT) return check;
        if (!Essences.take(player, EssenceType.ICE, perk.costOfTier(owned + 1))) return Result.TOO_POOR;
        EssencePerks.setTier(player, perk, owned + 1);
        syncSkyBlockXP(player);
        return Result.BOUGHT;
    }

    static Result check(int owned, IceEssencePerk perk, long essence) {
        if (owned >= perk.getMaxTier()) return Result.MAXED;
        if (perk.getUnavailableReason() != null) return Result.UNAVAILABLE;
        if (essence < perk.costOfTier(owned + 1)) return Result.TOO_POOR;
        return Result.BOUGHT;
    }

    public static void maxAvailable(SkyBlockPlayer player) {
        for (IceEssencePerk perk : IceEssencePerk.values()) {
            if (perk.getUnavailableReason() == null) EssencePerks.setTier(player, perk, perk.getMaxTier());
        }
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ICE_ESSENCE_SHOP,
                skyBlockXP(perk -> EssencePerks.tier(player, perk)));
    }

    static int skyBlockXP(ToIntFunction<IceEssencePerk> tiers) {
        int total = 0;
        for (IceEssencePerk perk : IceEssencePerk.values()) {
            int[] perTier = TIER_XP.get(perk);
            for (int tier = 0; tier < Math.min(tiers.applyAsInt(perk), perTier.length); tier++) total += perTier[tier];
        }
        return total;
    }

    public static int maxSkyBlockXP() {
        return skyBlockXP(IceEssencePerk::getMaxTier);
    }

    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double coldResistance = EssencePerks.value(player, IceEssencePerk.FROZEN_SKIN);
        if (coldResistance == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.COLD_RESISTANCE, coldResistance).build();
    }

    /** Season of Joy: each Gift Attack gift has a chance at one extra. */
    public static int seasonOfJoyCount(SkyBlockPlayer player, ItemType gift, int baseCount) {
        return boostedCount(player.getUuid(), gift, baseCount, EssencePerks.value(player, IceEssencePerk.SEASON_OF_JOY));
    }

    static int boostedCount(@Nullable UUID player, ItemType gift, int baseCount, double percent) {
        if (percent <= 0 || baseCount <= 0) return baseCount;
        double scaled = baseCount * (1 + percent / 100D);
        int whole = (int) Math.floor(scaled);
        if (scaled == whole) return whole;
        return LootRoll.roll(player, gift, (scaled - whole) * 100) ? whole + 1 : whole;
    }
}
