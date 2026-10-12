package net.swofty.type.skyblockgeneric.item.handlers.bow;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.entity.mob.MobStatusEffects;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ArrowComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/** The passive effects of the special arrows: Armorshred, Bouncy, Explosive, Glue, Icy and Nansorb. */
public final class ArrowEffects {
    public static final double DEFENSE_IGNORED = 0.05;
    public static final double ICY_SLOWNESS = 0.15;
    public static final long ICY_SLOW_MILLIS = 3000;
    public static final long GLUE_ROOT_MILLIS = 1500;
    public static final long GLUE_COOLDOWN_MILLIS = 6000;
    public static final double BOUNCE_CHANCE = 0.5;
    public static final int BOUNCE_TARGETS = 4;
    public static final double BOUNCE_DAMAGE_LOSS = 0.15;
    public static final double BOUNCE_RANGE = 8;
    public static final int BOUNCE_DELAY_TICKS = 3;
    public static final double EXPLOSION_RADIUS = 2.5;
    public static final double EXPLOSION_SELF_HEALTH_LOSS = 0.1;
    public static final double SKEWER_BONUS = 0.075;
    public static final int SKEWER_VICTIMS = 5;

    private static final Map<UUID, Long> GLUED_UNTIL = new ConcurrentHashMap<>();

    private ArrowEffects() {
    }

    public static @Nullable ArrowEffect of(SkyBlockItem arrowItem) {
        if (!arrowItem.hasComponent(ArrowComponent.class)) return null;
        return arrowItem.getComponent(ArrowComponent.class).getEffect();
    }

    public static ItemStatistics enemyStatistics(@Nullable ArrowEffect effect, ItemStatistics enemy) {
        if (effect != ArrowEffect.ARMORSHRED) return enemy;
        return enemy.withExactValue(ItemStatistic.DEFENSE, enemy.getOverall(ItemStatistic.DEFENSE) * (1 - DEFENSE_IGNORED));
    }

    public static double pierceFactor(@Nullable ArrowEffect effect, int targetsAlreadyHit, double defaultFactor) {
        if (effect != ArrowEffect.NANSORB) return defaultFactor;
        if (targetsAlreadyHit >= SKEWER_VICTIMS) return 0;
        return Math.pow(1 + SKEWER_BONUS, targetsAlreadyHit);
    }

    public static double bounceFactor(int bounce) {
        return Math.pow(1 - BOUNCE_DAMAGE_LOSS, bounce);
    }

    public static boolean tryGlue(UUID victim, long now) {
        GLUED_UNTIL.values().removeIf(until -> until <= now);
        if (GLUED_UNTIL.containsKey(victim)) return false;
        GLUED_UNTIL.put(victim, now + GLUE_COOLDOWN_MILLIS);
        return true;
    }

    public static double explosionShare(double damage, int victims) {
        return victims <= 0 ? 0 : damage / victims;
    }

    public static boolean explosionHurtsShooter(double distance) {
        return distance <= EXPLOSION_RADIUS;
    }

    public static void onHit(SkyBlockPlayer shooter, ArrowEntityImpl arrow, SkyBlockMob target, float damage, boolean critical) {
        ArrowEffect effect = of(arrow.getArrowItem());
        if (effect == null) return;
        switch (effect) {
            case ICY -> MobStatusEffects.slow(target, ICY_SLOWNESS, ICY_SLOW_MILLIS);
            case GLUE -> {
                if (tryGlue(target.getUuid(), System.currentTimeMillis())) MobStatusEffects.stun(target, GLUE_ROOT_MILLIS);
            }
            case BOUNCY -> {
                if (ThreadLocalRandom.current().nextDouble() < BOUNCE_CHANCE) bounce(shooter, target, damage, critical);
            }
            default -> {
            }
        }
    }

    private static void bounce(SkyBlockPlayer shooter, SkyBlockMob first, float damage, boolean critical) {
        Instance instance = first.getInstance();
        if (instance == null) return;
        Set<UUID> visited = new HashSet<>();
        visited.add(first.getUuid());
        SkyBlockMob current = first;
        for (int bounce = 1; bounce <= BOUNCE_TARGETS; bounce++) {
            SkyBlockMob from = current;
            SkyBlockMob next = instance.getNearbyEntities(from.getPosition(), BOUNCE_RANGE).stream()
                    .filter(entity -> entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved()
                            && !mob.isImmuneToArrows() && !visited.contains(mob.getUuid()))
                    .map(entity -> (SkyBlockMob) entity)
                    .min(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(from.getPosition())))
                    .orElse(null);
            if (next == null) return;
            visited.add(next.getUuid());
            current = next;
            float bounced = (float) (damage * bounceFactor(bounce));
            SkyBlockMob target = next;
            MinecraftServer.getSchedulerManager().scheduleTask(
                    () -> AttackService.applyHit(shooter, target, bounced, critical),
                    TaskSchedule.tick(BOUNCE_DELAY_TICKS * bounce), TaskSchedule.stop());
        }
    }

    public static void explode(ArrowEntityImpl arrow, Pos center) {
        if (!(arrow.getShooter() instanceof SkyBlockPlayer shooter) || !arrow.claimExplosion()) return;
        Instance instance = arrow.getInstance();
        if (instance == null) return;
        List<SkyBlockMob> victims = new ArrayList<>();
        for (Entity entity : instance.getNearbyEntities(center, EXPLOSION_RADIUS)) {
            if (entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved() && !mob.isImmuneToArrows()
                    && mob.getPosition().distance(center) <= EXPLOSION_RADIUS) victims.add(mob);
        }
        for (SkyBlockMob mob : victims) {
            var hit = ArrowDamage.roll(shooter, arrow, mob.getStatistics());
            AttackService.applyHit(shooter, mob, (float) explosionShare(hit.getKey(), victims.size()), hit.getValue());
        }
        if (explosionHurtsShooter(shooter.getPosition().distance(center))) {
            float loss = (float) (shooter.getMaxHealth() * EXPLOSION_SELF_HEALTH_LOSS);
            shooter.setHealth(Math.max(1, shooter.getHealth() - loss));
        }
    }
}
