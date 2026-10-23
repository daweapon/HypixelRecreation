package dev.daweapon.type.skyblockgeneric.dante;

public final class DanteSunlight {
    public static final long MIDNIGHT = 18000;

    private DanteSunlight() {
    }

    public static long worldTime(int calendarHour) {
        return DanteAccess.securityActive() ? MIDNIGHT : (calendarHour * 1000L + 6000) % 24000;
    }
}
