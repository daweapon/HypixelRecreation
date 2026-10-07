package net.swofty.type.skyblockgeneric.data;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Covers the XP arithmetic only; anything touching getLevel() needs the level YAML on disk.
class DatapointSkyBlockExperienceTest {

    private DatapointSkyBlockExperience.PlayerSkyBlockExperience experience() {
        return new DatapointSkyBlockExperience.PlayerSkyBlockExperience();
    }

    @Test
    void awardsSourceXPUpToThatSourcesMaximum() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        experience.addSourceXP(SkyBlockXPSource.CATACOMBS_LEVEL_UP, 500);
        assertEquals(500, experience.getSourceXP(SkyBlockXPSource.CATACOMBS_LEVEL_UP));

        experience.addSourceXP(SkyBlockXPSource.CATACOMBS_LEVEL_UP, 10_000);
        assertEquals(SkyBlockXPSource.CATACOMBS_LEVEL_UP.getMaxXP(),
                experience.getSourceXP(SkyBlockXPSource.CATACOMBS_LEVEL_UP));
    }

    @Test
    void refusesSourceXPForSourcesBackedByTheirOwnCauses() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        experience.addSourceXP(SkyBlockXPSource.SKILL_LEVEL_UP, 400);

        assertEquals(0, experience.getSourceXP(SkyBlockXPSource.SKILL_LEVEL_UP));
        assertEquals(0, experience.getTotalXP());
    }

    @Test
    void clampsNegativeSourceXPToZero() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        experience.setSourceXP(SkyBlockXPSource.THE_DOJO, -50);

        assertEquals(0, experience.getSourceXP(SkyBlockXPSource.THE_DOJO));
        assertTrue(experience.getSourceProgress().isEmpty());
    }

    @Test
    void bonusXPAddsAndSubtracts() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        experience.addBonusXP(1000);
        assertEquals(1000, experience.getTotalXP());

        experience.addBonusXP(-250);
        assertEquals(750, experience.getTotalXP());
    }

    @Test
    void totalXPNeverGoesNegative() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        experience.addBonusXP(-5000);

        assertEquals(0, experience.getTotalXP());
    }

    @Test
    void setTotalXPMovesOnlyTheBonusOffset() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();
        experience.addSourceXP(SkyBlockXPSource.THE_DOJO, 425);

        experience.setTotalXP(10_000);

        assertEquals(10_000, experience.getTotalXP());
        assertEquals(425, experience.getSourceXP(SkyBlockXPSource.THE_DOJO),
                "earned progress must survive an admin set");
        assertEquals(9_575, experience.getBonusXP());
    }

    @Test
    void completingEverySourcePutsEveryCappedTaskAtItsMaximum() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        experience.completeAllSourceProgress();

        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            double expected = source.isUncapped() ? 0 : source.getMaxXP();
            assertEquals(expected, experience.getSourceXP(source), source.name());
        }
        assertEquals(SkyBlockXPSource.TOTAL_MAX_XP, experience.getEarnedXP());
    }

    @Test
    void maxingTheProfileLandsExactlyOnTheRealMaximum() {
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = experience();

        // Mirrors MaxStickService.maxSkyBlockLevel: finish every source, then top up.
        experience.completeAllSourceProgress();
        experience.setTotalXP(SkyBlockXPSource.TOTAL_MAX_XP + experience.getUncappedSourceXP());

        assertEquals(SkyBlockXPSource.TOTAL_MAX_XP, experience.getTotalXP());
        assertEquals(SkyBlockXPSource.getMaxLevel(), experience.getPreciseLevel(), 1e-9);
    }
}
