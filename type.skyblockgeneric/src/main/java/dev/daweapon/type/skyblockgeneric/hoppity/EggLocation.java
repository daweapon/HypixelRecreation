package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.region.IslandType;

public record EggLocation(IslandType island, int index, String zone, double x, double y, double z, String message) {
    public String key() {
        return island.name() + ":" + index;
    }
}
