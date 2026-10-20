package dev.daweapon.type.skyblockgeneric.hotf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhisperExperienceTest {
    @Test
    void oneExperiencePerTwoThousandFourHundredWhispersUpToTheLinearCap() {
        assertEquals(0, WhisperExperience.experience(0));
        assertEquals(1, WhisperExperience.experience(2_400), 1e-9);
        assertEquals(250, WhisperExperience.experience(600_000), 1e-9);
    }

    @Test
    void theTailApproachesTheSixHundredTwentyCap() {
        assertTrue(WhisperExperience.experience(1_200_000) > 250);
        assertTrue(WhisperExperience.experience(1_200_000) < 620);
        assertEquals(620, WhisperExperience.experience(12_500_000), 0.01);
        assertTrue(WhisperExperience.experience(2_000_000_000L) <= 620);
    }
}
