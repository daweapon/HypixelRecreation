package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.minestom.server.coordinate.Vec;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.utility.AttackService;

/**
 * Golem's Toss: every 5th melee hit throws the mob into the air for 5x damage, with a 5 second
 * cooldown.
 */
public final class TossHandler implements PetAbilityHandler {
    private static final int HITS = 5;
    private static final double EXTRA_MULTIPLIER = 4;
    private static final long COOLDOWN_MS = 5_000;
    private static final Vec LAUNCH = new Vec(0, 15, 0);

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.MeleeHit hit) || context.runtime() == null) return;
        AbilityRuntime runtime = context.runtime();
        runtime.setHits(runtime.getHits() + 1);
        long now = System.currentTimeMillis();
        if (runtime.getHits() < HITS || now - runtime.getLastProc() < COOLDOWN_MS) return;
        runtime.setHits(0);
        runtime.setLastProc(now);
        hit.mob().setVelocity(LAUNCH);
        AttackService.applyHit(hit.player(), hit.mob(), (float) (hit.damage() * EXTRA_MULTIPLIER), false);
    }
}
