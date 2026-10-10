package net.swofty.type.generic.achievement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementProgressTest {
    private static final AchievementDefinition TIERED = AchievementDefinition.builder()
            .id("test.skill")
            .name("Skill")
            .description("Reach Level %%value%%")
            .type(AchievementType.TIERED)
            .category(AchievementCategory.SKYBLOCK)
            .tiers(List.of(new AchievementTier(1, 5, 5), new AchievementTier(2, 10, 5), new AchievementTier(3, 15, 10)))
            .build();

    @Test
    void raisingProgressUnlocksEveryCrossedTier() {
        AchievementData.AchievementProgress progress = new AchievementData.AchievementProgress();
        assertEquals(2, progress.raiseProgress(TIERED, 12));
        assertEquals(2, progress.getCurrentTier());
        assertEquals(0, progress.raiseProgress(TIERED, 8));
        assertEquals(12, progress.getCurrentProgress());
        assertEquals(1, progress.raiseProgress(TIERED, 15));
    }

    @Test
    void addingProgressAccumulates() {
        AchievementData.AchievementProgress progress = new AchievementData.AchievementProgress();
        assertEquals(0, progress.addProgress(TIERED, 4));
        assertEquals(1, progress.addProgress(TIERED, 1));
    }

    @Test
    void tierDescriptionUsesThatTiersGoal() {
        assertEquals("Reach Level 5", TIERED.getDescriptionForTier(1));
        assertEquals("Reach Level 15", TIERED.getDescriptionForTier(3));
    }

    @Test
    void coopPrefixIsDetected() {
        AchievementDefinition coop = AchievementDefinition.builder().id("test.coop").name("Coop")
                .description("[Co-op or you] Craft a saddle.").type(AchievementType.CHALLENGE)
                .category(AchievementCategory.SKYBLOCK).points(5).goal(1).build();
        assertTrue(coop.isCoop());
    }
}
