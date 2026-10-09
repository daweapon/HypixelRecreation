package net.swofty.type.kuudra.fight;

/**
 * Kuudra's health and his rise/submerge loop (research K1 3.3, 3.6). Health is 100,000 points shown as a
 * percentage. While the loop runs he stays up for a while or until a volley of Ballista shots lands, sinks,
 * waits under the lava and rises again. He can only be shot while fully up and not about to sink.
 */
public final class KuudraBossState {
    public enum Stance {DORMANT, RISING, SURFACED, SINKING, SUBMERGED}

    public enum Event {NONE, SURFACED, SUBMERGED}

    public static final int SHOT_GRACE_TICKS = 20;

    private final double maxHealth;
    private final int surfaceTicks;
    private final int shotsPerSurface;
    private final int submergedTicks;
    private final int moveTicks;

    private double health;
    private Stance stance = Stance.DORMANT;
    private int ticksInStance;
    private int shotsThisSurface;
    private boolean cycling;

    public KuudraBossState(double maxHealth, int surfaceSeconds, int shotsPerSurface, int submergedSeconds, int moveTicks) {
        if (maxHealth <= 0 || surfaceSeconds <= 0 || submergedSeconds <= 0 || moveTicks <= 0 || shotsPerSurface <= 0) {
            throw new IllegalArgumentException("Kuudra's health and timings must be positive");
        }
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.surfaceTicks = surfaceSeconds * 20;
        this.shotsPerSurface = shotsPerSurface;
        this.submergedTicks = submergedSeconds * 20;
        this.moveTicks = moveTicks;
    }

    public void rise() {
        if (stance == Stance.RISING || stance == Stance.SURFACED) return;
        stance = Stance.RISING;
        ticksInStance = 0;
    }

    public void sink() {
        if (stance == Stance.SINKING || stance == Stance.SUBMERGED || stance == Stance.DORMANT) return;
        stance = Stance.SINKING;
        ticksInStance = 0;
    }

    public void setCycling(boolean cycling) {
        this.cycling = cycling;
    }

    public Event tick() {
        ticksInStance++;
        switch (stance) {
            case RISING -> {
                if (ticksInStance >= moveTicks) {
                    stance = Stance.SURFACED;
                    ticksInStance = 0;
                    shotsThisSurface = 0;
                    return Event.SURFACED;
                }
            }
            case SINKING -> {
                if (ticksInStance >= moveTicks) {
                    stance = Stance.SUBMERGED;
                    ticksInStance = 0;
                    return Event.SUBMERGED;
                }
            }
            case SURFACED -> {
                if (cycling && ticksInStance >= surfaceTicks) sink();
            }
            case SUBMERGED -> {
                if (cycling && ticksInStance >= submergedTicks) rise();
            }
            case DORMANT -> {
            }
        }
        return Event.NONE;
    }

    public boolean canBeShot() {
        return stance == Stance.SURFACED && (!cycling || surfaceTicks - ticksInStance > SHOT_GRACE_TICKS);
    }

    /** Applies a Ballista hit; returns true when it brought him to 0. A full volley sends him under. */
    public boolean shoot(double damage) {
        if (!canBeShot()) return false;
        boolean dead = damage(damage);
        if (!dead && cycling && ++shotsThisSurface >= shotsPerSurface) sink();
        return dead;
    }

    /** Raw damage, as from a staff command; returns true when it brought him to 0. */
    public boolean damage(double amount) {
        if (health <= 0) return false;
        health = Math.max(0, health - Math.max(0, amount));
        return health <= 0;
    }

    public double health() {
        return health;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public double fraction() {
        return health / maxHealth;
    }

    /** Kuudra HP as the mods show it, e.g. 72.5. */
    public double percent() {
        return Math.round(fraction() * 1000) / 10.0;
    }

    public Stance stance() {
        return stance;
    }

    /** How far below his surfaced height he is, 0 (up) to 1 (submerged), for the rising/sinking animation. */
    public double depth() {
        return switch (stance) {
            case SURFACED -> 0;
            case SUBMERGED, DORMANT -> 1;
            case RISING -> 1 - Math.min(1, (double) ticksInStance / moveTicks);
            case SINKING -> Math.min(1, (double) ticksInStance / moveTicks);
        };
    }

    /** Seconds until he sinks while up and cycling, else -1. */
    public int secondsUntilSubmerge() {
        if (stance != Stance.SURFACED || !cycling) return -1;
        return Math.max(0, (surfaceTicks - ticksInStance + 19) / 20);
    }
}
