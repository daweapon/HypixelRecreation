package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.type.skyblockgeneric.healing.VitalityRegen;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Turtle's Turtle Shell: under 40% health mob damage is cut by {0}%, and after 10 hits taken the
 * player regenerates {1} Vitality (hit counter resets).
 */
public final class TurtleShellHandler implements PetAbilityHandler {
    private static final double LOW_HEALTH = 0.4;
    private static final int HITS = 10;

    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.DamagedByMob hit) || context.runtime() == null) return;
        SkyBlockPlayer player = hit.player();
        if (player.getHealth() < player.getMaxHealth() * LOW_HEALTH) {
            double percent = Math.max(0, Math.min(100, context.value(0)));
            hit.damage(hit.damage() * (1 - percent / 100D));
        }
        AbilityRuntime runtime = context.runtime();
        runtime.setHits(runtime.getHits() + 1);
        if (runtime.getHits() < HITS) return;
        runtime.setHits(0);
        runtime.setLastProc(System.currentTimeMillis());
        VitalityRegen.grant(player, context.value(1));
    }
}
