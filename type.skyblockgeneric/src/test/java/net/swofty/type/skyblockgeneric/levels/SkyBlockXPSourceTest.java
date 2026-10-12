package net.swofty.type.skyblockgeneric.levels;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pins the XP catalogue to real Hypixel's published figures. If a source's value is edited these
// totals must be re-derived from the source data, not just nudged until the test passes.
class SkyBlockXPSourceTest {

    /**
     * Per-category subtotals read straight off the live Ways to Level Up menu on 2026-10-05.
     * These are authoritative over any spreadsheet: uncapped tasks contribute nothing to them,
     * exactly as Hypixel counts.
     */
    private static final Map<SkyBlockXPSource.Group, Integer> EXPECTED_GROUP_TOTALS = Map.of(
            SkyBlockXPSource.Group.CORE, 19822,
            SkyBlockXPSource.Group.EVENT, 1439,
            SkyBlockXPSource.Group.DUNGEON, 2760,
            SkyBlockXPSource.Group.ESSENCE_SHOP, 2140,
            SkyBlockXPSource.Group.SLAYING, 12295,
            SkyBlockXPSource.Group.SKILL_RELATED, 11684,
            SkyBlockXPSource.Group.MISCELLANEOUS, 6306,
            SkyBlockXPSource.Group.STORY, 1132,
            SkyBlockXPSource.Group.CONSUMABLES, 299
    );

    @Test
    void everyGroupMatchesTheRealHypixelSubtotal() {
        EXPECTED_GROUP_TOTALS.forEach((group, expected) ->
                assertEquals(expected, SkyBlockXPSource.getTotalXPFor(group),
                        "XP subtotal drifted for group " + group));
    }

    @Test
    void totalXPReachesTheRealMaximumLevel() {
        assertEquals(57877, SkyBlockXPSource.TOTAL_MAX_XP);
        assertEquals(578.77, SkyBlockXPSource.getMaxLevel(), 1e-9);
    }

    @Test
    void groupTotalsAccountForEverySource() {
        int summed = EXPECTED_GROUP_TOTALS.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(SkyBlockXPSource.TOTAL_MAX_XP, summed,
                "A group is missing from the expected totals, so some sources are uncounted");
    }

    @Test
    void everyCappedSourceCarriesAPositiveMaximum() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (source.isUncapped()) continue;
            assertTrue(source.getMaxXP() > 0, source + " has no XP value");
        }
    }

    @Test
    void uncappedSourcesAreExcludedFromTotals() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (!source.isUncapped()) continue;
            assertEquals(SkyBlockXPSource.UNCAPPED, source.getMaxXP(),
                    source + " is uncapped so it must not contribute to a category total");
        }
    }

    @Test
    void compositeTasksAreHeadedByATaskCount() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (source.getSubTasks().isEmpty()) continue;
            assertTrue(source.getAction().clickable(),
                    source + " lists sub-tasks so its tooltip must offer to open them");
        }
    }

    @Test
    void everySourceRecordsItsXPExactlyOneWay() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            boolean hasCause = source.getBackingCauseType() != null;
            assertEquals(!hasCause, source.usesSourceProgress(),
                    source + " must read XP from either a cause class or source progress, never both");
        }
    }

    @Test
    void sourcesBackedByACauseClassAreObtainable() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (source.getBackingCauseType() == null) continue;
            assertTrue(source.isObtainable(),
                    source + " has a cause class awarding it, so it cannot be marked unavailable");
        }
    }

    @Test
    void unavailableSourcesCanStillRecordProgress() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (source.isObtainable()) continue;
            assertNull(source.getBackingCauseType(),
                    source + " is not obtainable yet claims a backing cause class");
            assertTrue(source.usesSourceProgress(),
                    source + " cannot record any progress once its content ships");
        }
    }

    @Test
    void backingCauseClassesAreNotSharedBetweenSources() {
        Set<Class<?>> seen = new HashSet<>();
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            Class<?> backing = source.getBackingCauseType();
            if (backing == null) continue;
            assertTrue(seen.add(backing),
                    backing.getSimpleName() + " backs more than one source, double-counting its XP");
        }
    }

    @Test
    void everySubAreaSourceSitsInTheSkillRelatedGroup() {
        for (SkyBlockXPSource source : SkyBlockXPSource.values()) {
            if (source.getSubArea() == null) continue;
            assertEquals(SkyBlockXPSource.Group.SKILL_RELATED, source.getGroup(),
                    source + " has a sub-area but is not a skill related task");
        }
    }

    @Test
    void skillRelatedSubAreasMatchTheirPublishedSubtotals() {
        assertEquals(4892, subAreaTotal(SkyBlockXPSource.SubArea.MINING));
        assertEquals(2518, subAreaTotal(SkyBlockXPSource.SubArea.FARMING));
        assertEquals(1755, subAreaTotal(SkyBlockXPSource.SubArea.FISHING));
        assertEquals(2519, subAreaTotal(SkyBlockXPSource.SubArea.FORAGING));
    }

    @Test
    void everySkillRelatedSourceIsAssignedToASubArea() {
        long unassigned = SkyBlockXPSource.getByGroup(SkyBlockXPSource.Group.SKILL_RELATED).stream()
                .filter(source -> source.getSubArea() == null)
                .count();
        assertEquals(0, unassigned);
    }

    @Test
    void groupedSourcesCoverTheWholeCatalogue() {
        int grouped = Arrays.stream(SkyBlockXPSource.Group.values())
                .mapToInt(group -> SkyBlockXPSource.getByGroup(group).size())
                .sum();
        assertEquals(SkyBlockXPSource.values().length, grouped);
    }

    private int subAreaTotal(SkyBlockXPSource.SubArea subArea) {
        return SkyBlockXPSource.getBySubArea(subArea).stream()
                .mapToInt(SkyBlockXPSource::getMaxXP)
                .sum();
    }
}
