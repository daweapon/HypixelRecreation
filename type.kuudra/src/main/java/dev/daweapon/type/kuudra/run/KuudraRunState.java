package dev.daweapon.type.kuudra.run;

import dev.daweapon.type.game.game.GameState;
import org.jetbrains.annotations.Nullable;

/**
 * The run's state machine, free of the server so it can be tested: WAITING until someone talks to Elle,
 * IN_PROGRESS through the phases with the wave timer running, ENDING while the shutdown timer counts down,
 * then TERMINATED.
 */
public final class KuudraRunState {
    public enum Outcome {WIN, LOSE}

    public enum Tick {NOTHING, NEW_WAVE, SHUTDOWN}

    private final boolean stunRequired;
    private final boolean lairPhase;
    private final int secondsPerWave;
    private final int maxWave;
    private final int shutdownSeconds;

    private GameState stage = GameState.WAITING;
    private @Nullable KuudraPhase phase;
    private @Nullable Outcome outcome;
    private int wave;
    private int secondsToNextWave;
    private long elapsedSeconds;
    private int shutdownSecondsLeft;

    public KuudraRunState(boolean stunRequired, boolean lairPhase, int secondsPerWave, int maxWave, int shutdownSeconds) {
        if (secondsPerWave <= 0 || maxWave <= 0 || shutdownSeconds <= 0) {
            throw new IllegalArgumentException("Kuudra wave timing and shutdown must be positive");
        }
        this.stunRequired = stunRequired;
        this.lairPhase = lairPhase;
        this.secondsPerWave = secondsPerWave;
        this.maxWave = maxWave;
        this.shutdownSeconds = shutdownSeconds;
    }

    public boolean start() {
        if (stage != GameState.WAITING) return false;
        stage = GameState.IN_PROGRESS;
        phase = KuudraPhase.SUPPLIES;
        wave = 1;
        secondsToNextWave = secondsPerWave;
        return true;
    }

    /** Moves to the phase that naturally follows; false when the current one is the last. */
    public boolean advancePhase() {
        if (stage != GameState.IN_PROGRESS || phase == null) return false;
        KuudraPhase next = phase.next(stunRequired);
        if (next == null) return false;
        phase = next;
        return true;
    }

    public boolean enterPhase(KuudraPhase target) {
        if (stage != GameState.IN_PROGRESS || !target.availableIn(stunRequired, lairPhase) || target == phase) return false;
        phase = target;
        return true;
    }

    /** A win needs a fight in progress; a loss can also close a run nobody started. */
    public boolean finish(Outcome result) {
        boolean allowed = stage == GameState.IN_PROGRESS || (result == Outcome.LOSE && stage == GameState.WAITING);
        if (!allowed) return false;
        stage = GameState.ENDING;
        outcome = result;
        shutdownSecondsLeft = shutdownSeconds;
        return true;
    }

    public boolean terminate() {
        if (stage == GameState.TERMINATED) return false;
        stage = GameState.TERMINATED;
        return true;
    }

    /** Advances one second; SHUTDOWN is reported once, when the shutdown timer runs out. */
    public Tick tick() {
        if (stage == GameState.IN_PROGRESS) {
            elapsedSeconds++;
            if (wave >= maxWave) return Tick.NOTHING;
            if (--secondsToNextWave > 0) return Tick.NOTHING;
            wave++;
            secondsToNextWave = secondsPerWave;
            return Tick.NEW_WAVE;
        }
        if (stage == GameState.ENDING && shutdownSecondsLeft > 0) {
            return --shutdownSecondsLeft == 0 ? Tick.SHUTDOWN : Tick.NOTHING;
        }
        return Tick.NOTHING;
    }

    /** Whether an empty run should be cleaned up: nobody online in it for longer than the lifetime. */
    public static boolean abandoned(int onlineMembers, long emptySinceMillis, long nowMillis, long lifetimeMillis) {
        return onlineMembers == 0 && emptySinceMillis > 0 && nowMillis - emptySinceMillis >= lifetimeMillis;
    }

    public GameState stage() {
        return stage;
    }

    public @Nullable KuudraPhase phase() {
        return phase;
    }

    public @Nullable Outcome outcome() {
        return outcome;
    }

    public int wave() {
        return wave;
    }

    public int secondsToNextWave() {
        return wave >= maxWave ? 0 : secondsToNextWave;
    }

    public long elapsedSeconds() {
        return elapsedSeconds;
    }

    public int shutdownSecondsLeft() {
        return shutdownSecondsLeft;
    }

    public boolean stunRequired() {
        return stunRequired;
    }
}
