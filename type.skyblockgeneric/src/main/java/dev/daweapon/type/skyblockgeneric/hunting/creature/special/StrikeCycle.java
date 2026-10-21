package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

final class StrikeCycle {
    enum Phase {ORBIT, DIVE, RECOVER}

    private static final double ORBIT_ANGLE_PER_STEP = 0.15;

    private final int orbitSteps;
    private final int diveLimit;
    private final int recoverSteps;
    private Phase phase = Phase.ORBIT;
    private int steps;
    private long elapsed;

    StrikeCycle(int orbitSteps, int diveLimit, int recoverSteps) {
        this.orbitSteps = orbitSteps;
        this.diveLimit = diveLimit;
        this.recoverSteps = recoverSteps;
    }

    Phase phase() {
        return phase;
    }

    double orbitAngle() {
        return elapsed * ORBIT_ANGLE_PER_STEP;
    }

    void tick(boolean reachedTarget) {
        steps++;
        elapsed++;
        switch (phase) {
            case ORBIT -> {
                if (steps >= orbitSteps) enter(Phase.DIVE);
            }
            case DIVE -> {
                if (reachedTarget || steps >= diveLimit) enter(Phase.RECOVER);
            }
            case RECOVER -> {
                if (steps >= recoverSteps) enter(Phase.ORBIT);
            }
        }
    }

    void reset() {
        enter(Phase.ORBIT);
    }

    private void enter(Phase next) {
        phase = next;
        steps = 0;
    }
}
