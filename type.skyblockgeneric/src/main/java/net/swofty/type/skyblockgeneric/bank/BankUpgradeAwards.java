package net.swofty.type.skyblockgeneric.bank;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * SkyBlock XP for bank account upgrades (wiki: SkyBlock_Levels/Tasks, Bank Upgrades): Gold 20, Deluxe 25,
 * Super Deluxe 30, Premier 35, Luxurious 40, Palatial 50, and a higher account also keeps every lower tier's XP.
 */
public final class BankUpgradeAwards {
    private BankUpgradeAwards() {
    }

    public static int xpForTier(BankAccountTier tier) {
        return switch (tier) {
            case STARTER -> 0;
            case GOLD -> 20;
            case DELUXE -> 25;
            case SUPER_DELUXE -> 30;
            case PREMIER -> 35;
            case LUXURIOUS -> 40;
            case PALATIAL -> 50;
        };
    }

    public static int skyBlockXp(BankAccountTier current) {
        int total = 0;
        for (BankAccountTier tier : BankAccountTier.values()) {
            if (tier.ordinal() <= current.ordinal()) total += xpForTier(tier);
        }
        return total;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        DatapointBankData.BankData data = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue();
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.BANK_UPGRADES, skyBlockXp(data.getAccountTier()));
    }
}
