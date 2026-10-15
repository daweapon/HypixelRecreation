package dev.daweapon.type.skyblockgeneric.garden;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GardenProgress {
    public enum UpgradeResult {
        UPGRADED, MAX_TIER, LEVEL_TOO_LOW, NOT_ENOUGH_COPPER
    }

    public enum PlotStatus {
        UNLOCKABLE, ALREADY_UNLOCKED, NOT_ADJACENT, LEVEL_TOO_LOW, INVALID
    }

    public record MilestoneProgress(int newTiers, int gardenXp, int farmingXp, int skyblockXp) {
    }

    public record PlotQuote(PlotStatus status, int compostCost, int requiredLevel) {
    }

    private GardenProgress() {
    }

    public static int level(GardenProfile profile) {
        GardenConfig config = GardenConfig.get();
        int level = 1;
        for (GardenConfig.Level entry : config.levels()) {
            if (profile.getGardenXp() >= entry.cumulativeXp()) {
                level = entry.level();
            }
        }
        return level;
    }

    public static List<Integer> addGardenXp(GardenProfile profile, double amount) {
        int before = level(profile);
        profile.setGardenXp(profile.getGardenXp() + Math.max(0, amount));
        List<Integer> reached = new ArrayList<>();
        for (int level = before + 1; level <= level(profile); level++) {
            reached.add(level);
        }
        return reached;
    }

    public static int skyblockXpFromLevel(GardenProfile profile) {
        int total = 0;
        for (GardenConfig.Level entry : GardenConfig.get().levels()) {
            if (entry.level() <= level(profile)) {
                total += entry.skyblockXp();
            }
        }
        return total;
    }

    public static MilestoneProgress addCrops(GardenProfile profile, GardenCrop crop, long amount) {
        GardenConfig config = GardenConfig.get();
        long total = profile.addCropCount(crop, amount);
        int tier = profile.milestoneTier(crop);
        int newTiers = 0;
        int gardenXp = 0;
        int farmingXp = 0;
        int skyblockXp = 0;
        while (tier < config.maxMilestoneTier() && total >= config.cumulativeThreshold(crop, tier + 1)) {
            GardenConfig.MilestoneTier reward = config.milestoneTiers().get(tier);
            gardenXp += reward.gardenXp();
            farmingXp += reward.farmingXp();
            skyblockXp += reward.skyblockXp();
            tier++;
            newTiers++;
        }
        profile.setMilestoneTier(crop, tier);
        return new MilestoneProgress(newTiers, gardenXp, farmingXp, skyblockXp);
    }

    public static int totalMilestoneTiers(GardenProfile profile) {
        int total = 0;
        for (GardenCrop crop : GardenCrop.values()) {
            total += profile.milestoneTier(crop);
        }
        return total;
    }

    public static boolean cropUnlocked(GardenProfile profile, GardenCrop crop) {
        int level = level(profile);
        return GardenConfig.get().levels().stream()
                .anyMatch(entry -> entry.level() <= level && entry.crops().contains(crop));
    }

    public static Optional<GardenConfig.UpgradeTier> nextUpgrade(GardenProfile profile, GardenCrop crop) {
        int tier = profile.cropUpgradeTier(crop);
        List<GardenConfig.UpgradeTier> tiers = GardenConfig.get().upgradeTiers();
        return tier >= tiers.size() ? Optional.empty() : Optional.of(tiers.get(tier));
    }

    public static UpgradeResult upgradeCrop(GardenProfile profile, GardenCrop crop) {
        Optional<GardenConfig.UpgradeTier> next = nextUpgrade(profile, crop);
        if (next.isEmpty()) {
            return UpgradeResult.MAX_TIER;
        }
        if (level(profile) < next.get().gardenLevel()) {
            return UpgradeResult.LEVEL_TOO_LOW;
        }
        if (!profile.spendCopper(next.get().copper())) {
            return UpgradeResult.NOT_ENOUGH_COPPER;
        }
        profile.setCropUpgradeTier(crop, next.get().tier());
        return UpgradeResult.UPGRADED;
    }

    public static int totalUpgradeTiers(GardenProfile profile) {
        int total = 0;
        for (GardenCrop crop : GardenCrop.values()) {
            total += profile.cropUpgradeTier(crop);
        }
        return total;
    }

    public static int cropFortune(GardenProfile profile, GardenCrop crop) {
        return profile.cropUpgradeTier(crop) * GardenConfig.get().fortunePerUpgradeTier();
    }

    public static PlotQuote quotePlot(GardenProfile profile, int plot) {
        GardenConfig config = GardenConfig.get();
        for (GardenConfig.PlotGroup group : config.plotGroups()) {
            if (!group.plots().contains(plot)) {
                continue;
            }
            if (profile.isPlotUnlocked(plot)) {
                return new PlotQuote(PlotStatus.ALREADY_UNLOCKED, 0, group.minGardenLevel());
            }
            long owned = group.plots().stream().filter(profile::isPlotUnlocked).count();
            int cost = group.costs().get((int) owned);
            PlotStatus status = PlotStatus.UNLOCKABLE;
            if (!GardenLayout.touchesBarnOrOwnedPlot(plot, profile.getUnlockedPlots())) {
                status = PlotStatus.NOT_ADJACENT;
            } else if (level(profile) < group.minGardenLevel()) {
                status = PlotStatus.LEVEL_TOO_LOW;
            }
            return new PlotQuote(status, cost, group.minGardenLevel());
        }
        return new PlotQuote(PlotStatus.INVALID, 0, 0);
    }

    public static int visitorsUnlocked(GardenProfile profile) {
        int level = level(profile);
        return GardenConfig.get().levels().stream().filter(entry -> entry.level() <= level).mapToInt(GardenConfig.Level::newVisitors).sum();
    }

    public static int cropGrowth(GardenProfile profile) {
        int level = level(profile);
        return GardenConfig.get().levels().stream().filter(entry -> entry.level() <= level).mapToInt(GardenConfig.Level::cropGrowth).sum();
    }
}
