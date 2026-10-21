package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class CaveHide {
    enum Phase {OPEN, HIDING, IN_CAVE}

    private final long travelLimitTicks;
    private final long stayTicks;
    private Phase phase = Phase.OPEN;
    private long since;

    CaveHide(long travelLimitTicks, long stayTicks) {
        this.travelLimitTicks = travelLimitTicks;
        this.stayTicks = stayTicks;
    }

    Phase phase() {
        return phase;
    }

    void startHiding(long now) {
        phase = Phase.HIDING;
        since = now;
    }

    Phase tick(long now, boolean atCave) {
        if (phase == Phase.HIDING && (atCave || now - since >= travelLimitTicks)) {
            phase = Phase.IN_CAVE;
            since = now;
        } else if (phase == Phase.IN_CAVE && now - since >= stayTicks) {
            phase = Phase.OPEN;
        }
        return phase;
    }
}
