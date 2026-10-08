package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Thunder cycles three attacks (wiki: Thunder, Behaviour section): a jump slam on players beneath it, an eye beam
 * for 2,000 damage and a lightning circle where a Sea Lantern lands a second later for 15,000. Each attack charges
 * the victim's Empty Thunder Bottle (20, 10 and 5 charge); on death it bursts into sparks worth 250 charge each.
 * The attack interval is an estimate.
 */
public class ThunderMob extends LavaSeaCreatureMob {
    static final int ATTACK_INTERVAL_TICKS = 100;
    static final double SLAM_DAMAGE = LavaSeaCreature.THUNDER.getDamage();
    static final double BEAM_DAMAGE = 2_000;
    static final double CIRCLE_DAMAGE = 15_000;
    static final double CIRCLE_RADIUS = 3;
    private static final double RANGE = 24;

    public ThunderMob(@Nullable SkyBlockPlayer angler, boolean corrupted) {
        super(LavaSeaCreature.THUNDER, angler, corrupted);
    }

    @Override
    protected void onTick(int ticks) {
        if (ticks % ATTACK_INTERVAL_TICKS != 0) return;
        Instance instance = getInstance();
        if (instance == null) return;
        switch ((ticks / ATTACK_INTERVAL_TICKS) % 3) {
            case 0 -> slam(instance);
            case 1 -> LavaAttacks.target(this, RANGE).ifPresent(target -> beam(instance, target));
            default -> LavaAttacks.target(this, RANGE).ifPresent(target -> circle(instance, target));
        }
    }

    private void slam(Instance instance) {
        MythologicalCombat.particles(instance, Particle.EXPLOSION, getPosition(), 1.5f, 6);
        for (SkyBlockPlayer player : playersWithin(4)) {
            hit(player, SLAM_DAMAGE);
            ThunderBottles.charge(player, 20);
        }
    }

    private void beam(Instance instance, SkyBlockPlayer target) {
        LavaAttacks.beam(instance, getPosition().add(0, 0.6, 0), target.getPosition().add(0, 1.2, 0), Particle.ELECTRIC_SPARK);
        hit(target, BEAM_DAMAGE);
        ThunderBottles.charge(target, 10);
    }

    private void circle(Instance instance, SkyBlockPlayer target) {
        Pos centre = target.getPosition();
        LavaAttacks.ring(instance, centre, CIRCLE_RADIUS, Particle.ELECTRIC_SPARK);
        scheduler().buildTask(() -> {
            if (isRemoved() || isDead()) return;
            MythologicalCombat.lightning(instance, centre);
            for (SkyBlockPlayer player : playersWithin(RANGE)) {
                if (player.getPosition().distance(centre) <= CIRCLE_RADIUS) {
                    hit(player, CIRCLE_DAMAGE);
                    ThunderBottles.charge(player, 5);
                }
            }
        }).delay(TaskSchedule.seconds(1)).schedule();
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Pos at = getPosition();
        super.kill();
        if (instance != null) ThunderSparks.burst(instance, at);
    }
}
