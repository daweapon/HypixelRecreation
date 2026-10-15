package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Golem's Ricochet: {0}% of mob attacks bounce off and hit the attacker instead.
 */
public final class RicochetHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.DamagedByMob hit) || hit.damage() <= 0) return;
        if (ThreadLocalRandom.current().nextDouble(100) >= context.value(0)) return;
        AttackService.applyHit(hit.player(), hit.mob(), (float) hit.damage(), false);
        hit.damage(0);
    }
}
