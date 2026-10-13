package net.swofty.type.skyblockgeneric.drill;

import lombok.Getter;

@Getter
public enum DrillSlot {
    TANK("Fuel Tank"),
    ENGINE("Drill Engine"),
    MODULE("Upgrade Module");

    private final String displayName;

    DrillSlot(String displayName) {
        this.displayName = displayName;
    }
}
