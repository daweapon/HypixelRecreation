package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.Getter;

@Getter
public enum PetVisibility {
    ALL("Show all pets"),
    NONE("Hide all pets"),
    OWN("Only your pet");

    private final String label;

    PetVisibility(String label) {
        this.label = label;
    }

    public PetVisibility next(boolean backwards) {
        PetVisibility[] values = values();
        return values[Math.floorMod(ordinal() + (backwards ? -1 : 1), values.length)];
    }

    public static PetVisibility fromName(String name) {
        for (PetVisibility visibility : values()) {
            if (visibility.name().equals(name)) return visibility;
        }
        return ALL;
    }
}
