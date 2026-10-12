package net.swofty.type.skyblockgeneric.entity.mob;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.combat.PetPerkMath;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MobStatusEffects {
    private static final Key STUN_MODIFIER = Key.key("skyblock", "mob_stun");
    private static final Key SLOW_MODIFIER = Key.key("skyblock", "mob_slow");
    private static final Map<UUID, Long> SLOWED_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> STUNNED_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, HealingReduction> HEALING_REDUCED = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> WITHERED_UNTIL = new ConcurrentHashMap<>();

    private record HealingReduction(double percent, long until) {
    }

    private MobStatusEffects() {
    }

    public static void stun(SkyBlockMob mob, long millis) {
        long now = System.currentTimeMillis();
        markStunned(mob.getUuid(), millis, now);
        AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        speed.removeModifier(STUN_MODIFIER);
        speed.addModifier(new AttributeModifier(STUN_MODIFIER, -1, AttributeOperation.ADD_MULTIPLIED_TOTAL));
        MinecraftServer.getSchedulerManager().scheduleTask(() -> release(mob), TaskSchedule.millis(millis),
                TaskSchedule.stop());
    }

    public static void slow(SkyBlockMob mob, double fraction, long millis) {
        long now = System.currentTimeMillis();
        SLOWED_UNTIL.values().removeIf(until -> until <= now);
        SLOWED_UNTIL.put(mob.getUuid(), now + millis);
        AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
        speed.removeModifier(SLOW_MODIFIER);
        speed.addModifier(new AttributeModifier(SLOW_MODIFIER, -fraction, AttributeOperation.ADD_MULTIPLIED_TOTAL));
        MinecraftServer.getSchedulerManager().scheduleTask(() -> releaseSlow(mob), TaskSchedule.millis(millis),
                TaskSchedule.stop());
    }

    private static void releaseSlow(SkyBlockMob mob) {
        Long until = SLOWED_UNTIL.get(mob.getUuid());
        if (until != null && until > System.currentTimeMillis()) return;
        SLOWED_UNTIL.remove(mob.getUuid());
        if (!mob.isRemoved()) mob.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(SLOW_MODIFIER);
    }

    public static boolean isStunned(SkyBlockMob mob) {
        return isStunned(mob.getUuid(), System.currentTimeMillis());
    }

    public static void markStunned(UUID id, long millis, long now) {
        STUNNED_UNTIL.values().removeIf(until -> until <= now);
        STUNNED_UNTIL.put(id, now + millis);
    }

    public static boolean isStunned(UUID id, long now) {
        Long until = STUNNED_UNTIL.get(id);
        if (until == null) return false;
        if (now < until) return true;
        STUNNED_UNTIL.remove(id);
        return false;
    }

    public static boolean tryWither(UUID id, long millis, long now) {
        WITHERED_UNTIL.values().removeIf(until -> until <= now);
        Long current = WITHERED_UNTIL.get(id);
        if (current != null && current > now) return false;
        WITHERED_UNTIL.put(id, now + millis);
        return true;
    }

    public static void wither(SkyBlockPlayer attacker, SkyBlockMob mob, double damagePerSecond) {
        if (damagePerSecond <= 0) return;
        long duration = PetPerkMath.WITHER_SECONDS * 1000L;
        if (!tryWither(mob.getUuid(), duration, System.currentTimeMillis())) return;
        for (int second = 1; second <= PetPerkMath.WITHER_SECONDS; second++) {
            MinecraftServer.getSchedulerManager().scheduleTask(() -> {
                if (!mob.isDead() && !mob.isRemoved() && attacker.isOnline()) {
                    AttackService.applyHit(attacker, mob, (float) damagePerSecond, false);
                }
            }, TaskSchedule.seconds(second), TaskSchedule.stop());
        }
    }

    public static void reduceHealing(SkyBlockMob mob, double percent, long millis) {
        reduceHealing(mob.getUuid(), percent, millis, System.currentTimeMillis());
    }

    public static void reduceHealing(UUID id, double percent, long millis, long now) {
        HEALING_REDUCED.values().removeIf(reduction -> reduction.until() <= now);
        HealingReduction current = HEALING_REDUCED.get(id);
        if (current != null && current.until() > now && current.percent() > percent) return;
        HEALING_REDUCED.put(id, new HealingReduction(percent, now + millis));
    }

    public static float healing(SkyBlockMob mob, double amount) {
        return (float) healing(mob.getUuid(), amount, System.currentTimeMillis());
    }

    public static double healing(UUID id, double amount, long now) {
        HealingReduction reduction = HEALING_REDUCED.get(id);
        if (reduction == null) return amount;
        if (now >= reduction.until()) {
            HEALING_REDUCED.remove(id);
            return amount;
        }
        return PetPerkMath.healingAfterReduction(amount, reduction.percent());
    }

    private static void release(SkyBlockMob mob) {
        if (isStunned(mob.getUuid(), System.currentTimeMillis())) return;
        if (!mob.isRemoved()) mob.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(STUN_MODIFIER);
    }
}
