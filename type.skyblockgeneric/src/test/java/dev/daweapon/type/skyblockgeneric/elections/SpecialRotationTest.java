package dev.daweapon.type.skyblockgeneric.elections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialRotationTest {
    @Test
    void specialCandidateFollowsTheWikiYears() {
        assertEquals(SkyBlockMayor.SCORPIUS, SkyBlockMayor.specialCandidateFor(96));
        assertEquals(SkyBlockMayor.DERPY, SkyBlockMayor.specialCandidateFor(104));
        assertEquals(SkyBlockMayor.JERRY, SkyBlockMayor.specialCandidateFor(112));
        assertEquals(SkyBlockMayor.SCORPIUS, SkyBlockMayor.specialCandidateFor(504));
        assertEquals(SkyBlockMayor.DERPY, SkyBlockMayor.specialCandidateFor(512));
        assertEquals(SkyBlockMayor.JERRY, SkyBlockMayor.specialCandidateFor(496));
    }
}
