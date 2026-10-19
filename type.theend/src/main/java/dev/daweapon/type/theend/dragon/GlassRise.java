package dev.daweapon.type.theend.dragon;

public final class GlassRise {
    public static final int RISE_TICKS = 100;
    public static final double BASE_Y = 10;
    public static final double EGG_Y = 77;

    private GlassRise() {
    }

    public static double heightAt(int tick) {
        double progress = Math.min(1, Math.max(0, (double) tick / RISE_TICKS));
        return (EGG_Y - BASE_Y) * progress;
    }
}
