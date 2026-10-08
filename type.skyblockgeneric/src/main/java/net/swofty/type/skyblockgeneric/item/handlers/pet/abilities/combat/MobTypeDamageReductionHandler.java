package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;

/**
 * Take {0}% less damage from mobs of one {@link MobType} (Enderman's Enderian).
 */
public final class MobTypeDamageReductionHandler implements PetAbilityHandler {
    private final MobType type;

    public MobTypeDamageReductionHandler(MobType type) {
        this.type = type;
    }

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.DamagedByMob hit) || !hit.mob().getMobTypes().contains(type)) return;
        double percent = Math.max(0, Math.min(100, context.value(0)));
        hit.damage(hit.damage() * (1 - percent / 100D));
    }
}
