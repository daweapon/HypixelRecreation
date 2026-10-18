package dev.daweapon.type.skyblockgeneric.fishing.lava;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MobProjectile;
import dev.daweapon.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lord Jawbus (wiki: Lord_Jawbus) shoots fireballs (2,500) and a laser (30,000). At 60M, 40M and 20M health it
 * rises inside a fire sphere, invulnerable, and calls 2 Jawbus Followers (random island-wide lava Sea Creatures,
 * never Thunder, Jawbus or Plhlegblast); killing them drops the sphere. While they live it lobs TNT (5,000 damage, 4 block
 * blast, both estimates) that detonates when it lands. Attack intervals are estimates.
 */
public class LordJawbusMob extends LavaSeaCreatureMob {
    static final double FIREBALL_DAMAGE = 2_500;
    static final double LASER_DAMAGE = 30_000;
    static final double[] SHIELD_THRESHOLDS = {60_000_000, 40_000_000, 20_000_000};
    static final List<LavaSeaCreature> FOLLOWER_POOL = List.of(LavaSeaCreature.MAGMA_SLUG, LavaSeaCreature.MOOGMA,
            LavaSeaCreature.LAVA_LEECH, LavaSeaCreature.PYROCLASTIC_WORM, LavaSeaCreature.LAVA_FLAME,
            LavaSeaCreature.FIRE_EEL, LavaSeaCreature.TAURUS);
    private static final int FIREBALL_INTERVAL_TICKS = 60;
    private static final int LASER_INTERVAL_TICKS = 200;
    private static final double RANGE = 30;
    private static final int TNT_INTERVAL_TICKS = 50;
    private static final int TNT_MAX_TICKS = 100;
    private static final double TNT_DAMAGE = 5_000;
    private static final double TNT_RADIUS = 4;
    private static final double TNT_HORIZONTAL_SPEED = 12;
    private static final double TNT_UPWARD_SPEED = 14;

    private final List<LavaSeaCreatureMob> followers = new ArrayList<>();
    private int phasesTriggered;

    public LordJawbusMob(@Nullable SkyBlockPlayer angler) {
        super(LavaSeaCreature.LORD_JAWBUS, angler, false);
    }

    private boolean shielded() {
        followers.removeIf(follower -> follower.isDead() || follower.isRemoved());
        return !followers.isEmpty();
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (shielded()) return false;
        boolean hurt = super.damage(damage);
        if (phasesTriggered < SHIELD_THRESHOLDS.length && getHealth() <= SHIELD_THRESHOLDS[phasesTriggered] && !isDead()) {
            phasesTriggered++;
            summonFollowers();
        }
        return hurt;
    }

    private void summonFollowers() {
        Instance instance = getInstance();
        if (instance == null) return;
        for (int i = 0; i < 2; i++) {
            LavaSeaCreature pick = FOLLOWER_POOL.get(ThreadLocalRandom.current().nextInt(FOLLOWER_POOL.size()));
            LavaSeaCreatureMob follower = LavaSeaCreatures.create(pick, getAngler(), false);
            follower.setInstance(instance, getPosition().add(i == 0 ? 2 : -2, 0, 0));
            followers.add(follower);
        }
        teleport(getPosition().add(0, 3, 0));
    }

    @Override
    protected void onTick(int ticks) {
        Instance instance = getInstance();
        if (instance == null) return;
        if (shielded()) {
            if (ticks % 5 == 0) MythologicalCombat.particles(instance, Particle.FLAME, getPosition().add(0, 1.5, 0), 1.6f, 25);
            if (ticks % TNT_INTERVAL_TICKS == 0) LavaAttacks.target(this, RANGE).ifPresent(this::lobTnt);
            return;
        }
        if (ticks % FIREBALL_INTERVAL_TICKS == 0) {
            LavaAttacks.target(this, RANGE).ifPresent(target -> MobProjectile.item(this, target,
                    ItemStack.of(Material.FIRE_CHARGE), 0.9, hit -> hit(hit, FIREBALL_DAMAGE)));
        }
        if (ticks % LASER_INTERVAL_TICKS == 0) {
            LavaAttacks.target(this, RANGE).ifPresent(target -> {
                Pos eyes = getPosition().add(0, 2.2, 0);
                LavaAttacks.beam(instance, eyes, target.getPosition().add(0, 1, 0), Particle.FLAME);
                hit(target, LASER_DAMAGE);
            });
        }
    }

    private void lobTnt(SkyBlockPlayer target) {
        Instance instance = getInstance();
        if (instance == null) return;
        Entity tnt = new Entity(EntityType.TNT);
        Pos origin = getPosition().add(0, 2, 0);
        Vec horizontal = target.getPosition().sub(origin).asVec().withY(0);
        Vec launch = horizontal.length() < 0.01 ? Vec.ZERO : horizontal.normalize().mul(TNT_HORIZONTAL_SPEED);
        tnt.setInstance(instance, origin).thenRun(() -> {
            tnt.setVelocity(launch.withY(TNT_UPWARD_SPEED));
            AtomicInteger age = new AtomicInteger();
            Task[] task = new Task[1];
            task[0] = MinecraftServer.getSchedulerManager().buildTask(() -> {
                boolean landed = age.incrementAndGet() > 10 && tnt.isOnGround();
                if (tnt.isRemoved() || (!landed && age.get() < TNT_MAX_TICKS)) {
                    if (tnt.isRemoved()) task[0].cancel();
                    return;
                }
                task[0].cancel();
                explodeTnt(instance, tnt);
            }).repeat(TaskSchedule.tick(1)).schedule();
        });
    }

    private void explodeTnt(Instance instance, Entity tnt) {
        Pos at = tnt.getPosition();
        tnt.remove();
        MythologicalCombat.particles(instance, Particle.EXPLOSION, at.add(0, 0.5, 0), 1.5f, 6);
        LavaAttacks.ring(instance, at, TNT_RADIUS, Particle.FLAME);
        for (Player player : instance.getPlayers()) {
            if (player instanceof SkyBlockPlayer skyBlockPlayer && player.getPosition().distance(at) <= TNT_RADIUS) {
                hit(skyBlockPlayer, TNT_DAMAGE);
            }
        }
    }

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        followers.forEach(LavaSeaCreatureMob::remove);
    }
}
