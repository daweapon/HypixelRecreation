package dev.daweapon.type.skyblockgeneric.gifts;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A snowball flying in a straight line that hands the first SkyBlock mob it touches to its hit
 * action. Collision handling is adapted from ArrowEntityImpl.
 */
public class SnowballProjectile extends LivingEntity {
    public static final Tag<Boolean> SNOWBALL_DAMAGE_TAG = Tag.Boolean("workshop_snowball_damage");
    private static final int LIFETIME_TICKS = 40;
    private static final float THROWN_DAMAGE = 1f;

    private final SkyBlockPlayer shooter;
    private final Consumer<SkyBlockMob> onHit;

    private SnowballProjectile(SkyBlockPlayer shooter, Consumer<SkyBlockMob> onHit) {
        super(EntityType.SNOWBALL);
        this.collidesWithEntities = false;
        this.hasPhysics = false;
        this.shooter = shooter;
        this.onHit = onHit;
    }

    public static void shoot(SkyBlockPlayer shooter, Pos from, Vec direction, double speed, Consumer<SkyBlockMob> onHit) {
        Instance instance = shooter.getInstance();
        if (instance == null || direction.isZero()) return;
        SnowballProjectile snowball = new SnowballProjectile(shooter, onHit);
        snowball.setVelocity(direction.normalize().mul(speed * 20));
        snowball.setInstance(instance, from);
    }

    public static void throwAlongLook(SkyBlockPlayer shooter, double speed) {
        Pos eye = shooter.getPosition().add(0, shooter.getEyeHeight(), 0);
        shoot(shooter, eye, shooter.getPosition().direction(), speed, mob -> hitWithThrownSnowball(shooter, mob));
    }

    public static void hitWithThrownSnowball(SkyBlockPlayer shooter, SkyBlockMob mob) {
        Damage damage = Damage.fromPlayer(shooter, (float) (THROWN_DAMAGE * SnowmanPet.snowballMultiplier(shooter)));
        damage.tagHandler().setTag(SNOWBALL_DAMAGE_TAG, true);
        mob.damage(damage);
    }

    @Override
    public void spawn() {
        super.spawn();
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (!isRemoved()) remove();
        }, TaskSchedule.tick(LIFETIME_TICKS), TaskSchedule.stop());
    }

    @Override
    public void tick(long time) {
        final Pos posBefore = getPosition();
        super.tick(time);
        if (isRemoved()) return;
        final Pos posNow = getPosition();

        Vec diff = posNow.sub(posBefore).asVec();
        PhysicsResult result = CollisionUtils.handlePhysics(
                instance, this.getChunk(),
                this.getBoundingBox(),
                posBefore, diff,
                null, true
        );

        Collection<EntityCollisionResult> collided = CollisionUtils.checkEntityCollisions(
                instance, this.getBoundingBox(), posBefore, diff, 3, e -> e != this, result);
        for (EntityCollisionResult collisionResult : collided) {
            if (collisionResult == null || collisionResult.entity() == shooter) continue;
            if (!(collisionResult.entity() instanceof SkyBlockMob mob) || mob.isDead()) continue;
            onHit.accept(mob);
            remove();
            return;
        }

        if (result.hasCollision()) remove();
    }
}
