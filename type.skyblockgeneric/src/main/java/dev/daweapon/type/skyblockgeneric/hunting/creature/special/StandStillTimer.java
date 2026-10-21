package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class StandStillTimer {
    enum Phase {IDLE, RED, YELLOW, READY}

    private final long delayTicks;
    private final long yellowTicks;
    private final long readyTicks;
    private long still;

    StandStillTimer(long delayTicks, long yellowTicks, long readyTicks) {
        this.delayTicks = delayTicks;
        this.yellowTicks = yellowTicks;
        this.readyTicks = readyTicks;
    }

    Phase tick(boolean playerNearby, boolean moved) {
        if (!playerNearby) {
            still = 0;
            return Phase.IDLE;
        }
        still = moved ? 0 : still + 1;
        return phase();
    }

    Phase phase() {
        if (still < delayTicks) return Phase.RED;
        long countdown = still - delayTicks;
        if (countdown >= readyTicks) return Phase.READY;
        return countdown >= yellowTicks ? Phase.YELLOW : Phase.RED;
    }
}
