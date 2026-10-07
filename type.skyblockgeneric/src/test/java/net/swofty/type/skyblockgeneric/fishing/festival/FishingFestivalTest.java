package net.swofty.type.skyblockgeneric.fishing.festival;

import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishingFestivalTest {

    @Test
    void festivalStartsOnTheFirstOfEveryMonthAndLastsThreeDays() {
        List<Long> starts = FishingFestival.startTimesInYear();
        assertEquals(12, starts.size());
        for (int month = 0; month < 12; month++) {
            assertEquals((long) month * SkyBlockCalendar.MONTH, starts.get(month));
        }
        assertEquals(3L * SkyBlockCalendar.DAY, FishingFestival.DURATION_TICKS);
    }

    @Test
    void sharkChanceTopsOutAtEightyPercent() {
        double max = FishingFestival.BASE_SHARK_CHANCE + Arrays.stream(SharkChanceSource.values())
                .mapToDouble(SharkChanceSource::getMaxBonus).sum();
        assertEquals(80D, max);
    }

    @Test
    void nurseSharkIsTheMostCommonSharkByWeight() {
        int total = Arrays.stream(FestivalShark.values()).mapToInt(FestivalShark::getWeight).sum();
        assertEquals(4_975, total);
        assertEquals(57.79D, Math.round(FestivalShark.NURSE_SHARK.getWeight() * 10_000D / total) / 100D);
        assertEquals(2.01D, Math.round(FestivalShark.GREAT_WHITE_SHARK.getWeight() * 10_000D / total) / 100D);
    }
}
