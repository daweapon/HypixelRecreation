package dev.daweapon.type.catacombs.engine;

/** Since 2026-05-11 stepping into the Blood Room without having opened its door with the Blood Key fails the run. */
public final class BloodRoomGate {
    public enum Verdict {ALLOWED, FAILS_RUN}

    private BloodRoomGate() {
    }

    public static Verdict onEnter(boolean bloodDoorOpenedWithKey) {
        return bloodDoorOpenedWithKey ? Verdict.ALLOWED : Verdict.FAILS_RUN;
    }
}
