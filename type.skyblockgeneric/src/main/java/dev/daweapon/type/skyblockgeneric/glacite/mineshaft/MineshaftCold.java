package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

public final class MineshaftCold {
    public static final long STEP_MILLIS = 150_000;
    public static final double STEP_VULNERABILITY = 0.25;
    public static final String STEP_MESSAGE = "<b><l>BRRR! <r><a>It's getting colder in here...";

    private MineshaftCold() {
    }

    public static int steps(long elapsedMillis) {
        return (int) Math.max(0, elapsedMillis / STEP_MILLIS);
    }

    public static double vulnerability(long elapsedMillis) {
        return steps(elapsedMillis) * STEP_VULNERABILITY;
    }
}
