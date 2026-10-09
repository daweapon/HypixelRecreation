package net.swofty.type.skyblockgeneric.item.handlers.bow;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.custom.SalvationBeamEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AbilityComponent;
import net.swofty.type.skyblockgeneric.item.set.sets.terror.HydraStrike;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.utility.AttackService;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Terminator's Salvation (wiki): after three arrow hits, left click shoots a 32-block beam that passes through up
 * to five enemies and always crits.
 */
public final class Salvation {
    public static final String ABILITY_ID = "SALVATION";
    public static final double RANGE = 32;
    public static final int MAX_TARGETS = 5;
    private static final double STEP = 0.25;
    private static final Map<UUID, SalvationCharge> CHARGES = new ConcurrentHashMap<>();

    private Salvation() {
    }

    public static boolean hasSalvation(@Nullable SkyBlockItem item) {
        return item != null && item.hasComponent(AbilityComponent.class)
                && item.getComponent(AbilityComponent.class).getAbilityIds().contains(ABILITY_ID);
    }

    public static void onArrowHit(SkyBlockPlayer player, @Nullable SkyBlockItem bow) {
        if (hasSalvation(bow)) charge(player).hit();
    }

    public static boolean ready(SkyBlockPlayer player) {
        SalvationCharge charge = CHARGES.get(player.getUuid());
        return charge != null && charge.ready();
    }

    private static SalvationCharge charge(SkyBlockPlayer player) {
        return CHARGES.computeIfAbsent(player.getUuid(), _ -> new SalvationCharge());
    }

    public static Text display(SkyBlockPlayer player) {
        SalvationCharge charge = CHARGES.get(player.getUuid());
        if (charge == null || charge.hits() == 0) return Text.empty();
        if (!hasSalvation(new SkyBlockItem(player.getItemInMainHand()))) return Text.empty();
        return switch (charge.hits()) {
            case 1 -> Text.of("<c><l>T1");
            case 2 -> Text.of("<6><l>T2");
            default -> Text.of("<a><l>T3!");
        };
    }

    public static boolean cast(SkyBlockPlayer player) {
        SalvationCharge charge = CHARGES.get(player.getUuid());
        Instance instance = player.getInstance();
        if (charge == null || instance == null || !charge.consume()) return false;

        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = player.getPosition().direction().normalize();
        double reach = reach(instance, eye, direction);

        ItemStatistics stats = ArrowDamage.alwaysCrit(player.getStatistics().allStatistics());
        double multiplier = HydraStrike.damageMultiplier(player);
        List<Entity> others = new ArrayList<>();
        int struck = 0;
        for (Entity entity : targets(instance, player, eye, direction, reach)) {
            if (struck >= MAX_TARGETS) break;
            struck++;
            if (entity instanceof SkyBlockMob mob) {
                double damage = PlayerStatistics.runPrimaryDamageFormula(stats, mob.getStatistics()).getKey() * multiplier;
                AttackService.applyHit(player, mob, (float) damage, true);
            } else {
                others.add(entity);
            }
        }
        if (!others.isEmpty()) {
            double damage = PlayerStatistics.runPrimaryDamageFormula(stats, ItemStatistics.empty()).getKey() * multiplier;
            HypixelEventHandler.callCustomEvent(new SalvationBeamEvent(player, List.copyOf(others), damage));
        }

        for (double distance = 0; distance <= reach; distance += 0.5) {
            Point at = eye.add(direction.mul(distance));
            instance.sendGroupedPacket(new ParticlePacket(Particle.FIREWORK, at, Vec.ZERO, 0, 1));
        }
        instance.playSound(Sound.sound(Key.key("entity.guardian.attack"), Sound.Source.PLAYER, 1f, 1.6f), eye);
        return true;
    }

    private static double reach(Instance instance, Pos eye, Vec direction) {
        for (double distance = 0; distance <= RANGE; distance += STEP) {
            Point at = eye.add(direction.mul(distance));
            if (!instance.isChunkLoaded(at) || instance.getBlock(at).isSolid()) return distance;
        }
        return RANGE;
    }

    private static List<Entity> targets(Instance instance, SkyBlockPlayer player, Pos eye, Vec direction, double reach) {
        Point middle = eye.add(direction.mul(reach / 2));
        record Struck(Entity entity, double distance) {
        }
        List<Struck> struck = new ArrayList<>();
        for (Entity entity : instance.getNearbyEntities(middle, reach / 2 + ArrowEntityImpl.LARGEST_TARGET_REACH)) {
            if (entity == player || entity instanceof ArrowEntityImpl || !entity.hasEntityCollision()) continue;
            EntityType type = entity.getEntityType();
            if (type == EntityType.PLAYER || type == EntityType.ARMOR_STAND || type == EntityType.ITEM) continue;
            Pos at = entity.getPosition();
            double distance = rayEntry(eye, direction, reach,
                    at.add(entity.getBoundingBox().relativeStart()), at.add(entity.getBoundingBox().relativeEnd()));
            if (distance >= 0) struck.add(new Struck(entity, distance));
        }
        return struck.stream().sorted(Comparator.comparingDouble(Struck::distance)).map(Struck::entity).toList();
    }

    /** Distance along the ray at which it enters the box, or -1 when it misses within {@code reach}. */
    public static double rayEntry(Point origin, Vec direction, double reach, Point min, Point max) {
        double near = 0;
        double far = reach;
        double[] o = {origin.x(), origin.y(), origin.z()};
        double[] d = {direction.x(), direction.y(), direction.z()};
        double[] lo = {min.x(), min.y(), min.z()};
        double[] hi = {max.x(), max.y(), max.z()};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(d[axis]) < 1e-9) {
                if (o[axis] < lo[axis] || o[axis] > hi[axis]) return -1;
                continue;
            }
            double t1 = (lo[axis] - o[axis]) / d[axis];
            double t2 = (hi[axis] - o[axis]) / d[axis];
            near = Math.max(near, Math.min(t1, t2));
            far = Math.min(far, Math.max(t1, t2));
            if (near > far) return -1;
        }
        return near;
    }
}
