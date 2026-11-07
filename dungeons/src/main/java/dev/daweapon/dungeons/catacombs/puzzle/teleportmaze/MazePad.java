package dev.daweapon.dungeons.catacombs.puzzle.teleportmaze;

/**
 * One end portal frame pad of the Teleport Maze, in the template's frame. {@code room} is 0-6 for the seven side
 * rooms; the entry pad and the centre pad have their own kinds. A player sent to this pad lands on {@code (landX,
 * landZ)}, the block diagonally inside the room from it.
 */
public record MazePad(int x, int z, Kind kind, int room, double landX, double landZ) {
    public enum Kind {
        ENTRY,
        ROOM,
        CENTRE
    }

    public boolean at(int blockX, int blockZ) {
        return x == blockX && z == blockZ;
    }
}
