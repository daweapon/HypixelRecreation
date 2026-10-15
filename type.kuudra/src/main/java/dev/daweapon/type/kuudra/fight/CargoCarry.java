package dev.daweapon.type.kuudra.fight;

import java.util.UUID;

/** A player carrying a crate or Fuel Cell: dropped back into the lava when too slow or hit too hard (K1 3.1). */
public final class CargoCarry {
    private final UUID carrier;
    private final int timeoutTicks;
    private int ticksCarried;

    public CargoCarry(UUID carrier, int timeoutSeconds) {
        this.carrier = carrier;
        this.timeoutTicks = timeoutSeconds * 20;
    }

    /** True once the carry has taken too long. */
    public boolean tick() {
        return ++ticksCarried >= timeoutTicks;
    }

    public UUID carrier() {
        return carrier;
    }

    public int secondsLeft() {
        return Math.max(0, (timeoutTicks - ticksCarried + 19) / 20);
    }

    public static boolean dropsOnHit(double damage, double maxHealth, double fraction) {
        return maxHealth > 0 && damage >= maxHealth * fraction;
    }
}
