package dev.daweapon.anticheat.world;

// Minimal block snapshot (liquid state and collision box) cached by the anticheat's own world model.
public record Block(boolean isWater, boolean isLava, BoundingBox boundingBox) {

}
