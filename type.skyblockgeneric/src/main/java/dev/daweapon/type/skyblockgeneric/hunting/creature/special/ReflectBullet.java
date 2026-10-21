package dev.daweapon.type.skyblockgeneric.hunting.creature.special;

import dev.daweapon.type.skyblockgeneric.hunting.creature.HuntingCreatureMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectBullet {
    private static final Map<Integer, ReflectBullet> ACTIVE = new ConcurrentHashMap<>();
    private static final double SPEED = 0.45;
    private static final double HIT_RADIUS = 0.9;
    private static final double ABSORB_RADIUS = 1.2;
    private static final double DEFLECT_REACH = 6;
    private static final int MAX_TICKS = 140;
    private static final int LEVITATION_TICKS = 40;
    private static final String COW_HAT = "COW_HEAD";

    private final HuntingCreatureMob owner;
    private final BulletExchange exchange;
    private final Entity visual;
    private final Runnable onDone;
    private Vec step;
    private boolean returning;
    private int ticks;
    private Task task;
    private boolean finished;

    private ReflectBullet(HuntingCreatureMob owner, BulletExchange exchange, Entity visual, Vec step, Runnable onDone) {
        this.owner = owner;
        this.exchange = exchange;
        this.visual = visual;
        this.step = step;
        this.onDone = onDone;
    }

    static void launch(HuntingCreatureMob owner, SkyBlockPlayer target, BulletExchange exchange, Runnable onDone) {
        Instance instance = owner.getInstance();
        if (instance == null) {
            onDone.run();
            return;
        }
        Pos start = owner.getPosition().add(0, 1.2, 0);
        Vec direction = Vec.fromPoint(target.getPosition().add(0, 1, 0).sub(start)).normalize();
        Entity visual = new Entity(EntityType.SHULKER_BULLET);
        visual.setNoGravity(true);
        ReflectBullet bullet = new ReflectBullet(owner, exchange, visual, direction.mul(SPEED), onDone);
        visual.setInstance(instance, start).thenRun(() -> bullet.start());
    }

    public static boolean deflect(SkyBlockPlayer player, Entity entity) {
        ReflectBullet bullet = ACTIVE.get(entity.getEntityId());
        if (bullet == null || bullet.returning || bullet.finished) return false;
        if (bullet.visual.getPosition().distance(player.getPosition().add(0, 1, 0)) > DEFLECT_REACH) return false;
        bullet.hitBack(player);
        return true;
    }

    private void start() {
        ACTIVE.put(visual.getEntityId(), this);
        task = MinecraftServer.getSchedulerManager().buildTask(this::tick).repeat(TaskSchedule.tick(1)).schedule();
    }

    private void hitBack(SkyBlockPlayer player) {
        returning = true;
        Vec toOwner = Vec.fromPoint(owner.getPosition().add(0, 1, 0).sub(visual.getPosition())).normalize();
        step = toOwner.mul(SPEED);
        SpecialEffects.burst(visual, Particle.CRIT, visual.getPosition(), 10, 0.2f);
        SpecialMessages.send(player, owner.spec(), "hit_back");
        SpecialMessages.send(player, owner.spec(), "bullet_returned");
        if (!exchange.returned()) return;
        SpecialMessages.send(player, owner.spec(), "defeated");
        finish();
        owner.capture(player);
    }

    private void tick() {
        Instance instance = visual.getInstance();
        if (finished || instance == null || visual.isRemoved() || owner.isRemoved() || owner.isDead() || ticks++ >= MAX_TICKS) {
            finish();
            return;
        }
        Pos next = visual.getPosition().add(step);
        visual.teleport(next);
        if (returning) {
            if (next.distance(owner.getPosition().add(0, 1, 0)) <= ABSORB_RADIUS) finish();
            return;
        }
        for (var player : instance.getPlayers()) {
            if (!(player instanceof SkyBlockPlayer victim) || victim.isDead()) continue;
            if (victim.getPosition().add(0, 0.9, 0).distance(next) > HIT_RADIUS) continue;
            strike(victim);
            finish();
            return;
        }
    }

    private void strike(SkyBlockPlayer victim) {
        MobAttackService.strike(owner, victim, owner.spec().damage());
        if (!HeldItems.wearsHelmet(victim, COW_HAT)) victim.addEffect(new Potion(PotionEffect.LEVITATION, 0, LEVITATION_TICKS));
    }

    private void finish() {
        if (finished) return;
        finished = true;
        ACTIVE.remove(visual.getEntityId());
        if (task != null) task.cancel();
        if (!visual.isRemoved()) visual.remove();
        onDone.run();
    }
}
