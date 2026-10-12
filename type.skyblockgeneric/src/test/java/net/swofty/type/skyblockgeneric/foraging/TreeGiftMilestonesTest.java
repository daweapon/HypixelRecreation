package net.swofty.type.skyblockgeneric.foraging;

import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TreeGiftMilestonesTest {
    @Test
    void tiersFollowGiftThresholds() {
        assertEquals(0, TreeGiftMilestones.milestones(9));
        assertEquals(1, TreeGiftMilestones.milestones(10));
        assertEquals(2, TreeGiftMilestones.milestones(99));
        assertEquals(7, TreeGiftMilestones.milestones(5000));
    }

    @Test
    void maxedTreesReachTheSourceCap() {
        Map<String, Integer> maxed = Map.of("FIG", 2500, "MANGROVE", 2500, "HELIX", 2500);
        assertEquals(SkyBlockXPSource.TREE_GIFT_MILESTONES.getMaxXP(), TreeGiftMilestones.skyBlockXp(maxed));
        assertEquals(0, TreeGiftMilestones.skyBlockXp(Map.of()));
    }
}
