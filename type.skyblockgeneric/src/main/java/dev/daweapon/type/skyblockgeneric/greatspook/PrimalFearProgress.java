package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.generic.data.datapoints.DatapointInteger;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class PrimalFearProgress {
    private PrimalFearProgress() {
    }

    public static long lastSpawn(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PRIMAL_FEAR_LAST_SPAWN, DatapointLong.class).getValue();
    }

    public static void markSpawn(SkyBlockPlayer player, long now) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PRIMAL_FEAR_LAST_SPAWN, DatapointLong.class).setValue(now);
    }

    public static int kills(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PRIMAL_FEAR_KILLS, DatapointInteger.class).getValue();
    }

    public static void recordKill(SkyBlockPlayer player) {
        DatapointInteger kills = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.PRIMAL_FEAR_KILLS, DatapointInteger.class);
        kills.setValue(kills.getValue() + 1);
    }

    public static long remainingMillis(SkyBlockPlayer player) {
        return PrimalFearRules.remainingMillis(lastSpawn(player), GreatSpookFear.fear(player), System.currentTimeMillis());
    }
}
