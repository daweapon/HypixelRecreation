package dev.daweapon.type.skyblockgeneric.experiments;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentGamesTest {
    @Test
    void chronomatronFailingAtNoteSixKeepsChainOfFive() {
        ChronomatronGame game = new ChronomatronGame(ChronomatronStake.HIGH, new Random(1));
        for (int round = 1; round <= 5; round++) {
            game.nextRound();
            ChronomatronGame.Result last = null;
            for (int color : game.sequence()) last = game.press(color);
            assertEquals(ChronomatronGame.Result.ROUND_COMPLETE, last);
        }
        game.nextRound();
        int wrong = (game.sequence().getFirst() + 1) % ChronomatronStake.HIGH.colors();
        assertEquals(ChronomatronGame.Result.FAILED, game.press(wrong));
        assertEquals(5, game.longestChain());
        assertEquals(7_500, ChronomatronStake.HIGH.xpFor(game.longestChain()));
        assertEquals(1, ChronomatronStake.HIGH.clickBonus().clicksFor(2, 0));
        assertEquals(2, ChronomatronStake.HIGH.clickBonus().clicksFor(6, 0));
    }

    @Test
    void chronomatronXpCapsAtFifteenNotes() {
        assertEquals(90_000, ChronomatronStake.METAPHYSICAL.xpFor(30));
        assertEquals(3, ChronomatronStake.METAPHYSICAL.clickBonus().clicksFor(9, 0));
        assertEquals(54_000, ChronomatronStake.METAPHYSICAL.xpFor(9));
    }

    @Test
    void serumShortensChains() {
        assertEquals(1, ChronomatronStake.HIGH.clickBonus().clicksFor(1, 1));
        assertEquals(0, ChronomatronStake.HIGH.clickBonus().clicksFor(1, 0));
    }

    @Test
    void ultrasequencerSequenceNineOnMetaphysicalPaysSixtyThreeThousand() {
        assertEquals(63_000, UltrasequencerStake.METAPHYSICAL.xpFor(9));
        assertEquals(3, UltrasequencerStake.METAPHYSICAL.clickBonus().clicksFor(6, 0));
    }

    @Test
    void ultrasequencerRewardsOrderedClicks() {
        UltrasequencerGame game = new UltrasequencerGame(UltrasequencerStake.SUPREME, new Random(2));
        game.nextRound();
        assertEquals(UltrasequencerGame.Result.ROUND_COMPLETE, game.press(game.order().getFirst()));
        game.nextRound();
        int wrong = UltrasequencerStake.SUPREME.slots().stream().filter(slot -> !game.order().contains(slot)).findFirst().orElseThrow();
        assertEquals(UltrasequencerGame.Result.FAILED, game.press(wrong));
        assertEquals(1, game.longestSequence());
    }

    @Test
    void superpairsMatchingPairAwardsItAndCostsTwoClicks() {
        PairPrize xp = PairPrize.xp("XP_0", net.minestom.server.item.Material.LAPIS_LAZULI, 5_000);
        PairPrize other = PairPrize.xp("XP_1", net.minestom.server.item.Material.IRON_INGOT, 6_000);
        SuperpairsGame game = new SuperpairsGame(SuperpairsStake.BEGINNER, List.of(xp, other), 10, new Random(3));
        int first = 0;
        int partner = -1;
        for (int tile = 1; tile < game.tileCount(); tile++) if (game.prizeAt(tile) == game.prizeAt(first)) partner = tile;
        game.click(first);
        SuperpairsGame.Click result = game.click(partner);
        assertEquals(SuperpairsGame.Outcome.MATCHED, result.outcome());
        assertEquals(8, game.clicksRemaining());
        assertEquals(1, game.pairsFound());
    }

    @Test
    void featherAddsThreeClicksAndDiamondInstantFinds() {
        PairPrize feather = PairPrize.powerUp(PairPrize.Kind.FEATHER);
        PairPrize diamond = PairPrize.powerUp(PairPrize.Kind.DIAMOND);
        SuperpairsGame game = new SuperpairsGame(SuperpairsStake.BEGINNER, List.of(feather, diamond), 10, new Random(4));
        int tile = 0;
        int partner = -1;
        for (int i = 1; i < 4; i++) if (game.prizeAt(i) == game.prizeAt(tile)) partner = i;
        game.click(tile);
        game.click(partner);
        PairPrize first = game.prizeAt(tile);
        assertEquals(first.kind() == PairPrize.Kind.FEATHER ? 11 : 9, game.clicksRemaining());
        if (first.kind() == PairPrize.Kind.DIAMOND) {
            assertTrue(game.instantFindActive());
            int next = -1;
            for (int i = 0; i < 4; i++) if (!game.isMatched(i)) next = i;
            assertEquals(SuperpairsGame.Outcome.MATCHED, game.click(next).outcome());
            assertFalse(game.instantFindActive());
        }
    }

    @Test
    void boardHasOnePrizePerPairWithUniqueKeys() {
        for (SuperpairsStake stake : SuperpairsStake.values()) {
            List<PairPrize> board = SuperpairsRewards.board(stake, null, 1, null, new Random(5));
            assertEquals(stake.pairCount(), board.size());
            assertEquals(board.size(), board.stream().map(PairPrize::key).distinct().count());
        }
    }

    @Test
    void mismatchedTilesStayRevealed() {
        PairPrize a = PairPrize.xp("XP_0", net.minestom.server.item.Material.LAPIS_LAZULI, 5_000);
        PairPrize b = PairPrize.xp("XP_1", net.minestom.server.item.Material.IRON_INGOT, 6_000);
        SuperpairsGame game = new SuperpairsGame(SuperpairsStake.BEGINNER, List.of(a, b), 10, new Random(7));
        int other = -1;
        for (int tile = 1; tile < game.tileCount(); tile++) if (game.prizeAt(tile) != game.prizeAt(0)) other = tile;
        game.click(0);
        assertEquals(SuperpairsGame.Outcome.MISMATCHED, game.click(other).outcome());
        assertTrue(game.isRevealed(0));
        assertTrue(game.isRevealed(other));
        assertEquals(8, game.clicksRemaining());
    }

    @Test
    void clicksRunningOutEndsTheGame() {
        PairPrize a = PairPrize.xp("XP_0", net.minestom.server.item.Material.LAPIS_LAZULI, 5_000);
        PairPrize b = PairPrize.xp("XP_1", net.minestom.server.item.Material.IRON_INGOT, 6_000);
        SuperpairsGame game = new SuperpairsGame(SuperpairsStake.BEGINNER, List.of(a, b), 1, new Random(6));
        game.click(0);
        assertTrue(game.isOver());
    }
}
