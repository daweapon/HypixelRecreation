package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Hound's Scavenger: +{0} coins per monster kill.
 */
public final class CoinsPerKillHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill)) return;
        int coins = (int) Math.floor(context.value(0));
        if (coins > 0) kill.player().addCoins(coins);
    }
}
