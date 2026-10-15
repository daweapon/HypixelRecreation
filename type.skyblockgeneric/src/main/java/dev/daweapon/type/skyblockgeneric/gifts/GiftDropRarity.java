package dev.daweapon.type.skyblockgeneric.gifts;

import lombok.Getter;

@Getter
public enum GiftDropRarity {
    COMMON("<f>"),
    RARE("<9>"),
    SWEET("<6>"),
    SANTA("<c>");

    private final String color;

    GiftDropRarity(String color) {
        this.color = color;
    }

    public boolean isRareItemTier() {
        return this == SWEET || this == SANTA;
    }

    public String label() {
        return color + "<l>" + name() + "!</l>";
    }
}
