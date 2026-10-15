package dev.daweapon.type.skyblockgeneric.entity.mob.ai;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Drop-in replacement for Minestom's {@code RandomStrollGoal} that wanders to a random nearby block,
 * but drives movement through the mob's {@link dev.daweapon.type.generic.entity.ai.vanilla.VanillaNavigator}
 * (our own A* + vanilla follow loop) rather than Minestom's built-in navigator.
 * Strolls are cheap by design: only mobs a player can see wander, each stroll tries a few targets with a
 * small node budget, start times are jittered, and only a handful of strolls may path in any one tick.
 */
public class VanillaRandomStrollGoal extends GoalSelector {
    static final long MIN_DELAY_MS = 2500;
    static final long DELAY_JITTER_MS = 3000;
    static final int ATTEMPTS = 3;
    static final int STROLLS_PER_TICK = 6;
    private static final int MAX_VERTICAL = 2;
    private static final long TICK_MS = 50;

    private static long budgetWindow;
    private static int budgetUsed;

    private final int radius;
    private final int maxNodes;
    private long nextStroll;

    public VanillaRandomStrollGoal(@NotNull EntityCreature entityCreature, int radius) {
        super(entityCreature);
        this.radius = radius;
        this.maxNodes = nodeBudget(radius);
        this.nextStroll = System.currentTimeMillis() + ThreadLocalRandom.current().nextLong(MIN_DELAY_MS + DELAY_JITTER_MS);
    }

    static int nodeBudget(int radius) {
        return Math.max(64, radius * 25);
    }

    @Override
    public boolean shouldStart() {
        long now = System.currentTimeMillis();
        if (now < nextStroll) return false;
        if (entityCreature.getViewers().isEmpty()) {
            scheduleNext(now);
            return false;
        }
        return takeBudget(now);
    }

    static synchronized boolean takeBudget(long now) {
        long window = now / TICK_MS;
        if (window != budgetWindow) {
            budgetWindow = window;
            budgetUsed = 0;
        }
        return budgetUsed++ < STROLLS_PER_TICK;
    }

    @Override
    public void start() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Pos position = entityCreature.getPosition();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            Pos target = position.add(random.nextInt(-radius, radius + 1),
                    random.nextInt(0, Math.min(radius, MAX_VERTICAL) + 1),
                    random.nextInt(-radius, radius + 1));
            if (((SkyBlockMob) entityCreature).getVanillaNavigator().pathTo(target, maxNodes)) break;
        }
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public boolean shouldEnd() {
        return true;
    }

    @Override
    public void end() {
        scheduleNext(System.currentTimeMillis());
    }

    private void scheduleNext(long now) {
        nextStroll = now + MIN_DELAY_MS + ThreadLocalRandom.current().nextLong(DELAY_JITTER_MS);
    }

    public int getRadius() {
        return radius;
    }
}
