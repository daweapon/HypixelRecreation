package dev.daweapon.type.skyblockgeneric.torrhus;

public final class TempleDoorState {
    public enum State { CLOSED, OPEN, SLAMMED }

    public enum Change { NONE, OPENED, SLAMMED, CLOSED }

    public static final double SLAM_Z = 239.5;
    public static final double ARM_Z = 243;
    public static final double REOPEN_Z = 237.5;

    private final double slamZ;
    private final double armZ;
    private final double reopenZ;
    private State state = State.CLOSED;
    private boolean armed;

    public TempleDoorState() {
        this(SLAM_Z, ARM_Z, REOPEN_Z);
    }

    public TempleDoorState(double slamZ, double armZ, double reopenZ) {
        this.slamZ = slamZ;
        this.armZ = armZ;
        this.reopenZ = reopenZ;
    }

    public State state() {
        return state;
    }

    public Change update(boolean unlocked, boolean forceOpen, double z) {
        if (forceOpen) {
            return moveTo(State.OPEN);
        }
        if (!unlocked) {
            armed = false;
            return moveTo(State.CLOSED);
        }
        switch (state) {
            case CLOSED -> {
                return moveTo(State.OPEN);
            }
            case OPEN -> {
                if (z >= slamZ) {
                    armed = false;
                    return moveTo(State.SLAMMED);
                }
            }
            case SLAMMED -> {
                if (z >= armZ) armed = true;
                if (armed && z <= reopenZ) return moveTo(State.OPEN);
            }
        }
        return Change.NONE;
    }

    private Change moveTo(State target) {
        if (state == target) return Change.NONE;
        state = target;
        return switch (target) {
            case OPEN -> Change.OPENED;
            case SLAMMED -> Change.SLAMMED;
            case CLOSED -> Change.CLOSED;
        };
    }
}
