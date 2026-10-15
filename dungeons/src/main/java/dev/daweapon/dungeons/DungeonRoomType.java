package dev.daweapon.dungeons;

import lombok.Getter;

@Getter
// Types of rooms that can appear in a generated dungeon layout, and how they should be placed.
public enum DungeonRoomType {
    ENTRANCE(),
    BASE(),
    FAIRY(true, false),
    PUZZLE(true),
    MINI_BOSS(true),
    TRAP(),
    BLOOD(),
    EXIT(),
    ;

    private boolean requiresData = false;
    private boolean automaticallyPlace = true;

    DungeonRoomType() {}

    DungeonRoomType(boolean requiresData) {
        this.requiresData = requiresData;
    }

    DungeonRoomType(boolean requiresData, boolean automaticallyPlace) {
        this.requiresData = requiresData;
        this.automaticallyPlace = automaticallyPlace;
    }
}
