package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    public static final EssenceShop<IceEssencePerk> SHOP = new EssenceShop<>(EssenceType.ICE,
            List.of(IceEssencePerk.values()), TIER_XP, SkyBlockXPSource.ICE_ESSENCE_SHOP);

    private IceEssenceShop() {
    }

    public static EssenceShop.Result buy(SkyBlockPlayer player, IceEssencePerk perk) {
        return SHOP.buy(player, perk);
    }

    static EssenceShop.Result check(int owned, IceEssencePerk perk, long essence) {
        return SHOP.check(owned, perk, essence);
    }

    public static void maxAvailable(SkyBlockPlayer player) {
        SHOP.maxAvailable(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        SHOP.syncSkyBlockXP(player);
    }

    static int skyBlockXP(ToIntFunction<IceEssencePerk> tiers) {
        return SHOP.skyBlockXP(tiers);
    }

    public static int maxSkyBlockXP() {
        return SHOP.maxSkyBlockXP();
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
