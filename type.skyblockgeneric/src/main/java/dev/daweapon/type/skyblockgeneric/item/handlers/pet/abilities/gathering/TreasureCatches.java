package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import dev.daweapon.type.skyblockgeneric.fishing.catches.CatchPayload;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;

/**
 * Whether a fishing catch was Treasure, for abilities that react to "upon catching Treasure".
 */
public final class TreasureCatches {
    private TreasureCatches() {
    }

    public static boolean isTreasure(PetEvent event) {
        return event instanceof PetEvent.FishCaught caught && isTreasure(caught.payload());
    }

    public static boolean isTreasure(CatchPayload payload) {
        return switch (payload) {
            case CatchPayload.Treasure ignored -> true;
            case CatchPayload.Essence ignored -> true;
            case CatchPayload.Item item -> item.fromTreasure();
            case CatchPayload.Multi multi -> multi.payloads().stream().anyMatch(TreasureCatches::isTreasure);
            default -> false;
        };
    }
}
