package dev.daweapon.type.skyblockgeneric.item.components;

import dev.daweapon.type.skyblockgeneric.item.SkyBlockItemComponent;

/** Marks a dungeon item: its stats scale with the Catacombs level and stars inside the Catacombs. */
public class DungeonItemComponent extends SkyBlockItemComponent {
    public static final String RARITY_PREFIX = "DUNGEON";

    public static String rarityDisplay(String extraDisplay) {
        return extraDisplay == null || extraDisplay.isBlank() ? RARITY_PREFIX + " ITEM" : RARITY_PREFIX + " " + extraDisplay;
    }
}
