package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Magma Glare (wiki: Magma_Glare): split off the Magma Boss for one player, bouncing slowly after them and
 * shooting for (2 x n) x 1,000, where n is how many Glares target that player (capped at 14,000).
 */
public class MobMagmaGlare extends CrimsonIsleMob {
    static final double MAX_DAMAGE = 14_000;
    private static final int SHOT_INTERVAL = 40;
    private final @Nullable SkyBlockPlayer owner;

    public MobMagmaGlare() {
        this(null);
    }

    public MobMagmaGlare(@Nullable SkyBlockPlayer owner) {
        super(CrimsonMobs.MAGMA_GLARE);
        this.owner = owner;
    }

    public static double damageFor(int glaresTargeting) {
        return Math.min(MAX_DAMAGE, 2D * glaresTargeting * 1_000);
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    protected int strollRadius() {
        return 0;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(2));
    }

    @Override
    protected void onAiTick(int ticks) {
        if (owner == null || owner.isRemoved() || owner.getInstance() != getInstance()) {
            if (ticks > 20) remove();
            return;
        }
        Vec toward = owner.getPosition().sub(getPosition()).asVec().withY(0);
        if (ticks % 30 == 0 && toward.length() > 3 && isOnGround()) {
            setVelocity(toward.normalize().mul(3).withY(6));
        }
        if (ticks % SHOT_INTERVAL != 0 || owner.isDead()) return;
        int targeting = (int) SkyBlockMob.getMobs().stream()
                .filter(mob -> mob instanceof MobMagmaGlare glare && glare.owner == owner && !glare.isDead())
                .count();
        double damage = damageFor(targeting);
        fireball(owner, damage);
        owner.sendActionBar(Text.parse("<c>" + targeting + " Magma Glares <7>(<c>" + (2 * targeting) + "x<7>)").asComponent());
    }
}
