package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.layout.DoorType;

import java.util.Optional;

public final class DoorRules {
    private DoorRules() {
    }

    public static Optional<KeyType> requiredKey(DoorType door) {
        return switch (door) {
            case PLAIN -> Optional.empty();
            case WITHER -> Optional.of(KeyType.WITHER);
            case BLOOD -> Optional.of(KeyType.BLOOD);
        };
    }

    public static boolean canOpen(DoorType door, KeyRing keys) {
        return requiredKey(door).map(keys::has).orElse(true);
    }

    /** Opens the door if the party holds its key, spending the key; plain doors are always open. */
    public static boolean tryOpen(DoorType door, KeyRing keys) {
        return requiredKey(door).map(keys::consume).orElse(true);
    }
}
