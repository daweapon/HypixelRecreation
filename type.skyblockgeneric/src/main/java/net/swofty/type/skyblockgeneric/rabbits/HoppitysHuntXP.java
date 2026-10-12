package net.swofty.type.skyblockgeneric.rabbits;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.ChocolateFactoryData;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class HoppitysHuntXP {
    public static final int PRESTIGE_XP = 25;
    public static final int MAX_PRESTIGES = 5;
    public static final int MYTHIC_RABBIT_XP = 10;
    public static final int DIVINE_RABBIT_XP = 10;

    private HoppitysHuntXP() {
    }

    public static int total(int prestigeLevel, int mythicRabbits, int divineRabbits) {
        return Math.min(prestigeLevel, MAX_PRESTIGES) * PRESTIGE_XP
                + mythicRabbits * MYTHIC_RABBIT_XP
                + divineRabbits * DIVINE_RABBIT_XP;
    }

    public static int total(ChocolateFactoryData data) {
        return total(data.getPrestigeLevel(),
                countFound(data, ChocolateRabbit.RabbitRarity.MYTHIC),
                countFound(data, ChocolateRabbit.RabbitRarity.DIVINE));
    }

    private static int countFound(ChocolateFactoryData data, ChocolateRabbit.RabbitRarity rarity) {
        int count = 0;
        for (ChocolateRabbit rabbit : data.getFoundRabbits()) {
            if (rabbit.getRarity() == rarity) count++;
        }
        return count;
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.HOPPITYS_HUNT,
                total(ChocolateFactoryHelper.getData(player)));
    }
}
