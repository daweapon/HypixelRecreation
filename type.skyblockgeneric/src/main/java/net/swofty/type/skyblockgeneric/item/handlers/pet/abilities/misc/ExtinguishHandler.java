package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * The Wisps' Extinguish: dealing or taking melee damage marks the player as in combat. The pools themselves are
 * spawned by {@link ExtinguishPools} on the pet tick.
 */
public final class ExtinguishHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (event instanceof PetEvent.MeleeHit || event instanceof PetEvent.DamagedByMob) {
            CombatState.record(event.player().getUuid(), System.currentTimeMillis());
        }
    }
}
