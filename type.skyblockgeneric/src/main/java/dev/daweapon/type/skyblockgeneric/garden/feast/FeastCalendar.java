package dev.daweapon.type.skyblockgeneric.garden.feast;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;
import dev.daweapon.type.skyblockgeneric.elections.SkyBlockMayor;
import dev.daweapon.type.skyblockgeneric.elections.perks.ElectionPerks;
import dev.daweapon.type.skyblockgeneric.garden.GardenCrop;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class FeastCalendar {
    public static final int FIRST_MONTH = 7;
    public static final int LAST_MONTH = 9;
    public static final int IN_SEASON = 4;
    public static final double MONTH_HOURS = 10 + 20 / 60.0;

    private static final Map<GardenCrop, String> RARE_CROPS = Map.ofEntries(
            Map.entry(GardenCrop.WHEAT, "CORNUCOPIA"), Map.entry(GardenCrop.CARROT, "CARROT_ZEST"),
            Map.entry(GardenCrop.POTATO, "DEEPFRIES"), Map.entry(GardenCrop.PUMPKIN, "AGGOURDIAN"),
            Map.entry(GardenCrop.SUGAR_CANE, "CANE_KNOT"), Map.entry(GardenCrop.MELON, "MELON_JUICE"),
            Map.entry(GardenCrop.CACTUS, "CACTUS_FLOWER"), Map.entry(GardenCrop.COCOA_BEANS, "DESIGNER_COFFEE_BEANS"),
            Map.entry(GardenCrop.MUSHROOM, "FEASTFUNGUS"), Map.entry(GardenCrop.NETHER_WART, "BOTROOT"),
            Map.entry(GardenCrop.SUNFLOWER, "SALTED_SUNFLOWER_SEEDS"), Map.entry(GardenCrop.MOONFLOWER, "CRYSTALIZED_MOONLIGHT"),
            Map.entry(GardenCrop.WILD_ROSE, "FLORAL_GELATIN"));

    private FeastCalendar() {
    }

    public static boolean seasonMonth(int month) {
        return month >= FIRST_MONTH && month <= LAST_MONTH;
    }

    public static boolean grand() {
        return ElectionPerks.isActive(SkyBlockMayor.Perk.GRAND_FEAST);
    }

    public static boolean active() {
        return grand() || seasonMonth(SkyBlockCalendar.getMonth());
    }

    public static int year() {
        return SkyBlockCalendar.getYear();
    }

    public static List<GardenCrop> inSeason(int year, int month) {
        List<GardenCrop> crops = new ArrayList<>(List.of(GardenCrop.values()));
        Collections.shuffle(crops, new Random(year * 31L + month));
        return List.copyOf(crops.subList(0, IN_SEASON));
    }

    public static List<GardenCrop> inSeasonNow() {
        return inSeason(SkyBlockCalendar.getYear(), SkyBlockCalendar.getMonth());
    }

    public static List<GardenCrop> inSeasonNext() {
        int month = SkyBlockCalendar.getMonth() % 12 + 1;
        int year = SkyBlockCalendar.getYear() + (month == 1 ? 1 : 0);
        return inSeason(year, month);
    }

    public static boolean isInSeason(GardenCrop crop) {
        return active() && inSeasonNow().contains(crop);
    }

    public static @Nullable String rareCrop(GardenCrop crop) {
        return RARE_CROPS.get(crop);
    }

    public static double hoursUntilRotation() {
        long intoMonth = SkyBlockCalendar.getElapsed() % SkyBlockCalendar.MONTH;
        return (SkyBlockCalendar.MONTH - intoMonth) / (double) SkyBlockCalendar.HOUR;
    }
}
