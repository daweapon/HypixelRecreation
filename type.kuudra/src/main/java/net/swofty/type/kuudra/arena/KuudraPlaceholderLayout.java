package net.swofty.type.kuudra.arena;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraArena;

import java.util.HashSet;
import java.util.Set;

/**
 * The stand-in Kuudra's Hollow built from {@code arena.yml} while the captured map is missing: a lava sea,
 * the platform with a marker under every drop-off pile, supply spot and the Ballista site, and the bridge.
 */
public final class KuudraPlaceholderLayout {
    public enum Tile {AIR, BEDROCK, LAVA, FOUNDATION, FLOOR, BRIDGE, PILE, SUPPLY_SPOT, BALLISTA}

    private final KuudraArena arena;
    private final Set<Long> piles = new HashSet<>();
    private final Set<Long> platformSpots = new HashSet<>();
    private final Set<Long> lavaSpots = new HashSet<>();
    private final long ballista;

    public KuudraPlaceholderLayout(KuudraArena arena) {
        this.arena = arena;
        arena.piles().values().forEach(pile -> piles.add(column(pile)));
        arena.supplySpots().values().forEach(spot -> {
            Pos position = spot.position();
            (arena.platform().containsColumn(position.blockX(), position.blockZ()) ? platformSpots : lavaSpots)
                    .add(column(position));
        });
        ballista = column(arena.ballista());
    }

    public Tile tileAt(int x, int y, int z) {
        int markerY = arena.floorY() - 1;
        if (y == markerY && arena.platform().contains(x, y, z)) {
            long column = column(x, z);
            if (column == ballista) return Tile.BALLISTA;
            if (piles.contains(column)) return Tile.PILE;
            if (platformSpots.contains(column)) return Tile.SUPPLY_SPOT;
        }
        if (arena.platform().contains(x, y, z)) return y == arena.platform().maxY() ? Tile.FLOOR : Tile.FOUNDATION;
        if (y == markerY && arena.bridge().contains(x, y, z)) return Tile.BRIDGE;
        if (arena.sea().contains(x, y, z)) {
            return y == arena.sea().maxY() && lavaSpots.contains(column(x, z)) ? Tile.SUPPLY_SPOT : Tile.LAVA;
        }
        if (y == arena.sea().minY() - 1 && arena.sea().containsColumn(x, z)) return Tile.BEDROCK;
        return Tile.AIR;
    }

    public int minY() {
        return Math.min(arena.sea().minY() - 1, Math.min(arena.platform().minY(), arena.floorY() - 1));
    }

    public int maxY() {
        return Math.max(arena.sea().maxY(), Math.max(arena.platform().maxY(), arena.floorY() - 1));
    }

    private static long column(Pos position) {
        return column(position.blockX(), position.blockZ());
    }

    private static long column(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
