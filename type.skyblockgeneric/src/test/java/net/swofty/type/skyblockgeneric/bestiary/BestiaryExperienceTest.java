package net.swofty.type.skyblockgeneric.bestiary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BestiaryExperienceTest {
    @Test
    void everyTierIsOneXpAndEveryHundredTiersAddTen() {
        assertEquals(0, BestiaryExperience.skyBlockXp(0));
        assertEquals(99, BestiaryExperience.skyBlockXp(99));
        assertEquals(110, BestiaryExperience.skyBlockXp(100));
        assertEquals(1_120, BestiaryExperience.skyBlockXp(1_020));
    }

    @Test
    void wikiCapIsReachedAtItsTierTotal() {
        assertEquals(5_635, BestiaryExperience.skyBlockXp(5_125));
        assertEquals(5_655, BestiaryExperience.skyBlockXp(5_145));
    }
}
