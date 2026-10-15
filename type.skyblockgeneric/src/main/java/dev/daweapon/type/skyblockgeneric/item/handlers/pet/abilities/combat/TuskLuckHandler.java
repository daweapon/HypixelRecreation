package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetConditions;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Mammoth's Tusk Luck: +{0} Magic Find for every 100 Mining Fortune, doubled in the Glacite Tunnels.
 */
public final class TuskLuckHandler implements PetAbilityHandler {
    private static final double FORTUNE_STEP = 100;
    private static final Predicate<SkyBlockPlayer> IN_GLACITE =
            PetConditions.inRegions(EnumSet.of(RegionType.GLACITE_TUNNELS, RegionType.GREAT_GLACITE_LAKE));

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null) return ItemStatistics.empty();
        double fortune = player.getStatistics().allStatistics().getOverall(ItemStatistic.MINING_FORTUNE);
        double magicFind = context.value(0) * Math.floor(fortune / FORTUNE_STEP) * (IN_GLACITE.test(player) ? 2 : 1);
        if (magicFind <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.MAGIC_FIND, magicFind).build();
    }
}
