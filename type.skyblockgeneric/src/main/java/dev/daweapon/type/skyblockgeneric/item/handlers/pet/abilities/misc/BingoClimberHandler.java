package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Bingo's Climber: HOTM and HOTF experience is raised by {0}%.
 */
public final class BingoClimberHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.XpGain gain)) return;
        if (gain.type() != PetEvent.XpType.HOTM && gain.type() != PetEvent.XpType.HOTF) return;
        gain.amount(gain.amount() * (1 + context.value(0) / 100D));
    }
}
