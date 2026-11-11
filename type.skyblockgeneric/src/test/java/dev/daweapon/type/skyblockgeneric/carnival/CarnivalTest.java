package dev.daweapon.type.skyblockgeneric.carnival;

import dev.daweapon.type.skyblockgeneric.carnival.CarnivalGameResult.Metric;
import dev.daweapon.type.skyblockgeneric.carnival.games.CatchAFishScore;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitDiggingBoard.DowsingMode;
import dev.daweapon.type.skyblockgeneric.carnival.games.FruitTile;
import dev.daweapon.type.skyblockgeneric.carnival.games.ZombieShootoutScore;
import dev.daweapon.type.skyblockgeneric.carnival.games.ZombieShootoutScore.ZombieTier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarnivalTest {

    @Test
    void eachEventShopCostsThreeThousandThreeHundredTokensAndFortyNineXp() {
        for (CarnivalPerkEvent event : CarnivalPerkEvent.values()) {
            int tokens = 0;
            int xp = 0;
            for (CarnivalPerk perk : CarnivalPerk.of(event)) {
                for (int tier = 1; tier <= perk.maxTier(); tier++) {
                    tokens += perk.costOf(tier);
                    xp += perk.xpOf(tier);
                }
                assertEquals(perk.maxTier(), perk.getValues().length, perk.name());
            }
            assertEquals(4, CarnivalPerk.of(event).size(), event.name());
            assertEquals(3_300, tokens, event.name());
            assertEquals(event.getXpSource().getMaxXP(), xp, event.name());
        }
    }

    @Test
    void goalsPayTwelveHundredTokensAndSeventyFiveTickets() {
        assertEquals(1_200, Arrays.stream(CarnivalGoal.values()).mapToInt(CarnivalGoal::getTokens).sum());
        assertEquals(75, CarnivalGoal.values().length * CarnivalGoal.TICKETS_PER_GOAL);
    }

    @Test
    void perkDescriptionsFormatTheirTierValue() {
        assertTrue(CarnivalPerk.ICY_HOOK.description(1).contains("+0.5α"));
        assertTrue(CarnivalPerk.STORIED_STINGER.description(5).contains("+25%"));
        assertEquals(0, CarnivalPerk.DIANAS_FAVOR.valueAt(0));
    }

    @Test
    void tokensAreOnePerHundredPoints() {
        assertEquals(0, Carnival.tokensFor(99));
        assertEquals(31, Carnival.tokensFor(3_150));
    }

    @Test
    void boardHoldsTheWikiTiles() {
        FruitDiggingBoard board = new FruitDiggingBoard(new Random(1));
        Map<FruitTile, Integer> counts = new EnumMap<>(FruitTile.class);
        for (int x = 0; x < FruitDiggingBoard.SIZE; x++) {
            for (int z = 0; z < FruitDiggingBoard.SIZE; z++) counts.merge(board.tileAt(x, z), 1, Integer::sum);
        }
        for (FruitTile tile : FruitTile.values()) assertEquals(tile.getPerBoard(), counts.get(tile), tile.name());
    }

    @Test
    void applesAndCherriesGrowWithEachOneFound() {
        FruitDiggingBoard board = board(FruitTile.APPLE, FruitTile.APPLE, FruitTile.CHERRY, FruitTile.CHERRY);
        board.dig(0, 0, DowsingMode.MINES);
        board.dig(1, 0, DowsingMode.MINES);
        assertEquals(300, board.getScore());
        board.dig(2, 0, DowsingMode.MINES);
        board.dig(3, 0, DowsingMode.MINES);
        assertEquals(300 + 200 + 500, board.getScore());
    }

    @Test
    void treasureNamesTheFruitWorthMostRightNow() {
        FruitDiggingBoard board = board(FruitTile.APPLE, FruitTile.APPLE, FruitTile.APPLE, FruitTile.MANGO, FruitTile.APPLE);
        board.dig(0, 0, DowsingMode.MINES);
        board.dig(1, 0, DowsingMode.MINES);
        assertEquals("<6><l>TREASURE! <f>There is a <a>Mango <f>nearby.", board.dig(2, 0, DowsingMode.TREASURE).dowsing());
        assertEquals("<6><l>TREASURE! <f>There is an <a>Apple <f>nearby.", board.dig(3, 0, DowsingMode.TREASURE).dowsing());
        assertEquals("<6><l>TREASURE! <f>There are no fruits nearby!", board.dig(4, 0, DowsingMode.TREASURE).dowsing());
        assertEquals("<c><l>MINES! <f>There are <6>0 <f>bombs hidden nearby.", board.dig(5, 0, DowsingMode.MINES).dowsing());
    }

    @Test
    void bombSpreadsToTwoOrThreeHiddenFruits() {
        FruitTile[][] tiles = new FruitTile[FruitDiggingBoard.SIZE][FruitDiggingBoard.SIZE];
        for (FruitTile[] column : tiles) Arrays.fill(column, FruitTile.MANGO);
        tiles[3][3] = FruitTile.BOMB;
        for (int seed = 0; seed < 20; seed++) {
            FruitDiggingBoard board = new FruitDiggingBoard(copy(tiles), new Random(seed));
            int destroyed = board.dig(3, 3, DowsingMode.MINES).destroyed().size();
            assertTrue(destroyed == 2 || destroyed == 3, "destroyed " + destroyed);
            assertEquals(1, board.found().get(FruitTile.BOMB));
        }
    }

    @Test
    void pomegranateAndDurianScaleTheNextDig() {
        FruitDiggingBoard board = board(FruitTile.POMEGRANATE, FruitTile.MANGO, FruitTile.DURIAN, FruitTile.MANGO);
        board.dig(0, 0, DowsingMode.MINES);
        board.dig(1, 0, DowsingMode.MINES);
        assertEquals(200 + 450, board.getScore());
        board.dig(2, 0, DowsingMode.MINES);
        board.dig(3, 0, DowsingMode.MINES);
        assertEquals(200 + 450 + 800 + 150, board.getScore());
    }

    @Test
    void bombsDestroyNeighboursUnlessACoconutWasDug() {
        FruitDiggingBoard board = board(FruitTile.BOMB, FruitTile.MANGO);
        FruitDiggingBoard.Dig dig = board.dig(0, 0, DowsingMode.MINES);
        assertNotNull(dig);
        assertEquals(1, dig.destroyed().size());
        assertEquals(FruitDiggingBoard.CellState.DESTROYED, board.stateAt(1, 0));

        FruitDiggingBoard shielded = board(FruitTile.COCONUT, FruitTile.BOMB, FruitTile.MANGO);
        shielded.dig(0, 0, DowsingMode.MINES);
        assertTrue(shielded.dig(1, 0, DowsingMode.MINES).bombDefused());
        assertEquals(FruitDiggingBoard.CellState.HIDDEN, shielded.stateAt(2, 0));
    }

    @Test
    void watermelonRevealsANeighbourForHalfAndRumSilencesTheShovel() {
        FruitDiggingBoard board = board(FruitTile.WATERMELON, FruitTile.MANGO);
        FruitDiggingBoard.Dig dig = board.dig(0, 0, DowsingMode.TREASURE);
        assertEquals(2, dig.reveals().size());
        assertEquals(100 + 150, board.getScore());
        assertEquals(FruitDiggingBoard.DIGS - 1, board.getDigsLeft());

        FruitDiggingBoard rum = board(FruitTile.RUM, FruitTile.MANGO);
        rum.dig(0, 0, DowsingMode.MINES);
        assertNull(rum.dig(1, 0, DowsingMode.MINES).dowsing());
    }

    @Test
    void fruitResultCountsGoalMetrics() {
        FruitDiggingBoard board = board(FruitTile.DRAGONFRUIT, FruitTile.APPLE);
        board.dig(0, 0, DowsingMode.MINES);
        board.dig(1, 0, DowsingMode.MINES);
        CarnivalGameResult result = board.result();
        assertEquals(1, result.metric(Metric.DRAGONFRUITS));
        assertEquals(2, result.metric(Metric.UNIQUE_FRUITS));
        assertTrue(CarnivalGoal.FRUIT_DRAGONFRUIT.isMetBy(result, 1));
    }

    @Test
    void catchStreakAddsFivePerCatchFromTheSecondUpToFifty() {
        CatchAFishScore score = new CatchAFishScore();
        assertEquals(200, score.onCatch(CatchAFishScore.FishColor.YELLOW, false));
        assertEquals(120, score.onCatch(CatchAFishScore.FishColor.RED, true));
        assertEquals(65, score.onCatch(CatchAFishScore.FishColor.RED, false));
        for (int i = 0; i < 5; i++) score.onCatch(CatchAFishScore.FishColor.RED, false);
        assertEquals(145, score.onCatch(CatchAFishScore.FishColor.BLUE, false));
        assertEquals(200, score.onCatch(CatchAFishScore.FishColor.RED, true));
        for (int i = 0; i < 3; i++) score.onCatch(CatchAFishScore.FishColor.BLUE, false);
        assertEquals(250, score.onCatch(CatchAFishScore.FishColor.YELLOW, false));
        score.breakStreak();
        assertEquals(50, score.onCatch(CatchAFishScore.FishColor.RED, false));
        assertEquals(14, score.getBestStreak());
        assertEquals(1880, score.getScore());
        assertEquals(2, score.caught(CatchAFishScore.FishColor.YELLOW));
    }

    @Test
    void reelBarsFillAsCaptured() {
        int[] red = {2, 7, 12, 16, 21, 26, 31, 36, 40, 45};
        int[] blue = {1, 5, 9, 12, 16, 20, 24, 27, 31, 35, 38, 42, 46};
        for (int i = 0; i < red.length; i++) assertEquals(red[i], CatchAFishScore.FishColor.RED.barFilled(i));
        for (int i = 0; i < blue.length; i++) assertEquals(blue[i], CatchAFishScore.FishColor.BLUE.barFilled(i));
        assertEquals(46, CatchAFishScore.FishColor.YELLOW.barFilled(15));
        assertTrue(CatchAFishScore.FishColor.YELLOW.isReeled(16));
        assertEquals("<c>", CatchAFishScore.FishColor.YELLOW.barColor(3));
        assertEquals("<e>", CatchAFishScore.FishColor.YELLOW.barColor(4));
        assertEquals("<e>", CatchAFishScore.FishColor.YELLOW.barColor(11));
        assertEquals("<a>", CatchAFishScore.FishColor.YELLOW.barColor(12));
        assertEquals("<c>|<8>" + "|".repeat(46), CatchAFishScore.FishColor.YELLOW.bar(0));
        assertEquals(0.5238f, CatchAFishScore.FishColor.YELLOW.hatPitch(0), 0.0005);
        assertEquals(1.5238f, CatchAFishScore.FishColor.YELLOW.hatPitch(16), 0.0005);
        assertEquals(0.6349f, CatchAFishScore.FishColor.RED.hatPitch(1), 0.0005);
    }

    @Test
    void lampsBoostZombiesAndBabiesDoublePoints() {
        ZombieShootoutScore score = new ZombieShootoutScore();
        assertEquals(60, score.onZombie(ZombieTier.LEATHER, true, 0));
        score.onLamp(10);
        assertEquals(144, score.onZombie(ZombieTier.DIAMOND, false, 20), 0.001);
        assertEquals(120, score.onZombie(ZombieTier.DIAMOND, false, 10 + ZombieShootoutScore.LAMP_BOOST_TICKS), 0.001);
        assertEquals(3, score.totalKills());
        assertEquals(1, score.result().metric(Metric.LAMPS));
    }

    @Test
    void profileSurvivesSerialization() {
        CarnivalProfile profile = new CarnivalProfile();
        profile.setTokens(420);
        profile.getPerkTiers().put(CarnivalPerk.RED_FAD, 2);
        profile.meet(CarnivalNpc.DOUG);
        profile.rollOver(7);
        profile.getCompletedGoals().add(CarnivalGoal.FISH_STREAK);
        profile.stats(CarnivalGame.CATCH_A_FISH).record(3_100);

        CarnivalProfile copy = CarnivalProfile.deserialize(profile.serialize());
        assertEquals(420, copy.getTokens());
        assertEquals(2, copy.tier(CarnivalPerk.RED_FAD));
        assertTrue(copy.hasMet(CarnivalNpc.DOUG));
        assertTrue(copy.getCompletedGoals().contains(CarnivalGoal.FISH_STREAK));
        assertEquals(3_100, copy.stats(CarnivalGame.CATCH_A_FISH).getLifetimeHighScore());

        copy.rollOver(8);
        assertTrue(copy.getCompletedGoals().isEmpty());
        assertEquals(0, copy.stats(CarnivalGame.CATCH_A_FISH).getYearAttempts());
        assertEquals(1, copy.stats(CarnivalGame.CATCH_A_FISH).getLifetimeAttempts());
    }

    private static FruitTile[][] copy(FruitTile[][] tiles) {
        FruitTile[][] copy = new FruitTile[tiles.length][];
        for (int i = 0; i < tiles.length; i++) copy[i] = tiles[i].clone();
        return copy;
    }

    private static FruitDiggingBoard board(FruitTile... firstRow) {
        FruitTile[][] tiles = new FruitTile[FruitDiggingBoard.SIZE][FruitDiggingBoard.SIZE];
        for (FruitTile[] column : tiles) Arrays.fill(column, FruitTile.RUM);
        for (int x = 0; x < firstRow.length; x++) tiles[x][0] = firstRow[x];
        return new FruitDiggingBoard(tiles, new Random(3));
    }
}
