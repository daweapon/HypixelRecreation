package dev.daweapon.type.theend.npcs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoneAdventurerQuestTest {
    @Test
    void firstStandCoversStagesBeforeTheEnderArmorQuest() {
        assertTrue(LoneAdventurerStage.NOT_STARTED.atFirstStand());
        assertTrue(LoneAdventurerStage.KILLING_ENDERMEN.atFirstStand());
        assertFalse(LoneAdventurerStage.ENDER_ARMOR.atFirstStand());
        assertFalse(LoneAdventurerStage.GUIDE_READY.atFirstStand());
    }

    @Test
    void unknownOrdinalsClampToValidStages() {
        assertEquals(LoneAdventurerStage.NOT_STARTED, LoneAdventurerStage.fromOrdinal(-3));
        assertEquals(LoneAdventurerStage.GUIDE_READY, LoneAdventurerStage.fromOrdinal(99));
        assertEquals(LoneAdventurerStage.SHORTBOW, LoneAdventurerStage.fromOrdinal(LoneAdventurerStage.SHORTBOW.ordinal()));
    }

    @Test
    void guideNeedsCombatLevelAndOneSkyBlockDay() {
        long day = LoneAdventurerStage.SKYBLOCK_DAY_MILLIS;
        assertFalse(LoneAdventurerStage.GUIDE_WAIT.guideUnlocked(17, day));
        assertFalse(LoneAdventurerStage.GUIDE_WAIT.guideUnlocked(18, day - 1));
        assertTrue(LoneAdventurerStage.GUIDE_WAIT.guideUnlocked(18, day));
        assertFalse(LoneAdventurerStage.SHORTBOW.guideUnlocked(60, day * 5));
    }

    @Test
    void enderArmorReportsEveryMissingPiece() {
        assertEquals(8, EnderArmorSet.missing(List.of(), List.of()).size());
        assertTrue(EnderArmorSet.missing(EnderArmorSet.ARMOR, EnderArmorSet.ACCESSORIES).isEmpty());
        assertEquals(List.of("ENDER_GAUNTLET"),
                EnderArmorSet.missing(EnderArmorSet.ARMOR, List.of("ENDER_CLOAK", "ENDER_BELT", "ENDER_NECKLACE")));
    }
}
