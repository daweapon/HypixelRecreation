package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;

/**
 * T-Rex's Tyrant: the Combat stats the held pet item adds to this pet are {0}% higher.
 */
public final class TyrantHandler implements PetAbilityHandler {
    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockItem pet = context.pet();
        if (PetItems.held(pet) == null) return ItemStatistics.empty();
        ItemStatistics own = pet.getComponent(PetComponent.class).statisticsAt(context.rarity(), context.level());
        ItemStatistics fromItem = PetItems.statistics(pet).sub(own);
        return PetItems.combatStatistics(fromItem, context.value(0) / 100D);
    }
}
