package dev.daweapon.type.skyblockgeneric.carnival.games;

import lombok.Getter;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGame;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The rules of Fruit Digging on a 7x7 board: 34 fruits, 10 bombs and 5 rums, dug 15 times with
 * the Carnival Shovel. Column {@code x} grows east and row {@code z} grows south.
 */
public class FruitDiggingBoard {
    public static final int SIZE = 7;
    public static final int DIGS = 15;
    private static final double POMEGRANATE_MULTIPLIER = 1.5;
    private static final double DURIAN_MULTIPLIER = 0.5;
    private static final double WATERMELON_SHARE = 0.5;

    public enum CellState { HIDDEN, REVEALED, DESTROYED }

    public enum DowsingMode {
        MINES("<c>Mines"), ANCHOR("<b>Anchor"), TREASURE("<6>Treasure");

        @Getter
        private final String displayName;

        DowsingMode(String displayName) {
            this.displayName = displayName;
        }

        public DowsingMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public record Reveal(int x, int z, FruitTile tile, double points, boolean fromWatermelon) {}

    public record Dig(List<Reveal> reveals, List<int[]> destroyed, boolean bombDefused, @Nullable String dowsing) {}

    private final FruitTile[][] tiles;
    private final CellState[][] states = new CellState[SIZE][SIZE];
    private final Random random;
    @Getter
    private int digsLeft = DIGS;
    @Getter
    private double score;
    private double nextMultiplier = 1;
    private boolean bombShield;
    private boolean dowsingDisabled;
    private int apples;
    private int cherries;
    private final Set<FruitTile> uniqueFruits = EnumSet.noneOf(FruitTile.class);
    private final Map<FruitTile, Integer> found = new EnumMap<>(FruitTile.class);

    public FruitDiggingBoard(Random random) {
        this(shuffledTiles(random), random);
    }

    public FruitDiggingBoard(FruitTile[][] tiles, Random random) {
        this.tiles = tiles;
        this.random = random;
        for (CellState[] row : states) java.util.Arrays.fill(row, CellState.HIDDEN);
    }

    private static FruitTile[][] shuffledTiles(Random random) {
        List<FruitTile> bag = new ArrayList<>();
        for (FruitTile tile : FruitTile.values()) {
            for (int i = 0; i < tile.getPerBoard(); i++) bag.add(tile);
        }
        Collections.shuffle(bag, random);
        FruitTile[][] tiles = new FruitTile[SIZE][SIZE];
        for (int i = 0; i < bag.size(); i++) tiles[i % SIZE][i / SIZE] = bag.get(i);
        return tiles;
    }

    public FruitTile tileAt(int x, int z) {
        return tiles[x][z];
    }

    public CellState stateAt(int x, int z) {
        return states[x][z];
    }

    public boolean isFinished() {
        return digsLeft <= 0;
    }

    public @Nullable Dig dig(int x, int z, DowsingMode mode) {
        if (!inside(x, z) || states[x][z] != CellState.HIDDEN || isFinished()) return null;
        double multiplier = nextMultiplier;
        nextMultiplier = 1;
        boolean shielded = bombShield;
        bombShield = false;
        boolean dowse = !dowsingDisabled;
        dowsingDisabled = false;
        digsLeft--;

        List<Reveal> reveals = new ArrayList<>();
        List<int[]> destroyed = new ArrayList<>();
        FruitTile tile = tiles[x][z];
        states[x][z] = CellState.REVEALED;
        boolean defused = false;
        if (tile == FruitTile.BOMB) {
            if (shielded) defused = true;
            else explode(x, z, destroyed);
            reveals.add(new Reveal(x, z, tile, 0, false));
        } else if (tile == FruitTile.RUM) {
            dowsingDisabled = true;
            reveals.add(new Reveal(x, z, tile, 0, false));
        } else {
            double points = collect(tile) * multiplier;
            score += points;
            reveals.add(new Reveal(x, z, tile, points, false));
            activate(x, z, tile, reveals);
        }
        return new Dig(reveals, destroyed, defused, dowse ? dowse(x, z, mode) : null);
    }

    private double collect(FruitTile tile) {
        uniqueFruits.add(tile);
        found.merge(tile, 1, Integer::sum);
        return switch (tile) {
            case APPLE -> ++apples * FruitTile.APPLE_BONUS;
            case CHERRY -> tile.getPoints() + ++cherries * FruitTile.CHERRY_BONUS;
            default -> tile.getPoints();
        };
    }

    private void activate(int x, int z, FruitTile tile, List<Reveal> reveals) {
        switch (tile) {
            case POMEGRANATE -> nextMultiplier = POMEGRANATE_MULTIPLIER;
            case DURIAN -> nextMultiplier = DURIAN_MULTIPLIER;
            case COCONUT -> bombShield = true;
            case WATERMELON -> chain(x, z, reveals);
            default -> {}
        }
    }

