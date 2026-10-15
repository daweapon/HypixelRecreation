package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.fishing.resolver.FishingLootResolver;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;

import java.util.List;

/**
 * Precursor Drone Contraband: catching a sea creature has a {0}% chance to also give treasure.
 */
public final class ContrabandHandler implements PetAbilityHandler {
    @Override
    public void onEvent(PetEvent event, PetAbilityContext context) {
        if (!(event instanceof PetEvent.FishCaught caught) || caught.context() == null) return;
        if (!(caught.payload() instanceof CatchPayload.SeaCreature)) return;
        if (!LootRoll.roll(null, null, context.value(0))) return;
        FishingLootResolver.treasure(caught.context())
                .ifPresent(treasure -> caught.payload(new CatchPayload.Multi(List.of(caught.payload(), treasure))));
    }
}
