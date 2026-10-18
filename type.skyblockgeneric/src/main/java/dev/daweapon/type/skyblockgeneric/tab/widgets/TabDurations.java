package dev.daweapon.type.skyblockgeneric.tab.widgets;

final class TabDurations {
    private TabDurations() {
    }

    static String clock(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes > 0 ? String.format("%d:%02d", minutes, seconds) : seconds + "s";
    }

    static String spans(long durationMs) {
        long totalMinutes = Math.max(0, durationMs / 60_000);
        long days = totalMinutes / 1440;
        long hours = (totalMinutes % 1440) / 60;
        long minutes = totalMinutes % 60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}
