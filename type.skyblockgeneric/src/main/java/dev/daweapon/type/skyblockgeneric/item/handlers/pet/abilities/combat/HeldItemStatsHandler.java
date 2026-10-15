package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

import java.util.List;

/**
 * Value {i} of the i-th listed stat while the player holds one weapon (Pigman's Pork Master on the
 * Pigman Sword, Jerry's bonus on the Aspect of the Jerry).
 */
public final class HeldItemStatsHandler implements PetAbilityHandler {
    private final ItemType weapon;
    private final List<ItemStatistic> statistics;

    public HeldItemStatsHandler(ItemType weapon, ItemStatistic... statistics) {
        this.weapon = weapon;
        this.statistics = List.of(statistics);
    }

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        if (context.player() == null) return ItemStatistics.empty();
        if (new SkyBlockItem(context.player().getItemInMainHand()).getAttributeHandler().getPotentialType() != weapon) {
            return ItemStatistics.empty();
        }
        ItemStatistics.Builder builder = ItemStatistics.builder();
        for (int i = 0; i < statistics.size(); i++) builder.withBase(statistics.get(i), context.value(i));
        return builder.build();
    }
}
