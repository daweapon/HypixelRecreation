package dev.daweapon.type.skyblockgeneric.hotf.throwing;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;

public final class ThrownAxeEntity extends Entity {
    public static final double BLOCKS_PER_TICK = 1.6;
    private static final int MAX_FLIGHT_TICKS = 80;

    private final Point target;
    private final Runnable onArrival;
    private final double blocksPerTick;
    private int flightTicks;

    private ThrownAxeEntity(ItemStack stack, Point target, double bonus, Runnable onArrival) {
        super(EntityType.ITEM_DISPLAY);
        this.target = target;
        this.onArrival = onArrival;
        this.blocksPerTick = BLOCKS_PER_TICK * (1 + bonus);
        setNoGravity(true);
        double scale = 1.2 * (1 + bonus);
        editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(stack);
            meta.setScale(new Vec(scale, scale, scale));
            meta.setPosRotInterpolationDuration(2);
        });
    }

    public static void launch(Instance instance, Pos from, Point target, ItemStack stack, Runnable onArrival) {
        launch(instance, from, target, stack, 0, onArrival);
    }

    public static void launch(Instance instance, Pos from, Point target, ItemStack stack, double bonus, Runnable onArrival) {
        ThrownAxeEntity axe = new ThrownAxeEntity(stack, target, bonus, onArrival);
        axe.setInstance(instance, from).thenRun(axe::fly);
    }

    private void fly() {
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (isRemoved()) return TaskSchedule.stop();
            Vec toTarget = Vec.fromPoint(target).sub(getPosition());
            double distance = toTarget.length();
            if (distance <= blocksPerTick || ++flightTicks > MAX_FLIGHT_TICKS) {
                remove();
                onArrival.run();
                return TaskSchedule.stop();
            }
            Vec step = toTarget.normalize().mul(blocksPerTick);
            Pos next = getPosition().add(step).withView(getPosition().yaw() + 60, 0);
            teleport(next);
            return TaskSchedule.nextTick();
        });
    }
}
