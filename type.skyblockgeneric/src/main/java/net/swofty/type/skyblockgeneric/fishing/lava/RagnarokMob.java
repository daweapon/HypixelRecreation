package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ragnarok (wiki: Ragnarok): above half health it leaps around its target every ~15 seconds sending out fire waves;
 * at half health it calls down lightning while invulnerable; below half the ground around it heats up and standing
 * in the scorched ring hurts badly. Wave damage, ring size and the invulnerable window are estimates.
 */
public class RagnarokMob extends LavaSeaCreatureMob {
    static final int WAVE_INTERVAL_TICKS = 300;
    static final int SCORCH_INTERVAL_TICKS = 100;
    static final int INVULNERABLE_TICKS = 200;
    static final double WAVE_RADIUS = 6;
    static final double SCORCH_RADIUS = 4;
    private static final double RANGE = 30;

    private int invulnerableUntil = -1;
    private boolean enraged;

    public RagnarokMob(@Nullable SkyBlockPlayer angler, boolean corrupted) {
        super(LavaSeaCreature.RAGNAROK, angler, corrupted);
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        if (getTicksAlive() < invulnerableUntil) return false;
        boolean hurt = super.damage(damage);
        if (!enraged && !isDead() && getHealth() <= getMaxHealth() / 2) {
            enraged = true;
            invulnerableUntil = getTicksAlive() + INVULNERABLE_TICKS;
        }
        return hurt;
    }

    @Override
    protected void onTick(int ticks) {
        Instance instance = getInstance();
        if (instance == null) return;
        if (ticks < invulnerableUntil) {
            if (ticks % 20 == 0) LavaAttacks.target(this, RANGE)
                    .ifPresent(target -> MythologicalCombat.lightning(instance, target.getPosition()));
            return;
        }
        if (ticks % WAVE_INTERVAL_TICKS == 0) fireWave(instance);
        if (enraged && ticks % SCORCH_INTERVAL_TICKS == 0) scorch(instance);
    }

    private void fireWave(Instance instance) {
        LavaAttacks.target(this, RANGE).ifPresent(target -> teleport(target.getPosition().add(3, 0, 0)));
        LavaAttacks.ring(instance, getPosition(), WAVE_RADIUS, Particle.FLAME);
        playersWithin(WAVE_RADIUS).forEach(player -> hit(player, getCreature().getDamage()));
    }

    private void scorch(Instance instance) {
        Pos centre = getPosition();
        LavaAttacks.ring(instance, centre, SCORCH_RADIUS, Particle.LAVA);
        scheduler().buildTask(() -> {
            if (isDead() || isRemoved()) return;
            playersWithin(RANGE).stream()
                    .filter(player -> player.getPosition().distance(centre) <= SCORCH_RADIUS)
                    .forEach(player -> hit(player, getCreature().getDamage() * 3));
        }).delay(TaskSchedule.seconds(2)).schedule();
    }
}
