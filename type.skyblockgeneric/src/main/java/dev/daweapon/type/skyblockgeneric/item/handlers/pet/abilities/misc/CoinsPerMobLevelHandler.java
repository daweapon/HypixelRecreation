package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Bingo's Scavenger: {0} coins per level of the mob killed.
 */
public final class CoinsPerMobLevelHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.Kill kill)) return;
        double coins = PetPerkMath.coinsPerMobLevel(context.value(0), kill.mob().getLevel());
        if (coins > 0) kill.player().addCoins(coins);
    }
}
