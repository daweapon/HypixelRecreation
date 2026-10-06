package net.swofty.type.jerrysworkshop.defense;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.EntityCollisionResult;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Collection;

// Snowball projectile used to damage WorkshopDefenseMob during "Defend Jerry's Workshop" (Roadmap
// #20) - real Hypixel restricts these mobs to snowball-only damage. Physics/collision handling is
// adapted from ArrowEntityImpl. Each hit deals 1 damage, as on Hypixel.
public class SnowballEntityImpl extends LivingEntity {

    private static final float DAMAGE_PER_HIT = 1f;

    private final SkyBlockPlayer shooter;

    public SnowballEntityImpl(SkyBlockPlayer shooter) {
        super(EntityType.SNOWBALL);
        this.collidesWithEntities = false;
        this.hasPhysics = false;
        this.shooter = shooter;
    }

    @Override
    public void spawn() {
        super.spawn();
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (!isRemoved()) remove();
        }, TaskSchedule.tick(40), TaskSchedule.stop());
    }

    @Override
    public void tick(long time) {
        final Pos posBefore = getPosition();
        super.tick(time);
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
            Entity entity = collisionResult.entity();
            if (!(entity instanceof WorkshopDefenseMob mob)) continue;

            Damage damage = Damage.fromProjectile(shooter, this, DAMAGE_PER_HIT);
            damage.tagHandler().setTag(WorkshopDefenseMob.SNOWBALL_DAMAGE_TAG, true);
            mob.damage(damage);

            remove();
            return;
        }

        if (result.hasCollision()) remove();
    }
}
