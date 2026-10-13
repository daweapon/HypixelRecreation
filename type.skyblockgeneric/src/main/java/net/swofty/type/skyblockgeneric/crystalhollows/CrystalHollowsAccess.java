package net.swofty.type.skyblockgeneric.crystalhollows;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCrystalHollows;
import net.swofty.type.skyblockgeneric.hotm.HotmService;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CrystalHollowsAccess {
    public static final int REQUIRED_HOTM_TIER = 4;
    public static final int REQUIRED_MINING_LEVEL = 12;
    public static final int PERMANENT_HOTM_TIER = 7;
    public static final double PASS_PRICE = 10_000;
    public static final long PASS_DURATION_MILLIS = 6L * 60 * 60 * 1000;
    private static final long FULL_ACCESS_PASS_MILLIS = 30L * 24 * 60 * 60 * 1000;
    public static final String HELPED_DULIN_FLAG = "helped_dulin";

    private CrystalHollowsAccess() {
    }

    public static DatapointCrystalHollows datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CRYSTAL_HOLLOWS, DatapointCrystalHollows.class);
    }

    public static DatapointCrystalHollows.CrystalHollowsData data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    public static void save(SkyBlockPlayer player, DatapointCrystalHollows.CrystalHollowsData data) {
        datapoint(player).setValue(data);
    }

    public static boolean meetsRequirements(SkyBlockPlayer player) {
        return HotmService.data(player).getTier() >= REQUIRED_HOTM_TIER
                && player.getSkills().getCurrentLevel(SkillCategories.MINING) >= REQUIRED_MINING_LEVEL;
    }

    public static String missingRequirement(SkyBlockPlayer player) {
        if (HotmService.data(player).getTier() < REQUIRED_HOTM_TIER) {
            return "Come back to me when you reach Heart of the Mountain Tier " + REQUIRED_HOTM_TIER + ".";
        }
        if (player.getSkills().getCurrentLevel(SkillCategories.MINING) < REQUIRED_MINING_LEVEL) {
            return "Come back to me when you reach Mining " + REQUIRED_MINING_LEVEL + ".";
        }
        return null;
    }

    public static boolean hasPermanentAccess(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = data(player);
        return data.isPermanentAccess() && HotmService.data(player).getTier() >= PERMANENT_HOTM_TIER;
    }

    public static boolean hasValidPass(SkyBlockPlayer player) {
        return hasPermanentAccess(player) || data(player).getPassExpiresAt() > System.currentTimeMillis();
    }

    public static long millisLeft(SkyBlockPlayer player) {
        return Math.max(0, data(player).getPassExpiresAt() - System.currentTimeMillis());
    }

    public static boolean canUnlockPermanent(SkyBlockPlayer player) {
        return HotmService.data(player).getTier() >= PERMANENT_HOTM_TIER
                && data(player).hasFlag(HELPED_DULIN_FLAG) && !data(player).isPermanentAccess();
    }

    public static void unlockPermanent(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = data(player);
        data.setPermanentAccess(true);
        save(player, data);
    }

    public static void markHelpedDulin(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = data(player);
        data.getFlags().add(HELPED_DULIN_FLAG);
        save(player, data);
    }

    public static String buyPass(SkyBlockPlayer player) {
        String missing = missingRequirement(player);
        if (missing != null) return missing;
        if (player.getCoins() < PASS_PRICE) return "You don't have enough coins!";
        player.removeCoins(PASS_PRICE);
        DatapointCrystalHollows.CrystalHollowsData data = data(player);
        data.setPassExpiresAt(System.currentTimeMillis() + PASS_DURATION_MILLIS);
        save(player, data);
        return null;
    }

    public static void grantFullAccess(SkyBlockPlayer player) {
        DatapointCrystalHollows.CrystalHollowsData data = data(player);
        data.setPermanentAccess(true);
        data.getFlags().add(HELPED_DULIN_FLAG);
        data.setPassExpiresAt(Math.max(data.getPassExpiresAt(), System.currentTimeMillis() + FULL_ACCESS_PASS_MILLIS));
        save(player, data);
    }
}
