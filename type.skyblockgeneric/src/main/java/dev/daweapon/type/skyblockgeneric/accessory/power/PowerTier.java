package dev.daweapon.type.skyblockgeneric.accessory.power;

import lombok.Getter;

@Getter
public enum PowerTier {
    STARTER("Starter Power"),
    INTERMEDIATE("Intermediate Power"),
    STARTER_STONE("Starter Stone Power"),
    INTERMEDIATE_STONE("Intermediate Stone Power"),
    ADVANCED_STONE("Advanced Stone Power"),
    MASTER_STONE("Master Stone Power"),
    GRANDIOSE_STONE("Grandiose Stone Power"),
    MARVELOUS_STONE("Marvelous Stone Power");

    private final String displayName;

    PowerTier(String displayName) {
        this.displayName = displayName;
    }
}
