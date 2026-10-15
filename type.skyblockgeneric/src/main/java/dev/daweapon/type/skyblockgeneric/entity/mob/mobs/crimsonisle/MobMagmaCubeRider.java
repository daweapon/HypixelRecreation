package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;

/**
 * Magma Cube Rider (wiki: Magma_Cube_Rider): docile until pushed, then jumps at the player every few seconds.
 */
public class MobMagmaCubeRider extends CrimsonIsleMob {
    public MobMagmaCubeRider() {
        super(CrimsonMobs.MAGMA_CUBE_RIDER);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(3));
    }

    @Override
    protected void onAiTick(int ticks) {
        if (ticks % 60 != 0) return;
        rangedTarget(10).ifPresent(player ->
                setVelocity(player.getPosition().sub(getPosition()).asVec().normalize().mul(10).withY(8)));
    }

    @Override
    protected Temper temper() {
        return Temper.NEUTRAL;
    }
}
