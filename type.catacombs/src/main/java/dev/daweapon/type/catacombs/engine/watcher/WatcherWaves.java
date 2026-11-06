package dev.daweapon.type.catacombs.engine.watcher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * The Watcher's timeline, one tick at a time, as both captured Floor I runs played it: the opening line, the boss bar
 * two seconds in, a first wave of four summons each announced by a line two seconds before it, then nothing until that
 * wave is dead, then the rest one at a time (never more than four alive), "That will be enough for now." after the last
 * summon, "You have proven yourself. You may pass." once all are dead, and the portal a few seconds later.
 */
public final class WatcherWaves {
    public sealed interface Action {
    }

    public record Say(String line) implements Action {
    }

    public record FlyOut() implements Action {
    }

    public record Summon() implements Action {
    }

    public record ShowBar() implements Action {
    }

    public record OpenPortal() implements Action {
    }

    private record Planned(long lineTick, long spawnTick) {
    }

    private static final int BAR_FLIP_TICKS = 20;

    private final WatcherConfig config;
    private final int total;
    private final RandomGenerator random;
    private final Deque<Planned> planned = new ArrayDeque<>();
    private long now;
    private int spawned;
    private int killed;
    private long lastSpawnTick = -1;
    private long enoughTick = -1;
    private long provenTick = -1;
    private long portalTick = -1;
    private long clearedTick = -1;
    private boolean firstWaveCleared;
    private boolean finished;

    public WatcherWaves(WatcherConfig config, int total, RandomGenerator random) {
        this.config = config;
        this.total = total;
        this.random = random;
        int wave = Math.min(firstWave(), total);
        for (int index = 0; index < wave; index++) {
            long line = config.firstWaveLineTicks().get(Math.min(index, config.firstWaveLineTicks().size() - 1));
            planned.add(new Planned(line, line + config.lineToSpawnTicks()));
        }
    }

    private int firstWave() {
        return config.firstWave();
    }

    public List<Action> tick() {
        List<Action> actions = new ArrayList<>();
        if (finished) return actions;
        now++;
        if (now == config.introTicks()) actions.add(new Say(config.introLine()));
        if (now == config.bossBarTicks()) actions.add(new ShowBar());
        for (Planned next : planned) {
            if (next.lineTick() == now) {
                actions.add(new Say(pick(config.spawnLines())));
                actions.add(new FlyOut());
            }
        }
        Planned due = planned.peekFirst();
        if (due != null && due.spawnTick() <= now) {
            if (alive() < firstWave()) {
                planned.pollFirst();
                spawned++;
                lastSpawnTick = now;
                actions.add(new Summon());
                if (firstWaveCleared) planLater();
            }
        }
        if (spawned == total && enoughTick < 0 && now >= lastSpawnTick + config.enoughAfterLastSpawnTicks()) {
            enoughTick = now;
            actions.add(new Say(config.enoughLine()));
        }
        if (cleared() && enoughTick >= 0 && provenTick < 0 && now >= Math.max(enoughTick, clearedTick) + config.provenAfterClearTicks()) {
            provenTick = now;
            portalTick = now + config.portalAfterProvenTicks();
            actions.add(new Say(config.provenLine()));
        }
        if (portalTick >= 0 && now >= portalTick) {
            finished = true;
            actions.add(new OpenPortal());
        }
        return actions;
    }

    public List<Action> killed() {
        List<Action> actions = new ArrayList<>();
        if (killed >= spawned) return actions;
        killed++;
        if (cleared()) clearedTick = now;
        if (!firstWaveCleared && killed >= Math.min(firstWave(), total) && spawned >= Math.min(firstWave(), total)) {
            firstWaveCleared = true;
            if (spawned < total) {
                actions.add(new Say(pick(config.killLines())));
                planLater();
            }
        }
        return actions;
    }

    private void planLater() {
        if (spawned + planned.size() >= total) return;
        long after = Math.max(lastSpawnTick, now);
        long spawnTick = Math.max(now + config.lineToSpawnTicks(),
                after + config.laterGapMinTicks() + random.nextInt(config.laterGapMaxTicks() - config.laterGapMinTicks() + 1));
        planned.add(new Planned(spawnTick - config.lineToSpawnTicks(), spawnTick));
    }

    private String pick(List<String> lines) {
        return lines.get(random.nextInt(lines.size()));
    }

    public int alive() {
        return spawned - killed;
    }

    public int spawned() {
        return spawned;
    }

    public int remaining() {
        return total - killed;
    }

    public int total() {
        return total;
    }

    public boolean cleared() {
        return killed >= total;
    }

    public boolean finished() {
        return finished;
    }

    public float progress() {
        return total == 0 ? 0 : (float) remaining() / total;
    }

    /** The bar's name flips between the plain title and the title with the count still to kill, once a second. */
    public String barTitle(String title) {
        return (now / BAR_FLIP_TICKS) % 2 == 1 && remaining() > 0 ? title + " " + remaining() + "/" + total : title;
    }

    public long now() {
        return now;
    }
}
