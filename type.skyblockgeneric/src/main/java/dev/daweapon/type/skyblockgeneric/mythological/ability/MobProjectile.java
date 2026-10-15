package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.entity.metadata.projectile.ArrowMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

/**
 * A straight-line projectile a mob fires at a player. It is moved server side every tick, so it
 * ignores vanilla projectile physics, and it reports the first player it passes through.
 */
public final class MobProjectile {
    private static final double HIT_RADIUS = 0.9;
    private static final int MAX_TICKS = 100;

    private final Entity visual;
    private final Vec step;
    private final Consumer<SkyBlockPlayer> onHit;
    private final boolean spins;
    private Task task;
    private int ticks;

    private MobProjectile(Entity visual, Vec step, boolean spins, Consumer<SkyBlockPlayer> onHit) {
        this.visual = visual;
        this.step = step;
        this.spins = spins;
        this.onHit = onHit;
    }

    public static void arrow(Entity shooter, Entity target, double blocksPerTick, Consumer<SkyBlockPlayer> onHit) {
        Entity arrow = new Entity(EntityType.ARROW);
        arrow.editEntityMeta(ArrowMeta.class, meta -> meta.setShooter(shooter));
        fire(arrow, shooter, aim(shooter, target, 0), blocksPerTick, false, onHit);
    }

    public static void arrowSpread(Entity shooter, Entity target, double blocksPerTick, double spreadDegrees,
                                   Consumer<SkyBlockPlayer> onHit) {
        Entity arrow = new Entity(EntityType.ARROW);
        arrow.editEntityMeta(ArrowMeta.class, meta -> meta.setShooter(shooter));
        fire(arrow, shooter, aim(shooter, target, spreadDegrees), blocksPerTick, false, onHit);
    }

    public static void item(Entity shooter, Entity target, ItemStack item, double blocksPerTick,
                            Consumer<SkyBlockPlayer> onHit) {
        Entity display = new Entity(EntityType.ITEM_DISPLAY);
        display.setNoGravity(true);
        display.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(item);
            meta.setPosRotInterpolationDuration(1);
        });
        fire(display, shooter, aim(shooter, target, 0), blocksPerTick, true, onHit);
    }

    public static void witherSkull(Entity shooter, Entity target, double blocksPerTick, Consumer<SkyBlockPlayer> onHit) {
        fire(new Entity(EntityType.WITHER_SKULL), shooter, aim(shooter, target, 0), blocksPerTick, false, onHit);
    }

    public static void bobber(Entity shooter, Entity target, double blocksPerTick, Consumer<SkyBlockPlayer> onHit) {
        Entity hook = new Entity(EntityType.FISHING_BOBBER);
        hook.editEntityMeta(FishingHookMeta.class, meta -> meta.setOwnerEntity(shooter));
        fire(hook, shooter, aim(shooter, target, 0), blocksPerTick, false, onHit);
    }

    private static Vec aim(Entity shooter, Entity target, double spreadDegrees) {
        Pos from = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        Pos to = target.getPosition().add(0, target.getEyeHeight() * 0.7, 0);
        Vec direction = to.sub(from).asVec().normalize();
        if (spreadDegrees > 0) {
            double yaw = Math.toRadians((Math.random() - 0.5) * 2 * spreadDegrees);
            double pitch = Math.toRadians((Math.random() - 0.5) * spreadDegrees);
            direction = direction.rotateAroundY(yaw).add(0, Math.sin(pitch), 0).normalize();
        }
        return direction;
    }

    private static void fire(Entity visual, Entity shooter, Vec direction, double blocksPerTick, boolean spins,
                             Consumer<SkyBlockPlayer> onHit) {
        Instance instance = shooter.getInstance();
        if (instance == null) return;
        visual.setNoGravity(true);
        Pos start = shooter.getPosition().add(0, shooter.getEyeHeight(), 0).withDirection(direction);
        MobProjectile projectile = new MobProjectile(visual, direction.mul(blocksPerTick), spins, onHit);
        visual.setInstance(instance, start).thenRun(projectile::launch);
    }

    private void launch() {
        task = MinecraftServer.getSchedulerManager().buildTask(this::step)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private void step() {
        Instance instance = visual.getInstance();
        if (instance == null || visual.isRemoved() || ticks++ >= MAX_TICKS) {
            finish();
            return;
        }
        Pos next = visual.getPosition().add(step);
        if (spins) next = next.withPitch(visual.getPosition().pitch() + 40);
        if (instance.getBlock(next).isSolid()) {
            finish();
            return;
        }
        visual.teleport(next);
        for (var player : instance.getPlayers()) {
            if (!(player instanceof SkyBlockPlayer skyBlockPlayer) || player.isDead()) continue;
            Pos body = player.getPosition().add(0, 0.9, 0);
            if (body.distance(next) <= HIT_RADIUS) {
                finish();
                onHit.accept(skyBlockPlayer);
                return;
            }
        }
    }

    private void finish() {
        if (task != null) task.cancel();
        if (!visual.isRemoved()) visual.remove();
    }
}
