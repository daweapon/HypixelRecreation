package net.swofty.type.crimsonisle.town;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An NPC that spends the SkyBlock day at one spot and the night at home (Edelis, Keran and Prie),
 * with separate first-meeting and idle lines for each place.
 */
public class DayNightNPC extends CrimsonNPC {
    /** SkyBlock hours counted as day (06:00 to 19:59). The wiki does not give the switch time. */
    public static final int DAWN = 6;
    public static final int DUSK = 20;

    private final Pos night;
    private final Place dayPlace;
    private final Place nightPlace;

    public record Place(String[] introduction, String[][] idle) {
    }

    public DayNightNPC(String name, String texture, Pos day, Pos night, Place dayPlace, Place nightPlace) {
        super(name, texture, day);
        this.night = night;
        this.dayPlace = dayPlace;
        this.nightPlace = nightPlace;
    }

    public static boolean isDay() {
        int hour = SkyBlockCalendar.getHour();
        return hour >= DAWN && hour < DUSK;
    }

    @Override
    protected Pos position(SkyBlockPlayer player) {
        return isDay() ? home() : night;
    }

    @Override
    protected boolean moves() {
        return true;
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        boolean day = isDay();
        Place place = day ? dayPlace : nightPlace;
        String key = "town:" + getName().toLowerCase(Locale.ROOT) + (day ? ":day" : ":night");
        if (place.introduction().length > 0 && CrimsonSideQuests.firstMeeting(player, key)) {
            introduce(player, day, place.introduction());
            return;
        }
        String[][] idle = place.idle();
        if (idle.length == 0) return;
        say(player, idle[ThreadLocalRandom.current().nextInt(idle.length)]);
    }

    protected void introduce(SkyBlockPlayer player, boolean day, String[] lines) {
        say(player, lines);
    }
}
