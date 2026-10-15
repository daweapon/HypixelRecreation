package dev.daweapon.type.catacombs.engine;

import dev.daweapon.dungeons.catacombs.layout.DoorType;

public enum KeyType {
    WITHER("Wither Key", "<8>", DoorType.WITHER),
    BLOOD("Blood Key", "<c>", DoorType.BLOOD);

    private final String displayName;
    private final String colour;
    private final DoorType door;

    KeyType(String displayName, String colour, DoorType door) {
        this.displayName = displayName;
        this.colour = colour;
        this.door = door;
    }

    public String displayName() {
        return displayName;
    }

    public String coloured() {
        return colour + displayName;
    }

    public DoorType door() {
        return door;
    }
}
