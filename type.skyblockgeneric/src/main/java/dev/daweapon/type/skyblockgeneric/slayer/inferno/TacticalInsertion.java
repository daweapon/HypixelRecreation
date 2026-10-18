package dev.daweapon.type.skyblockgeneric.slayer.inferno;

import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.DamageIndicator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gorilla Tactics: marks the caster's position and returns them there after three seconds at half health,
 * burning nearby mobs for a tenth of the damage dealt in those three seconds over six seconds.
 */
public final class TacticalInsertion {
    public static final int RETURN_TICKS = 60;
    public static final long DAMAGE_WINDOW_MS = 3_000;
    public static final double BURN_RADIUS = 3;
    public static final double BURN_DAMAGE_FRACTION = 0.10;
    public static final int BURN_SECONDS = 6;

    private record Hit(long at, double damage) {
    }

    private static final Map<UUID, ArrayDeque<Hit>> RECENT = new ConcurrentHashMap<>();

    private TacticalInsertion() {
    }

    public static void recordDamage(SkyBlockPlayer player, double damage) {
        ArrayDeque<Hit> hits = RECENT.computeIfAbsent(player.getUuid(), _ -> new ArrayDeque<>());
        synchronized (hits) {
            hits.addLast(new Hit(System.currentTimeMillis(), damage));
        }
    }

    public static double recentDamage(UUID player, long now) {
        ArrayDeque<Hit> hits = RECENT.get(player);
        if (hits == null) return 0;
        synchronized (hits) {
            while (!hits.isEmpty() && now - hits.peekFirst().at() > DAMAGE_WINDOW_MS) hits.removeFirst();
            return hits.stream().mapToDouble(Hit::damage).sum();
        }
    }

    public static double burnPerSecond(double recentDamage) {
        return recentDamage * BURN_DAMAGE_FRACTION / BURN_SECONDS;
    }

    public static float halfHealth(float health) {
        return Math.max(1F, health / 2F);
    }

    public static boolean begin(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos mark = player.getPosition();
        player.sendMessage("<6>Gorilla Tactics! <7>Returning in <a>3s<7>.");
        MinecraftServer.getSchedulerManager().buildTask(() -> returnToMark(player, instance, mark))
            .delay(TaskSchedule.tick(RETURN_TICKS)).schedule();
        return true;
    }

    private static void returnToMark(SkyBlockPlayer player, Instance instance, Pos mark) {
        if (!player.isOnline() || player.getInstance() != instance) return;
        double burn = burnPerSecond(recentDamage(player.getUuid(), System.currentTimeMillis()));
        player.teleport(mark);
        player.setHealth(halfHealth(player.getHealth()));
        ParticlePacket packet = new ParticlePacket(Particle.FLAME, true, true, mark.x(), mark.y() + 1, mark.z(),
            1.5f, 0.5f, 1.5f, 0.02f, 40);
        instance.getPlayers().forEach(viewer -> viewer.sendPacket(packet));
        if (burn <= 0) return;
        for (Entity entity : instance.getEntities()) {
            if (!(entity instanceof SkyBlockMob) || !(entity instanceof LivingEntity target) || target.isDead()) continue;
            if (target.getPosition().distance(mark) <= BURN_RADIUS) ignite(player, target, burn);
        }
    }

    private static void ignite(SkyBlockPlayer player, LivingEntity target, double perSecond) {
        AtomicInteger ticks = new AtomicInteger();
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            if (ticks.getAndIncrement() >= BURN_SECONDS || target.isRemoved() || target.isDead()) return TaskSchedule.stop();
            target.damage(new Damage(DamageType.PLAYER_ATTACK, player, player, player.getPosition(), (float) perSecond));
            new DamageIndicator().damage((float) perSecond).pos(target.getPosition()).critical(false)
                .display(target.getInstance());
            return TaskSchedule.seconds(1);
        });
    }
}
