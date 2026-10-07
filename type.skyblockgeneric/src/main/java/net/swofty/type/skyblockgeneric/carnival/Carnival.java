package net.swofty.type.skyblockgeneric.carnival;

import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.elections.perks.ElectionPerks;
import net.swofty.type.skyblockgeneric.region.RegionPresence;
import net.swofty.type.skyblockgeneric.region.RegionType;

/**
 * Foxy's Chivalrous Carnival. It runs in the Hub for the whole term of a Mayor or Minister Foxy
 * with the perk, and stops taking new games 15 minutes before that term ends.
 */
public final class Carnival {
    public static final int TICKETS_PER_GAME = 5;
    public static final int DAILY_TICKETS = 25;
    public static final int POINTS_PER_TOKEN = 100;
    private static final long CLOSING_TICKS = 15L * 60L * 20L;

    private Carnival() {}

    public static void initialize() {
        RegionPresence.register(RegionType.CARNIVAL, Carnival::isOpen);
        CarnivalSessions.initialize();
    }

    public static boolean isOpen() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.CHIVALROUS_CARNIVAL);
    }

    public static boolean acceptsNewGames() {
        if (!isOpen()) return false;
        long untilTermEnds = SkyBlockCalendar.ticksUntilEvent(CalendarEvent.ELECTION_CLOSE);
        return untilTermEnds < 0 || untilTermEnds > CLOSING_TICKS;
    }

    public static int year() {
        return ElectionManager.getElectionData().getMayorElectedYear();
    }

    public static int tokensFor(long score) {
        return (int) Math.max(0, score / POINTS_PER_TOKEN);
    }
}
