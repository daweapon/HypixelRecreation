package dev.daweapon.type.skyblockgeneric.moonglade.beacon;

public record BeaconPane(int color, int speed, int pitch, long anchorMillis, long anchorSteps, boolean paused) {
    public static final int WIDTH = 9;
    public static final int COLORS = 10;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    private static final long BASE_STEP_MILLIS = 1200;

    public static BeaconPane start(int color, int speed, int pitch, long now) {
        return new BeaconPane(color, speed, pitch, now, 0, false);
    }

    public static long stepMillis(int speed) {
        return BASE_STEP_MILLIS / Math.max(MIN_LEVEL, speed);
    }

    public static int bounce(long steps) {
        int span = 2 * (WIDTH - 1);
        int phase = (int) Math.floorMod(steps, (long) span);
        return phase < WIDTH ? phase : span - phase;
    }

    public long steps(long now) {
        if (paused) return anchorSteps;
        return anchorSteps + Math.max(0, now - anchorMillis) / stepMillis(speed);
    }

    public int position(long now) {
        return bounce(steps(now));
    }

    public boolean arrivedAtEnd(long now, long sinceMillis) {
        if (paused) return false;
        int position = position(now);
        return (position == 0 || position == WIDTH - 1) && steps(now) != steps(now - sinceMillis);
    }

    public BeaconPane withColor(int value) {
        return new BeaconPane(Math.floorMod(value, COLORS), speed, pitch, anchorMillis, anchorSteps, paused);
    }

    public BeaconPane withSpeed(int value, long now) {
        return new BeaconPane(color, Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, value)), pitch, now, steps(now), paused);
    }

    public BeaconPane withPitch(int value) {
        return new BeaconPane(color, speed, Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, value)), anchorMillis, anchorSteps, paused);
    }

    public BeaconPane pausedAt(long now) {
        return paused ? this : new BeaconPane(color, speed, pitch, now, steps(now), true);
    }

    public BeaconPane resumedAt(long now) {
        return paused ? new BeaconPane(color, speed, pitch, now, anchorSteps, false) : this;
    }

    public BeaconPane toggledPause(long now) {
        return paused ? resumedAt(now) : pausedAt(now);
    }

    public boolean sameSettings(BeaconPane other) {
        return color == other.color && speed == other.speed && pitch == other.pitch;
    }
}
