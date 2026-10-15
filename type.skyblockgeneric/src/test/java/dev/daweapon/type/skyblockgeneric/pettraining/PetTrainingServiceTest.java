package dev.daweapon.type.skyblockgeneric.pettraining;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetTraining.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PetTrainingServiceTest {
    private static final long DAY = PetTrainingService.DAY_MILLIS;

    @Test
    void freeTrainingScalesWithTamingBetweenWikiBounds() {
        assertEquals(20_000, PetTrainingType.FREE.xpPerDay(10));
        assertEquals(50_000, PetTrainingType.FREE.xpPerDay(60));
        assertEquals(50_000, PetTrainingType.FREE.xpPerDay(70));
    }

    @Test
    void paidTrainingUsesTheWikiRate() {
        assertEquals(250_000, PetTrainingType.LIGHT.xpPerDay(60));
        assertEquals(2_500_000, PetTrainingType.TURBO.xpPerDay(60));
    }

    @Test
    void trainingTypesUnlockEveryTenTamingLevels() {
        assertTrue(PetTrainingType.MODERATE.isUnlocked(30));
        assertFalse(PetTrainingType.MODERATE.isUnlocked(29));
    }

    @Test
    void earnedExperienceStopsAtTheEndOfTheSession() {
        Session session = new Session(null, PetTrainingType.LIGHT, 0, 5 * DAY, 250_000, 200_000, 0);
        assertEquals(125_000, PetTrainingService.xpEarned(session, DAY / 2));
        assertEquals(1_250_000, PetTrainingService.xpEarned(session, 10 * DAY));
        assertTrue(PetTrainingService.isFinished(session, 5 * DAY));
    }
}
