package dev.daweapon.dungeons.catacombs.puzzle;

/** A block in a room template's own frame: x/z from the room's north-west corner, y above the dungeon floor. */
public record RoomBlock(int x, int y, int z) {
}
