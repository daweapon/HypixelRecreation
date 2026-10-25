package dev.daweapon.type.skyblockgeneric.greatspook;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VargulServiceTest {
    @Test
    void everyUnearthingIsFiveLinesEndingInTheAwakening() {
        for (int visit = 1; visit <= VargulService.unearthingCount() + 3; visit++) {
            List<String> script = VargulService.script(visit);
            assertEquals(5, script.size());
            assertTrue(script.getLast().contains("Vargul the Unearthed has awakened!"));
        }
    }

    @Test
    void theFirstVisitUsesTheOpeningAndLaterVisitsWalkTheRotation() {
        assertTrue(VargulService.script(1).get(1).contains("Who dares disturb my eternal slumber?"));
        assertTrue(VargulService.script(2).get(1).contains("You've become a regular visitor."));
        assertTrue(VargulService.script(2).getFirst().contains("The earth shakes with knowing fury."));
        assertTrue(VargulService.script(7).get(3).contains("Have you brought more Dark Candies?"));
    }

    @Test
    void theLastLinesRepeatOnceTheRotationRunsOut() {
        assertEquals(VargulService.script(7), VargulService.script(40));
        assertEquals(7, VargulService.unearthingCount());
    }

    @Test
    void emblemMilestoneIsSixHundredCandies() {
        assertEquals(600, VargulProgress.EMBLEM_CANDY);
        assertEquals(6, VargulProgress.EMBLEM_MILESTONE);
    }
}
