package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A ring of projectiles flying straight outward from a point (Bladesoul's skull ring, Duke X's Flaming Fists).
 * Each player is hit at most once per ring.
 */
public final class RadialProjectiles {
    private static final double HIT_RADIUS = 1.0;

    private final List<Entity> visuals = new ArrayList<>();
    private final List<Vec> directions = new ArrayList<>();
    private final Set<UUID> hit = new HashSet<>();
    private final double step;
    private final int lifetime;
    private final Consumer<SkyBlockPlayer> onHit;
    private Task task;
    private int ticks;

    private RadialProjectiles(double step, int lifetime, Consumer<SkyBlockPlayer> onHit) {
        this.step = step;
        this.lifetime = lifetime;
        this.onHit = onHit;
    }

    public static void fire(Instance instance, Pos origin, int count, double blocksPerTick, double range,
                            ItemStack item, Consumer<SkyBlockPlayer> onHit) {
        if (instance == null) return;
        RadialProjectiles ring = new RadialProjectiles(blocksPerTick, (int) Math.ceil(range / blocksPerTick), onHit);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            Vec direction = new Vec(Math.cos(angle), 0, Math.sin(angle));
            Entity display = new Entity(EntityType.ITEM_DISPLAY);
            display.setNoGravity(true);
            display.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(item);
                meta.setPosRotInterpolationDuration(1);
            });
            display.setInstance(instance, origin);
            ring.visuals.add(display);
            ring.directions.add(direction);
        }
        ring.task = MinecraftServer.getSchedulerManager().buildTask(ring::tick).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void tick() {
        if (ticks++ >= lifetime || visuals.isEmpty()) {
            finish();
            return;
        }
        for (int i = 0; i < visuals.size(); i++) {
            Entity visual = visuals.get(i);
            Instance instance = visual.getInstance();
            if (instance == null) continue;
            Pos next = visual.getPosition().add(directions.get(i).mul(step));
            visual.teleport(next);
            for (var player : instance.getPlayers()) {
                if (!(player instanceof SkyBlockPlayer skyBlockPlayer) || player.isDead() || hit.contains(player.getUuid())) continue;
                if (player.getPosition().add(0, 0.9, 0).distance(next) <= HIT_RADIUS + 0.4) {
                    hit.add(player.getUuid());
                    onHit.accept(skyBlockPlayer);
                }
            }
        }
    }

    private void finish() {
        if (task != null) task.cancel();
        visuals.forEach(visual -> {
            if (!visual.isRemoved()) visual.remove();
        });
        visuals.clear();
    }
}
