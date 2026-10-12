package net.swofty.type.skyblockgeneric.entity.mob;

import net.swofty.type.skyblockgeneric.data.datapoints.DatapointCollectedMobTypeRewards;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.levels.causes.CollectedMobTypesCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

public final class CollectedMobTypes {
    private CollectedMobTypes() {
    }

    public static void claim(SkyBlockPlayer player, MobType mobType) {
        player.getCollectedMobTypesData().claim(mobType.name());
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        DatapointCollectedMobTypeRewards.PlayerCollectedMobTypeRewards rewards = player.getCollectedMobTypesData();
        player.getSkyBlockExperience().batch(() -> {
            for (MobType mobType : MobType.values()) {
                if (rewards.hasClaimed(mobType.name())) award(player, mobType.name());
            }
            if (hasClaimedAll(rewards)) award(player, CollectedMobTypesCause.ALL_TYPES);
        });
    }

    public static boolean hasClaimedAll(DatapointCollectedMobTypeRewards.PlayerCollectedMobTypeRewards rewards) {
        return Arrays.stream(MobType.values()).allMatch(type -> rewards.hasClaimed(type.name()));
    }

    private static void award(SkyBlockPlayer player, String causeName) {
        player.getSkyBlockExperience().addExperience(SkyBlockLevelCause.getCause("mob-type-" + causeName));
    }
}
