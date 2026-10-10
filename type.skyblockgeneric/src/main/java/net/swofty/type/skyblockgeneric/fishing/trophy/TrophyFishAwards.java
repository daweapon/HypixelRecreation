package net.swofty.type.skyblockgeneric.fishing.trophy;

import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointTrophyFish;
import net.swofty.type.skyblockgeneric.event.custom.TrophyFishCaughtEvent;
import net.swofty.type.skyblockgeneric.fishing.catches.TrophyTier;
import net.swofty.type.skyblockgeneric.fishing.registry.FishingRegistry;
import net.swofty.type.skyblockgeneric.fishing.registry.TrophyFishDefinition;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.BitsMultiplier;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * What a Trophy Fish catch is worth beyond the fish (wiki: Trophy_Fish): SkyBlock XP for each fish's first catch of
 * each tier (4/8/16/32; a higher tier also grants the lower tiers' XP, 1,080 in total), +15 Bits for a Gold and +30
 * for a Diamond (times the Bits multiplier), and the catch event other systems listen to.
 */
public final class TrophyFishAwards {
    public static final int GOLD_BITS = 15;
    public static final int DIAMOND_BITS = 30;

    private TrophyFishAwards() {
    }

    public static void record(SkyBlockPlayer player, String fishId, TrophyTier tier, String itemId) {
        DatapointTrophyFish.TrophyFishData data = player.getTrophyFishData();
        data.getProgress(fishId).increment(tier.name());
        save(player, data);

        int bits = switch (tier) {
            case GOLD -> GOLD_BITS;
            case DIAMOND -> DIAMOND_BITS;
            default -> 0;
        };
        if (bits > 0) {
            int gained = (int) Math.round(bits * BitsMultiplier.of(player));
            player.addBits(gained);
            player.sendMessage("<b>+" + gained + " Bits");
        }
        syncSkyBlockXP(player);
        HypixelEventHandler.callCustomEvent(new TrophyFishCaughtEvent(player, fishId, tier, itemId));
    }

    public static void save(SkyBlockPlayer player, DatapointTrophyFish.TrophyFishData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TROPHY_FISH, DatapointTrophyFish.class).setValue(data);
    }

    public static int skyBlockXp(DatapointTrophyFish.TrophyFishData data) {
        int total = 0;
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            DatapointTrophyFish.FishProgress progress = data.getFish().get(fish.id());
            if (progress == null) continue;
            TrophyTier highest = highestTier(progress);
            if (highest == null) continue;
            for (TrophyTier tier : TrophyTier.values()) {
                if (tier.ordinal() <= highest.ordinal()) total += tier.firstCatchSkyBlockXp();
            }
        }
        return total;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.TROPHY_FISH, skyBlockXp(player.getTrophyFishData()));
        awardAchievements(player, player.getTrophyFishData());
    }

    private static void awardAchievements(SkyBlockPlayer player, DatapointTrophyFish.TrophyFishData data) {
        boolean all = true;
        boolean gold = false;
        for (TrophyFishDefinition fish : FishingRegistry.getTrophyFish()) {
            DatapointTrophyFish.FishProgress progress = data.getFish().get(fish.id());
            TrophyTier highest = progress == null ? null : highestTier(progress);
            if (highest == null) all = false;
            else if (highest.ordinal() >= TrophyTier.GOLD.ordinal()) gold = true;
        }
        if (all) player.getAchievementHandler().completeAchievement("skyblock.humble_beginnings");
        if (gold) player.getAchievementHandler().completeAchievement("skyblock.shining_scales");
    }

    static TrophyTier highestTier(DatapointTrophyFish.FishProgress progress) {
        if (progress.getDiamond() > 0) return TrophyTier.DIAMOND;
        if (progress.getGold() > 0) return TrophyTier.GOLD;
        if (progress.getSilver() > 0) return TrophyTier.SILVER;
        if (progress.getBronze() > 0) return TrophyTier.BRONZE;
        return null;
    }
}
