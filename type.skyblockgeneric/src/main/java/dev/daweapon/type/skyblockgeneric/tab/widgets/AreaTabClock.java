package dev.daweapon.type.skyblockgeneric.tab.widgets;

final class AreaTabClock {
    private AreaTabClock() {
    }

    static String compact(long millis) {
        long seconds = Math.max(0, millis / 1000);
        if (seconds >= 86_400) return seconds / 86_400 + "d";
        if (seconds >= 3600) return seconds / 3600 + "h " + seconds % 3600 / 60 + "m";
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }

    static String minutes(long millis) {
        return (millis / 60_000 + 1) + "m";
    }
}
