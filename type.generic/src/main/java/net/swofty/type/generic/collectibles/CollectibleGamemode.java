package net.swofty.type.generic.collectibles;

// The gamemodes that have their own collectible catalog.
public enum CollectibleGamemode {
    BEDWARS("BedWars"),
    SKYWARS("SkyWars"),
    MURDER_MYSTERY("Murder Mystery"),
    PROTOTYPE("Prototype");

    private final String displayName;

    CollectibleGamemode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
