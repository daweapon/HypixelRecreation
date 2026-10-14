package net.swofty.dungeons.catacombs.run;

import net.swofty.dungeons.catacombs.CatacombsFloor;
import net.swofty.dungeons.catacombs.CatacombsMode;
import net.swofty.dungeons.catacombs.CatacombsRegistry;
import net.swofty.dungeons.catacombs.blessing.BlessingType;
import net.swofty.dungeons.catacombs.classes.DungeonClassType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatacombsRunStateTest {
    private static CatacombsRunState newRun(int rooms, int secrets) {
        var floor = CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL);
        Map<UUID, DungeonClassType> party = Map.of(
                UUID.randomUUID(), DungeonClassType.MAGE,
                UUID.randomUUID(), DungeonClassType.TANK);
        return CatacombsRunState.start(new CatacombsRunConfig(floor, party, rooms, secrets));
    }

    @Test
    void freshRunScoresFullSkillAndSpeedWithNoExploration() {
        DungeonScoreBreakdown score = newRun(10, 10).score();
        assertEquals(100, score.skill());
        assertEquals(0, score.exploration());
        assertEquals(100, score.speed());
        assertEquals(0, score.bonus());
    }

    @Test
    void deathsAndFailedPuzzlesReduceSkillWithFloorOfTwenty() {
        CatacombsRunState run = newRun(10, 10);
        run.recordDeath(false).recordDeath(false);
        assertEquals(96, run.score().skill());
        run.failPuzzle(1).failPuzzle(2);
        assertEquals(76, run.score().skill());
        for (int i = 0; i < 40; i++) run.recordDeath(false);
        assertEquals(20, run.score().skill());
    }

    @Test
    void spiritPetReducesFirstDeathPenaltyByOne() {
        CatacombsRunState run = newRun(10, 10);
        run.recordDeath(true);
        assertEquals(99, run.score().skill());
    }

    @Test
    void explorationWeighsRoomsSixtyAndSecretsForty() {
        CatacombsRunState run = newRun(10, 10);
        for (int i = 0; i < 5; i++) run.markRoom(i, DungeonRoomState.COMPLETED);
        assertEquals(30, run.score().exploration());
        for (int i = 0; i < 5; i++) run.recordSecret();
        assertEquals(50, run.score().exploration());
    }

    @Test
    void completingSameRoomTwiceCountsOnce() {
        CatacombsRunState run = newRun(10, 10);
        run.markRoom(3, DungeonRoomState.COMPLETED).markRoom(3, DungeonRoomState.COMPLETED);
        assertEquals(1, run.completedRooms());
    }

    @Test
    void failingPuzzleMarksRoomFailed() {
        CatacombsRunState run = newRun(10, 10);
        run.failPuzzle(4);
        assertEquals(DungeonRoomState.FAILED, run.rooms().get(4));
        assertEquals(1, run.failedPuzzles());
    }

    @Test
    void bonusCapsCryptsAtFiveAndAddsFiveForAllSecrets() {
        CatacombsRunState run = newRun(10, 2);
        for (int i = 0; i < 9; i++) run.recordCrypt();
        assertEquals(5, run.score().bonus());
        run.recordSecret().recordSecret();
        assertEquals(10, run.score().bonus());
    }

    @Test
    void progressionTraceFollowsPhases() {
        CatacombsRunState run = newRun(10, 10).enterBloodRoom();
        Map<DungeonRunPhase, Boolean> trace = run.progressionTrace();
        assertTrue(trace.get(DungeonRunPhase.START_ROOM));
        assertTrue(trace.get(DungeonRunPhase.BLOOD_ROOM));
        assertFalse(trace.get(DungeonRunPhase.BOSS));
    }

    @Test
    void failedRunHasReachedEverythingButCompleted() {
        Map<DungeonRunPhase, Boolean> trace = newRun(10, 10).fail().progressionTrace();
        assertFalse(trace.get(DungeonRunPhase.COMPLETED));
        assertTrue(trace.get(DungeonRunPhase.BOSS));
    }

    @Test
    void blessingsStackAndInvalidPartySizeIsRejected() {
        CatacombsRunState run = newRun(10, 10).addBlessing(BlessingType.values()[0], 2)
                .addBlessing(BlessingType.values()[0], 3);
        assertEquals(5, run.blessings().level(BlessingType.values()[0]));
        var floor = CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL);
        assertThrows(IllegalArgumentException.class,
                () -> new CatacombsRunConfig(floor, Map.of(UUID.randomUUID(), DungeonClassType.ARCHER), 5, 5));
    }

    @Test
    void scoreRankBoundaries() {
        assertEquals(DungeonScoreRank.S_PLUS, DungeonScoreRank.fromScore(300));
        assertEquals(DungeonScoreRank.S, DungeonScoreRank.fromScore(299));
        assertEquals(DungeonScoreRank.A, DungeonScoreRank.fromScore(230));
        assertEquals(DungeonScoreRank.D, DungeonScoreRank.fromScore(40));
        assertEquals(DungeonScoreRank.NONE, DungeonScoreRank.fromScore(39));
    }
}
