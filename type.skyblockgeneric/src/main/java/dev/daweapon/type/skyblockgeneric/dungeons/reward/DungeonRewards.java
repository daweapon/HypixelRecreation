package dev.daweapon.type.skyblockgeneric.dungeons.reward;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestRules;
import dev.daweapon.dungeons.catacombs.reward.DungeonChestTier;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerk;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShops;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeEffectService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeId;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Rolls a won run's reward chests (wiki Dungeon Reward Chest and the per-floor /Loot pages): which chests the
 * score unlocks, each chest's quality with the player's upgrades, its contents and its price. Rolled once at
 * the end of the run and stored at Croesus.
 */
public final class DungeonRewards {
    private static final AttributeId CATACOMBS_BOX = AttributeId.parse("R13");
    private static final String BOSS_LUCK = "catacombs_boss_luck";

    private DungeonRewards() {
    }

    public record Bonuses(double treasureMultiplier, double flatBonus) {
        public static final Bonuses NONE = new Bonuses(1.0, 0);
    }

    public static @Nullable DungeonChestRun completeRun(SkyBlockPlayer player, CatacombsFloor floor, CatacombsMode mode,
                                                        int score, List<String> party) {
        DungeonChestRun run = roll(player.getUuid(), floor, mode, score, party, bonuses(player), DungeonLootTable.get(),
                ThreadLocalRandom.current(), System.currentTimeMillis());
        if (run == null) return null;
        DungeonChestStorage.store(player, run);
        return run;
    }

    public static @Nullable DungeonChestRun roll(@Nullable UUID player, CatacombsFloor floor, CatacombsMode mode, int score,
                                                 List<String> party, Bonuses bonuses, DungeonLootTable table,
                                                 RandomGenerator random, long now) {
        DungeonScoreRank rank = DungeonScoreRank.fromScore(score);
        List<DungeonChestTier> tiers = DungeonChestRules.availableTiers(rank, floor, mode);
        if (tiers.isEmpty() || !table.has(floor, mode)) return null;
        List<DungeonRewardChest> chests = new ArrayList<>();
        for (DungeonChestTier tier : tiers) chests.add(rollChest(player, floor, mode, rank, tier, bonuses, table, random));
        return DungeonChestRun.create(floor, mode, score, rank, now, party, chests);
    }

    public static DungeonRewardChest rollChest(@Nullable UUID player, CatacombsFloor floor, CatacombsMode mode,
                                               DungeonScoreRank rank, DungeonChestTier tier, Bonuses bonuses,
                                               DungeonLootTable table, RandomGenerator random) {
        int quality = DungeonChestRules.quality(tier.baseQuality(floor, mode), rank == DungeonScoreRank.S_PLUS,
                bonuses.treasureMultiplier(), bonuses.flatBonus());
        DungeonChestRoller.Rolled rolled = DungeonChestRoller.roll(player, table.entries(floor, mode, tier), quality, random);
        return new DungeonRewardChest(tier, rolled.rewards(), tier.baseCost(floor) + rolled.addedCost(), false, false);
    }

    public static DungeonRewardChest reroll(SkyBlockPlayer player, DungeonChestRun run, DungeonChestTier tier) {
        return rollChest(player.getUuid(), run.floor(), run.mode(), run.rank(), tier, bonuses(player), DungeonLootTable.get(),
                ThreadLocalRandom.current());
    }

    public static Bonuses bonuses(SkyBlockPlayer player) {
        return new Bonuses(treasureMultiplier(player), bossLuck(player) + AttributeEffectService.value(player.getHuntingData(), CATACOMBS_BOX));
    }

    private static double treasureMultiplier(SkyBlockPlayer player) {
        List<SkyBlockItem> accessories = player.getAccessoryBag().getAllAccessories();
        if (owns(accessories, ItemType.TREASURE_ARTIFACT)) return 1.03;
        if (owns(accessories, ItemType.TREASURE_RING)) return 1.02;
        if (owns(accessories, ItemType.TREASURE_TALISMAN)) return 1.01;
        return 1.0;
    }

    private static boolean owns(List<SkyBlockItem> accessories, ItemType type) {
        return accessories.stream().anyMatch(item -> type.equals(item.getAttributeHandler().getPotentialType()));
    }

    private static double bossLuck(SkyBlockPlayer player) {
        return EssenceShops.shop(EssenceType.UNDEAD)
                .flatMap(shop -> shop.perks().stream().filter(perk -> perk.id().equals(BOSS_LUCK)).findFirst())
                .map(perk -> EssencePerks.value(player, (EssencePerk) perk))
                .orElse(0.0);
    }
}
