package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;

import java.util.HashSet;
import java.util.Set;

/**
 * Golden Dragon's Symbiosis: with a level 200 Golden Dragon owned, each monster kill pays 5 coins
 * for every other unique Combat pet the player owns at its max level.
 */
public final class SymbiosisHandler implements PetAbilityHandler {
    private static final int COINS_PER_PET = 5;

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill)) return;
        int pets = maxedCombatPets(kill.player());
        if (pets > 0) kill.player().addCoins(COINS_PER_PET * pets);
    }

    private static int maxedCombatPets(SkyBlockPlayer player) {
        boolean maxedDragon = false;
        Set<ItemType> others = new HashSet<>();
        for (SkyBlockItem pet : player.getPetData().getPetsMap().keySet()) {
            if (!pet.hasComponent(PetComponent.class) || !PetLevels.isMaxLevel(pet)) continue;
            ItemType type = pet.getAttributeHandler().getPotentialType();
            if (type == ItemType.GOLDEN_DRAGON_PET) maxedDragon = true;
            else if (pet.getComponent(PetComponent.class).getPetType() == PetType.COMBAT) others.add(type);
        }
        return maxedDragon ? others.size() : 0;
    }
}
