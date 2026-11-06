package dev.daweapon.type.catacombs.run;

import dev.daweapon.commons.text.Text;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import dev.daweapon.type.skyblockgeneric.dungeons.gui.GUIDungeonRewardChest;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonChestRun;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonReward;
import dev.daweapon.type.skyblockgeneric.dungeons.reward.DungeonRewardChest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** The reward chest menu against the captured one (gui.jsonl of both runs). */
class RewardChestMenuTest {
    private static final List<DungeonReward> ESSENCE = List.of(DungeonReward.essence("WITHER", 10), DungeonReward.essence("UNDEAD", 6));

    @Test
    void rewardsFillSlotsElevenToFifteenAsCaptured() {
        assertArrayEquals(new int[]{11, 13, 15}, GUIDungeonRewardChest.rewardSlots(3));
        assertArrayEquals(new int[]{11, 12, 14, 15}, GUIDungeonRewardChest.rewardSlots(4));
        assertArrayEquals(new int[]{11, 12, 13, 14, 15}, GUIDungeonRewardChest.rewardSlots(5));
        assertArrayEquals(new int[]{11, 12, 13, 14, 15}, GUIDungeonRewardChest.rewardSlots(7));
        assertEquals(31, GUIDungeonRewardChest.OPEN_SLOT);
        assertEquals(49, GUIDungeonRewardChest.CLOSE_SLOT);
        assertEquals(50, GUIDungeonRewardChest.REROLL_SLOT);
    }

    @Test
    void freeChestShowsContentsCostAndClickToOpen() {
        DungeonRewardChest wood = new DungeonRewardChest(DungeonChestTier.WOOD, ESSENCE, 0, false, false);
        DungeonChestRun run = DungeonChestRun.create(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 242, DungeonScoreRank.A,
                0, List.of(), List.of(wood));
        assertEquals(List.of("Contents", "Wither Essence x10", "Undead Essence x6", "", "Cost", "FREE", "", "Click to open!"),
                plain(GUIDungeonRewardChest.openLore(run, wood)));
    }

    @Test
    void paidChestAddsTheBankNote() {
        DungeonRewardChest gold = new DungeonRewardChest(DungeonChestTier.GOLD, ESSENCE, 20_000, false, false);
        DungeonChestRun run = DungeonChestRun.create(CatacombsFloor.FLOOR_ONE, CatacombsMode.NORMAL, 242, DungeonScoreRank.A,
                0, List.of(), List.of(gold));
        assertEquals(List.of("Contents", "Wither Essence x10", "Undead Essence x6", "", "Cost", "20,000 Coins", "",
                        "NOTE: Coins are withdrawn from your", "bank if you don't have enough in", "your purse.", "", "Click to open!"),
                plain(GUIDungeonRewardChest.openLore(run, gold)));
    }

    @Test
    void floorOneChestsCostWhatTheCapturesCharged() {
        assertEquals(0, DungeonChestTier.WOOD.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(20_000, DungeonChestTier.GOLD.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(40_000, DungeonChestTier.DIAMOND.baseCost(CatacombsFloor.FLOOR_ONE));
        assertEquals(80_000, DungeonChestTier.EMERALD.baseCost(CatacombsFloor.FLOOR_ONE));
    }

    private static List<String> plain(List<Text> lore) {
        return lore.stream().map(Text::plain).toList();
    }
}
