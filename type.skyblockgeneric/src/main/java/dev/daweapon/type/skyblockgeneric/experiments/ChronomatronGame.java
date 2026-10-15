package dev.daweapon.type.skyblockgeneric.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class ChronomatronGame {
    public enum Result { CORRECT, ROUND_COMPLETE, FAILED, FINISHED }

    private final ChronomatronStake stake;
    private final RandomGenerator random;
    private final List<Integer> sequence = new ArrayList<>();
    private int inputIndex;
    private int longestChain;
    private boolean over;

    public ChronomatronGame(ChronomatronStake stake, RandomGenerator random) {
        this.stake = stake;
        this.random = random;
    }

    public ChronomatronStake stake() {
        return stake;
    }

    public List<Integer> sequence() {
        return List.copyOf(sequence);
    }

    public int round() {
        return sequence.size();
    }

    public int longestChain() {
        return longestChain;
    }

    public boolean isOver() {
        return over;
    }

    public void nextRound() {
        sequence.add(random.nextInt(stake.colors()));
        inputIndex = 0;
    }

    public Result press(int color) {
        if (over) return Result.FINISHED;
        if (color != sequence.get(inputIndex)) {
            over = true;
            return Result.FAILED;
        }
        inputIndex++;
        if (inputIndex < sequence.size()) return Result.CORRECT;
        longestChain = sequence.size();
        if (longestChain >= ChronomatronStake.MAX_ROUNDS) {
            over = true;
            return Result.FINISHED;
        }
        return Result.ROUND_COMPLETE;
    }

    public void abandon() {
        over = true;
    }
}
