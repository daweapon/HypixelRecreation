package dev.daweapon.type.skyblockgeneric.fishing.tag;

import java.util.function.IntPredicate;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.fishing.FishingContext;

// Fishing tag that gates loot to a range of in-game hours (night, day, dawn, dusk).
public record TimeOfDayTag(String id, IntPredicate hourPredicate) implements FishingTag {

    public static final TimeOfDayTag NIGHT = new TimeOfDayTag("NIGHT", hour -> hour < 6 || hour >= 20);
    public static final TimeOfDayTag DAY = new TimeOfDayTag("DAY", hour -> hour >= 6 && hour < 20);
    public static final TimeOfDayTag DAWN = new TimeOfDayTag("DAWN", hour -> hour >= 5 && hour < 8);
    public static final TimeOfDayTag DUSK = new TimeOfDayTag("DUSK", hour -> hour >= 17 && hour < 20);

    @Override
    public boolean isAvailable(FishingContext context) {
        return hourPredicate.test(SkyBlockCalendar.getHour());
    }
}
