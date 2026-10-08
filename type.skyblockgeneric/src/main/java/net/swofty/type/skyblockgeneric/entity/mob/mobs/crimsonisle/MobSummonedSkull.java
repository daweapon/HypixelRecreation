package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.particle.Particle;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.crimson.mob.LavaBeam;
import org.jetbrains.annotations.Nullable;

/**
 * The Mage Outlaw's Summoned Skulls (wiki: Mage_Outlaw): they drift to him over 8 s; one that arrives unkilled
 * becomes a 500k skull that circles him and beams players for 2,000.
 */
public class MobSummonedSkull extends CrimsonIsleMob {
    private static final int DRIFT_TICKS = 160;
    private static final int BEAM_INTERVAL = 40;
    private final @Nullable MobMageOutlaw owner;
    private final boolean empowered;

    public MobSummonedSkull() {
        this(null, false);
    }

    public MobSummonedSkull(@Nullable MobMageOutlaw owner, boolean empowered) {
        super(empowered ? CrimsonMobs.EMPOWERED_SKULL : CrimsonMobs.SUMMONED_SKULL);
        this.owner = owner;
        this.empowered = empowered;
    }

    @Override
    protected Temper temper() {
        return Temper.PASSIVE;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setNoGravity(true);
        setHelmet(ItemStack.of(Material.WITHER_SKELETON_SKULL));
    }

    @Override
    protected void onAiTick(int ticks) {
        if (owner == null || owner.isDead() || owner.isRemoved()) {
            remove();
            return;
        }
        if (!empowered) {
            Vec toward = owner.towardMe(getPosition());
            if (toward.length() <= 1.5) {
                owner.empower(this);
                return;
            }
            setVelocity(toward.normalize().mul(Math.max(1.5, toward.length() * 20 / Math.max(1, DRIFT_TICKS - ticks))));
            return;
        }
        double angle = ticks * 0.08;
        Pos centre = owner.getPosition();
        teleport(new Pos(centre.x() + Math.cos(angle) * 3, centre.y() + 2.5, centre.z() + Math.sin(angle) * 3));
        if (ticks % BEAM_INTERVAL != 0) return;
        nearestPlayer(20, player -> true).ifPresent(player -> {
            LavaBeam.draw(getInstance(), getPosition(), player.getPosition().add(0, 1, 0), Particle.SOUL_FIRE_FLAME);
            strike(player, getProfile().damage());
        });
    }
}
