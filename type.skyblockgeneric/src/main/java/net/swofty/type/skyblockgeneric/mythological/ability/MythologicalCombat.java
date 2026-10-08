package net.swofty.type.skyblockgeneric.mythological.ability;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.mythological.MythologicalCreatureMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class MythologicalCombat {
    private static final double DIGGER_LEASH = 40;
    private static final double BYSTANDER_RANGE = 20;
    private static final long DEBUFF_SAFETY_EXPIRY_MS = 10 * 60 * 1000;

    private MythologicalCombat() {
    }

    public static Optional<SkyBlockPlayer> target(MythologicalCreatureMob mob) {
        Instance instance = mob.getInstance();
        if (instance == null) return Optional.empty();
        if (mob.getDigger() != null) {
            SkyBlockPlayer digger = SkyBlockGenericLoader.getFromUUID(mob.getDigger());
            if (digger != null && digger.getInstance() == instance && !digger.isDead()
                    && digger.getPosition().distance(mob.getPosition()) <= DIGGER_LEASH) {
                return Optional.of(digger);
            }
        }
        return nearby(mob, BYSTANDER_RANGE).stream()
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(mob.getPosition())));
    }

    public static List<SkyBlockPlayer> nearby(Entity source, double range) {
        Instance instance = source.getInstance();
        if (instance == null) return List.of();
        return instance.getPlayers().stream()
                .filter(player -> player instanceof SkyBlockPlayer && !player.isDead())
                .map(player -> (SkyBlockPlayer) player)
                .filter(player -> player.getPosition().distance(source.getPosition()) <= range)
                .toList();
    }

    public static void lightning(Instance instance, Point position) {
        Entity bolt = new Entity(EntityType.LIGHTNING_BOLT);
        bolt.setInstance(instance, Pos.fromPoint(position));
        MinecraftServer.getSchedulerManager().scheduleTask(bolt::remove, TaskSchedule.seconds(1), TaskSchedule.stop());
    }

    public static void launch(SkyBlockPlayer player, Vec velocity) {
        if (PetItems.isKnockbackImmune(player)) return;
        player.setVelocity(velocity);
    }

    public static Vec away(Entity from, Entity target, double horizontal, double vertical) {
        Vec direction = target.getPosition().sub(from.getPosition()).asVec().withY(0);
        if (direction.lengthSquared() < 1.0E-4) direction = new Vec(1, 0, 0);
        return direction.normalize().mul(horizontal).withY(vertical);
    }

    public static void slay(MythologicalCreatureMob mob, SkyBlockPlayer player) {
        player.damage(new EntityDamage(mob, Float.MAX_VALUE));
    }

    public static void particles(Instance instance, Particle particle, Point at, float spread, int count) {
        if (instance == null) return;
        ParticlePacket packet = new ParticlePacket(particle, true, true,
                at.x(), at.y(), at.z(), spread, spread, spread, 0.05f, count);
        instance.getPlayers().forEach(player -> player.sendPacket(packet));
    }

    public static void sound(Entity source, String key, float volume, float pitch) {
        Instance instance = source.getInstance();
        if (instance == null) return;
        instance.playSound(Sound.sound(Key.key(key), Sound.Source.HOSTILE, volume, pitch), source.getPosition());
    }

    public static void debuff(SkyBlockPlayer player, String name, ItemStatistics statistics) {
        clearDebuff(player, name);
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(statistics)
                .withExpirationInMs(DEBUFF_SAFETY_EXPIRY_MS)
                .withDisplayName(name)
                .withDisplayColor("<c>")
                .build());
    }

    public static void clearDebuff(SkyBlockPlayer player, String name) {
        player.getStatistics().removeTemporaryStatistics(statistic -> name.equals(statistic.getDisplayName()));
    }
}
