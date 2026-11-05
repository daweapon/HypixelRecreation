package dev.daweapon.dungeons.catacombs.layout;

/** A real room's block file and where its corner sits: x/z room-local in the unrotated shape, y relative to {@link CellGeometry#FLOOR_Y}. */
public record RoomStructure(String file, int x, int y, int z) {
}
