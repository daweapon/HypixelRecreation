package dev.daweapon.type.skyblockgeneric.calendar;

import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;

import java.util.List;
import java.util.SplittableRandom;

public final class FoxyExtraEvent {
    public static final long START = 5L * SkyBlockCalendar.MONTH + 21L * SkyBlockCalendar.DAY;
    public static final long MINING_FIESTA_TICKS = 7L * SkyBlockCalendar.DAY;

    public enum Kind {
        SPOOKY_FESTIVAL("Spooky Festival"),
        MINING_FIESTA("Mining Fiesta"),
        FISHING_FESTIVAL("Fishing Festival");

        private final String displayName;

        Kind(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private FoxyExtraEvent() {
    }

    public static Kind kindFor(int year) {
        Kind[] kinds = Kind.values();
        return kinds[new SplittableRandom(year * 0x9E3779B97F4A7C15L).nextInt(kinds.length)];
    }

    public static List<Long> timesFor(Kind kind) {
        if (!ElectionPerks.isActive(SkyBlockMayor.Perk.EXTRA_EVENT)) return List.of();
        return kindFor(SkyBlockCalendar.getYear()) == kind ? List.of(START) : List.of();
    }
}
