package dev.daweapon.type.kuudra.fight;

import java.util.UUID;

/**
 * One player standing still to lift a crate or Fuel Cell out of the lava (research K1 3.1): progress fills over
 * the tier's pick-up time, sped up by Steady Hands and organs; drifting away from the starting spot drops it.
 */
public final class CargoPickup {
    public enum Result {PROGRESS, DONE, SLIPPED}

    private static final int BAR_WIDTH = 20;

    private final UUID player;
    private final double startX;
    private final double startZ;
    private final double tolerance;
    private final int ticksNeeded;
    private int ticksDone;

    public CargoPickup(UUID player, double x, double z, double tolerance, int ticksNeeded) {
        this.player = player;
        this.startX = x;
        this.startZ = z;
        this.tolerance = tolerance;
        this.ticksNeeded = Math.max(1, ticksNeeded);
    }

    public static int ticksNeeded(double seconds, double speedBonus) {
        return (int) Math.ceil(seconds * 20 / (1 + Math.max(0, speedBonus)));
    }

    public Result tick(double x, double z) {
        double dx = x - startX;
        double dz = z - startZ;
        if (dx * dx + dz * dz > tolerance * tolerance) return Result.SLIPPED;
        return ++ticksDone >= ticksNeeded ? Result.DONE : Result.PROGRESS;
    }

    public UUID player() {
        return player;
    }

    public double fraction() {
        return Math.min(1, (double) ticksDone / ticksNeeded);
    }

    public int percent() {
        return (int) Math.floor(fraction() * 100);
    }

    /** The title Hypixel shows while lifting: {@code §8[§c|§f|||…§8] §b5%}, red to yellow to green. */
    public String bar() {
        int filled = (int) Math.round(fraction() * BAR_WIDTH);
        int percent = percent();
        String colour = percent <= 30 ? "<c>" : percent < 70 ? "<e>" : "<a>";
        return "<8>[" + colour + "|".repeat(filled) + "<f>" + "|".repeat(BAR_WIDTH - filled) + "<8>] <b>" + percent + "%";
    }
}
