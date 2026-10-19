package dev.daweapon.type.theend.npcs;

import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointCrimsonIsle;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class LoneAdventurerProgress {
    private static final String STAGE = "the_end:lone_adventurer:stage";
    private static final String ENDERMEN = "the_end:lone_adventurer:endermen";
    private static final String SHORTBOW_AT = "the_end:lone_adventurer:shortbow_at";

    private LoneAdventurerProgress() {
    }

    public static LoneAdventurerStage stage(SkyBlockPlayer player) {
        return LoneAdventurerStage.fromOrdinal(DatapointCrimsonIsle.of(player).getNumber(STAGE));
    }

    public static void setStage(SkyBlockPlayer player, LoneAdventurerStage stage) {
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(STAGE, (long) stage.ordinal()));
    }

    public static int endermenKilled(SkyBlockPlayer player) {
        return (int) DatapointCrimsonIsle.of(player).getNumber(ENDERMEN);
    }

    public static void recordEndermanKill(SkyBlockPlayer player) {
        if (stage(player) != LoneAdventurerStage.KILLING_ENDERMEN) return;
        int killed = endermenKilled(player);
        if (killed >= LoneAdventurerStage.ENDERMEN_REQUIRED) return;
        DatapointCrimsonIsle.edit(player, data -> data.getNumbers().put(ENDERMEN, killed + 1L));
    }

    public static void receiveShortbow(SkyBlockPlayer player) {
        DatapointCrimsonIsle.edit(player, data -> {
            data.getNumbers().put(STAGE, (long) LoneAdventurerStage.GUIDE_WAIT.ordinal());
            data.getNumbers().put(SHORTBOW_AT, System.currentTimeMillis());
        });
    }

    public static long millisSinceShortbow(SkyBlockPlayer player) {
        long at = DatapointCrimsonIsle.of(player).getNumber(SHORTBOW_AT);
        return at == 0 ? 0 : System.currentTimeMillis() - at;
    }
}
