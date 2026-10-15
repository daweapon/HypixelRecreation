package dev.daweapon.type.skyblockgeneric.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class UltrasequencerGame {
    public enum Result { CORRECT, ROUND_COMPLETE, FAILED, FINISHED }

    private final UltrasequencerStake stake;
    private final RandomGenerator random;
    private final List<Integer> slots;
    private final List<Integer> order = new ArrayList<>();
    private int inputIndex;
    private int longestSequence;
    private boolean over;

    public UltrasequencerGame(UltrasequencerStake stake, RandomGenerator random) {
        this.stake = stake;
        this.random = random;
        this.slots = stake.slots();
    }

    public UltrasequencerStake stake() {
        return stake;
    }

    public int round() {
        return order.size();
    }

    public int longestSequence() {
        return longestSequence;
    }

    public boolean isOver() {
        return over;
    }

    public List<Integer> order() {
        return List.copyOf(order);
    }

    public int numberAt(int slot) {
        int index = order.indexOf(slot);
        return index < 0 ? 0 : index + 1;
    }

    public void nextRound() {
        List<Integer> free = new ArrayList<>(slots);
        free.removeAll(order);
        order.add(free.get(random.nextInt(free.size())));
        inputIndex = 0;
    }

    public Result press(int slot) {
        if (over) return Result.FINISHED;
        if (slot != order.get(inputIndex)) {
            over = true;
            return Result.FAILED;
        }
        inputIndex++;
        if (inputIndex < order.size()) return Result.CORRECT;
        longestSequence = order.size();
        if (longestSequence >= stake.maxNumbers()) {
            over = true;
            return Result.FINISHED;
        }
        return Result.ROUND_COMPLETE;
    }

    public void abandon() {
        over = true;
    }
}
