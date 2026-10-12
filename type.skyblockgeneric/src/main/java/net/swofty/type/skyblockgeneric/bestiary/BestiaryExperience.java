package net.swofty.type.skyblockgeneric.bestiary;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class BestiaryExperience {
    public static final int TIERS_PER_MILESTONE = 10;
    public static final int MILESTONES_PER_REWARD = 10;
    public static final int MILESTONE_REWARD_XP = 10;

    private static final BestiaryData BESTIARY = new BestiaryData();

    private BestiaryExperience() {
    }

    public static double skyBlockXp(int totalTiers) {
        int rewardedMilestoneGroups = totalTiers / (TIERS_PER_MILESTONE * MILESTONES_PER_REWARD);
        return totalTiers + (double) rewardedMilestoneGroups * MILESTONE_REWARD_XP;
    }

    public static int totalTiers(DatapointBestiary.PlayerBestiary bestiary) {
        int total = 0;
        for (BestiaryCategories category : BestiaryCategories.values()) {
            for (BestiaryEntry entry : category.getEntries()) {
                List<BestiaryMob> mobs = entry.getMobs();
                if (mobs.isEmpty()) continue;
                total += BESTIARY.getCurrentBestiaryTier(mobs.getFirst(), bestiary.getAmount(mobs));
            }
        }
        return total;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.BESTIARY_PROGRESS, skyBlockXp(totalTiers(player.getBestiaryData())));
    }
}
