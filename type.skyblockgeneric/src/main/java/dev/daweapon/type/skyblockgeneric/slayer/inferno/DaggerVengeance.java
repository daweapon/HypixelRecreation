package dev.daweapon.type.skyblockgeneric.slayer.inferno;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The daggers' second abilities. A dagger hit on a Hellion Shield arms them; if that shield switches
 * attunement within 6 seconds, Burning Vengeance repeats the last 8 hits' damage and Wraith Drain heals.
 */
public final class DaggerVengeance {
    private static final long WINDOW_MILLIS = 6_000L;
    private static final int REMEMBERED_HITS = 8;
    private static final double VENGEANCE_CAP = 25_000_000D;
    private static final double DRAIN_FLAT = 200D;
    private static final double DRAIN_PERCENT = 0.04D;
    private static final ThreadLocal<Boolean> APPLYING = ThreadLocal.withInitial(() -> false);
    private static final Map<UUID, Armed> ARMED = new ConcurrentHashMap<>();

    private DaggerVengeance() {
    }

    public static boolean isApplying() {
        return APPLYING.get();
    }

    static void recordHit(SkyBlockPlayer player, SkyBlockMob target, BlazeDagger dagger, float amount) {
        Armed armed = ARMED.computeIfAbsent(player.getUuid(), ignored -> new Armed());
        synchronized (armed) {
            armed.target = target;
            armed.dagger = dagger;
            armed.lastHitAt = System.currentTimeMillis();
            armed.hits.addLast((double) amount);
            while (armed.hits.size() > REMEMBERED_HITS) armed.hits.removeFirst();
        }
    }

    static void shieldSwitched(SkyBlockMob target) {
        long now = System.currentTimeMillis();
        ARMED.forEach((uuid, armed) -> {
            synchronized (armed) {
                if (armed.target != target || now - armed.lastHitAt > WINDOW_MILLIS || armed.pending) return;
                armed.pending = true;
                long delay = Math.max(1, WINDOW_MILLIS - (now - armed.lastHitAt));
                double stored = armed.hits.stream().mapToDouble(Double::doubleValue).sum();
                BlazeDagger dagger = armed.dagger;
                MinecraftServer.getSchedulerManager().buildTask(() -> trigger(uuid, target, dagger, stored))
                    .delay(TaskSchedule.millis(delay)).schedule();
            }
        });
    }

    private static void trigger(UUID uuid, SkyBlockMob target, BlazeDagger dagger, double stored) {
        Armed armed = ARMED.get(uuid);
        if (armed != null) {
            synchronized (armed) {
                armed.pending = false;
            }
        }
        if (!(MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid) instanceof SkyBlockPlayer player)) return;
        switch (dagger.vengeance()) {
            case BURNING_VENGEANCE -> {
                if (target.isDead() || target.isRemoved()) return;
                double damage = Math.min(VENGEANCE_CAP, stored);
                APPLYING.set(true);
                try {
                    target.damage(new Damage(DamageType.MAGIC, player, player, player.getPosition(), (float) damage));
                } finally {
                    APPLYING.set(false);
                }
                player.sendMessage(Text.of("<6>Burning Vengeance <7>dealt <c>{} <7>damage!", String.format("%,d", Math.round(damage))));
            }
            case WRAITH_DRAIN -> {
                double heal = DRAIN_FLAT + player.getMaxHealth() * DRAIN_PERCENT;
                player.setHealth((float) Math.min(player.getMaxHealth(), player.getHealth() + heal));
                player.sendMessage(Text.of("<6>Wraith Drain <7>healed you for <c>{}❤<7>!", Math.round(heal)));
            }
        }
    }

    private static final class Armed {
        private final Deque<Double> hits = new ArrayDeque<>();
        private SkyBlockMob target;
        private BlazeDagger dagger;
        private long lastHitAt;
        private boolean pending;
    }
}
