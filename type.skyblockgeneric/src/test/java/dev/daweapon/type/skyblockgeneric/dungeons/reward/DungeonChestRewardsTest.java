package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import dev.daweapon.type.skyblockgeneric.enchantment.EnchantmentType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonChestRewardsTest {
    private static final DungeonLootTable TABLE = DungeonLootTable.load(Path.of("../configuration/skyblock/dungeons/chest_loot.json"));
    private static final long HOUR = Duration.ofHours(1).toMillis();

    @Test
    void everyFloorHasItsChests() {
        for (CatacombsMode mode : CatacombsMode.values()) {
            for (CatacombsFloor floor : CatacombsFloor.values()) {
                if (floor == CatacombsFloor.ENTRANCE) continue;
                for (DungeonChestTier tier : DungeonChestTier.values()) {
                    boolean expected = tier != DungeonChestTier.BEDROCK || floor.ordinal() >= CatacombsFloor.FLOOR_FIVE.ordinal();
                    assertEquals(expected, !TABLE.entries(floor, mode, tier).isEmpty(), mode + " " + floor + " " + tier);
                }
            }
        }
        assertFalse(TABLE.has(CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL));
    }

    @Test
    void everyLootLineIsARealItemOrEnchantment() {
        List<String> enchantments = Arrays.stream(EnchantmentType.values()).map(Enum::name).toList();
        TABLE.floors().values().forEach(chests -> chests.values().forEach(entries -> entries.forEach(entry -> {
            if (entry.isEssence()) {
                assertTrue(List.of("WITHER", "UNDEAD").contains(entry.essence()), entry.toString());
            } else {
                assertNotNull(ItemType.get(entry.itemId()), entry.toString());
                if (entry.enchantment() != null) assertTrue(enchantments.contains(entry.enchantment()), entry.toString());
            }
        })));
    }

    @Test
    void floorOneWoodChestSpendsItsQualityAndAddsTheGuaranteedEssence() {
        List<DungeonLootEntry> wood = TABLE.entries(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, DungeonChestTier.WOOD);
        for (long seed = 0; seed < 200; seed++) {
            DungeonChestRoller.Rolled rolled = DungeonChestRoller.roll(null, wood, 110, new SplittableRandom(seed));
            Map<String, Long> essence = rolled.rewards().stream().filter(DungeonReward::isEssence)
                    .collect(Collectors.toMap(DungeonReward::essenceType, DungeonReward::essence));
            long items = rolled.rewards().stream().filter(reward -> !reward.isEssence()).count();
            assertTrue(essence.get("UNDEAD") >= 3);
            assertTrue(essence.get("WITHER") >= 4);
            assertTrue(items >= 1 && items <= 1, "110 quality buys exactly one book or the Brooch: " + rolled.rewards());
            assertEquals(0, rolled.addedCost());
        }
    }

    @Test
    void sameSeedRollsTheSameChest() {
        List<DungeonLootEntry> obsidian = TABLE.entries(CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, DungeonChestTier.BEDROCK);
        DungeonChestRoller.Rolled first = DungeonChestRoller.roll(null, obsidian, 388, new SplittableRandom(42));
        DungeonChestRoller.Rolled second = DungeonChestRoller.roll(null, obsidian, 388, new SplittableRandom(42));
        assertEquals(first, second);
    }

    @Test
    void addedCostComesFromTheRolledItems() {
        List<DungeonLootEntry> diamond = TABLE.entries(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, DungeonChestTier.DIAMOND);
        boolean sawMask = false;
        for (long seed = 0; seed < 5_000 && !sawMask; seed++) {
            DungeonChestRoller.Rolled rolled = DungeonChestRoller.roll(null, diamond, 200, new SplittableRandom(seed));
            boolean mask = rolled.rewards().stream().anyMatch(reward -> "BONZO_MASK".equals(reward.itemId()));
            assertEquals(mask ? 1_000_000 : 0, rolled.addedCost());
            sawMask = mask;
        }
        assertTrue(sawMask);
    }

    @Test
    void firstRollChanceMatchesTheWiki() {
        List<DungeonLootEntry> wood = TABLE.entries(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, DungeonChestTier.WOOD);
        SplittableRandom random = new SplittableRandom(7);
        int brooches = 0;
        int runs = 40_000;
        for (int i = 0; i < runs; i++) {
            if (DungeonChestRoller.roll(null, wood, 110, random).rewards().stream().anyMatch(reward -> "NECROMANCER_BROOCH".equals(reward.itemId()))) brooches++;
        }
        assertEquals(0.7519, brooches * 100.0 / runs, 0.15);
    }

    @Test
    void runRollsTheChestsTheScoreUnlocks() {
        DungeonChestRun low = DungeonRewards.roll(null, CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 150, List.of(),
                DungeonRewards.Bonuses.NONE, TABLE, new SplittableRandom(1), 0);
        assertNotNull(low);
        assertEquals(List.of(DungeonChestTier.WOOD, DungeonChestTier.GOLD, DungeonChestTier.DIAMOND),
                low.chests().stream().map(DungeonRewardChest::tier).toList());
        assertEquals(25_000, low.chest(DungeonChestTier.GOLD).cost() - extraCost(low.chest(DungeonChestTier.GOLD)));

        DungeonChestRun perfect = DungeonRewards.roll(null, CatacombsFloor.FLOOR_SEVEN, CatacombsMode.NORMAL, 300, List.of(),
                DungeonRewards.Bonuses.NONE, TABLE, new SplittableRandom(1), 0);
        assertEquals(6, perfect.chests().size());
        assertEquals(DungeonScoreRank.S_PLUS, perfect.rank());

        assertNull(DungeonRewards.roll(null, CatacombsFloor.ENTRANCE, CatacombsMode.NORMAL, 300, List.of(),
                DungeonRewards.Bonuses.NONE, TABLE, new SplittableRandom(1), 0));
    }

    private static long extraCost(DungeonRewardChest chest) {
        return chest.rewards().stream().anyMatch(reward -> "BONZO_MASK".equals(reward.itemId())) ? 1_000_000 : 0;
    }

    @Test
    void oneClaimThenAKeyThenNoMore() {
        DungeonChestRun run = sampleRun(0);
        assertTrue(run.canOpenMore());
        assertFalse(run.nextNeedsKey());
        run = run.open(DungeonChestTier.WOOD, false);
        assertTrue(run.nextNeedsKey());
        assertTrue(run.canOpenMore());
        run = run.open(DungeonChestTier.GOLD, true);
        assertFalse(run.canOpenMore());
        assertEquals(2, run.openedCount());
    }

    @Test
    void kismetRerollsOnceAndNeverAnOpenedChest() {
        DungeonChestRun run = sampleRun(0);
        assertTrue(run.canReroll(DungeonChestTier.GOLD));
        DungeonRewardChest fresh = new DungeonRewardChest(DungeonChestTier.GOLD, List.of(DungeonReward.item("HOT_POTATO_BOOK", 1)), 25_000, false, false);
        run = run.reroll(DungeonChestTier.GOLD, fresh);
        assertEquals(fresh.rewards(), run.chest(DungeonChestTier.GOLD).rewards());
        assertTrue(run.chest(DungeonChestTier.GOLD).rerolled());
        assertFalse(run.canReroll(DungeonChestTier.WOOD));
        assertFalse(sampleRun(0).open(DungeonChestTier.WOOD, false).canReroll(DungeonChestTier.WOOD));
    }

    @Test
    void storageKeepsSixtyRunsForSeventyTwoHours() {
        List<DungeonChestRun> runs = new ArrayList<>();
        long now = 100 * HOUR;
        for (int i = 0; i < 70; i++) runs = DungeonChestStorage.withRun(runs, sampleRun(now - 60 * HOUR + i * 1000L), now);
        assertEquals(DungeonChestStorage.MAX_RUNS, runs.size());
        assertEquals(now - 60 * HOUR + 69 * 1000L, runs.getFirst().completedAt());

        List<DungeonChestRun> mixed = List.of(sampleRun(now - 73 * HOUR), sampleRun(now - 71 * HOUR));
        assertEquals(1, DungeonChestStorage.alive(mixed, now).size());
    }

    @Test
    void survivesTheSaveFormat() {
        DungeonChestRun run = sampleRun(5 * HOUR).open(DungeonChestTier.GOLD, false);
        List<DungeonChestRun> parsed = DungeonChestStorage.parse(DungeonChestStorage.serialize(List.of(run)));
        assertEquals(run, parsed.getFirst());
    }

    @Test
    void starChancesAddUpToOneHundred() {
        SplittableRandom random = new SplittableRandom(3);
        int starred = 0;
        for (int i = 0; i < 20_000; i++) if (DungeonChestUpgrades.stars(null, false, random) > 0) starred++;
        assertEquals(21, starred * 100.0 / 20_000, 1.5);
    }

    private static DungeonChestRun sampleRun(long completedAt) {
        DungeonChestRun rolled = DungeonRewards.roll(null, CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 240, List.of("<a>Tester"),
                DungeonRewards.Bonuses.NONE, TABLE, new SplittableRandom(completedAt), completedAt);
        assertNotNull(rolled);
        return rolled;
    }
}
