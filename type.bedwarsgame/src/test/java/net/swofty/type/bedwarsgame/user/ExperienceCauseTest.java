package net.swofty.type.bedwarsgame.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceCauseTest {
    @Test
    void calculateXpMultipliesByUnitsAndTreatsZeroOrOneAsSingle() {
        assertEquals(10, ExperienceCause.FINAL_KILL.calculateXp(0));
        assertEquals(10, ExperienceCause.FINAL_KILL.calculateXp(1));
        assertEquals(30, ExperienceCause.EMERALDS.calculateXp(10));
        assertEquals(45, ExperienceCause.TIME_PLAYED.calculateXp(3));
    }

    @Test
    void onlyPlaytimeIsNotBoostable() {
        for (ExperienceCause cause : ExperienceCause.values()) {
            assertEquals(cause != ExperienceCause.TIME_PLAYED, cause.isBoostable(), cause.name());
        }
    }

    @Test
    void formattedNameTitleCasesWords() {
        assertEquals("Solo Doubles Win", ExperienceCause.SOLO_DOUBLES_WIN.getFormattedName());
        assertEquals("Bed Break", ExperienceCause.BED_BREAK.getFormattedName());
        assertEquals("Emeralds", ExperienceCause.EMERALDS.getFormattedName());
    }
}
