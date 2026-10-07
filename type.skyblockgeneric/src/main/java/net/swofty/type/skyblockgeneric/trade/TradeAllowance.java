package net.swofty.type.skyblockgeneric.trade;

import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * How many coins a profile may still send through trades today. The limit grows with SkyBlock
 * Level and resets at midnight UTC.
 */
public final class TradeAllowance {
    private static final long BASE_LIMIT = 50_000_000L;
    private static final long LEVEL_50_LIMIT = 1_000_000_000L;
    private static final long LEVEL_100_LIMIT = 10_000_000_000L;

    private TradeAllowance() {}

    public static long dailyLimit(SkyBlockPlayer player) {
        int level = player.getSkyBlockExperience().getLevel().asInt();
        if (level >= 100) return LEVEL_100_LIMIT;
        if (level >= 50) return LEVEL_50_LIMIT;
        return BASE_LIMIT;
    }

    public static long sentToday(SkyBlockPlayer player) {
        String[] stored = datapoint(player).getValue().split(":");
        if (stored.length != 2 || !stored[0].equals(today())) return 0;
        try {
            return Long.parseLong(stored[1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static long remaining(SkyBlockPlayer player) {
        return Math.max(0, dailyLimit(player) - sentToday(player));
    }

    static void recordWithoutWrite(SkyBlockPlayer player, long coins) {
        if (coins <= 0) return;
        datapoint(player).setFrom(new DatapointString(SkyBlockDataHandler.Data.TRADE_COINS_SENT.getKey(),
                today() + ":" + (sentToday(player) + coins)));
    }

    private static DatapointString datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.TRADE_COINS_SENT, DatapointString.class);
    }

    private static String today() {
        return String.valueOf(LocalDate.now(ZoneOffset.UTC).toEpochDay());
    }
}
