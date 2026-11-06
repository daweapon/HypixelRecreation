package dev.daweapon.type.catacombs.engine;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Hypixel's gate and door opening, as captured on the Entrance gate, Wither doors, the Blood door and Bonzo's exit
 * gates: the blocks turn into barriers, a falling block riding an invisible bat stands in for each one and the whole
 * gate sinks into the floor, gaining speed; the barriers become air at the motion's open tick and the riders vanish at
 * its last tick.
 */
public final class GateLift {
    public static final int OPEN_TICK = 14;
    public static final int DURATION_TICKS = 22;
    static final double RIDER_DROP = 0.65625;
    private static final int STILL_TICKS = 3;
    private static final double SINK_ACCELERATION = 0.012;

    /** How a gate sinks: still for a few ticks, then accelerating up to a top speed (blocks per tick). */
    public record Motion(int stillTicks, double acceleration, double topSpeed, int openTick, int durationTicks) {
        /** Doors and the Entrance gate: barriers gone after 0.7 s, riders after 1.1 s, no speed cap. */
        public static final Motion DOOR = new Motion(STILL_TICKS, SINK_ACCELERATION, Double.MAX_VALUE, OPEN_TICK, DURATION_TICKS);
        /**
         * Bonzo's exit gates (both captured runs): the riders sink about 0.31 blocks every three ticks once moving,
         * the barriers turn to air after 2.6 s and the riders are removed after 4.1 s.
         */
        public static final Motion BOSS_GATE = new Motion(STILL_TICKS, SINK_ACCELERATION, 0.104, 52, 81);
        /** A pulled secret lever (M7 run 1, Trap room): barriers at once, air after 1.25 s, riders gone after 2 s. */
        public static final Motion LEVER = new Motion(STILL_TICKS, SINK_ACCELERATION, Double.MAX_VALUE, 25, 40);

        public double sinkAfter(int ticks) {
            int moving = Math.max(0, ticks - stillTicks);
            if (topSpeed == Double.MAX_VALUE) return acceleration * moving * moving;
            double cappedFrom = topSpeed / (2 * acceleration);
            if (moving <= cappedFrom) return acceleration * moving * moving;
            return acceleration * cappedFrom * cappedFrom + topSpeed * (moving - cappedFrom);
        }
    }

    private final Instance instance;
    private final List<int[]> blocks;
    private final Motion motion;
    private final List<Entity> bats = new ArrayList<>();
    private final List<Entity> riders = new ArrayList<>();
    private int ticks;
    private boolean opened;

    private GateLift(Instance instance, List<int[]> blocks, Motion motion) {
        this.instance = instance;
        this.blocks = List.copyOf(blocks);
        this.motion = motion;
    }

    public static GateLift start(Instance instance, List<int[]> blocks, Block appearance) {
        return start(instance, blocks, _ -> appearance, Motion.DOOR);
    }

    public static GateLift start(Instance instance, List<int[]> blocks, Function<int[], Block> appearance, Motion motion) {
        GateLift lift = new GateLift(instance, blocks, motion);
        lift.begin(appearance);
        return lift;
    }

    public static void run(Instance instance, List<int[]> blocks, Block appearance) {
        run(instance, blocks, _ -> appearance, Motion.DOOR);
    }

    public static void run(Instance instance, List<int[]> blocks, Function<int[], Block> appearance, Motion motion) {
        GateLift lift = start(instance, blocks, appearance, motion);
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (lift.tick()) task[0].cancel();
        }).repeat(TaskSchedule.nextTick()).schedule();
    }

    private void begin(Function<int[], Block> appearance) {
        for (int[] at : blocks) {
            Block look = appearance.apply(at);
            instance.setBlock(at[0], at[1], at[2], Block.BARRIER);
            Entity bat = new Entity(EntityType.BAT);
            bat.setInvisible(true);
            bat.setNoGravity(true);
            bat.setSilent(true);
            Entity rider = new Entity(EntityType.FALLING_BLOCK);
            rider.editEntityMeta(FallingBlockMeta.class, meta -> meta.setBlock(look == null ? Block.STONE : look));
            rider.setNoGravity(true);
            Pos home = home(at);
            bat.setInstance(instance, home).thenCompose(_ -> rider.setInstance(instance, home.add(0, bat.getBoundingBox().height(), 0)))
                    .thenRun(() -> bat.addPassenger(rider));
            bats.add(bat);
            riders.add(rider);
        }
    }

    private static Pos home(int[] at) {
        return new Pos(at[0] + 0.5, at[1] - RIDER_DROP, at[2] + 0.5);
    }

    /** Advances the lift by one tick; true once it has finished and cleaned up. */
    public boolean tick() {
        ticks++;
        double sink = motion.sinkAfter(ticks);
        for (int index = 0; index < bats.size(); index++) {
            Entity bat = bats.get(index);
            if (bat.getInstance() != null) bat.teleport(home(blocks.get(index)).sub(0, sink, 0));
        }
        if (!opened && ticks >= motion.openTick()) {
            opened = true;
            for (int[] at : blocks) instance.setBlock(at[0], at[1], at[2], Block.AIR);
        }
        if (ticks < motion.durationTicks()) return false;
        riders.forEach(Entity::remove);
        bats.forEach(Entity::remove);
        return true;
    }

    static double sinkAfter(int ticks) {
        return Motion.DOOR.sinkAfter(ticks);
    }

    public boolean opened() {
        return opened;
    }

    public List<Entity> riders() {
        return List.copyOf(riders);
    }
}
