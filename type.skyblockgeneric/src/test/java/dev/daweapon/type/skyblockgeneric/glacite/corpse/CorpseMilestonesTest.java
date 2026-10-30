package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseMilestonesTest {
    private static int highest(Map<CorpseType, Integer> looted) {
        return CorpseMilestones.highestReached(type -> looted.getOrDefault(type, 0));
    }

    @Test
    void sevenMilestonesWorthTheWholeSkyBlockXpTask() {
        assertEquals(7, CorpseMilestones.all().size());
        assertEquals(SkyBlockXPSource.FROZEN_CORPSE_MILESTONES.getMaxXP(), CorpseMilestones.skyBlockXp(7));
        assertEquals(140, CorpseMilestones.skyBlockXp(7));
        assertEquals(SkyBlockXPSource.Availability.LIVE, SkyBlockXPSource.FROZEN_CORPSE_MILESTONES.getAvailability());
    }

    @Test
    void thresholdsMatchTheWiki() {
        assertEquals(0, highest(Map.of(CorpseType.LAPIS, 9)));
        assertEquals(1, highest(Map.of(CorpseType.LAPIS, 10)));
        assertEquals(1, highest(Map.of(CorpseType.LAPIS, 25, CorpseType.UMBER, 1)));
        assertEquals(2, highest(Map.of(CorpseType.LAPIS, 25, CorpseType.UMBER, 1, CorpseType.TUNGSTEN, 1)));
        assertEquals(3, highest(Map.of(CorpseType.LAPIS, 50, CorpseType.UMBER, 5, CorpseType.TUNGSTEN, 5)));
        assertEquals(4, highest(Map.of(CorpseType.LAPIS, 100, CorpseType.UMBER, 10, CorpseType.TUNGSTEN, 10, CorpseType.VANGUARD, 1)));
        assertEquals(7, highest(Map.of(CorpseType.LAPIS, 1000, CorpseType.UMBER, 100, CorpseType.TUNGSTEN, 100, CorpseType.VANGUARD, 20)));
    }

    @Test
    void accessoryRewardsComeAtTiersOneFourAndSeven() {
        assertEquals(ItemType.ORGAN_DONOR_TALISMAN, CorpseMilestones.all().get(0).rewardItem());
        assertEquals(ItemType.ORGAN_DONOR_RING, CorpseMilestones.all().get(3).rewardItem());
        assertEquals(ItemType.ORGAN_DONOR_ARTIFACT, CorpseMilestones.all().get(6).rewardItem());
        assertEquals(3, CorpseMilestones.all().stream().filter(milestone -> milestone.rewardItem() != null).count());
    }

    @Test
    void powderAndMeterXpTotalsMatchTheWiki() {
        assertEquals(610_000, CorpseMilestones.all().stream().mapToLong(CorpseMilestone::powder).sum());
        assertEquals(193_500, CorpseMilestones.all().stream().mapToLong(CorpseMilestone::rngXp).sum());
        assertTrue(CorpseMilestones.bonusCorpsePercent(7) == 20 && CorpseMilestones.bonusRollPercent(7) == 20);
    }
}
