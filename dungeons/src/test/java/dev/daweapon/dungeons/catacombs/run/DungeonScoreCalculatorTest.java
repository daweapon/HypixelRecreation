package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DungeonScoreCalculatorTest {
    private static DungeonScoreInput input(CatacombsFloor floor, CatacombsMode mode, long seconds) {
        return new DungeonScoreInput(floor, mode, 0, false, 0, 10, 10, 10, 10, seconds, 0, false, false, false, false, true);
    }

    private static DungeonScoreInput withDeaths(int deaths, boolean spirit, int failedPuzzles) {
        return new DungeonScoreInput(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, deaths, spirit, failedPuzzles,
                10, 10, 10, 10, 0, 0, false, false, false, false, true);
    }

    private static DungeonScoreInput withSecrets(CatacombsFloor floor, CatacombsMode mode, int found, int total) {
        return new DungeonScoreInput(floor, mode, 0, false, 0, 0, 10, found, total, 0, 0, false, false, false, false, true);
    }

    @Test
    void floorOneRunOfSevenHundredSecondsScoresNinetyTwoSpeed() {
        assertEquals(92, DungeonScoreCalculator.speed(input(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 700)));
    }

    @Test
    void speedIsHundredBelowFourHundredEightyAdjustedSeconds() {
        assertEquals(100, DungeonScoreCalculator.speed(input(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 599)));
        assertEquals(100, DungeonScoreCalculator.speed(input(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 0)));
    }

    @Test
    void speedPiecewiseBands() {
        assertEquals(100, DungeonScoreCalculator.speedForAdjustedSeconds(479));
        assertEquals(100, DungeonScoreCalculator.speedForAdjustedSeconds(480));
        assertEquals(91, DungeonScoreCalculator.speedForAdjustedSeconds(599));
        assertEquals(90, DungeonScoreCalculator.speedForAdjustedSeconds(600));
        assertEquals(80, DungeonScoreCalculator.speedForAdjustedSeconds(840));
        assertEquals(81, DungeonScoreCalculator.speedForAdjustedSeconds(839));
        assertEquals(70, DungeonScoreCalculator.speedForAdjustedSeconds(1140));
        assertEquals(1, DungeonScoreCalculator.speedForAdjustedSeconds(3900));
        assertEquals(0, DungeonScoreCalculator.speedForAdjustedSeconds(3940));
        assertEquals(0, DungeonScoreCalculator.speedForAdjustedSeconds(10_000));
    }

    @Test
    void timeOffsetsPerFloorAndMode() {
        assertEquals(120, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_FIVE, CatacombsMode.NORMAL));
        assertEquals(240, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_FOUR, CatacombsMode.NORMAL));
        assertEquals(240, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_SIX, CatacombsMode.NORMAL));
        assertEquals(360, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL));
        assertEquals(0, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_THREE, CatacombsMode.MASTER));
        assertEquals(120, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_SIX, CatacombsMode.MASTER));
        assertEquals(360, DungeonScoreTables.timeOffsetSeconds(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER));
    }

    @Test
    void timeLimitsPerFloorAndMode() {
        assertEquals(600, DungeonScoreTables.timeLimitSeconds(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertEquals(720, DungeonScoreTables.timeLimitSeconds(CatacombsFloor.FLOOR_FOUR, CatacombsMode.NORMAL));
        assertEquals(840, DungeonScoreTables.timeLimitSeconds(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL));
        assertEquals(480, DungeonScoreTables.timeLimitSeconds(CatacombsFloor.FLOOR_ONE, CatacombsMode.MASTER));
        assertEquals(600, DungeonScoreTables.timeLimitSeconds(CatacombsFloor.FLOOR_SIX, CatacombsMode.MASTER));
        assertEquals(840, DungeonScoreTables.timeLimitSeconds(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER));
        assertThrows(IllegalArgumentException.class,
                () -> DungeonScoreTables.timeLimitSeconds(CatacombsFloor.ENTRANCE, CatacombsMode.MASTER));
    }

    @Test
    void secretRequirementPerFloor() {
        int[] expected = {30, 30, 40, 50, 60, 70, 85, 100};
        for (CatacombsFloor floor : CatacombsFloor.values()) {
            assertEquals(expected[floor.ordinal()],
                    DungeonScoreTables.secretRequirementPercent(floor, CatacombsMode.NORMAL));
        }
        assertEquals(100, DungeonScoreTables.secretRequirementPercent(CatacombsFloor.FLOOR_ONE, CatacombsMode.MASTER));
    }

    @Test
    void skillSubtractsTwoPerDeathAndFourteenPerFailedPuzzle() {
        assertEquals(100, DungeonScoreCalculator.skill(withDeaths(0, false, 0)));
        assertEquals(96, DungeonScoreCalculator.skill(withDeaths(2, false, 0)));
        assertEquals(86, DungeonScoreCalculator.skill(withDeaths(0, false, 1)));
        assertEquals(70, DungeonScoreCalculator.skill(withDeaths(1, false, 2)));
        assertEquals(99, DungeonScoreCalculator.skill(withDeaths(1, true, 0)));
        assertEquals(100, DungeonScoreCalculator.skill(withDeaths(0, true, 0)));
        assertEquals(20, DungeonScoreCalculator.skill(withDeaths(50, false, 0)));
    }

    @Test
    void explorationSecretsAreScaledByFloorRequirement() {
        assertEquals(40, DungeonScoreCalculator.exploration(withSecrets(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 3, 10)));
        assertEquals(20, DungeonScoreCalculator.exploration(withSecrets(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 3, 20)));
        assertEquals(40, DungeonScoreCalculator.exploration(withSecrets(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 10, 10)));
        assertEquals(10, DungeonScoreCalculator.exploration(withSecrets(CatacombsFloor.FLOOR_THREE, CatacombsMode.NORMAL, 2, 15)));
        assertEquals(20, DungeonScoreCalculator.exploration(withSecrets(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, 5, 10)));
    }

    @Test
    void explorationRoomsFloorSixtyTimesRatio() {
        DungeonScoreInput run = new DungeonScoreInput(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 0, false, 0,
                7, 9, 0, 10, 0, 0, false, false, false, false, true);
        assertEquals(46, DungeonScoreCalculator.exploration(run));
    }

    @Test
    void bonusComponentsAndMimicOnlyFromFloorSix() {
        DungeonScoreInput base = input(CatacombsFloor.FLOOR_FIVE, CatacombsMode.NORMAL, 0);
        DungeonScoreInput all = new DungeonScoreInput(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, 0, false, 0,
                0, 10, 0, 10, 0, 8, true, true, true, true, true);
        assertEquals(0, DungeonScoreCalculator.bonus(base));
        assertEquals(19, DungeonScoreCalculator.bonus(all));
        DungeonScoreInput mimicOnFloorFive = new DungeonScoreInput(CatacombsFloor.FLOOR_FIVE, CatacombsMode.NORMAL, 0,
                false, 0, 0, 10, 0, 10, 0, 0, true, false, false, false, true);
        assertEquals(0, DungeonScoreCalculator.bonus(mimicOnFloorFive));
    }

    @Test
    void totalIsCappedAtThreeHundred() {
        DungeonScoreInput perfect = new DungeonScoreInput(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, 0, false, 0,
                10, 10, 10, 10, 0, 5, true, true, true, true, true);
        DungeonScoreBreakdown score = DungeonScoreCalculator.calculate(perfect);
        assertEquals(300, score.total());
        assertEquals(DungeonScoreRank.S_PLUS, score.rank());
    }

    @Test
    void leavingBeforeBossDiesCutsThirtyPercentAndSpeedIsRoomPercent() {
        DungeonScoreInput left = new DungeonScoreInput(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 0, false, 0,
                5, 10, 0, 10, 0, 0, false, false, false, false, false);
        DungeonScoreBreakdown score = DungeonScoreCalculator.calculate(left);
        assertEquals(50, score.speed());
        assertEquals(180, score.skill() + score.exploration() + score.speed() + score.bonus());
        assertEquals(54, score.leavePenalty());
        assertEquals(126, score.total());
    }

    @Test
    void rankBoundaries() {
        assertEquals(DungeonScoreRank.D, DungeonScoreRank.fromScore(99));
        assertEquals(DungeonScoreRank.C, DungeonScoreRank.fromScore(100));
        assertEquals(DungeonScoreRank.C, DungeonScoreRank.fromScore(159));
        assertEquals(DungeonScoreRank.B, DungeonScoreRank.fromScore(160));
        assertEquals(DungeonScoreRank.B, DungeonScoreRank.fromScore(229));
        assertEquals(DungeonScoreRank.A, DungeonScoreRank.fromScore(230));
        assertEquals(DungeonScoreRank.A, DungeonScoreRank.fromScore(269));
        assertEquals(DungeonScoreRank.A, DungeonScoreRank.fromScore(269.4));
        assertEquals(DungeonScoreRank.S, DungeonScoreRank.fromScore(269.5));
        assertEquals(DungeonScoreRank.S, DungeonScoreRank.fromScore(270));
        assertEquals(DungeonScoreRank.S, DungeonScoreRank.fromScore(299));
        assertEquals(DungeonScoreRank.S_PLUS, DungeonScoreRank.fromScore(300));
    }

    @Test
    void runRulesUseWikiLimits() {
        DungeonRunRules rules = DungeonRunRules.of(CatacombsFloor.FLOOR_FOUR, CatacombsMode.NORMAL);
        assertEquals(3600, rules.maximumDurationSeconds());
        assertTrue(rules.allowsPartySize(2));
        assertTrue(rules.allowsPartySize(5));
        assertFalse(rules.allowsPartySize(1));
        assertFalse(rules.allowsPartySize(6));
        assertTrue(rules.allowsPartySize(1, true));
        assertTrue(rules.automaticGhostRevive());
        assertFalse(DungeonRunRules.of(CatacombsFloor.FLOOR_FOUR, CatacombsMode.MASTER).automaticGhostRevive());
        assertFalse(DungeonRunRules.of(CatacombsFloor.FLOOR_TWO, CatacombsMode.NORMAL).trapRoomsEnabled());
        assertTrue(DungeonRunRules.of(CatacombsFloor.FLOOR_THREE, CatacombsMode.NORMAL).trapRoomsEnabled());
    }
}
