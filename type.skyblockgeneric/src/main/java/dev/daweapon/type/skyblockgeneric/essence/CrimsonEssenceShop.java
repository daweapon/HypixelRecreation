package dev.daweapon.type.skyblockgeneric.essence;

import net.minestom.server.item.Material;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.crimson.CrimsonLocation;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

/**
 * Vulcan's Crimson Essence Shop. Only the 194 XP total is known, so the per-tier SkyBlock XP split
 * below is an estimate that adds up to it.
 */
public final class CrimsonEssenceShop {
    private static final Map<CrimsonEssencePerk, int[]> TIER_XP = Map.of(
            CrimsonEssencePerk.STRONGARM, new int[]{5, 7},
            CrimsonEssencePerk.FRESH_TOOLS, new int[]{3, 3, 4, 4, 5},
            CrimsonEssencePerk.FUNGUS_FORTUNA, new int[]{2, 2, 2, 3, 3, 3, 3, 4, 4, 4},
            CrimsonEssencePerk.HARENA_FORTUNA, new int[]{2, 2, 2, 3, 3, 3, 3, 4, 4, 4},
            CrimsonEssencePerk.HEADSTART, new int[]{3, 3, 4, 4, 5},
            CrimsonEssencePerk.KUUDRA_MASTER, new int[]{20},
            CrimsonEssencePerk.CRIMSON_TRAINING, new int[]{8, 12, 14},
            CrimsonEssencePerk.WITHER_PIPER, new int[]{4, 5, 6, 7, 8});

    public static final EssenceShop<CrimsonEssencePerk> SHOP = new EssenceShop<>(EssenceType.CRIMSON,
            List.of(CrimsonEssencePerk.values()), TIER_XP, SkyBlockXPSource.CRIMSON_ESSENCE_SHOP);

    private CrimsonEssenceShop() {
    }

    public static void maxAvailable(SkyBlockPlayer player) {
        SHOP.maxAvailable(player);
    }

    /** Crimson Training: Combat Wisdom while on the Crimson Isle (and in Kuudra, once it exists). */
    public static ItemStatistics statistics(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null || !CrimsonLocation.isCrimsonIsleServer()) return ItemStatistics.empty();
        double wisdom = EssencePerks.value(player, CrimsonEssencePerk.CRIMSON_TRAINING);
        if (wisdom == 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.COMBAT_WISDOM, wisdom).build();
    }

    /** Fungus and Harena Fortuna: extra Mining Fortune for the Mycelium or Red Sand being broken. */
    public static double miningFortune(SkyBlockPlayer player, Material block) {
        if (block == Material.MYCELIUM) return EssencePerks.value(player, CrimsonEssencePerk.FUNGUS_FORTUNA);
        if (block == Material.RED_SAND) return EssencePerks.value(player, CrimsonEssencePerk.HARENA_FORTUNA);
        return 0;
    }

    /** Wither Piper: percent increase to the chance of a Vanquisher spawning on the Crimson Isle. */
    public static double witherPiperBonus(SkyBlockPlayer player) {
        return EssencePerks.value(player, CrimsonEssencePerk.WITHER_PIPER);
    }
}
