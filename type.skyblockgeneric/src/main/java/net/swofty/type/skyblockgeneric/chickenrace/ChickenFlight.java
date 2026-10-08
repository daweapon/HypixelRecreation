package net.swofty.type.skyblockgeneric.chickenrace;

/**
 * Vertical motion of a racing chicken, in blocks and ticks: gravity pulls it down to a terminal
 * fall speed and a flap, when off cooldown, launches it upwards. Horizontal speed is constant.
 */
public final class ChickenFlight {
    private final Physics physics;
    private double verticalVelocity;
    private int cooldown;

    public record Physics(double blocksPerSecond, double flapVelocity, double gravity, double terminalVelocity,
                          int flapCooldownTicks) {
        public double blocksPerTick() {
            return blocksPerSecond / 20D;
        }
    }

    public ChickenFlight(Physics physics) {
        this.physics = physics;
    }

    public boolean flapReady() {
        return cooldown <= 0;
    }

    public void flap() {
        verticalVelocity = physics.flapVelocity();
        cooldown = physics.flapCooldownTicks();
    }

    /** Advances one tick and returns the vertical distance moved. */
    public double tick(boolean jumping) {
        if (cooldown > 0) cooldown--;
        if (jumping && flapReady()) flap();
        verticalVelocity = Math.max(verticalVelocity - physics.gravity(), -physics.terminalVelocity());
        return verticalVelocity;
    }
}
