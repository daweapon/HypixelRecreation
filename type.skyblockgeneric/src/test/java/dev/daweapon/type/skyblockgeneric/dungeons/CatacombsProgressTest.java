package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsProgressTest {
    @Test
    void levelsFollowTheCatacombsXpTable() {
        assertEquals(0, CatacombsProgress.level(0));
        assertEquals(0, CatacombsProgress.level(49));
        assertEquals(1, CatacombsProgress.level(50));
        assertEquals(5, CatacombsProgress.level(625));
        assertEquals(50, CatacombsProgress.level(CatacombsLevels.xpForLevel(50)));
        assertEquals(50, CatacombsProgress.level(Long.MAX_VALUE));
    }

    @Test
    void floorBaseXpIsThePlaceholderTable() {
        assertEquals(40, CatacombsRunXp.base(CatacombsFloor.ENTRANCE));
        assertEquals(80, CatacombsRunXp.base(CatacombsFloor.FLOOR_ONE));
        assertEquals(0, CatacombsRunXp.base(CatacombsFloor.FLOOR_SEVEN));
    }

    @Test
    void theDailyBonusAddsFortyPercent() {
        assertEquals(112, CatacombsRunXp.withBonus(80, 40));
        assertEquals(56, CatacombsRunXp.withBonus(40, 40));
        assertEquals(80, CatacombsRunXp.withBonus(80, 0));
    }

    @Test
    void theFirstFiveRunsOfADayGetTheBonus() {
        CatacombsDailyBonus state = CatacombsDailyBonus.NONE;
        for (int run = 0; run < 5; run++) {
            assertEquals(40, state.bonusPercent(100), "run " + (run + 1));
            state = state.afterRun(100);
        }
        assertEquals(0, state.bonusPercent(100));
        assertEquals(5, state.runsOn(100));
    }

    @Test
    void aNewDayResetsTheBonus() {
        CatacombsDailyBonus state = new CatacombsDailyBonus(100, 7);
        assertEquals(0, state.bonusPercent(100));
        assertEquals(40, state.bonusPercent(101));
        assertEquals(new CatacombsDailyBonus(101, 1), state.afterRun(101));
    }

    @Test
    void dailyStateRoundTripsAndToleratesGarbage() {
        CatacombsDailyBonus state = new CatacombsDailyBonus(19_000, 3);
        assertEquals(state, CatacombsDailyBonus.parse(state.encode()));
        assertEquals(CatacombsDailyBonus.NONE, CatacombsDailyBonus.parse(""));
        assertEquals(CatacombsDailyBonus.NONE, CatacombsDailyBonus.parse(null));
        assertEquals(CatacombsDailyBonus.NONE, CatacombsDailyBonus.parse("x:y"));
    }

    @Test
    void completionsCountPerFloorAndMode() {
        CatacombsCompletions completions = CatacombsCompletions.parse("")
                .with(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL)
                .with(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL)
                .with(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL);
        CatacombsCompletions reloaded = CatacombsCompletions.parse(completions.encode());
        assertEquals(2, reloaded.count(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertEquals(1, reloaded.count(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL));
        assertEquals(0, reloaded.count(CatacombsFloor.FLOOR_ONE, CatacombsMode.MASTER));
        assertEquals(3, reloaded.total());
        assertEquals(0, CatacombsCompletions.parse("broken,=,a=b").total());
    }

    @Test
    void chatLinesNameTheXpAndTheLevels() {
        assertEquals("<3>+<b>112 Catacombs Experience <8>(Floor I, S+) <8>(+40% daily bonus)",
                CatacombsProgress.runXpLine(CatacombsFloor.FLOOR_ONE, DungeonScoreRank.S_PLUS, 112, 40));
        assertTrue(CatacombsProgress.levelUpLines(4, 5).get(1).contains("4➜<3>5"));
    }
}
