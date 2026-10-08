package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.utility.AttackService;

/**
 * Ankylosaurus's Clubbed Tail: every 5th melee hit deals {0}% of that hit to every mob within 5
 * blocks, and those mobs deal 10% less damage for 10 seconds.
 */
public final class ClubbedTailHandler implements PetAbilityHandler {
    private static final int HITS = 5;
    private static final double RANGE = 5;
    private static final long WEAKEN_MS = 10_000;

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        runtime.setHits(runtime.getHits() + 1);
        if (runtime.getHits() < HITS) return;
        runtime.setHits(0);

        Instance instance = hit.player().getInstance();
        if (instance == null) return;
        float splash = (float) (hit.damage() * context.value(0) / 100D);
        for (Entity entity : instance.getNearbyEntities(hit.player().getPosition(), RANGE)) {
            if (!(entity instanceof SkyBlockMob mob) || mob.isDead()) continue;
            if (splash > 0) AttackService.applyHit(hit.player(), mob, splash, false);
            CombatPetHooks.weaken(mob, WEAKEN_MS);
        }
    }
}
