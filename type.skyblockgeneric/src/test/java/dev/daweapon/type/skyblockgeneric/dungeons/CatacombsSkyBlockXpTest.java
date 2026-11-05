package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatacombsSkyBlockXpTest {
    @Test
    void levelUpPaysTwentyThenFortyForATotalOf1220() {
        assertEquals(0, CatacombsSkyBlockXp.levelUpXp(0));
        assertEquals(20, CatacombsSkyBlockXp.levelUpXp(1));
        assertEquals(20, CatacombsSkyBlockXp.levelUpXp(39));
        assertEquals(40, CatacombsSkyBlockXp.levelUpXp(40));
        assertEquals(40, CatacombsSkyBlockXp.levelUpXp(50));
        assertEquals(0, CatacombsSkyBlockXp.levelUpXp(51));
        assertEquals(780, CatacombsSkyBlockXp.levelUpTotal(39));
        assertEquals(1_220, CatacombsSkyBlockXp.levelUpTotal(50));
        assertEquals(1_220, CatacombsSkyBlockXp.levelUpTotal(70));
        assertEquals(SkyBlockXPSource.CATACOMBS_LEVEL_UP.getMaxXP(), CatacombsSkyBlockXp.levelUpTotal(50));
    }

    @Test
    void floorCompletionsPayOncePerFloor() {
        assertEquals(20, CatacombsSkyBlockXp.floorXp(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL));
        assertEquals(20, CatacombsSkyBlockXp.floorXp(CatacombsFloor.FLOOR_FOUR, CatacombsMode.NORMAL));
        assertEquals(30, CatacombsSkyBlockXp.floorXp(CatacombsFloor.FLOOR_FIVE, CatacombsMode.NORMAL));
        assertEquals(30, CatacombsSkyBlockXp.floorXp(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL));
        assertEquals(50, CatacombsSkyBlockXp.floorXp(CatacombsFloor.FLOOR_ONE, CatacombsMode.MASTER));
        assertEquals(0, CatacombsSkyBlockXp.floorXp(CatacombsFloor.ENTRANCE, CatacombsMode.MASTER));

        CatacombsCompletions completions = CatacombsCompletions.parse("")
                .with(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL)
                .with(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL)
                .with(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL);
        assertEquals(40, CatacombsSkyBlockXp.completionTotal(completions));
    }

    @Test
    void everyFloorCompletedPaysTheFull540() {
        CatacombsCompletions all = CatacombsCompletions.parse("NORMAL:FLOOR_ONE=12").withEveryFloorCompleted();
        assertEquals(12, all.count(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL));
        assertEquals(1, all.count(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.MASTER));
        assertEquals(0, all.count(CatacombsFloor.ENTRANCE, CatacombsMode.MASTER));
        assertEquals(540, CatacombsSkyBlockXp.completionTotal(all));
        assertEquals(SkyBlockXPSource.COMPLETE_DUNGEONS.getMaxXP(), CatacombsSkyBlockXp.completionTotal(all));
    }

    @Test
    void syncAwardsBothTasksAndIsIdempotent() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = new DatapointSkyBlockExperience.PlayerSkyBlockExperience();
        CatacombsCompletions completions = CatacombsCompletions.parse("").with(CatacombsFloor.FLOOR_FIVE, CatacombsMode.NORMAL);

        CatacombsSkyBlockXp.sync(experience, 41, completions);
        CatacombsSkyBlockXp.sync(experience, 41, completions);

        assertEquals(39 * 20 + 2 * 40, experience.getSourceXP(SkyBlockXPSource.CATACOMBS_LEVEL_UP));
        assertEquals(30, experience.getSourceXP(SkyBlockXPSource.COMPLETE_DUNGEONS));
        assertEquals(890, experience.getEarnedXP());
    }

    @Test
    void theMaximumSkyBlockLevelIsUnchanged() {
        assertEquals(57_877, SkyBlockXPSource.TOTAL_MAX_XP);
    }

    @Test
    void completedFloorsCountEitherMode() {
        CatacombsCompletions master = CatacombsCompletions.parse("MASTER:FLOOR_SEVEN=1");
        assertEquals(true, master.hasCompleted(CatacombsFloor.FLOOR_SEVEN));
        assertEquals(false, master.hasCompleted(CatacombsFloor.FLOOR_SIX));
    }
}
