package net.swofty.type.skyblockgeneric.spookyfestival;

import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Green and Purple Candy a player has collected during this year's Spooky Festival. */
public record SpookyFestivalCandyTally(int green, int purple) {
    public static final int GREEN_POINTS = 1;
    public static final int PURPLE_POINTS = 5;

    public int points() {
        return green * GREEN_POINTS + purple * PURPLE_POINTS;
    }

    public static SpookyFestivalCandyTally of(SkyBlockPlayer player) {
        SkyBlockDataHandler data = player.getSkyblockDataHandler();
        if (data.get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_CANDY_YEAR, DatapointInteger.class).getValue()
                != SkyBlockCalendar.getYear()) {
            return new SpookyFestivalCandyTally(0, 0);
        }
        return new SpookyFestivalCandyTally(
                data.get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_GREEN_CANDY, DatapointInteger.class).getValue(),
                data.get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_PURPLE_CANDY, DatapointInteger.class).getValue());
    }

    public static SpookyFestivalCandyTally record(SkyBlockPlayer player, boolean purple) {
        SpookyFestivalCandyTally current = of(player);
        SpookyFestivalCandyTally updated = purple
                ? new SpookyFestivalCandyTally(current.green, current.purple + 1)
                : new SpookyFestivalCandyTally(current.green + 1, current.purple);

        SkyBlockDataHandler data = player.getSkyblockDataHandler();
        data.get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_CANDY_YEAR, DatapointInteger.class)
                .setValue(SkyBlockCalendar.getYear());
        data.get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_GREEN_CANDY, DatapointInteger.class)
                .setValue(updated.green);
        data.get(SkyBlockDataHandler.Data.SPOOKY_FESTIVAL_PURPLE_CANDY, DatapointInteger.class)
                .setValue(updated.purple);
        return updated;
    }
}
