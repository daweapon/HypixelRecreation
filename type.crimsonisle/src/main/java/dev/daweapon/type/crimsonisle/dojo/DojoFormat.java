package dev.daweapon.type.crimsonisle.dojo;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoGrade;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoTest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/** Text shared by the menus and the result screen. */
public final class DojoFormat {
    private DojoFormat() {
    }

    /** Hypixel's Dojo time format, minutes:seconds:hundredths. */
    public static String time(long millis) {
        long minutes = millis / 60_000;
        long seconds = (millis / 1000) % 60;
        long hundredths = (millis / 10) % 100;
        return String.format("%02d:%02d:%02d", minutes, seconds, hundredths);
    }

    public static String clock(long millis) {
        long totalSeconds = millis / 1000;
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    public static String rankLine(SkyBlockPlayer player, DojoTest test) {
        if (!DojoProgress.hasAttempted(player, test)) return "<6>Your Rank<7>: <8>None";
        int best = DojoProgress.best(player, test);
        return "<6>Your Rank<7>: " + DojoGrade.of(best).coloured() + " <8>(" + StringUtility.commaify(best) + ")";
    }

    public static String timeLine(SkyBlockPlayer player, DojoTest test) {
        return "<7>Time: <b>" + time(DojoProgress.bestTimeMillis(player, test));
    }

    public static String compact(long value) {
        if (value >= 1000 && value % 1000 == 0) return (value / 1000) + "k";
        if (value >= 1000) return String.format("%.1fk", value / 1000.0);
        return Long.toString(value);
    }
}
