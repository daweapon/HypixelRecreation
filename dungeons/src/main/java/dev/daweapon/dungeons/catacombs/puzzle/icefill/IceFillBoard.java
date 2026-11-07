package dev.daweapon.dungeons.catacombs.puzzle.icefill;

import java.util.Set;

/** One of the ice layers: the tiles to walk at height {@code y}, entered on {@code start}, left from {@code exit}. */
public record IceFillBoard(int y, Set<IceTile> tiles, IceTile start, IceTile exit) {
    public IceFillBoard {
        tiles = Set.copyOf(tiles);
        if (!tiles.contains(start) || !tiles.contains(exit)) {
            throw new IllegalArgumentException("An Ice Fill board must start and end on one of its tiles");
        }
    }

    public boolean contains(IceTile tile) {
        return tiles.contains(tile);
    }
}
