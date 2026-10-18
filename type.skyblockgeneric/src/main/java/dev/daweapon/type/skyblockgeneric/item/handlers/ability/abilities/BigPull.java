package dev.daweapon.type.skyblockgeneric.item.handlers.ability.abilities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BigPull {
    private static final double RANGE = 15;
    private static final int MAX_TARGETS = 5;
    private static final double AIM_DOT = 0.9;
    private static final double DAMAGE_MULTIPLIER = 2;
    private static final long LANDING_GRACE_MILLIS = 600;
    private static final long MAX_PULLED_MILLIS = 8_000;
    private static final Map<UUID, Long> PULLED = new ConcurrentHashMap<>();

    private BigPull() {
    }

    public static boolean cast(SkyBlockPlayer player) {
        List<SkyBlockMob> targets = targets(player);
        if (targets.isEmpty()) return false;
        long now = System.currentTimeMillis();
        for (SkyBlockMob mob : targets) {
            Vec pull = player.getPosition().sub(mob.getPosition()).asVec().normalize().mul(25).withY(8);
            mob.setNoGravity(false);
            mob.setVelocity(pull);
            PULLED.put(mob.getUuid(), now);
        }
        return true;
    }

    public static double damageMultiplier(SkyBlockMob mob) {
        Long pulledAt = PULLED.get(mob.getUuid());
        if (pulledAt == null) return 1;
        long elapsed = System.currentTimeMillis() - pulledAt;
        if (elapsed > MAX_PULLED_MILLIS || (elapsed > LANDING_GRACE_MILLIS && mob.isOnGround())) {
            PULLED.remove(mob.getUuid());
            return 1;
        }
        return DAMAGE_MULTIPLIER;
    }

    static boolean grabbable(SkyBlockMob mob) {
        if (mob.isDead() || mob.isRemoved() || mob instanceof SlayerBossMob) return false;
        if (mob.getEntityType() == EntityType.BLAZE) return true;
        String name = mob.getDisplayName();
        return name != null && (name.contains("Torrid") || name.contains("Silkbreeze"));
    }

    private static List<SkyBlockMob> targets(SkyBlockPlayer player) {
        if (player.getInstance() == null) return List.of();
        Vec look = player.getPosition().direction();
        Pos eyes = player.getPosition().add(0, player.getEyeHeight(), 0);
        return player.getInstance().getNearbyEntities(eyes, RANGE).stream()
                .filter(entity -> entity instanceof SkyBlockMob mob && grabbable(mob))
                .map(entity -> (SkyBlockMob) entity)
                .filter(mob -> {
                    Vec toMob = mob.getPosition().add(0, mob.getEyeHeight() / 2, 0).sub(eyes).asVec();
                    return toMob.length() > 0 && toMob.normalize().dot(look) > AIM_DOT;
                })
                .sorted(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(eyes)))
                .limit(MAX_TARGETS)
                .toList();
    }
}
