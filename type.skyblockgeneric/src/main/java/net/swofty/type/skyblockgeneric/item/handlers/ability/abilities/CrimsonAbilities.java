package net.swofty.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.handlers.ability.AbilityRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityActivation;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility.AbilityManaCost;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.TemporaryStatistic;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Item abilities for Crimson Isle gear. Other workstreams append their abilities to {@link #register()}.
 */
public final class CrimsonAbilities {
    private static final double FIRE_VEIL_RADIUS = 3.5;
    private static final double FIRE_FREEZE_RADIUS = 5;
    private static final double FIRESTORM_RADIUS = 7;
    private static final double LIFE_BLOOD_RADIUS = 10;
    private static final double SPELL_SCALING = 0.1;

    private CrimsonAbilities() {
    }

    public static void register() {
        BlazeSlayerAbilities.register();
        AbilityRegistry.register(new RegisteredAbility(
                "FIRE_VEIL",
                "Fire Veil",
                (_, _) -> "<7>Creates a veil of fire around you for <a>5s<7>, dealing <c>15,000 <7>damage per second to mobs within.",
                AbilityActivation.RIGHT_CLICK,
                20,
                new AbilityManaCost(300),
                (player, _, _, _) -> {
                    repeat(player, 5, () -> {
                        particles(player, player.getPosition(), FIRE_VEIL_RADIUS);
                        AbilityDamage.hitArea(player, player.getPosition(), FIRE_VEIL_RADIUS, "Fire Veil", 15_000, SPELL_SCALING);
                    });
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "FIRE_FREEZE",
                "Fire Freeze",
                (_, _) -> "<7>Creates a circle with a radius of <a>5 <7>blocks. After <a>5s<7>, all mobs inside are frozen for <a>10s<7>.",
                AbilityActivation.RIGHT_CLICK,
                200,
                new AbilityManaCost(500),
                (player, _, _, _) -> {
                    Pos centre = player.getPosition();
                    repeat(player, 5, () -> particles(player, centre, FIRE_FREEZE_RADIUS));
                    MinecraftServer.getSchedulerManager().buildTask(() -> {
                        if (player.getInstance() == null) return;
                        for (Entity entity : player.getInstance().getNearbyEntities(centre, FIRE_FREEZE_RADIUS)) {
                            if (entity instanceof SkyBlockMob mob && !mob.isDead()) freeze(mob, 10);
                        }
                    }).delay(TaskSchedule.seconds(5)).schedule();
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "FIRESTORM",
                "Firestorm",
                (_, _) -> "<7>Shoots a projectile that on impact creates a firestorm, dealing up to <c>42,000 <7>damage over <a>10 <7>seconds to mobs in a <a>7 <7>block radius.",
                AbilityActivation.RIGHT_CLICK,
                400,
                new AbilityManaCost(1_000),
                (player, _, _, _) -> {
                    Point target = player.getTargetBlockPosition(40);
                    Pos impact = target == null
                            ? player.getPosition().add(player.getPosition().direction().mul(20))
                            : new Pos(target.blockX() + 0.5, target.blockY() + 1, target.blockZ() + 0.5);
                    repeat(player, 10, () -> {
                        particles(player, impact, FIRESTORM_RADIUS);
                        AbilityDamage.hitArea(player, impact, FIRESTORM_RADIUS, "Firestorm", 4_200, SPELL_SCALING);
                    });
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "LIFE_BLOOD",
                "Life Blood",
                (_, _) -> "<7>Use <a>10% <7>of your max health to boost your nearby allies by <c>+30 ❁ Strength <7>for <a>10 <7>seconds.",
                AbilityActivation.RIGHT_CLICK,
                20,
                HealthAbilityCost.percentOfMax(10),
                (player, _, _, _) -> {
                    for (Player nearby : player.getInstance().getPlayers()) {
                        if (!(nearby instanceof SkyBlockPlayer ally)) continue;
                        if (ally.getPosition().distance(player.getPosition()) > LIFE_BLOOD_RADIUS) continue;
                        boost(ally, "Life Blood", ItemStatistic.STRENGTH, 30, 10_000);
                    }
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "RAGNAROCK",
                "Ragnarock",
                (_, _) -> "<7>Begin a channel. After not taking damage for <a>3s<7>, gain <a>1.5x <7>this weapon's <c>❁ Strength <7>for <a>10s<7>.",
                AbilityActivation.RIGHT_CLICK,
                400,
                new AbilityManaCost(500),
                (player, item, _, _) -> {
                    double strength = item.getAttributeHandler().getStatistics().getOverall(ItemStatistic.STRENGTH) * 1.5;
                    channel(player, 3, () -> {
                        boost(player, "Ragnarock", ItemStatistic.STRENGTH, strength, 10_000);
                        player.sendMessage("<a>Your Ragnarock channel completed! <c>+" + StringUtility.commaify(strength) + " ❁ Strength <a>for 10s.");
                    });
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "BAD_HEALTH",
                "Bad Health",
                (_, _) -> "<7>Gain <c>+5 ❁ Strength <7>for every <a>5% <7>of total HP you have for <a>5s<7>. Capped at <c>+100 ❁ Strength<7>.",
                AbilityActivation.RIGHT_CLICK,
                100,
                HealthAbilityCost.flat(100),
                (player, _, _, _) -> {
                    double percent = player.getHealth() / player.getMaxHealth() * 100;
                    double strength = Math.min(100, Math.floor(percent / 5) * 5);
                    boost(player, "Bad Health", ItemStatistic.STRENGTH, strength, 5_000);
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "COIN_CONVERSION",
                "Coin Conversion",
                (_, _) -> "<7>Spend coins to cast a projectile which deals <c>10,000 <7>damage on hit and knocks enemies back.",
                AbilityActivation.RIGHT_CLICK,
                0,
                new CoinAbilityCost(100_000),
                (player, _, _, _) -> {
                    SkyBlockMob target = mobInSight(player, 30);
                    if (target == null) {
                        player.sendMessage("<c>There is nothing to hit!");
                        return false;
                    }
                    AbilityDamage.hitArea(player, target.getPosition(), 1, "Coin Conversion", 10_000, 0);
                    target.setVelocity(target.getPosition().sub(player.getPosition()).asVec().normalize().mul(20).withY(6));
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "BIG_PULL",
                "Big Pull",
                (_, _) -> "<7>Throw a hook to grab and pull blazes and certain other <f>✈ Airborne <7>mobs.\n\n"
                        + "<7>Pulling a mob multiplies its damage taken by <c>2x <7>and stops it from flying.\n"
                        + "<8>(The damage multiplier is not active yet)",
                AbilityActivation.RIGHT_CLICK,
                20,
                new AbilityManaCost(30),
                (player, _, _, _) -> {
                    SkyBlockMob target = mobInSight(player, 30);
                    if (target == null) return false;
                    Vec pull = player.getPosition().sub(target.getPosition()).asVec().normalize().mul(25).withY(8);
                    target.setVelocity(pull);
                    target.setNoGravity(false);
                    return true;
                }));

        AbilityRegistry.register(new RegisteredAbility(
                "DEPLOY_TOTEM_OF_CORRUPTION",
                "Deploy",
                (_, _) -> "<7>Place a totem down for <a>5m<7>, buffing only yourself within <a>30 <7>blocks.",
                AbilityActivation.RIGHT_CLICK,
                20,
                new AbilityManaCost(500),
                (player, _, _, _) -> CorruptionTotems.deploy(player)));
    }

    static void boost(SkyBlockPlayer player, String name, ItemStatistic statistic, double amount, long millis) {
        ItemStatistics stats = ItemStatistics.builder().withBase(statistic, amount).build();
        player.getStatistics().boostStatistic(TemporaryStatistic.builder()
                .withStatistics(stats).withExpirationInMs(millis).withDisplayName(name).build());
    }

    private static void repeat(SkyBlockPlayer player, int seconds, Runnable tick) {
        AtomicInteger remaining = new AtomicInteger(seconds);
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!player.isOnline() || remaining.getAndDecrement() <= 0) {
                task[0].cancel();
                return;
            }
            tick.run();
        }).repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void channel(SkyBlockPlayer player, int seconds, Runnable onComplete) {
        float startHealth = player.getHealth();
        AtomicInteger ticks = new AtomicInteger();
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!player.isOnline()) {
                task[0].cancel();
                return;
            }
            if (player.getHealth() < startHealth) {
                player.sendMessage("<c>Your Ragnarock channel was interrupted!");
                task[0].cancel();
                return;
            }
            if (ticks.incrementAndGet() >= seconds * 20) {
                task[0].cancel();
                onComplete.run();
            }
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private static void freeze(SkyBlockMob mob, int seconds) {
        Pos frozenAt = mob.getPosition();
        AtomicInteger ticks = new AtomicInteger();
        Task[] task = new Task[1];
        task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (mob.isDead() || mob.isRemoved() || ticks.incrementAndGet() > seconds * 20) {
                task[0].cancel();
                return;
            }
            mob.setVelocity(Vec.ZERO);
            mob.teleport(frozenAt.withView(mob.getPosition()));
        }).repeat(TaskSchedule.tick(1)).schedule();
    }

    private static void particles(SkyBlockPlayer player, Pos centre, double radius) {
        if (player.getInstance() == null) return;
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24;
            Pos point = centre.add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
            player.getInstance().sendGroupedPacket(new net.minestom.server.network.packet.server.play.ParticlePacket(
                    Particle.FLAME, point, Vec.ZERO, 0, 1));
        }
    }

    static SkyBlockMob mobInSight(SkyBlockPlayer player, double range) {
        Vec look = player.getPosition().direction();
        Pos eyes = player.getPosition().add(0, player.getEyeHeight(), 0);
        return player.getInstance().getNearbyEntities(eyes, range).stream()
                .filter(entity -> entity instanceof SkyBlockMob mob && !mob.isDead())
                .map(entity -> (SkyBlockMob) entity)
                .filter(mob -> {
                    Vec toMob = mob.getPosition().add(0, mob.getEyeHeight() / 2, 0).sub(eyes).asVec();
                    return toMob.length() > 0 && toMob.normalize().dot(look) > 0.97;
                })
                .min(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(eyes)))
                .orElse(null);
    }
}
