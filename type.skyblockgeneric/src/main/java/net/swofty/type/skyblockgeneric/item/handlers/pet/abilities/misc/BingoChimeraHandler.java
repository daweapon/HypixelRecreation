package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering.BaseStatBoostHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;

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
