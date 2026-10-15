package dev.daweapon.type.catacombs.run;

public final class CatacombsRunSlots {
    private CatacombsRunSlots() {
    }

    public static int remaining(int max, int active) {
        return Math.max(0, max - active);
    }

    public static boolean full(int max, int active) {
        return remaining(max, active) == 0;
    }
}
