package dev.daweapon.type.skyblockgeneric.utility;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.item.set.sets.RekindledEmberSet;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BurningDamage {
    public enum Source { FIRE_ASPECT, FLAME, INFERNO }

    public static final int TICKS_PER_SECOND = 20;

    private record Key(UUID target, Source source) {
    }

    private record PlayerBurn(long since, long until) {
    }

    private static final Map<Key, Effect> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerBurn> PLAYERS_ON_FIRE = new ConcurrentHashMap<>();

    private BurningDamage() {
    }

    public static void ignite(SkyBlockPlayer owner, LivingEntity target, Source source, RegistryKey<DamageType> damageType,
                              double damagePerSecond, int durationTicks) {
        if (durationTicks <= 0 || damagePerSecond <= 0) return;
        Key key = new Key(target.getUuid(), source);
        Effect effect = new Effect(key, owner, target, damageType, damagePerSecond, durationTicks);
        Effect previous = ACTIVE.put(key, effect);
        if (previous != null) previous.cancel();
        effect.schedule();
    }

    public static boolean isIgnited(LivingEntity target) {
        UUID id = target.getUuid();
        for (Key key : ACTIVE.keySet()) {
            if (key.target().equals(id)) return true;
        }
        return false;
    }

    public static void setPlayerOnFire(SkyBlockPlayer player, long millis) {
        long now = System.currentTimeMillis();
        PlayerBurn existing = PLAYERS_ON_FIRE.get(player.getUuid());
        long since = existing != null && existing.until() > now ? existing.since() : now;
        long until = now + millis;
        PLAYERS_ON_FIRE.put(player.getUuid(), new PlayerBurn(since, until));
        player.getEntityMeta().setOnFire(true);
        MinecraftServer.getSchedulerManager().scheduleTask(() -> extinguishIfExpired(player),
                TaskSchedule.millis(millis + 50), TaskSchedule.stop());
    }

    private static void extinguishIfExpired(SkyBlockPlayer player) {
        PlayerBurn burn = PLAYERS_ON_FIRE.get(player.getUuid());
        if (burn != null && burn.until() > System.currentTimeMillis()) return;
        PLAYERS_ON_FIRE.remove(player.getUuid());
        player.getEntityMeta().setOnFire(false);
    }

    public static double secondsOnFire(SkyBlockPlayer player) {
        PlayerBurn burn = PLAYERS_ON_FIRE.get(player.getUuid());
        long now = System.currentTimeMillis();
        if (burn == null || burn.until() <= now) return 0;
        return (now - burn.since()) / 1000D;
    }

    public static boolean isTakingFireDamage(SkyBlockPlayer player) {
        if (secondsOnFire(player) > 0) return true;
        if (player.getInstance() == null) return false;
        Block block = player.getInstance().getBlock(player.getPosition());
        return block.compare(Block.LAVA) || block.compare(Block.FIRE) || block.compare(Block.SOUL_FIRE);
    }

    private static final class Effect {
        private final Key key;
        private final SkyBlockPlayer owner;
        private final LivingEntity target;
        private final RegistryKey<DamageType> damageType;
        private final double damagePerSecond;
        private int ticksRemaining;
        private volatile boolean cancelled;

        private Effect(Key key, SkyBlockPlayer owner, LivingEntity target, RegistryKey<DamageType> damageType,
                       double damagePerSecond, int durationTicks) {
            this.key = key;
            this.owner = owner;
            this.target = target;
            this.damageType = damageType;
            this.damagePerSecond = damagePerSecond;
            this.ticksRemaining = durationTicks;
        }

        private void cancel() {
            cancelled = true;
        }

        private void schedule() {
            int step = Math.min(TICKS_PER_SECOND, ticksRemaining);
            MinecraftServer.getSchedulerManager().scheduleTask(() -> tick(step),
                    TaskSchedule.tick(step), TaskSchedule.stop());
        }

        private void tick(int step) {
            if (cancelled) return;
            if (target.isRemoved() || target.isDead() || target.getInstance() == null) {
                ACTIVE.remove(key, this);
                return;
            }
            float damage = (float) (damagePerSecond * step / TICKS_PER_SECOND * RekindledEmberSet.burningMultiplier(owner));
            target.damage(new Damage(damageType, owner, owner, owner.getPosition(), damage));
            new DamageIndicator().damage(damage).pos(target.getPosition()).critical(false).display(target.getInstance());
            Pos particlePos = target.getPosition().add(0, target.getEyeHeight(), 0);
            target.getInstance().sendGroupedPacket(new ParticlePacket(
                    Particle.FLAME, false, false,
                    particlePos.x(), particlePos.y(), particlePos.z(),
                    0.3f, 0.3f, 0.3f, 0.02f, 5));
            ticksRemaining -= step;
            if (ticksRemaining > 0) schedule();
            else ACTIVE.remove(key, this);
        }
    }
}
