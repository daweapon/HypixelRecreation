package net.swofty.type.skyblockgeneric.item.handlers.pet.registry;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Java behaviour for a pet ability whose effect YAML cannot express. Registered under an id in
 * {@link PetAbilityRegistry} and referenced from YAML with {@code kind: HANDLER, handler: <id>}.
 */
public interface PetAbilityHandler {
    default ItemStatistics statistics(PetAbilityContext context) {
        return ItemStatistics.empty();
    }

    default void onEvent(PetEvent event, PetAbilityContext context) {
    }

    default @Nullable List<String> description(PetAbilityContext context) {
        return null;
    }
}
