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
 * Fruit values and dowsing text follow the 2026-11-10 capture of the real game.
 */
public class FruitDiggingBoard {
    public static final int SIZE = 7;
    public static final int DIGS = 15;
    private static final double POMEGRANATE_MULTIPLIER = 1.5;
    private static final double DURIAN_MULTIPLIER = 0.5;
    private static final double WATERMELON_SHARE = 0.5;
    private static final int MIN_BOMB_SPREAD = 2;
    private static final int MAX_BOMB_SPREAD = 3;

    public enum CellState { HIDDEN, REVEALED, DESTROYED }

    public enum DowsingMode {
        MINES("<c>Mines", 1f, List.of("<7>Reveals the amount of <c>Bombs", "<7>adjacent to this block when you dig.")),
        ANCHOR("<b>Anchor", 1.1428572f, List.of("<7>Reveals the location of the",
                "<7>lowest-scoring <6>Fruit <7>adjacent to", "<7>this block when you dig.")),
        TREASURE("<6>Treasure", 1.2857143f, List.of("<7>Reveals the highest scoring <6>Fruit",
                "<7>adjacent to this block when you dig."));

        @Getter
        private final String displayName;
        @Getter
        private final float switchPitch;
        @Getter
        private final List<String> description;

        DowsingMode(String displayName, float switchPitch, List<String> description) {
            this.displayName = displayName;
            this.switchPitch = switchPitch;
            this.description = description;
        }

        public DowsingMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    public record Reveal(int x, int z, FruitTile tile, double points, boolean fromWatermelon, double multiplier,
                         @Nullable FruitTile multiplierSource) {}

    public record Dig(List<Reveal> reveals, List<int[]> destroyed, boolean bombDefused, @Nullable String dowsing,
                      @Nullable Reveal anchor) {}

    private final FruitTile[][] tiles;
    private final CellState[][] states = new CellState[SIZE][SIZE];
    private final Random random;
    @Getter
    private int digsLeft = DIGS;
    @Getter
    private double score;
    private double nextMultiplier = 1;
    private @Nullable FruitTile nextMultiplierSource;
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
        FruitTile multiplierSource = nextMultiplierSource;
        nextMultiplier = 1;
        nextMultiplierSource = null;
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
            found.merge(tile, 1, Integer::sum);
            if (shielded) defused = true;
            else explode(x, z, destroyed);
            reveals.add(new Reveal(x, z, tile, 0, false, 1, null));
        } else if (tile == FruitTile.RUM) {
            found.merge(tile, 1, Integer::sum);
            dowsingDisabled = true;
            reveals.add(new Reveal(x, z, tile, 0, false, 1, null));
        } else {
            double points = collect(tile) * multiplier;
            score += points;
            reveals.add(new Reveal(x, z, tile, points, false, multiplier, multiplier == 1 ? null : multiplierSource));
            activate(x, z, tile, reveals);
        }
        boolean anchored = dowse && mode == DowsingMode.ANCHOR;
        int[] lowest = anchored ? hiddenNeighbourFruit(x, z, false) : null;
        Reveal anchor = lowest == null ? null : new Reveal(lowest[0], lowest[1], tiles[lowest[0]][lowest[1]], 0, false, 1, null);
        String dowsing = !dowse || anchor != null ? null : dowse(x, z, mode);
        return new Dig(reveals, destroyed, defused, dowsing, anchor);
    }

    private double collect(FruitTile tile) {
        uniqueFruits.add(tile);
        found.merge(tile, 1, Integer::sum);
        return switch (tile) {
            case APPLE -> ++apples * FruitTile.APPLE_BONUS;
            case CHERRY -> tile.getPoints() + cherries++ * FruitTile.CHERRY_BONUS;
            default -> tile.getPoints();
        };
    }

    /** What digging this tile right now would score, before any Pomegranate or Durian multiplier. */
    public int valueOf(FruitTile tile) {
        return switch (tile) {
            case APPLE -> (apples + 1) * FruitTile.APPLE_BONUS;
            case CHERRY -> tile.getPoints() + cherries * FruitTile.CHERRY_BONUS;
            default -> tile.getPoints();
        };
    }

    public Map<FruitTile, Integer> found() {
        return Collections.unmodifiableMap(found);
    }

    private void activate(int x, int z, FruitTile tile, List<Reveal> reveals) {
        switch (tile) {
            case POMEGRANATE -> {
                nextMultiplier = POMEGRANATE_MULTIPLIER;
                nextMultiplierSource = tile;
            }
            case DURIAN -> {
                nextMultiplier = DURIAN_MULTIPLIER;
                nextMultiplierSource = tile;
            }
            case COCONUT -> bombShield = true;
            case WATERMELON -> chain(x, z, reveals);
            default -> {}
        }
    }

    private void chain(int x, int z, List<Reveal> reveals) {
        List<int[]> candidates = hiddenFruits(x, z);
        if (candidates.isEmpty()) return;
        int[] cell = candidates.get(random.nextInt(candidates.size()));
        FruitTile tile = tiles[cell[0]][cell[1]];
        states[cell[0]][cell[1]] = CellState.REVEALED;
        double points = collect(tile) * WATERMELON_SHARE;
        score += points;
        reveals.add(new Reveal(cell[0], cell[1], tile, points, true, WATERMELON_SHARE, FruitTile.WATERMELON));
        activate(cell[0], cell[1], tile, reveals);
    }

    private List<int[]> hiddenFruits(int x, int z) {
        List<int[]> cells = new ArrayList<>();
        for (int[] cell : neighbours(x, z)) {
            if (states[cell[0]][cell[1]] == CellState.HIDDEN && tiles[cell[0]][cell[1]].isFruit()) cells.add(cell);
        }
        return cells;
    }

    private void explode(int x, int z, List<int[]> destroyed) {
        List<int[]> targets = hiddenFruits(x, z);
        Collections.shuffle(targets, random);
        int spread = MIN_BOMB_SPREAD + random.nextInt(MAX_BOMB_SPREAD - MIN_BOMB_SPREAD + 1);
        for (int[] cell : targets.subList(0, Math.min(spread, targets.size()))) {
            states[cell[0]][cell[1]] = CellState.DESTROYED;
            destroyed.add(cell);
        }
    }

    private String dowse(int x, int z, DowsingMode mode) {
        return switch (mode) {
            case MINES -> {
                long bombs = neighbours(x, z).stream()
                        .filter(cell -> tiles[cell[0]][cell[1]] == FruitTile.BOMB && states[cell[0]][cell[1]] == CellState.HIDDEN)
                        .count();
                yield "<c><l>MINES! <f>There " + (bombs == 1 ? "is <6>1 <f>bomb" : "are <6>" + bombs + " <f>bombs") + " hidden nearby.";
            }
            case ANCHOR -> "<b><l>ANCHOR! <f>There are no fruits nearby!";
            case TREASURE -> {
                int[] highest = hiddenNeighbourFruit(x, z, true);
                if (highest == null) yield "<6><l>TREASURE! <f>There are no fruits nearby!";
                FruitTile fruit = tiles[highest[0]][highest[1]];
                yield "<6><l>TREASURE! <f>There is " + fruit.article() + " " + fruit.colored() + " <f>nearby.";
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
            int current = valueOf(tiles[best[0]][best[1]]);
            int value = valueOf(tile);
            if (highest ? value > current : value < current) best = cell;
        }
        return best;
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
