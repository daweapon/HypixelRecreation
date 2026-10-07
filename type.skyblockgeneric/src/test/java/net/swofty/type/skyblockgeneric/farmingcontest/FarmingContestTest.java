package net.swofty.type.skyblockgeneric.farmingcontest;

import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmingContestTest {

    @Test
    void contestsRunEveryThreeDaysFromTheSecondDay() {
        assertEquals(124, FarmingContest.PER_YEAR);
        assertEquals(SkyBlockCalendar.DAY, FarmingContest.startInYear(0));
        assertEquals(4L * SkyBlockCalendar.DAY, FarmingContest.startInYear(1));
        assertTrue(FarmingContest.of(1, FarmingContest.PER_YEAR - 1).endElapsed() <= SkyBlockCalendar.YEAR);
    }

    @Test
    void activeContestCoversExactlyOneDay() {
        long start = FarmingContest.of(3, 10).startElapsed();
        assertEquals(10, FarmingContest.activeAt(start).orElseThrow().index());
        assertEquals(10, FarmingContest.activeAt(start + SkyBlockCalendar.DAY - 1).orElseThrow().index());
        assertFalse(FarmingContest.activeAt(start + SkyBlockCalendar.DAY).isPresent());
        assertFalse(FarmingContest.activeAt(start - 1).isPresent());
    }

    @Test
    void cropsAreThreeDistinctAndStable() {
        FarmingContest contest = FarmingContest.of(42, 7);
        assertEquals(3, new HashSet<>(contest.crops()).size());
        assertEquals(contest.crops(), FarmingContest.of(42, 7).crops());
        assertTrue(contest.features(contest.anitaCrop()));
    }

    @Test
    void bracketsIncludeOneExtraPlayer() {
        assertEquals(ContestBracket.DIAMOND, ContestBracket.forPlacement(1, 1, false));
        assertEquals(ContestBracket.DIAMOND, ContestBracket.forPlacement(3, 100, false));
        assertEquals(ContestBracket.PLATINUM, ContestBracket.forPlacement(4, 100, false));
        assertEquals(ContestBracket.GOLD, ContestBracket.forPlacement(11, 100, false));
        assertEquals(ContestBracket.SILVER, ContestBracket.forPlacement(31, 100, false));
        assertEquals(ContestBracket.BRONZE, ContestBracket.forPlacement(61, 100, false));
        assertEquals(ContestBracket.PARTICIPATION, ContestBracket.forPlacement(62, 100, false));
    }

    @Test
    void goatedWidensEveryBracket() {
        assertEquals(ContestBracket.DIAMOND, ContestBracket.forPlacement(6, 100, true));
        assertEquals(ContestBracket.GOLD, ContestBracket.forPlacement(21, 100, true));
        assertEquals(ContestBracket.BRONZE, ContestBracket.forPlacement(71, 100, true));
        assertEquals(ContestBracket.PARTICIPATION, ContestBracket.forPlacement(72, 100, true));
    }

    @Test
    void personalBestFortuneCapsAtOneHundred() {
        assertEquals(10D, ContestCrop.WHEAT.personalBestFortune(100_000));
        assertEquals(100D, ContestCrop.WHEAT.personalBestFortune(5_000_000));
        assertEquals(100D, ContestCrop.MELON.personalBestFortune(5_000_000));
    }

    @Test
    void profileSurvivesSerialization() {
        FarmingContestProfile profile = new FarmingContestProfile();
        profile.setGoldMedals(3);
        profile.getUniqueBrackets().put(ContestCrop.CACTUS, ContestBracket.PLATINUM);
        profile.participate(FarmingContest.of(2, 5), ContestCrop.CACTUS, true).setCollected(420);
        FarmingContestProfile copy = FarmingContestProfile.deserialize(profile.serialize());
        assertEquals(3, copy.getGoldMedals());
        assertEquals(ContestBracket.PLATINUM, copy.uniqueBracket(ContestCrop.CACTUS));
        assertEquals(420, copy.getEntries().getFirst().getCollected());
        assertTrue(copy.getEntries().getFirst().isGoated());
    }
}
