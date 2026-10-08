package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonSkins;
import net.swofty.type.skyblockgeneric.crimson.mob.LavaBeam;
import net.swofty.type.skyblockgeneric.crimson.mob.RadialProjectiles;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMinibossMob;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Barbarian Duke X (wiki: Barbarian_Duke_X): can only be hurt inside his Flame Domain. He runs at players for
 * 4,500, leaps onto a marked circle that damages and roots everyone in it (more often as he weakens, and
 * almost arena-wide when he's outside the Domain), and throws Flaming Fists outward when someone is close. The
 * Domain radius, leap and fist timings, fist damage and the 3 s root are estimates.
 */
public class MobBarbarianDukeX extends CrimsonMinibossMob {
    static final double DOMAIN_RADIUS = 10;
    private static final double LEAP_RADIUS = 4;
    private static final double LEAP_RADIUS_OUTSIDE = 14;
    private static final int LEAP_TELEGRAPH = 30;
    private static final double FIST_DAMAGE = 2_500;
    private static final int FIST_INTERVAL = 160;

    private @Nullable Pos leapTarget;
    private int leapTelegraph;
    private int leapCooldown = 200;

    public MobBarbarianDukeX() {
        super(CrimsonMobs.BARBARIAN_DUKE_X);
    }

    @Override
    public CrimsonMiniboss miniboss() {
        return CrimsonMiniboss.BARBARIAN_DUKE_X;
    }

    @Override
    protected float aggroRange() {
        return 24;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        CrimsonSkins.dress(this, CrimsonSkins.BARBARIAN_DUKE_X);
        setItemInMainHand(ItemStack.of(Material.GOLDEN_AXE));
    }

    private boolean insideDomain() {
        return getPosition().distance(home()) <= DOMAIN_RADIUS;
    }

    @Override
    protected boolean acceptsDamageFrom(SkyBlockPlayer player, Damage damage) {
        return insideDomain();
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 20 == 0) LavaBeam.ring(getInstance(), home(), DOMAIN_RADIUS, Particle.FLAME);
        List<SkyBlockPlayer> fighters = fighters(24);
        if (leapTarget != null) {
            tickLeap();
            return;
        }
        if (fighters.isEmpty()) return;
        if (--leapCooldown <= 0) {
            SkyBlockPlayer target = fighters.get(ThreadLocalRandom.current().nextInt(fighters.size()));
            leapTarget = target.getPosition();
            leapTelegraph = LEAP_TELEGRAPH;
            double healthLeft = getHealth() / getMaxHealth();
            leapCooldown = (int) (80 + 160 * healthLeft);
        }
        if (ticks % FIST_INTERVAL == 0 && fighters.stream().anyMatch(player -> player.getPosition().distance(getPosition()) <= 6)) {
            RadialProjectiles.fire(getInstance(), getPosition().add(0, 1.2, 0), 12, 0.7, 16,
                    ItemStack.of(Material.BLAZE_POWDER), player -> strike(player, FIST_DAMAGE));
        }
    }

    private void tickLeap() {
        double radius = insideDomain() ? LEAP_RADIUS : LEAP_RADIUS_OUTSIDE;
        LavaBeam.ring(getInstance(), leapTarget, radius, Particle.WITCH);
        if (--leapTelegraph > 0) return;
        teleport(leapTarget);
        MythologicalCombat.particles(getInstance(), Particle.EXPLOSION, leapTarget, 1.5f, 4);
        Pos landed = leapTarget;
        fighters(radius + 1).stream().filter(player -> player.getPosition().distance(landed) <= radius).forEach(player -> {
            strike(player, getProfile().damage());
            player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 6, 60));
        });
        leapTarget = null;
    }
}
