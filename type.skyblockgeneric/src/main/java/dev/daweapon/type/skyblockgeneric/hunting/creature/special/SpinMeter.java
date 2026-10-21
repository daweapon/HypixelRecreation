package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class SpinMeter {
    enum Result {STARTED, SPINNING, KNOCKED_OUT, EXPLODED}

    static final double KNOCKOUT = 90;
    static final double EXPLODE = 100;

    private double value;
    private boolean spinning;

    Result add(double gain) {
        boolean first = !spinning;
        spinning = true;
        value += gain;
        if (value > EXPLODE) return Result.EXPLODED;
        if (value >= KNOCKOUT) return Result.KNOCKED_OUT;
        return first ? Result.STARTED : Result.SPINNING;
    }

    void decay(double amount) {
        if (!spinning) return;
        value -= amount;
        if (value <= 0) {
            value = 0;
            spinning = false;
        }
    }

    boolean spinning() {
        return spinning;
    }

    double value() {
        return value;
    }
}
