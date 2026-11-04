package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CenturyRaffleTest {
    private static final long HOUR = 3_600_000L;
    private static final CenturyEvent.Window YEAR = new CenturyEvent.Window("test", 0, 124 * HOUR);
    private static final CenturyRaffleConfig CONFIG = CenturyRaffleConfig.load(new File("../configuration/skyblock/century_raffle.yml"));

    @Test
    void everyPeriodHasSevenTasksOfEachDifficultyAndTheSameListEverywhere() {
        List<RaffleTask> tasks = RaffleTasks.forPeriod("year600", 3);
        assertEquals(21, tasks.size());
        for (RaffleTask.Difficulty difficulty : RaffleTask.Difficulty.values()) {
            assertEquals(7, tasks.stream().filter(task -> task.difficulty() == difficulty).count());
        }
        assertEquals(tasks, RaffleTasks.forPeriod("year600", 3));
        assertEquals(21, tasks.stream().distinct().count());
    }

    @Test
    void aSkyBlockYearHoldsSixtyTwoSpeedRafflesAndSixDailyRaffles() {
        assertEquals(62, RaffleClock.count(YEAR, RaffleType.SPEED));
        assertEquals(6, RaffleClock.count(YEAR, RaffleType.DAILY));
        assertEquals(1, RaffleClock.count(YEAR, RaffleType.BIG_ONE));
        assertEquals(124 * HOUR, RaffleClock.end(YEAR, RaffleType.DAILY, 5));
        assertEquals(2, RaffleClock.index(YEAR, RaffleType.SPEED, 5 * HOUR));
        assertEquals(List.of("speed:2", "daily:0", "big_one"), RaffleClock.currentKeys(YEAR, 5 * HOUR));
        assertEquals("Speed Raffle #3", RaffleClock.label("speed:2"));
    }

    @Test
    void consolationSlicesFollowTheWikiCaps() {
        assertEquals(2, RaffleType.SPEED.consolationSlices(40));
        assertEquals(5, RaffleType.DAILY.consolationSlices(40));
        assertEquals(0, RaffleType.BIG_ONE.consolationSlices(400));
        assertEquals(0, RaffleType.SPEED.consolationSlices(4));
    }

    @Test
    void theFeedingTableMatchesTheWikiWeights() {
        assertEquals(32, CONFIG.feeding().size());
        assertEquals(15_990, CONFIG.feeding().stream().mapToDouble(CenturyRaffleConfig.FeedingReward::weight).sum());
    }

    @Test
    void everyPrizeIsARealItemOrARandomCakeRoll() {
        for (PrizeTier tier : PrizeTier.values()) {
            assertFalse(CONFIG.pool(tier).isEmpty());
            for (String prize : CONFIG.pool(tier)) {
                assertTrue(CenturyRaffleConfig.isSpecial(prize) || ItemType.get(prize) != null, prize);
            }
        }
        for (RaffleMilestone milestone : RaffleMilestone.values()) {
            if (milestone.coins() == 0 && milestone.emblemKey() == null) assertNotNull(milestone.item(), milestone.name());
        }
    }

    @Test
    void prizeCountsScaleWithParticipants() {
        Map<UUID, Long> entries = new HashMap<>();
        for (int i = 0; i < 1_000; i++) entries.put(UUID.randomUUID(), 1L + i % 10);
        Map<UUID, PrizeTier> winners = RaffleDraws.pickWinners(RaffleType.SPEED, entries, CONFIG);
        assertTrue(winners.size() >= 150 && winners.size() <= 153, "winners " + winners.size());
        assertTrue(entries.keySet().containsAll(winners.keySet()));
    }

    @Test
    void tasksMatchTheirTriggers() {
        assertTrue(RaffleTask.SLAYER_TIER_THREE.matches(RaffleAction.of(RaffleAction.Trigger.SLAYER_BOSS, "SVEN_PACKMASTER", 4)));
        assertFalse(RaffleTask.SLAYER_TIER_FIVE.matches(RaffleAction.of(RaffleAction.Trigger.SLAYER_BOSS, "SVEN_PACKMASTER", 4)));
        assertTrue(RaffleTask.MINE_GEMSTONE.matches(RaffleAction.of(RaffleAction.Trigger.ITEM_MINED, "ROUGH_JASPER_GEM")));
        assertTrue(RaffleTask.MINE_JASPER.matches(RaffleAction.of(RaffleAction.Trigger.ITEM_MINED, "ROUGH_JASPER_GEM")));
        assertTrue(RaffleTask.MINE_DWARVEN_METAL.matches(RaffleAction.of(RaffleAction.Trigger.ITEM_MINED, "MITHRIL")));
        assertTrue(RaffleTask.GREAT_TREASURE.matches(RaffleAction.of(RaffleAction.Trigger.TREASURE, "OUTSTANDING")));
        assertFalse(RaffleTask.OUTSTANDING_TREASURE.matches(RaffleAction.of(RaffleAction.Trigger.TREASURE, "GREAT")));
    }
}
