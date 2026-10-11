package net.swofty.type.skyblockgeneric.fossil;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

/**
 * One Fossil Excavator dig: a 6x9 field of dirt hiding at most one fossil and a handful of
 * treasure tiles. Every click on dirt costs a Chisel charge; the dig ends when the fossil is
 * fully uncovered or the charges run out, and only what was uncovered is paid out.
 */
public final class ExcavatorGame {
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int SIZE = ROWS * COLS;
    public static final double FOSSIL_CHANCE_PERCENT = 10;

    public enum Dig {ALREADY_CLEARED, OVER, EMPTY, FOSSIL, TREASURE}

    private final boolean[] cleared = new boolean[SIZE];
    private final boolean[] fossilTiles = new boolean[SIZE];
    private final Map<Integer, ExcavatorLoot.Drop> treasure = new HashMap<>();
    private final Set<Integer> highlighted = new HashSet<>();
    private final @Nullable FossilType fossil;
    private final double essenceBonus;
    private int charges;
    private int fossilCleared;

    private ExcavatorGame(@Nullable FossilType fossil, int charges, double essenceBonus) {
        this.fossil = fossil;
        this.charges = charges;
        this.essenceBonus = essenceBonus;
    }

    public static ExcavatorGame start(UUID player, ChiselStats chisel, int bonusCharges, RandomGenerator random) {
        int charges = chisel.baseCharges() + bonusCharges + procs(player, chisel.extraChargeChance(), random);
        FossilType fossil = pickFossil(player, random);
        ExcavatorGame game = new ExcavatorGame(fossil, charges, chisel.essenceBonus());
        if (fossil != null) game.placeFossil(fossil, random);
        game.placeTreasure(player, ExcavatorLoot.rollCount(random) + procs(player, chisel.extraTreasureChance(), random), random);
        int highlights = procs(player, chisel.highlightChance(), random);
        List<Integer> hidden = new ArrayList<>(game.treasure.keySet());
        Collections.shuffle(hidden, new java.util.Random(random.nextLong()));
        for (int i = 0; i < Math.min(highlights, hidden.size()); i++) game.highlighted.add(hidden.get(i));
        return game;
    }

    static int procs(UUID player, double chancePercent, RandomGenerator random) {
        if (chancePercent <= 0) return 0;
        int guaranteed = (int) Math.floor(chancePercent / 100);
        double remainder = chancePercent - guaranteed * 100;
        return guaranteed + (remainder > 0 && LootRoll.roll(player, null, remainder, random) ? 1 : 0);
    }

    private static @Nullable FossilType pickFossil(UUID player, RandomGenerator random) {
        List<FossilType> pool = FossilType.excavatorPool();
        ItemType forced = LootRoll.claim(player, pool.stream().map(FossilType::getItem).toList());
        if (forced != null) return FossilType.byItem(forced);
        FossilType candidate = pool.get(random.nextInt(pool.size()));
        return LootRoll.roll(player, candidate.getItem(), FOSSIL_CHANCE_PERCENT, random) ? candidate : null;
    }

    private void placeFossil(FossilType type, RandomGenerator random) {
        int top = random.nextInt(ROWS - type.height() + 1);
        int left = random.nextInt(COLS - type.width() + 1);
        for (int row = 0; row < type.height(); row++) {
            String line = type.getShape().get(row);
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) == 'X') fossilTiles[(top + row) * COLS + left + col] = true;
            }
        }
    }

    private void placeTreasure(UUID player, int count, RandomGenerator random) {
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) if (!fossilTiles[i]) free.add(i);
        Collections.shuffle(free, new java.util.Random(random.nextLong()));
        for (int i = 0; i < Math.min(count, free.size()); i++) treasure.put(free.get(i), ExcavatorLoot.roll(player, random));
    }

    public Dig dig(int index) {
        if (index < 0 || index >= SIZE) return Dig.ALREADY_CLEARED;
        if (isOver()) return Dig.OVER;
        if (cleared[index]) return Dig.ALREADY_CLEARED;
        cleared[index] = true;
        charges--;
        if (fossilTiles[index]) {
            fossilCleared++;
            return Dig.FOSSIL;
        }
        return treasure.containsKey(index) ? Dig.TREASURE : Dig.EMPTY;
    }

    public boolean isCompleted() {
        return fossil != null && fossilCleared >= fossil.tileCount();
    }

    public boolean isOver() {
        return charges <= 0 || isCompleted();
    }

    public int charges() {
        return Math.max(charges, 0);
    }

    public boolean hasFossil() {
        return fossil != null;
    }

    public @Nullable FossilType fossil() {
        return fossil;
    }

    public double progressPercent() {
        return fossil == null ? 0 : 100.0 * fossilCleared / fossil.tileCount();
    }

    public boolean isCleared(int index) {
        return cleared[index];
    }

    public boolean isFossilTile(int index) {
        return fossilTiles[index];
    }

    public boolean isHighlighted(int index) {
        return !cleared[index] && highlighted.contains(index);
    }

    public @Nullable ExcavatorLoot.Drop uncoveredTreasure(int index) {
        return cleared[index] ? treasure.get(index) : null;
    }

    public ExcavatorLoot.@Nullable Drop treasureAt(int index) {
        return treasure.get(index);
    }

    public int treasureCount() {
        return treasure.size();
    }

    public List<ExcavatorLoot.Drop> uncoveredRewards() {
        List<ExcavatorLoot.Drop> rewards = new ArrayList<>();
        treasure.forEach((index, drop) -> {
            if (!cleared[index]) return;
            rewards.add(drop.kind() == ExcavatorLoot.Kind.ESSENCE
                    ? new ExcavatorLoot.Drop(drop.kind(), drop.item(), (int) Math.round(drop.amount() * (1 + essenceBonus / 100)))
                    : drop);
        });
        return rewards;
    }
}
