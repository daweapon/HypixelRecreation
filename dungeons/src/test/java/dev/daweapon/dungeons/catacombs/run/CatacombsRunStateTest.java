package dev.daweapon.dungeons.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.CatacombsRegistry;
import dev.daweapon.dungeons.catacombs.blessing.BlessingType;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
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
        assertEquals(68, run.score().skill());
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
    void bonusCapsCryptsAtFiveAndAddsMimicPaulAndAttributes() {
        CatacombsRunState run = newRun(10, 2);
        for (int i = 0; i < 9; i++) run.recordCrypt();
        assertEquals(5, run.score().bonus());
        run.recordMimicKilled().applyPaulBonus().triggerPrinceAttribute().triggerBatAttribute();
        assertEquals(19, run.score().bonus());
    }

    @Test
    void elapsedTimeFollowsInjectedClock() {
        MutableClock clock = new MutableClock();
        var floor = CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL);
        Map<UUID, DungeonClassType> party = Map.of(
                UUID.randomUUID(), DungeonClassType.MAGE,
                UUID.randomUUID(), DungeonClassType.TANK);
        CatacombsRunState run = CatacombsRunState.start(new CatacombsRunConfig(floor, party, 10, 10), clock);
        clock.advance(700);
        assertEquals(700, run.elapsed().toSeconds());
        assertEquals(92, run.score().speed());
        run.complete();
        clock.advance(500);
        assertEquals(700, run.elapsed().toSeconds());
    }

    @Test
    void failedRunLosesThirtyPercentAndSpeedBecomesRoomPercentage() {
        CatacombsRunState run = newRun(10, 10);
        for (int i = 0; i < 5; i++) run.markRoom(i, DungeonRoomState.COMPLETED);
        run.fail();
        DungeonScoreBreakdown score = run.score();
        assertEquals(50, score.speed());
        assertEquals(30, score.exploration());
        assertEquals(180, score.skill() + score.exploration() + score.speed() + score.bonus());
        assertEquals(126, score.total());
    }

    @Test
    void soloRunNeedsSaulsRecommendation() {
        var floor = CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL);
        Map<UUID, DungeonClassType> solo = Map.of(UUID.randomUUID(), DungeonClassType.ARCHER);
        assertDoesNotThrow(() -> new CatacombsRunConfig(floor, solo, 5, 5, true));
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
        assertEquals(DungeonScoreRank.D, DungeonScoreRank.fromScore(0));
    }

    private static final class MutableClock extends java.time.Clock {
        private java.time.Instant now = java.time.Instant.parse("2026-01-01T00:00:00Z");

        void advance(long seconds) {
            now = now.plusSeconds(seconds);
        }

        @Override
        public java.time.ZoneId getZone() {
            return java.time.ZoneOffset.UTC;
        }

        @Override
        public java.time.Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public java.time.Instant instant() {
            return now;
        }
    }
}
