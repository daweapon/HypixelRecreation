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

/**
 * Hypixel's gate and door opening, as captured on the Entrance gate, Wither doors and the Blood door: the blocks turn
 * into barriers, a falling block riding an invisible bat stands in for each one and the whole gate sinks into the
 * floor, gaining speed; the barriers become air after {@value #OPEN_TICK} ticks and the riders vanish after
 * {@value #DURATION_TICKS}.
 */
public final class GateLift {
    public static final int OPEN_TICK = 14;
    public static final int DURATION_TICKS = 22;
    static final double RIDER_DROP = 0.65625;
    private static final int STILL_TICKS = 3;
    private static final double SINK_ACCELERATION = 0.012;

    private final Instance instance;
    private final List<int[]> blocks;
    private final List<Entity> bats = new ArrayList<>();
    private final List<Entity> riders = new ArrayList<>();
    private int ticks;
    private boolean opened;

    private GateLift(Instance instance, List<int[]> blocks) {
        this.instance = instance;
        this.blocks = List.copyOf(blocks);
    }

    public static GateLift start(Instance instance, List<int[]> blocks, Block appearance) {
        GateLift lift = new GateLift(instance, blocks);
        lift.begin(appearance);
        return lift;
    }

    public static void run(Instance instance, List<int[]> blocks, Block appearance) {
        GateLift lift = start(instance, blocks, appearance);
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (lift.tick()) task[0].cancel();
        }).repeat(TaskSchedule.nextTick()).schedule();
    }

    private void begin(Block appearance) {
        for (int[] at : blocks) {
            instance.setBlock(at[0], at[1], at[2], Block.BARRIER);
            Entity bat = new Entity(EntityType.BAT);
            bat.setInvisible(true);
            bat.setNoGravity(true);
            bat.setSilent(true);
            Entity rider = new Entity(EntityType.FALLING_BLOCK);
            rider.editEntityMeta(FallingBlockMeta.class, meta -> meta.setBlock(appearance));
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
        double sink = sinkAfter(ticks);
        for (int index = 0; index < bats.size(); index++) {
            Entity bat = bats.get(index);
            if (bat.getInstance() != null) bat.teleport(home(blocks.get(index)).sub(0, sink, 0));
        }
        if (!opened && ticks >= OPEN_TICK) {
            opened = true;
            for (int[] at : blocks) instance.setBlock(at[0], at[1], at[2], Block.AIR);
        }
        if (ticks < DURATION_TICKS) return false;
        riders.forEach(Entity::remove);
        bats.forEach(Entity::remove);
        return true;
    }

    static double sinkAfter(int ticks) {
        int moving = Math.max(0, ticks - STILL_TICKS);
        return SINK_ACCELERATION * moving * moving;
    }

    public boolean opened() {
        return opened;
    }

    public List<Entity> riders() {
        return List.copyOf(riders);
    }
}
