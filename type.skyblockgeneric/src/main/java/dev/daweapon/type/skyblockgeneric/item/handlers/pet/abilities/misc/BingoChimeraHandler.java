package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering.BaseStatBoostHandler;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;

/**
 * Bingo's Chimera: the summoned pet's base stats are raised by {0}%.
 */
public final class BingoChimeraHandler implements PetAbilityHandler {
    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null) return ItemStatistics.empty();
        SkyBlockItem summoned = player.getPetData().getEnabledPet();
        if (summoned == null || !summoned.hasComponent(PetComponent.class)) return ItemStatistics.empty();
        ItemStatistics base = summoned.getComponent(PetComponent.class)
                .statisticsAt(summoned.getAttributeHandler().getRarity(), PetLevels.level(summoned));
        return BaseStatBoostHandler.scaled(base, context.value(0) / 100D);
    }
}
