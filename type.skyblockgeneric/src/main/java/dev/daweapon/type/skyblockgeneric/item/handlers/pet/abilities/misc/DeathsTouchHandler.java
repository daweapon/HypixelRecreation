package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.misc;

import dev.daweapon.type.skyblockgeneric.entity.mob.MobStatusEffects;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Wither Skeleton's Death's Touch: a melee hit withers the mob for {0}% of that hit over 3 seconds. It never stacks.
 */
public final class DeathsTouchHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || hit.mob().isDead()) return;
        MobStatusEffects.wither(hit.player(), hit.mob(), PetPerkMath.witherDamagePerSecond(hit.damage(), context.value(0)));
    }
}