    private void chain(int x, int z, List<Reveal> reveals) {
        List<int[]> candidates = hiddenFruits(x, z, true);
        if (candidates.isEmpty()) candidates = hiddenFruits(x, z, false);
        if (candidates.isEmpty()) return;
        int[] cell = candidates.get(random.nextInt(candidates.size()));
        FruitTile tile = tiles[cell[0]][cell[1]];
        states[cell[0]][cell[1]] = CellState.REVEALED;
        double points = collect(tile) * WATERMELON_SHARE;
        score += points;
        reveals.add(new Reveal(cell[0], cell[1], tile, points, true));
        activate(cell[0], cell[1], tile, reveals);
    }

    private List<int[]> hiddenFruits(int x, int z, boolean adjacentOnly) {
        List<int[]> cells = new ArrayList<>();
        for (int cx = 0; cx < SIZE; cx++) {
            for (int cz = 0; cz < SIZE; cz++) {
                if (adjacentOnly && (Math.abs(cx - x) > 1 || Math.abs(cz - z) > 1)) continue;
                if (states[cx][cz] == CellState.HIDDEN && tiles[cx][cz].isFruit()) cells.add(new int[]{cx, cz});
            }
        }
        return cells;
    }

    private void explode(int x, int z, List<int[]> destroyed) {
        for (int[] cell : neighbours(x, z)) {
            if (states[cell[0]][cell[1]] == CellState.HIDDEN && tiles[cell[0]][cell[1]].isFruit()) {
                states[cell[0]][cell[1]] = CellState.DESTROYED;
                destroyed.add(cell);
            }
        }
    }

    private String dowse(int x, int z, DowsingMode mode) {
        return switch (mode) {
            case MINES -> {
                long bombs = neighbours(x, z).stream()
                        .filter(cell -> tiles[cell[0]][cell[1]] == FruitTile.BOMB && states[cell[0]][cell[1]] == CellState.HIDDEN)
                        .count();
                yield "<c><l>MINES! <7>There " + (bombs == 1 ? "is <c>1 <7>bomb" : "are <c>" + bombs + " <7>bombs") + " hidden nearby.";
            }
            case ANCHOR -> {
                int[] lowest = hiddenNeighbourFruit(x, z, false);
                yield lowest == null ? "<b><l>ANCHOR! <7>There are no fruits nearby."
                        : "<b><l>ANCHOR! <7>The lowest scoring fruit nearby is to the <b>" + direction(x, z, lowest) + "<7>.";
            }
            case TREASURE -> {
                int[] highest = hiddenNeighbourFruit(x, z, true);
                yield highest == null ? "<6><l>TREASURE! <7>There are no fruits nearby."
                        : "<6><l>TREASURE! <7>There is a " + tiles[highest[0]][highest[1]].colored() + " <7>nearby.";
            }
        };
    }

    private @Nullable int[] hiddenNeighbourFruit(int x, int z, boolean highest) {
        int[] best = null;
        for (int[] cell : neighbours(x, z)) {
            FruitTile tile = tiles[cell[0]][cell[1]];
            if (states[cell[0]][cell[1]] != CellState.HIDDEN || !tile.isFruit()) continue;
            if (best == null) {
                best = cell;
                continue;
            }
            int current = tiles[best[0]][best[1]].getPoints();
            if (highest ? tile.getPoints() > current : tile.getPoints() < current) best = cell;
        }
        return best;
    }

    static String direction(int x, int z, int[] to) {
        String north = to[1] < z ? "North" : to[1] > z ? "South" : "";
        String east = to[0] > x ? "East" : to[0] < x ? "West" : "";
        if (north.isEmpty()) return east;
        return east.isEmpty() ? north : north + "-" + east;
    }

    private static List<int[]> neighbours(int x, int z) {
        List<int[]> cells = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if ((dx != 0 || dz != 0) && inside(x + dx, z + dz)) cells.add(new int[]{x + dx, z + dz});
            }
        }
        return cells;
    }

    private static boolean inside(int x, int z) {
        return x >= 0 && z >= 0 && x < SIZE && z < SIZE;
    }

    public CarnivalGameResult result() {
        Map<Metric, Integer> metrics = new EnumMap<>(Metric.class);
        metrics.put(Metric.APPLES, found.getOrDefault(FruitTile.APPLE, 0));
        metrics.put(Metric.UNIQUE_FRUITS, uniqueFruits.size());
        metrics.put(Metric.DRAGONFRUITS, found.getOrDefault(FruitTile.DRAGONFRUIT, 0));
        return new CarnivalGameResult(CarnivalGame.FRUIT_DIGGING, Math.round(score), metrics);
    }
}
