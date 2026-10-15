package dev.daweapon.type.skyblockgeneric.garden.pest;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.garden.tool.FarmingTools;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

public final class PestBooks {
    public static final String BOOKWORM = "BOOKWORM_BOOK";
    public static final String FARMING_FOR_DUMMIES = "FARMING_FOR_DUMMIES";

    private PestBooks() {
    }

    public static void registerAnvilCombines() {
        AnvilCombineRegistry.register(BOOKWORM, new AnvilCombineHandler(
                (upgrade, sacrifice) -> upgrade.getAttributeHandler().setBookwormBooks(upgrade.getAttributeHandler().getBookwormBooks() + 1),
                (player, upgrade, sacrifice) -> PestVacuum.tier(upgrade) != null
                        && upgrade.getAttributeHandler().getBookwormBooks() < PestRules.MAX_VACUUM_BOOKS,
                (upgrade, sacrifice, player) -> 0));
        AnvilCombineRegistry.register(FARMING_FOR_DUMMIES, new AnvilCombineHandler(
                (upgrade, sacrifice) -> upgrade.getAttributeHandler().setFarmingForDummies(upgrade.getAttributeHandler().getFarmingForDummies() + 1),
                (player, upgrade, sacrifice) -> acceptsDummies(upgrade)
                        && upgrade.getAttributeHandler().getFarmingForDummies() < PestRules.MAX_VACUUM_BOOKS,
                (upgrade, sacrifice, player) -> 0));
    }

    private static boolean acceptsDummies(SkyBlockItem item) {
        return PestVacuum.tier(item) != null || FarmingTools.of(item.getAttributeHandler().getTypeAsString()) != null;
    }

    public static ItemStatistics statistics(SkyBlockItem item) {
        int bookworm = item.getAttributeHandler().getBookwormBooks();
        int dummies = item.getAttributeHandler().getFarmingForDummies();
        if (bookworm <= 0 && dummies <= 0) return ItemStatistics.empty();
        ItemStatistics.Builder builder = ItemStatistics.builder();
        if (bookworm > 0) builder.withBase(ItemStatistic.DAMAGE, (double) bookworm * PestRules.BOOKWORM_DAMAGE);
        if (dummies > 0) builder.withBase(ItemStatistic.FARMING_FORTUNE, (double) dummies * PestRules.FARMING_FOR_DUMMIES_FORTUNE);
        return builder.build();
    }
}
