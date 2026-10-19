package dev.daweapon.type.skyblockgeneric.hoppity;

import dev.daweapon.type.skyblockgeneric.rabbits.HoppityEggType;
import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import lombok.Getter;

@Getter
public enum EggTimeOfDay {
    MORNING(7, HoppityEggType.BREAKFAST, HoppityEggType.BRUNCH),
    AFTERNOON(14, HoppityEggType.LUNCH, HoppityEggType.DEJEUNER),
    EVENING(21, HoppityEggType.DINNER, HoppityEggType.SUPPER);

    private final int hour;
    private final HoppityEggType evenDayType;
    private final HoppityEggType oddDayType;

    EggTimeOfDay(int hour, HoppityEggType evenDayType, HoppityEggType oddDayType) {
        this.hour = hour;
        this.evenDayType = evenDayType;
        this.oddDayType = oddDayType;
    }

    public long tickInDay() {
        return (long) hour * SkyBlockCalendar.HOUR;
    }

    public HoppityEggType typeOnDay(long dayInYear) {
        return dayInYear % 2 == 0 ? evenDayType : oddDayType;
    }
}
