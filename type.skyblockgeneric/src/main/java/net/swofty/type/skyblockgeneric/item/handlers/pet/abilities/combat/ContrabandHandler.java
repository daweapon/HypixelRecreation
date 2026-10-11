package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.fishing.resolver.FishingLootResolver;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import net.swofty.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;

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
