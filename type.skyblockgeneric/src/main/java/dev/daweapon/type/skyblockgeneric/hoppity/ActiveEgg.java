package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import dev.daweapon.type.skyblockgeneric.region.IslandType;

public record ActiveEgg(String spawnId, int year, int spawnNumber, HoppityEggType type, IslandType island, EggLocation location) {
}
