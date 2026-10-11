package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.entity.mob.MobStatusEffects;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Tiger Hemorrhage: a melee hit cuts the mob's healing by {0}% for 10 seconds.
 */
public final class HemorrhageHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || context.value(0) <= 0) return;
        MobStatusEffects.reduceHealing(hit.mob(), context.value(0), PetPerkMath.HEMORRHAGE_MS);
    }
}
