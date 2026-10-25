package dev.daweapon.type.skyblockgeneric.greatspook;

import dev.daweapon.type.generic.data.datapoints.DatapointMapStringLong;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockLevelCause;
import dev.daweapon.type.skyblockgeneric.levels.causes.VargulMilestoneCause;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public final class VargulProgress {
    public static final int EMBLEM_MILESTONE = 6;
    public static final int EMBLEM_CANDY = 600;
    private static final String SPENT = "spent";
    private static final String VISITS = "visits";

    private VargulProgress() {
    }

    private static DatapointMapStringLong datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.GREAT_SPOOK_VARGUL, DatapointMapStringLong.class);
    }

    private static void put(SkyBlockPlayer player, String key, long value) {
        DatapointMapStringLong datapoint = datapoint(player);
        Map<String, Long> updated = new HashMap<>(datapoint.getValue());
        updated.put(key, value);
        datapoint.setValue(updated);
    }

    public static int spent(SkyBlockPlayer player) {
        return (int) (long) datapoint(player).getValue().getOrDefault(SPENT, 0L);
    }

    public static int visits(SkyBlockPlayer player) {
        return (int) (long) datapoint(player).getValue().getOrDefault(VISITS, 0L);
    }

    public static int recordVisit(SkyBlockPlayer player) {
        int visit = visits(player) + 1;
        put(player, VISITS, visit);
        return visit;
    }

    public static void recordSpent(SkyBlockPlayer player, int candy) {
        if (candy <= 0) return;
        int total = spent(player) + candy;
        put(player, SPENT, total);
        if (total >= EMBLEM_CANDY) grantEmblem(player);
    }

    public static void setSpent(SkyBlockPlayer player, int total) {
        put(player, SPENT, total);
        if (total >= EMBLEM_CANDY) grantEmblem(player);
    }

    public static boolean hasEmblem(SkyBlockPlayer player) {
        VargulMilestoneCause cause = SkyBlockLevelCause.getVargulMilestoneCause(EMBLEM_MILESTONE);
        return cause != null && player.hasUnlockedXPCause(cause);
    }

    private static void grantEmblem(SkyBlockPlayer player) {
        VargulMilestoneCause cause = SkyBlockLevelCause.getVargulMilestoneCause(EMBLEM_MILESTONE);
        if (cause == null || player.hasUnlockedXPCause(cause)) return;
        player.getSkyBlockExperience().addExperience(cause);
        player.sendMessage("<8>  +<a>Great Spook Emblem <5>࿇");
    }

    public static void reset(SkyBlockPlayer player) {
        datapoint(player).setValue(new HashMap<>());
    }
}
