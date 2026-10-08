package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.particle.Particle;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.mythological.ability.MythologicalCombat;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Dive Ghast (wiki: Dive_Ghast): no fireballs; it hisses, dives at the player and explodes for 10,000. It leaves
 * anyone wearing a Netherrack-Looking Sunshade alone. The dive cooldown is an estimate.
 */
public class MobDiveGhast extends CrimsonIsleMob {
    public MobDiveGhast() {
        super(CrimsonMobs.DIVE_GHAST);
    }

    private static final double EXPLOSION_RADIUS = 3;
    private static final int DIVE_COOLDOWN_TICKS = 140;
    private int cooldown = 40;
    private SkyBlockPlayer diving;

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return !wearsSunshade(player);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
    }

    @Override
    protected void onAiTick(int ticks) {
        if (diving != null) {
            dive();
            return;
        }
        if (--cooldown > 0) return;
        rangedTarget(32).ifPresent(player -> {
            diving = player;
            MythologicalCombat.sound(this, "entity.ghast.warn", 2f, 1f);
            MythologicalCombat.particles(getInstance(), Particle.FLAME, getPosition().add(0, 2, 0), 1.5f, 20);
        });
    }

    private void dive() {
        if (diving.isDead() || diving.getInstance() != getInstance() || wearsSunshade(diving)) {
            diving = null;
            cooldown = DIVE_COOLDOWN_TICKS;
            return;
        }
        Vec toward = diving.getPosition().sub(getPosition()).asVec();
        if (toward.length() > EXPLOSION_RADIUS) {
            setVelocity(toward.normalize().mul(18));
            return;
        }
        MythologicalCombat.particles(getInstance(), Particle.EXPLOSION, getPosition(), 1f, 3);
        MythologicalCombat.sound(this, "entity.generic.explode", 2f, 1f);
        playersWithin(EXPLOSION_RADIUS + 1).forEach(player -> strike(player, getProfile().damage()));
        teleport(getPosition().add(0, 15, 0));
        diving = null;
        cooldown = DIVE_COOLDOWN_TICKS;
    }

    private static boolean wearsSunshade(SkyBlockPlayer player) {
        return new SkyBlockItem(player.getHelmet()).getAttributeHandler().getPotentialType() == ItemType.NETHERRACK_LOOKING_SUNSHADE;
    }
}
