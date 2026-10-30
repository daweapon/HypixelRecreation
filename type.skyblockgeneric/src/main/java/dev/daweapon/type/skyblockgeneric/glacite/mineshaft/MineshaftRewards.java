package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

public final class MineshaftRewards {
    public static final long BASE_POWDER = 1_000;
    public static final long PER_SCRAP = 500;
    public static final long PER_CORPSE = 250;
    public static final long PER_COLD = 10;

    private MineshaftRewards() {
    }

    public static long leavePowder(int scraps, int corpses, double coldEndured) {
        return BASE_POWDER + PER_SCRAP * Math.max(0, scraps) + PER_CORPSE * Math.max(0, corpses)
                + PER_COLD * (long) Math.floor(Math.max(0, coldEndured));
    }
}
