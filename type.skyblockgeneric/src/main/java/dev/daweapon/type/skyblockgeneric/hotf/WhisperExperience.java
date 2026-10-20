package dev.daweapon.type.skyblockgeneric.hotf;

public final class WhisperExperience {
    public static final long WHISPERS_PER_XP = 2_400;
    public static final long LINEAR_CAP = 600_000;
    public static final double MAX_XP = 620;
    private static final double LINEAR_XP = (double) LINEAR_CAP / WHISPERS_PER_XP;
    private static final double TAIL_SCALE = (MAX_XP - LINEAR_XP) * WHISPERS_PER_XP;

    private WhisperExperience() {
    }

    public static double experience(long whispers) {
        if (whispers <= 0) return 0;
        if (whispers <= LINEAR_CAP) return (double) whispers / WHISPERS_PER_XP;
        double excess = whispers - LINEAR_CAP;
        return LINEAR_XP + (MAX_XP - LINEAR_XP) * (1 - Math.exp(-excess / TAIL_SCALE));
    }
}
