package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class BarthererHandler implements PetAbilityHandler {
    public static final double FORTUNE_CAP = 175;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double fortune = Math.min(FORTUNE_CAP, context.value(0) * Gardens.profile(player).visitors().uniqueServed());
        if (fortune <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.SUGAR_CANE_FORTUNE, fortune).build();
    }
}
