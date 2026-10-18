package dev.daweapon.type.skyblockgeneric.tab.widgets;

final class TabTime {
    private TabTime() {
    }

    static String format(long millis) {
        long seconds = Math.max(0, millis / 1000);
        long days = seconds / 86_400;
        long hours = seconds % 86_400 / 3_600;
        long minutes = seconds % 3_600 / 60;
        long secs = seconds % 60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m " + secs + "s";
        return secs + "s";
    }

    static String formatTicks(long ticks) {
        return format(ticks * 50);
    }
}
