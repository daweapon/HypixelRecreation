package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Golden Dragon's Gold's Power: the held Midas' Sword's Greed bonus is {0}% stronger.
 */
public final class GoldsPowerHandler implements PetAbilityHandler {
    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        SkyBlockItem held = new SkyBlockItem(context.player().getItemInMainHand());
        if (held.getAttributeHandler().getPotentialType() != ItemType.MIDAS_SWORD) return ItemStatistics.empty();
        ItemStatistics greed = held.getAttributeHandler().getExtraDynamicStatistics();
        if (greed == null) return ItemStatistics.empty();
        double fraction = context.value(0) / 100D;
        return ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, greed.getOverall(ItemStatistic.DAMAGE) * fraction)
                .withBase(ItemStatistic.STRENGTH, greed.getOverall(ItemStatistic.STRENGTH) * fraction)
                .build();
    }
}
