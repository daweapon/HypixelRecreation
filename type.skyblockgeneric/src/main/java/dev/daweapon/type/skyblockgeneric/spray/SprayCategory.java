package dev.daweapon.type.skyblockgeneric.spray;

import lombok.Getter;

@Getter
public enum SprayCategory {
    UTILITY("Utility"),
    MISCELLANEOUS("Miscellaneous"),
    WINTER("Winter"),
    EASTER("Easter"),
    SUMMER("Summer"),
    HALLOWEEN("Halloween");

    private final String displayName;

    SprayCategory(String displayName) {
        this.displayName = displayName;
    }
}
