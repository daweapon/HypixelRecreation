package dev.daweapon.type.theend.dragon;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

public final class DragonProgress {
    private DragonProgress() {
    }

    private static String key(DragonVariant variant) {
        return "the_end:dragon_slain:" + variant.name();
    }

    public static boolean slain(SkyBlockPlayer player, DragonVariant variant) {
        return DatapointCrimsonIsle.of(player).getNumber(key(variant)) > 0;
    }

    public static int skyBlockXp(SkyBlockPlayer player) {
        return Arrays.stream(DragonVariant.values()).filter(variant -> slain(player, variant))
                .mapToInt(DragonVariant::firstKillXp).sum();
    }

    public static void recordKill(SkyBlockPlayer player, DragonVariant variant) {
        if (!slain(player, variant)) DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(key(variant), 1L));
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.SLAY_DRAGONS,
                Math.max(player.getSkyBlockExperience().getSourceXP(SkyBlockXPSource.SLAY_DRAGONS), skyBlockXp(player)));
        player.getAchievementHandler().completeAchievement("skyblock.dragon_slayer");
    }
}
