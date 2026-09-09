package net.swofty.anticheat.world;

// Axis-aligned bounding box used for the anticheat's own collision shape representation.
public record BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
}
