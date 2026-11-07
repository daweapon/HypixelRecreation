package dev.daweapon.dungeons.catacombs.puzzle.icefill;

public record IceTile(int x, int z) {
    public boolean touches(IceTile other) {
        return Math.abs(x - other.x) + Math.abs(z - other.z) == 1;
    }
}
