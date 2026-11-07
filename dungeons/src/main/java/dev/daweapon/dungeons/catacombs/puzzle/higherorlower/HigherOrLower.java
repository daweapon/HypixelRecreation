package dev.daweapon.dungeons.catacombs.puzzle.higherorlower;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Higher Or Lower: the blazes must die one by one in order of their health, weakest first when the chest starts at the
 * bottom of the chamber and strongest first when it starts at the top. A blaze killed out of turn fails the puzzle.
 */
public final class HigherOrLower {
    public enum Order {
        LOWEST_FIRST,
        HIGHEST_FIRST
    }

    public enum Kill {
        IN_ORDER,
        LAST,
        OUT_OF_ORDER,
        IGNORED
    }

    private final List<Integer> healths;
    private final List<Integer> sequence;
    private int killed;
    private boolean failed;

    public HigherOrLower(List<Integer> healths, Order order) {
        this.healths = List.copyOf(healths);
        if (new HashSet<>(healths).size() != healths.size()) throw new IllegalArgumentException("Every blaze needs its own health");
        Comparator<Integer> byHealth = Comparator.comparingInt(this.healths::get);
        List<Integer> indices = new ArrayList<>();
        for (int index = 0; index < healths.size(); index++) indices.add(index);
        indices.sort(order == Order.LOWEST_FIRST ? byHealth : byHealth.reversed());
        this.sequence = List.copyOf(indices);
    }

    /** Distinct healths between {@code min} and {@code max}, one per blaze. */
    public static List<Integer> dealHealths(int blazes, int min, int max, RandomGenerator random) {
        Set<Integer> chosen = new HashSet<>();
        List<Integer> healths = new ArrayList<>();
        while (healths.size() < blazes) {
            int health = min + random.nextInt(max - min + 1);
            if (chosen.add(health)) healths.add(health);
        }
        return healths;
    }

    public Kill kill(int blaze) {
        if (failed || done()) return Kill.IGNORED;
        if (sequence.get(killed) != blaze) {
            failed = true;
            return Kill.OUT_OF_ORDER;
        }
        killed++;
        return done() ? Kill.LAST : Kill.IN_ORDER;
    }

    public int next() {
        return done() ? -1 : sequence.get(killed);
    }

    public int killed() {
        return killed;
    }

    public boolean done() {
        return killed == sequence.size();
    }

    public boolean failed() {
        return failed;
    }

    public List<Integer> healths() {
        return healths;
    }

    public List<Integer> sequence() {
        return sequence;
    }

    /** How far up (or down) the column the chest has climbed after the kills so far, from 0 to {@code travel}. */
    public int chestOffset(int travel) {
        return Math.round((float) travel * killed / sequence.size());
    }
}
