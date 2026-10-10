package net.swofty.type.skyblockgeneric.achievement;

import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCommissions;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.museum.MuseumService;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;

public final class SkyBlockAchievementSync {
    private static final Map<SkillCategories, String> SKILL_ACHIEVEMENTS = Map.of(
            SkillCategories.FORAGING, "skyblock.gatherer",
            SkillCategories.ENCHANTING, "skyblock.augmentation",
            SkillCategories.TAMING, "skyblock.domesticator",
            SkillCategories.FARMING, "skyblock.harvester",
            SkillCategories.MINING, "skyblock.excavator",
            SkillCategories.COMBAT, "skyblock.combat",
            SkillCategories.HUNTING, "skyblock.predator",
            SkillCategories.FISHING, "skyblock.angler",
            SkillCategories.ALCHEMY, "skyblock.concoctor"
    );

    private SkyBlockAchievementSync() {
    }

    public static void syncAll(SkyBlockPlayer player) {
        SKILL_ACHIEVEMENTS.keySet().forEach(skill -> syncSkill(player, skill));
        syncCollections(player);
        syncCommissions(player);
        syncSkyBlockLevel(player);
        syncSlayer(player);
        syncMuseum(player);
        syncFairySouls(player);
    }

    public static void syncSkill(SkyBlockPlayer player, SkillCategories skill) {
        String achievement = SKILL_ACHIEVEMENTS.get(skill);
        if (achievement == null) return;
        handler(player).setProgress(achievement, player.getSkills().getCurrentLevel(skill));
    }

    public static void syncCollections(SkyBlockPlayer player) {
        var collection = player.getCollection();
        int unlocked = 0;
        for (CollectionCategory category : CollectionCategories.getCategories()) {
            for (CollectionCategory.ItemCollection itemCollection : category.getCollections()) {
                if (collection.unlocked(itemCollection.type())) unlocked++;
            }
        }
        handler(player).setProgress("skyblock.treasury", unlocked);
    }

    public static void syncCommissions(SkyBlockPlayer player) {
        handler(player).setProgress("skyblock.hard_working_miner", player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COMMISSIONS, DatapointCommissions.class).getValue().getTotalCompleted());
    }

    public static void syncSkyBlockLevel(SkyBlockPlayer player) {
        handler(player).setProgress("skyblock.climbing_the_ranks", player.getSkyBlockExperience().getLevel().asInt());
    }

    public static void syncSlayer(SkyBlockPlayer player) {
        long totalXp = player.getSlayerData().getProgress().values().stream()
                .mapToLong(DatapointSlayer.SlayerProgress::getXp)
                .sum();
        handler(player).setProgress("skyblock.slayer", (int) Math.min(Integer.MAX_VALUE, totalXp / 1000));
    }

    public static void syncMuseum(SkyBlockPlayer player) {
        handler(player).setProgress("skyblock.curator", (int) MuseumService.donatedCount(player.getMuseumData()));
    }

    public static void syncFairySouls(SkyBlockPlayer player) {
        int found = player.getFairySouls().getAllFairySouls().size();
        if (found > 0) handler(player).setProgressByTrigger("skyblock.fairy_soul_found", found);
    }

    private static PlayerAchievementHandler handler(SkyBlockPlayer player) {
        return player.getAchievementHandler();
    }
}
