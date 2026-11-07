package dev.daweapon.dungeons.catacombs.puzzle;

import dev.daweapon.dungeons.catacombs.puzzle.weirdos.WeirdosCase;
import dev.daweapon.dungeons.catacombs.puzzle.weirdos.WeirdosRiddle;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeirdosRiddleTest {
    private static final List<String> REWARDED_LINES = List.of("The reward is not in my chest!",
            "At least one of them is lying, and the reward is not in", "My chest doesn't have the reward. We are all telling the truth.",
            "My chest has the reward and I'm telling the truth!", "The reward isn't in any of our chests.",
            "Both of them are telling the truth. Also,");

    private static PuzzleCatalogue.WeirdosRoom room() {
        return PuzzleCatalogueFixture.load().weirdos("three_weirdos").orElseThrow();
    }

    @Test
    void theRoomHasThreeWeirdosWithTheirCapturedSkinsAndChests() {
        PuzzleCatalogue.WeirdosRoom room = room();
        assertEquals(3, room.weirdos().size());
        assertEquals(3, room.skins().size());
        assertEquals(6, room.cases().size());
        assertTrue(room.names().containsAll(List.of("Hugo", "Winona", "Ramsey")));
        assertEquals(new RoomBlock(24, 1, 16), room.weirdos().getFirst().chest());
    }

    @Test
    void theRewardedWeirdoAlwaysSaysOneOfTheKnownSolutionLines() {
        PuzzleCatalogue.WeirdosRoom room = room();
        Random random = new Random(3);
        Set<Integer> spots = new HashSet<>();
        for (int deal = 0; deal < 300; deal++) {
            WeirdosRiddle riddle = WeirdosRiddle.deal(room.cases(), room.names(), random, name -> "<" + name + ">");
            String line = riddle.lines().get(riddle.rewardedSpot());
            assertTrue(REWARDED_LINES.stream().anyMatch(line::startsWith), line);
            assertEquals(3, Set.copyOf(riddle.names()).size());
            riddle.lines().forEach(each -> assertFalse(each.matches(".*\\{[123]}.*"), each));
            spots.add(riddle.rewardedSpot());
        }
        assertEquals(Set.of(0, 1, 2), spots);
    }

    @Test
    void namesInLinesPointAtTheRightWeirdos() {
        WeirdosCase sixth = room().cases().get(5);
        WeirdosRiddle riddle = WeirdosRiddle.deal(List.of(sixth), List.of("Hugo", "Winona", "Ramsey"), new Random(1), name -> "[" + name + "]");
        String rewarded = riddle.rewardedName();
        int first = -1;
        for (int spot = 0; spot < 3; spot++) {
            if (riddle.lines().get(spot).endsWith("is telling the truth.")) first = spot;
        }
        assertTrue(riddle.lines().get(riddle.rewardedSpot()).contains("[" + riddle.names().get(first) + "]"));
        assertTrue(riddle.rewarded(riddle.names().indexOf(rewarded)));
    }
}
