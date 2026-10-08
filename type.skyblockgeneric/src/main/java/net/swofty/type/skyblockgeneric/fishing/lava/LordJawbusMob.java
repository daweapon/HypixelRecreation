package net.swofty.type.skyblockgeneric.fishing.lava;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.mythological.ability.MobProjectile;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lord Jawbus (wiki: Lord_Jawbus) shoots fireballs (2,500) and a laser (30,000). At 60M, 40M and 20M health it
 * rises inside a fire sphere, invulnerable, and calls 2 Jawbus Followers (random island-wide lava Sea Creatures,
 * never Thunder, Jawbus or Plhlegblast); killing them drops the sphere. Attack intervals are estimates, and the TNT
 * lobbing between phases is not modelled.
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

    @Override
    protected void onKilledBy(SkyBlockPlayer killer) {
        followers.forEach(LavaSeaCreatureMob::remove);
    }
}
