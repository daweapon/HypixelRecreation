package dev.daweapon.type.skyblockgeneric.starlyn;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class StarlynSidebar {
    private StarlynSidebar() {
    }

    public record Lines(String title, String bracket, String requirement) {
    }

    public static @Nullable Lines of(SkyBlockPlayer player) {
        StarlynSister sister = StarlynContests.islandSister();
        if (sister == null || player.getSkyblockDataHandler() == null) return null;
        long day = StarlynContests.currentDay();
        StarlynProfile.Entry entry = StarlynContests.data(player, sister).entryFor(day).orElse(null);
        if (entry == null) return null;
        long millisLeft = ((day + 1) * SkyBlockCalendar.DAY - SkyBlockCalendar.getElapsed()) * 50L;
        return of(sister, StarlynConfig.get(sister), entry.score(), millisLeft);
    }

    public static Lines of(StarlynSister sister, StarlynConfig config, long score, long millisLeft) {
        StarlynConfig.BracketReward reached = config.bracketFor(score);
        StarlynConfig.BracketReward next = config.brackets().stream()
                .filter(reward -> !reward.bracket().isHidden() && reward.points() > score)
                .findFirst().orElse(null);
        String title = "<e>" + sister.contestName() + " <a>" + StringUtility.formatTimeLeft(Math.max(0, millisLeft)).replace(" ", "");
        String bracket = reached == null
                ? "<f>Score <e>" + compact(score)
                : reached.bracket().getColor() + reached.bracket().name() + " <f>with <e>" + compact(score);
        String requirement;
        if (next != null) {
            requirement = label(next.bracket()) + " <7>requires <e>+" + compact(next.points() - score);
        } else {
            requirement = "<7>+" + compact(score - reached.points()) + " over " + label(reached.bracket());
        }
        return new Lines(title, bracket, requirement);
    }

    public static String compact(long value) {
        return StringUtility.shortenNumber(value).toLowerCase(Locale.ROOT);
    }

    private static String label(StarlynBracket bracket) {
        return bracket.getColor() + StringUtility.capitalize(bracket.name());
    }
}
