package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

/**
 * Tyrannosaurus Ferocious Roar: a melee hit has a {0}% chance to stun the target for 10 seconds, once every 10 seconds.
 */
public final class FerociousRoarHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        long now = System.currentTimeMillis();
        if (!PetPerkMath.offCooldown(runtime.getLastProc(), now, PetPerkMath.STUN_COOLDOWN_MS)) return;
        if (!LootRoll.roll(null, null, context.value(0))) return;
        runtime.setLastProc(now);
        MobStatusEffects.stun(hit.mob(), PetPerkMath.STUN_MS);
    }
}
