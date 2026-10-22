package dev.daweapon.type.skyblockgeneric.foraging;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeMilestoneRewardsTest {
    @Test
    void sweepGrowsByTreeTypePerTier() {
        assertEquals(7, TreeMilestoneRewards.sweep("FIG", 7));
        assertEquals(14, TreeMilestoneRewards.sweep("MANGROVE", 7));
        assertEquals(21, TreeMilestoneRewards.sweep("HELIX", 7));
        assertEquals(0, TreeMilestoneRewards.sweep("FIG", 0));
        assertEquals(21, TreeMilestoneRewards.sweep("HELIX", 99));
        assertEquals(0, TreeMilestoneRewards.sweep("OAK", 3));
    }

    @Test
    void foragingExperienceFollowsTheWikiTables() {
        assertEquals(2_000, TreeMilestoneRewards.foragingXp("FIG", 1));
        assertEquals(50_000, TreeMilestoneRewards.foragingXp("FIG", 7));
        assertEquals(100_000, TreeMilestoneRewards.foragingXp("MANGROVE", 7));
        assertEquals(5_000, TreeMilestoneRewards.foragingXp("HELIX", 1));
        assertEquals(0, TreeMilestoneRewards.foragingXp("FIG", 8));
        assertEquals(0, TreeMilestoneRewards.foragingXp("FIG", 0));
    }

    @Test
    void cumulativeFigExperienceMatchesTheWiki() {
        long total = 0;
        for (int tier = 1; tier <= 7; tier++) total += TreeMilestoneRewards.foragingXp("FIG", tier);
        assertEquals(112_000, total);
    }

    @Test
    void totalSweepSumsEveryTree() {
        assertEquals(1 + 2 + 3, TreeMilestoneRewards.totalSweep(Map.of("FIG", 10, "MANGROVE", 10, "HELIX", 10)));
        assertEquals(42, TreeMilestoneRewards.totalSweep(Map.of("FIG", 2500, "MANGROVE", 2500, "HELIX", 2500)));
        assertEquals(0, TreeMilestoneRewards.totalSweep(Map.of()));
    }
}
