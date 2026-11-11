package dev.daweapon.type.skyblockgeneric.carnival;

import dev.daweapon.type.skyblockgeneric.calendar.CalendarEvent;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.elections.ElectionManager;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.region.RegionPresence;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Foxy's Chivalrous Carnival. It runs in the Hub for the whole term of a Mayor or Minister Foxy
 * with the perk, and stops taking new games 15 minutes before that term ends.
 */
public final class Carnival {
    public static final int TICKETS_PER_GAME = 5;
    public static final int DAILY_TICKETS = 25;
    public static final int POINTS_PER_TOKEN = 100;
    public static final String NO_ABILITIES = "<c>Abilities can not be used in the Carnival area.";
    private static final long CLOSING_TICKS = 15L * 60L * 20L;

    private Carnival() {}

    public static void initialize() {
        RegionPresence.register(RegionType.CARNIVAL, Carnival::isOpen);
        CarnivalSessions.initialize();
    }

    public static boolean isOpen() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.CHIVALROUS_CARNIVAL);
    }

    public static boolean blocksAbilities(SkyBlockPlayer player) {
        SkyBlockRegion region = player.getRegion();
        return region != null && region.getType() == RegionType.CARNIVAL && isOpen();
    }

    public static boolean acceptsNewGames() {
        if (!isOpen()) return false;
        long untilTermEnds = SkyBlockCalendar.ticksUntilEvent(CalendarEvent.ELECTION_CLOSE);
        return untilTermEnds < 0 || untilTermEnds > CLOSING_TICKS;
    }

    public static int year() {
        return ElectionManager.getElectionData().getMayorElectedYear();
    }

    /** Time left in the Carnival as Hypixel's sidebar shows it, hours uncapped: {@code 102:52:22}. */
    public static String countdown() {
        long seconds = Math.max(0, SkyBlockCalendar.ticksUntilEvent(CalendarEvent.ELECTION_CLOSE) / 20);
        return String.format("%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    public static int tokensFor(long score) {
        return (int) Math.max(0, score / POINTS_PER_TOKEN);
    }
}
