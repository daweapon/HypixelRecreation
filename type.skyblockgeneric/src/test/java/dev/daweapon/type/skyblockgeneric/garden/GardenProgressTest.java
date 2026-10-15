package dev.daweapon.type.skyblockgeneric.garden;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardenProgressTest {
    @BeforeAll
    static void loadConfig() {
        GardenConfig.load(Path.of("../configuration/skyblock/garden"));
    }

    @Test
    void configMatchesTheTotalsTheXpSourcesExpect() {
        GardenConfig config = GardenConfig.get();

        assertEquals(15, config.maxLevel());
        assertEquals(46, config.maxMilestoneTier());
        assertEquals(9, config.maxUpgradeTier());
        assertEquals(140, config.levels().stream().mapToInt(GardenConfig.Level::newVisitors).sum());
        assertEquals(140, config.levels().stream().mapToInt(GardenConfig.Level::skyblockXp).sum());
        assertEquals(13 * 46, GardenCrop.values().length * config.maxMilestoneTier() * config.milestoneTiers().get(0).skyblockXp());
        assertEquals(24, config.plotGroups().stream().mapToInt(group -> group.plots().size()).sum());
        assertEquals(7685, config.upgradeTiers().stream().mapToInt(GardenConfig.UpgradeTier::copper).sum());
    }

    @Test
    void milestoneThresholdsMatchTheVideoTotals() {
        GardenConfig config = GardenConfig.get();

        assertEquals(38_836_820L, config.cumulativeThreshold(GardenCrop.WILD_ROSE, 45));
        assertEquals(19_418_410L, config.cumulativeThreshold(GardenCrop.SUNFLOWER, 45));
        assertEquals(19_418_760L, config.cumulativeThreshold(GardenCrop.MOONFLOWER, 45));
        assertEquals(63_644_500L, config.cumulativeThreshold(GardenCrop.CARROT, 45));
    }

    @Test
    void gardenXpRaisesTheLevelAndReportsEachLevelReached() {
        GardenProfile profile = new GardenProfile();

        assertEquals(1, GardenProgress.level(profile));
        assertEquals(List.of(2, 3), GardenProgress.addGardenXp(profile, 140));
        assertEquals(3, GardenProgress.level(profile));
        assertEquals(15, GardenProgress.level(maxed()));
        assertEquals(20, GardenProgress.skyblockXpFromLevel(profile));
    }

    @Test
    void cropsRaiseMilestoneTiersAndPayRewards() {
        GardenProfile profile = new GardenProfile();

        GardenProgress.MilestoneProgress first = GardenProgress.addCrops(profile, GardenCrop.WHEAT, 80);

        assertEquals(2, first.newTiers());
        assertEquals(30, first.gardenXp());
        assertEquals(350, first.farmingXp());
        assertEquals(2, first.skyblockXp());
        assertEquals(2, profile.milestoneTier(GardenCrop.WHEAT));
        assertEquals(0, GardenProgress.addCrops(profile, GardenCrop.WHEAT, 1).newTiers());
    }

    @Test
    void milestonesStopAtTheLastTier() {
        GardenProfile profile = new GardenProfile();

        GardenProgress.addCrops(profile, GardenCrop.CARROT, Long.MAX_VALUE / 4);

        assertEquals(46, profile.milestoneTier(GardenCrop.CARROT));
        assertEquals(46, GardenProgress.totalMilestoneTiers(profile));
    }

    @Test
    void cropUpgradesNeedLevelAndCopperAndRunInOrder() {
        GardenProfile profile = new GardenProfile();

        assertEquals(GardenProgress.UpgradeResult.LEVEL_TOO_LOW, GardenProgress.upgradeCrop(profile, GardenCrop.WHEAT));
        GardenProgress.addGardenXp(profile, 70);
        assertEquals(GardenProgress.UpgradeResult.NOT_ENOUGH_COPPER, GardenProgress.upgradeCrop(profile, GardenCrop.WHEAT));
        profile.addCopper(5);
        assertEquals(GardenProgress.UpgradeResult.UPGRADED, GardenProgress.upgradeCrop(profile, GardenCrop.WHEAT));
        assertEquals(5, GardenProgress.cropFortune(profile, GardenCrop.WHEAT));
        assertEquals(1, GardenProgress.totalUpgradeTiers(profile));
        assertEquals(GardenProgress.UpgradeResult.LEVEL_TOO_LOW, GardenProgress.upgradeCrop(profile, GardenCrop.WHEAT));
    }

    @Test
    void plotCostsFollowTheGroupAndHowManyAreAlreadyOwned() {
        GardenProfile profile = new GardenProfile();
        GardenProgress.addGardenXp(profile, 140);

        assertEquals(new GardenProgress.PlotQuote(GardenProgress.PlotStatus.UNLOCKABLE, 1, 1), GardenProgress.quotePlot(profile, 1));
        profile.unlockPlot(1);
        assertEquals(2, GardenProgress.quotePlot(profile, 2).compostCost());
        assertEquals(GardenProgress.PlotStatus.ALREADY_UNLOCKED, GardenProgress.quotePlot(profile, 1).status());
        assertEquals(16, GardenProgress.quotePlot(profile, 8).compostCost());
        assertEquals(GardenProgress.PlotStatus.NOT_ADJACENT, GardenProgress.quotePlot(profile, 8).status());
        assertEquals(GardenProgress.PlotStatus.LEVEL_TOO_LOW, GardenProgress.quotePlot(profile, 9).status());
        assertEquals(GardenProgress.PlotStatus.UNLOCKABLE, GardenProgress.quotePlot(profile, 6).status());
        assertEquals(GardenProgress.PlotStatus.NOT_ADJACENT, GardenProgress.quotePlot(profile, 24).status());
        assertEquals(GardenProgress.PlotStatus.INVALID, GardenProgress.quotePlot(profile, 25).status());
    }

    @Test
    void plotsMustTouchTheBarnOrAnOwnedPlot() {
        GardenProfile profile = new GardenProfile();
        GardenProgress.addGardenXp(profile, 1_000_000);

        assertEquals(GardenProgress.PlotStatus.UNLOCKABLE, GardenProgress.quotePlot(profile, 1).status());
        assertEquals(GardenProgress.PlotStatus.NOT_ADJACENT, GardenProgress.quotePlot(profile, 5).status());
        profile.unlockPlot(1);
        assertEquals(GardenProgress.PlotStatus.UNLOCKABLE, GardenProgress.quotePlot(profile, 5).status());
        assertEquals(GardenProgress.PlotStatus.UNLOCKABLE, GardenProgress.quotePlot(profile, 9).status());
        assertEquals(java.util.Set.of(1, 2, 3, 4), GardenLayout.neighbours(GardenLayout.BARN));
    }

    @Test
    void levelsUnlockCropsAndVisitors() {
        GardenProfile profile = new GardenProfile();

        assertTrue(GardenProgress.cropUnlocked(profile, GardenCrop.WHEAT));
        assertTrue(!GardenProgress.cropUnlocked(profile, GardenCrop.CARROT));
        assertEquals(47, GardenProgress.visitorsUnlocked(profile));
        assertEquals(140, GardenProgress.visitorsUnlocked(maxed()));
        assertEquals(140, GardenProgress.cropGrowth(maxed()));
    }

    private static GardenProfile maxed() {
        GardenProfile profile = new GardenProfile();
        GardenProgress.addGardenXp(profile, 1_000_000);
        return profile;
    }
}
