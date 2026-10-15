package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.CatacombsRegistry;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunConfig;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreBreakdown;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsScoringTest {
    private static CatacombsRunState wonRun() {
        CatacombsRunState state = CatacombsRunState.start(new CatacombsRunConfig(
                CatacombsRegistry.defaults().floor(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL),
                Map.of(UUID.randomUUID(), DungeonClassType.MAGE, UUID.randomUUID(), DungeonClassType.HEALER), 10, 10, true));
        for (int room = 0; room < 10; room++) state.markRoom(room, dev.daweapon.dungeons.catacombs.run.DungeonRoomState.COMPLETED);
        for (int secret = 0; secret < 10; secret++) state.recordSecret();
        return state.complete();
    }

    @Test
    void aPlayerWhoStayedGetsTheTeamScore() {
        CatacombsRunState state = wonRun();
        assertEquals(state.score().total(), CatacombsScoring.forPlayer(state, false).total());
    }

    @Test
    void aPlayerWhoLeftEarlyIsScoredWithTheLeavePenalty() {
        CatacombsRunState state = wonRun();
        DungeonScoreBreakdown stayed = CatacombsScoring.forPlayer(state, false);
        DungeonScoreBreakdown left = CatacombsScoring.forPlayer(state, true);
        assertTrue(left.leavePenalty() > 0);
        assertTrue(left.total() < stayed.total());
    }

    @Test
    void aFailedRunAlreadyCarriesThePenaltyForEveryone() {
        CatacombsRunState state = wonRun();
        CatacombsRunState failed = CatacombsRunState.start(state.config()).fail();
        assertEquals(CatacombsScoring.forPlayer(failed, false).total(), CatacombsScoring.forPlayer(failed, true).total());
    }
}
