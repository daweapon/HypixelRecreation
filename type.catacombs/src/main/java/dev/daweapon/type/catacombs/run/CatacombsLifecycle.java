package dev.daweapon.type.catacombs.run;

import dev.daweapon.dungeons.catacombs.run.CatacombsRunConfig;
import dev.daweapon.dungeons.catacombs.run.CatacombsRunState;
import dev.daweapon.dungeons.catacombs.run.DungeonRunPhase;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;

/**
 * The run's state machine, free of the server so it can be tested: WAITING until the party starts it, ACTIVE while the
 * {@link CatacombsRunState} advances through its phases and the one-hour cap counts down, ENDING while the shutdown
 * timer runs, then TERMINATED.
 */
public final class CatacombsLifecycle {
    public enum Stage {WAITING, ACTIVE, ENDING, TERMINATED}

    public enum Outcome {WIN, LOSE}

    public enum Tick {NOTHING, TIMED_OUT, SHUTDOWN}

    private final CatacombsRunConfig config;
    private final Clock clock;
    private final int shutdownSeconds;

    private Stage stage = Stage.WAITING;
    private @Nullable CatacombsRunState state;
    private @Nullable Outcome outcome;
    private long elapsedSeconds;
    private int shutdownSecondsLeft;

    public CatacombsLifecycle(CatacombsRunConfig config, Clock clock, int shutdownSeconds) {
        if (shutdownSeconds <= 0) throw new IllegalArgumentException("The shutdown timer must be positive");
        this.config = config;
        this.clock = clock;
        this.shutdownSeconds = shutdownSeconds;
    }

    public synchronized boolean begin() {
        if (stage != Stage.WAITING) return false;
        stage = Stage.ACTIVE;
        state = CatacombsRunState.start(config, clock);
        return true;
    }

    /** Moves forward to the given phase; never backwards, and only while the run is active. */
    public synchronized boolean advanceTo(DungeonRunPhase target) {
        if (stage != Stage.ACTIVE || state == null) return false;
        if (target.ordinal() <= state.phase().ordinal()) return false;
        switch (target) {
            case CLEARING -> state.enterClearing();
            case BLOOD_ROOM -> state.enterBloodRoom();
            case BOSS -> state.enterBoss();
            default -> {
                return false;
            }
        }
        return true;
    }

    /** A win needs an active run; a loss can also close a run that never started. */
    public synchronized boolean finish(Outcome result) {
        boolean allowed = stage == Stage.ACTIVE || (result == Outcome.LOSE && stage == Stage.WAITING);
        if (!allowed) return false;
        if (state != null) {
            if (result == Outcome.WIN) state.complete();
            else state.fail();
        }
        stage = Stage.ENDING;
        outcome = result;
        shutdownSecondsLeft = shutdownSeconds;
        return true;
    }

    public synchronized boolean terminate() {
        if (stage == Stage.TERMINATED) return false;
        stage = Stage.TERMINATED;
        return true;
    }

    public synchronized Tick tick() {
        if (stage == Stage.ACTIVE) {
            elapsedSeconds++;
            if (elapsedSeconds < config.floor().rules().maximumDurationSeconds()) return Tick.NOTHING;
            finish(Outcome.LOSE);
            return Tick.TIMED_OUT;
        }
        if (stage == Stage.ENDING && shutdownSecondsLeft > 0) {
            return --shutdownSecondsLeft == 0 ? Tick.SHUTDOWN : Tick.NOTHING;
        }
        return Tick.NOTHING;
    }

    public static boolean abandoned(int onlineMembers, long emptySinceMillis, long nowMillis, long lifetimeMillis) {
        return onlineMembers == 0 && emptySinceMillis > 0 && nowMillis - emptySinceMillis >= lifetimeMillis;
    }

    public synchronized Stage stage() {
        return stage;
    }

    public synchronized @Nullable CatacombsRunState state() {
        return state;
    }

    public synchronized @Nullable DungeonRunPhase phase() {
        return state == null ? null : state.phase();
    }

    public synchronized @Nullable Outcome outcome() {
        return outcome;
    }

    public synchronized long elapsedSeconds() {
        return elapsedSeconds;
    }

    public synchronized int shutdownSecondsLeft() {
        return shutdownSecondsLeft;
    }

    public CatacombsRunConfig config() {
        return config;
    }
}
