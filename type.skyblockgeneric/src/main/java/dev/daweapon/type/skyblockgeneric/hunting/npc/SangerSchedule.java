package dev.daweapon.type.skyblockgeneric.hunting.npc;

import dev.daweapon.type.skyblockgeneric.calendar.SkyBlockCalendar;

import java.util.List;

public final class SangerSchedule {
    public record Spot(double x, double y, double z, float yaw) {
    }

    public static final List<Spot> SPOTS = List.of(
            new Spot(-632.5, 94, 160.5, 90),
            new Spot(-746.5, 93, 261.5, 0),
            new Spot(-526.5, 97, 218.5, -90));

    private SangerSchedule() {
    }

    public static long dayNumber(long elapsedTicks) {
        return elapsedTicks / SkyBlockCalendar.DAY + 1;
    }

    public static Spot spotOn(long dayNumber) {
        return SPOTS.get((int) Math.floorMod(dayNumber, (long) SPOTS.size()));
    }

    public static Spot today() {
        return spotOn(dayNumber(SkyBlockCalendar.getElapsed()));
    }

    public static long currentDay() {
        return dayNumber(SkyBlockCalendar.getElapsed());
    }
}
