package dev.daweapon.type.skyblockgeneric.garden.skin;

import lombok.Getter;

@Getter
public enum GardenSkinType {
    BARN("Barn", "_BARN_SKIN", "DEFAULT_BARN_SKIN", "barn"),
    GREENHOUSE("Greenhouse", "_GREENHOUSE_SKIN", "DEFAULT_GREENHOUSE_SKIN", "Greenhouse");

    private final String displayName;
    private final String itemSuffix;
    private final String defaultId;
    private final String selectNoun;

    GardenSkinType(String displayName, String itemSuffix, String defaultId, String selectNoun) {
        this.displayName = displayName;
        this.itemSuffix = itemSuffix;
        this.defaultId = defaultId;
        this.selectNoun = selectNoun;
    }
}
