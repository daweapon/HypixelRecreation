package dev.daweapon.type.skyblockgeneric.item.armor;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.PlayerStatistics;
import dev.daweapon.type.skyblockgeneric.utility.AttackService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * A Witherborn wither: it hovers at its owner's shoulder until an enemy comes near, then flies to it and explodes,
 * hitting every enemy around for the owner's normal hit damage. Garden pests are left alone (0.19.11).
 */
public final class WitherbornMinion extends LivingEntity {
    public static final double SEEK_RANGE = 15;
    public static final double EXPLOSION_RADIUS = 4;
    public static final double FLIGHT_SPEED = 0.6;
    public static final double REACH = 1.5;
    private static final double SCALE = 0.4;
    private static final Vec SHOULDER = new Vec(-0.8, 2.2, 0);
    private static final Sound BLAST = Sound.sound(Key.key("entity.generic.explode"), Sound.Source.PLAYER, 1f, 1f);

    private final SkyBlockPlayer owner;
    private @Nullable SkyBlockMob target;
    private boolean exploded;

    public WitherbornMinion(SkyBlockPlayer owner) {
        super(EntityType.WITHER);
        this.owner = owner;
        setNoGravity(true);
        setInvulnerable(true);
        setSilent(true);
        getAttribute(Attribute.SCALE).setBaseValue(SCALE);
    }

    public SkyBlockPlayer owner() {
        return owner;
    }

    public boolean exploded() {
        return exploded;
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (exploded || isRemoved() || getInstance() == null) return;
        if (owner.getInstance() != getInstance()) {
            remove();
            return;
        }
        if (target == null || target.isDead() || target.isRemoved()) target = nearestEnemy();
        if (target == null) {
            hover();
            return;
        }
        Pos aim = target.getPosition().add(0, target.getEyeHeight() / 2, 0);
        Vec towards = aim.sub(getPosition()).asVec();
        if (towards.length() <= REACH) {
            explode();
            return;
        }
        Vec step = towards.normalize().mul(Math.min(FLIGHT_SPEED, towards.length()));
        teleport(getPosition().add(step).withDirection(towards));
    }

    private void hover() {
        Pos at = owner.getPosition();
        Vec offset = SHOULDER.rotateAroundY(Math.toRadians(-at.yaw()));
        teleport(at.add(offset).withView(at.yaw(), 0));
    }

    private @Nullable SkyBlockMob nearestEnemy() {
        return enemiesNear(owner.getPosition(), SEEK_RANGE).stream()
                .min(Comparator.comparingDouble(mob -> mob.getPosition().distanceSquared(owner.getPosition())))
                .orElse(null);
    }

    private List<SkyBlockMob> enemiesNear(Pos centre, double range) {
        Instance instance = getInstance();
        if (instance == null) return List.of();
        return instance.getNearbyEntities(centre, range).stream()
                .filter(WitherbornMinion::isEnemy)
                .map(SkyBlockMob.class::cast)
                .toList();
    }

    static boolean isEnemy(Entity entity) {
        return entity instanceof SkyBlockMob mob && !mob.isDead() && !mob.isRemoved()
                && !mob.getMobTypes().contains(MobType.PEST);
    }

    private void explode() {
        exploded = true;
        Pos centre = getPosition();
        Instance instance = getInstance();
        int hit = 0;
        double total = 0;
        for (SkyBlockMob mob : enemiesNear(centre, EXPLOSION_RADIUS)) {
            Map.Entry<Double, Boolean> damage = PlayerStatistics.runPrimaryDamageFormula(
                    owner.getStatistics().allStatistics(), mob.getStatistics());
            if (!AttackService.applyHit(owner, mob, damage.getKey().floatValue(), damage.getValue())) continue;
            hit++;
            total += damage.getKey();
        }
        if (instance != null) {
            instance.sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION_EMITTER, centre.x(), centre.y(), centre.z(), 0f, 0f, 0f, 0f, 1));
            instance.playSound(BLAST, centre.x(), centre.y(), centre.z());
        }
        if (hit > 0) owner.sendMessage(message(hit, total));
        remove();
    }

    public static String message(int hit, double damage) {
        return "<7>Your Witherborn hit <c>" + hit + " <7>" + (hit == 1 ? "enemy" : "enemies")
                + " for <c>" + StringUtility.commaify(damage) + " <7>damage.";
    }
}
