package dev.daweapon.type.theend;

public final class PadRules {
    public static final double RADIUS = 0.7;
    public static final double MAX_HEIGHT_ABOVE_PAD = 1.6;
    public static final long COOLDOWN_MILLIS = 3_000;

    private PadRules() {
    }

    public static boolean isOnPad(double x, double y, double z, int padX, int padY, int padZ) {
        return Math.abs(x - (padX + 0.5)) <= RADIUS
                && Math.abs(z - (padZ + 0.5)) <= RADIUS
                && y >= padY + 0.5
                && y <= padY + MAX_HEIGHT_ABOVE_PAD;
    }

    public static boolean coolingDown(long lastUseMillis, long nowMillis) {
        return nowMillis - lastUseMillis < COOLDOWN_MILLIS;
    }
}
