package dev.daweapon.type.skyblockgeneric.garden;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.garden.quest.GardenQuests;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterConfig;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseConfig;
import dev.daweapon.type.skyblockgeneric.garden.composter.ComposterUpgrade;
import dev.daweapon.type.skyblockgeneric.garden.visitor.VisitorCatalogue;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class GardenService {
    private GardenService() {
    }

    public static GardenProfile profile(SkyBlockPlayer player) {
        return Gardens.profile(player);
    }

    public static int level(SkyBlockPlayer player) {
        return GardenProgress.level(profile(player));
    }

    public static void addGardenXp(SkyBlockPlayer player, double amount) {
        GardenProfile profile = profile(player);
        List<Integer> reached = GardenProgress.addGardenXp(profile, amount);
        Gardens.save(player, profile);
        for (int level : reached) {
            player.sendMessage(Text.key("garden.level_up", level));
        }
        syncExperience(player);
    }

    public static void recordCrops(SkyBlockPlayer player, GardenCrop crop, long amount) {
        GardenProfile profile = profile(player);
        int previousTier = profile.milestoneTier(crop);
        GardenProgress.MilestoneProgress progress = GardenProgress.addCrops(profile, crop, amount);
        Gardens.save(player, profile);
        if (progress.newTiers() > 0) {
            sendMilestoneMessage(player, crop, previousTier, profile.milestoneTier(crop), progress);
            if (progress.farmingXp() > 0) {
                player.getSkills().increase(player, SkillCategories.FARMING, (double) progress.farmingXp());
            }
            addGardenXp(player, progress.gardenXp());
        } else {
            syncExperience(player);
        }
    }

    public static GardenProgress.UpgradeResult upgradeCrop(SkyBlockPlayer player, GardenCrop crop) {
        GardenProfile profile = profile(player);
        GardenProgress.UpgradeResult result = GardenProgress.upgradeCrop(profile, crop);
        if (result == GardenProgress.UpgradeResult.UPGRADED) {
            Gardens.save(player, profile);
            syncExperience(player);
            playExperienceSound(player);
        }
        return result;
    }

    public static GardenProgress.PlotQuote unlockPlot(SkyBlockPlayer player, int plot) {
        GardenProfile profile = profile(player);
        GardenProgress.PlotQuote quote = GardenProgress.quotePlot(profile, plot);
        if (quote.status() != GardenProgress.PlotStatus.UNLOCKABLE || !payCompost(player, quote.compostCost())) {
            return quote.status() == GardenProgress.PlotStatus.UNLOCKABLE
                    ? new GardenProgress.PlotQuote(GardenProgress.PlotStatus.INVALID, quote.compostCost(), quote.requiredLevel())
                    : quote;
        }
        profile.unlockPlot(plot);
        Gardens.save(player, profile);
        syncExperience(player);
        GardenQuests.plotUnlocked(player);
        return quote;
    }

    public static boolean canAffordCompost(SkyBlockPlayer player, int compost) {
        int perBundle = GardenConfig.get().compostPerBundle();
        return player.getAmountAnywhere(ItemType.COMPOST) + (long) player.getAmountAnywhere(ItemType.COMPOST_BUNDLE) * perBundle >= compost;
    }

    public static boolean payCompost(SkyBlockPlayer player, int compost) {
        if (!canAffordCompost(player, compost)) {
            return false;
        }
        int perBundle = GardenConfig.get().compostPerBundle();
        int bundlesUsed = Math.min(player.getAmountAnywhere(ItemType.COMPOST_BUNDLE), compost / perBundle);
        int remainder = compost - bundlesUsed * perBundle;
        int loose = player.getAmountAnywhere(ItemType.COMPOST);
        if (loose >= remainder) {
            if (bundlesUsed > 0) {
                player.removeItemFromPlayer(ItemType.COMPOST_BUNDLE, bundlesUsed);
            }
            if (remainder > 0) {
                player.removeItemFromPlayer(ItemType.COMPOST, remainder);
            }
            return true;
        }
        player.removeItemFromPlayer(ItemType.COMPOST_BUNDLE, bundlesUsed + 1);
        if (loose > 0) {
            player.removeItemFromPlayer(ItemType.COMPOST, loose);
        }
        int refund = (bundlesUsed + 1) * perBundle + loose - compost;
        if (refund > 0) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.COMPOST, refund));
        }
        return true;
    }

    private static void sendMilestoneMessage(SkyBlockPlayer player, GardenCrop crop, int from, int to, GardenProgress.MilestoneProgress progress) {
        player.sendMessage("<2><m>---------------------------------");
        player.sendMessage("  <2><l>GARDEN MILESTONE </l><a>{} <8>{} ➜ <a>{}", crop.getDisplayName(), from, to);
        player.sendMessage(" ");
        player.sendMessage("  <a><l>REWARDS");
        if (progress.gardenXp() > 0) player.sendMessage("   <8>+<2>{} Garden Experience", StringUtility.commaify(progress.gardenXp()));
        if (progress.farmingXp() > 0) player.sendMessage("   <8>+<3>{} Farming Experience", StringUtility.commaify(progress.farmingXp()));
        if (progress.skyblockXp() > 0) player.sendMessage("   <8>+<b>{} SkyBlock XP", StringUtility.commaify(progress.skyblockXp()));
        player.sendMessage("<2><m>---------------------------------");
    }

    public static void playExperienceSound(SkyBlockPlayer player) {
        player.playSound(net.kyori.adventure.sound.Sound.sound(net.kyori.adventure.key.Key.key("entity.experience_orb.pickup"),
                net.kyori.adventure.sound.Sound.Source.PLAYER, 1f, 2f), net.kyori.adventure.sound.Sound.Emitter.self());
    }

    public static void syncExperience(SkyBlockPlayer player) {
        GardenProfile profile = profile(player);
        var experience = player.getSkyBlockExperience();
        experience.batch(() -> {
            experience.setSourceXP(SkyBlockXPSource.GARDEN_LEVEL, GardenProgress.skyblockXpFromLevel(profile));
            experience.setSourceXP(SkyBlockXPSource.GARDEN_CROP_MILESTONES_REACHED, GardenProgress.totalMilestoneTiers(profile));
            experience.setSourceXP(SkyBlockXPSource.CROP_UPGRADES, GardenProgress.totalUpgradeTiers(profile));
            experience.setSourceXP(SkyBlockXPSource.GARDEN_VISITORS_SERVED, (double) profile.visitors().getUniqueTier() * 3);
            experience.setSourceXP(SkyBlockXPSource.OFFERS_ACCEPTED, (double) profile.visitors().getAcceptedTier() * 3);
            experience.setSourceXP(SkyBlockXPSource.COMPOSTER_UPGRADES, (double) ComposterConfig.get().skyblockXp(profile.composter().getTiers()));
            experience.setSourceXP(SkyBlockXPSource.GREENHOUSE_UPGRADES, (double) (profile.greenhouse().getGrowthSpeedTier() + profile.greenhouse().getPlantYieldTier() + profile.greenhouse().getPlotLimitTier()) * 5);
            experience.setSourceXP(SkyBlockXPSource.DNA_ANALYSIS_MILESTONES, (double) profile.greenhouse().getDnaClaimed() * 15);
            experience.setSourceXP(SkyBlockXPSource.FARMING_TOOLKIT, profile.toolkit().isUnlocked() ? 2.0 : 0.0);
            experience.setSourceXP(SkyBlockXPSource.GARDEN_UNLOCKED_PLOTS,
                    (double) profile.getUnlockedPlots().size() * GardenConfig.get().skyblockXpPerPlot());
        });
    }

    public static void maxOut(SkyBlockPlayer player) {
        GardenConfig config = GardenConfig.get();
        GardenProfile profile = profile(player);
        profile.setAccessUnlocked(true);
        profile.toolkit().setUnlocked(true);
        profile.setGardenXp(config.level(config.maxLevel()).cumulativeXp());
        for (GardenCrop crop : GardenCrop.values()) {
            profile.addCropCount(crop, config.cumulativeThreshold(crop, config.maxMilestoneTier()) - profile.cropCount(crop));
            profile.setMilestoneTier(crop, config.maxMilestoneTier());
            profile.setCropUpgradeTier(crop, config.maxUpgradeTier());
        }
        for (int plot = 1; plot <= GardenProfile.PLOT_COUNT; plot++) {
            profile.unlockPlot(plot);
        }
        for (ComposterUpgrade upgrade : ComposterUpgrade.values()) {
            profile.composter().setTier(upgrade, ComposterUpgrade.MAX_TIER);
        }
        GreenhouseConfig greenhouse = GreenhouseConfig.get();
        profile.greenhouse().setGrowthSpeedTier(greenhouse.maxTier(GreenhouseConfig.Upgrade.GROWTH_SPEED));
        profile.greenhouse().setPlantYieldTier(greenhouse.maxTier(GreenhouseConfig.Upgrade.PLANT_YIELD));
        profile.greenhouse().setPlotLimitTier(greenhouse.maxTier(GreenhouseConfig.Upgrade.PLOT_LIMIT));
        profile.greenhouse().setBlueprintOffered(true);
        profile.greenhouse().setBlueprintGiven(true);
        profile.greenhouse().setBlueprintDelivered(true);
        profile.greenhouse().setResourcesDelivered(true);
        for (var mutation : dev.daweapon.type.skyblockgeneric.garden.greenhouse.MutationCatalogue.get().all()) {
            profile.greenhouse().getAnalyzed().add(mutation.id());
        }
        profile.greenhouse().setDnaClaimed(dev.daweapon.type.skyblockgeneric.garden.analyzer.AnalyzerService.MILESTONE_TARGETS.length);
        profile.greenhouse().setFlasksConsumed(dev.daweapon.type.skyblockgeneric.garden.greenhouse.RosewaterFlask.PERMANENT_LIMIT);
        for (var chip : dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.values()) {
            profile.chips().setConsumed(chip, dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.CONSUMED_FOR_RARITY[2]);
            profile.chips().setLevel(chip, dev.daweapon.type.skyblockgeneric.garden.chip.GardenChip.MAX_LEVEL[2]);
        }
        profile.chips().setJeffStage(3);
        profile.pests().setMetPhillip(true);
        profile.pests().setMetPamela(true);
        profile.pests().setPhillipDebriefed(true);
        VisitorCatalogue visitors = VisitorCatalogue.get();
        visitors.visitors().forEach(visitor -> profile.visitors().markServed(visitor.id()));
        profile.visitors().setUniqueTier(visitors.uniqueMilestones().getLast().tier());
        profile.visitors().setAcceptedTier(visitors.acceptedMilestones().getLast().tier());
        Gardens.save(player, profile);
        syncExperience(player);
    }
}
