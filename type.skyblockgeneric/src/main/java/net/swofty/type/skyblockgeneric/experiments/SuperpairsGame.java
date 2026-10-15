package net.swofty.type.skyblockgeneric.experiments;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

public final class SuperpairsGame {
    public enum Outcome { IGNORED, FIRST_REVEALED, MATCHED, MISMATCHED }

    public record Click(Outcome outcome, @Nullable PairPrize found) {
    }

    private final SuperpairsStake stake;
    private final List<PairPrize> tiles = new ArrayList<>();
    private final boolean[] matched;
    private final boolean[] seen;
    private final List<PairPrize> foundPrizes = new ArrayList<>();
    private int clicksRemaining;
    private int clicksUsed;
    private int firstPick = -1;
    private boolean instantFind;
    private boolean over;

    public SuperpairsGame(SuperpairsStake stake, List<PairPrize> pairs, int clicks, RandomGenerator random) {
        this.stake = stake;
        for (PairPrize pair : pairs) {
            tiles.add(pair);
            tiles.add(pair);
        }
        Collections.shuffle(tiles, new java.util.Random(random.nextLong()));
        this.matched = new boolean[tiles.size()];
        this.seen = new boolean[tiles.size()];
        this.clicksRemaining = clicks;
    }

    public SuperpairsStake stake() {
        return stake;
    }

    public int tileCount() {
        return tiles.size();
    }

    public PairPrize prizeAt(int tile) {
        return tiles.get(tile);
    }

    public int clicksRemaining() {
        return clicksRemaining;
    }

    public int clicksUsed() {
        return clicksUsed;
    }

    public boolean isMatched(int tile) {
        return matched[tile];
    }

    public boolean isRevealed(int tile) {
        return matched[tile] || seen[tile];
    }

    public boolean instantFindActive() {
        return instantFind;
    }

    public boolean hasFirstPick() {
        return firstPick >= 0;
    }

    public boolean isOver() {
        return over;
    }

    public List<PairPrize> foundPrizes() {
        return List.copyOf(foundPrizes);
    }

    public int pairsFound() {
        return foundPrizes.size();
    }

    public void end() {
        over = true;
    }

    public Click click(int tile) {
        if (over || tile < 0 || tile >= tiles.size() || matched[tile] || tile == firstPick) {
            return new Click(Outcome.IGNORED, null);
        }
        clicksRemaining--;
        clicksUsed++;
        seen[tile] = true;
        if (instantFind && firstPick < 0) {
            instantFind = false;
            matched[tile] = true;
            int partner = partnerOf(tile);
            matched[partner] = true;
            seen[partner] = true;
            return new Click(Outcome.MATCHED, award(tiles.get(tile)));
        }
        if (firstPick < 0) {
            firstPick = tile;
            finishIfDone();
            return new Click(Outcome.FIRST_REVEALED, null);
        }
        int first = firstPick;
        firstPick = -1;
        if (tiles.get(first) == tiles.get(tile)) {
            matched[first] = true;
            matched[tile] = true;
            return new Click(Outcome.MATCHED, award(tiles.get(tile)));
        }
        finishIfDone();
        return new Click(Outcome.MISMATCHED, null);
    }

    private PairPrize award(PairPrize prize) {
        foundPrizes.add(prize);
        switch (prize.kind()) {
            case LAPIS_BLOCK -> clicksRemaining += 1;
            case FEATHER -> clicksRemaining += 3;
            case DIAMOND -> {
                instantFind = true;
                clicksRemaining += 1;
            }
            default -> {
            }
        }
        finishIfDone();
        return prize;
    }

    private int partnerOf(int tile) {
        for (int other = 0; other < tiles.size(); other++) {
            if (other != tile && tiles.get(other) == tiles.get(tile)) return other;
        }
        throw new IllegalStateException("tile without a partner");
    }

    private void finishIfDone() {
        boolean everyPairFound = foundPrizes.size() >= tiles.size() / 2;
        if (everyPairFound || clicksRemaining <= 0) over = true;
    }
}
