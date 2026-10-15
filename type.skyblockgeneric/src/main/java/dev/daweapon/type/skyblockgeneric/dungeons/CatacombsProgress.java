package dev.daweapon.type.skyblockgeneric.dungeons;

import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.level.CatacombsLevels;
import dev.daweapon.dungeons.catacombs.CatacombsMode;
import dev.daweapon.dungeons.catacombs.run.DungeonScoreRank;
import dev.daweapon.type.generic.data.datapoints.DatapointLong;
import dev.daweapon.type.generic.data.datapoints.DatapointString;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.LongSupplier;

/**
 * Per-profile Catacombs progression: the Catacombs XP and level, completed runs per floor and the daily XP bonus.
 * {@link #completeRun} is the single hook the Catacombs server calls when a run is completed. The SkyBlock XP
 * sources (Complete Dungeons, Catacombs Level Up) stay NOT_IN_GAME until the dungeon is public.
 */
public final class CatacombsProgress {
    private static volatile LongSupplier today = () -> LocalDate.now(ZoneOffset.UTC).toEpochDay();

    private CatacombsProgress() {
    }

    public static void useDayClock(LongSupplier clock) {
        today = clock;
    }

    public static long xp(SkyBlockPlayer player) {
        Long stored = xpPoint(player).getValue();
        return stored == null ? 0 : stored;
    }

    public static int level(SkyBlockPlayer player) {
        return level(xp(player));
    }

    public static int level(long xp) {
        return CatacombsLevels.levelForXp(xp);
    }

    public static long completions(SkyBlockPlayer player, CatacombsFloor floor, CatacombsMode mode) {
        return completionsOf(player).count(floor, mode);
    }

    public static long totalCompletions(SkyBlockPlayer player) {
        return completionsOf(player).total();
    }

    public static void completeRun(SkyBlockPlayer player, CatacombsFloor floor, CatacombsMode mode, int score, DungeonScoreRank rank) {
        long day = today.getAsLong();
        CatacombsDailyBonus daily = CatacombsDailyBonus.parse(dailyPoint(player).getValue());
        int bonus = daily.bonusPercent(day);
        long base = CatacombsRunXp.base(floor);
        long earned = CatacombsRunXp.withBonus(base, bonus);

        dailyPoint(player).setValue(daily.afterRun(day).encode());
        completionsPoint(player).setValue(completionsOf(player).with(floor, mode).encode());
        player.sendMessage(runXpLine(floor, rank, earned, bonus));
        addXp(player, earned);
    }

    public static String runXpLine(CatacombsFloor floor, DungeonScoreRank rank, long earned, int bonusPercent) {
        String bonus = bonusPercent > 0 ? " <8>(+" + bonusPercent + "% daily bonus)" : "";
        return "<3>+<b>" + String.format("%,d", earned) + " Catacombs Experience <8>(" + floor.displayName() + ", " + rank.displayName() + ")" + bonus;
    }

    public static void addXp(SkyBlockPlayer player, long amount) {
        if (amount <= 0) return;
        long before = xp(player);
        long after = Math.min(before + amount, CatacombsLevels.xpForLevel(CatacombsLevels.MAXIMUM_LEVEL));
        xpPoint(player).setValue(after);
        int from = level(before);
        int to = level(after);
        if (to > from) levelUpLines(from, to).forEach(player::sendMessage);
    }

    public static List<String> levelUpLines(int from, int to) {
        String bar = "<9><m>-----------------------------";
        return List.of(bar, "<3><l>  CATACOMBS LEVEL UP <b>Catacombs <8>" + from + "➜<3>" + to, bar);
    }

    public static void maxOut(SkyBlockPlayer player) {
        long max = CatacombsLevels.xpForLevel(CatacombsLevels.MAXIMUM_LEVEL);
        if (xp(player) < max) xpPoint(player).setValue(max);
    }

    private static CatacombsCompletions completionsOf(SkyBlockPlayer player) {
        return CatacombsCompletions.parse(completionsPoint(player).getValue());
    }

    private static DatapointLong xpPoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CATACOMBS_XP, DatapointLong.class);
    }

    private static DatapointString completionsPoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CATACOMBS_COMPLETIONS, DatapointString.class);
    }

    private static DatapointString dailyPoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CATACOMBS_DAILY, DatapointString.class);
    }
}
