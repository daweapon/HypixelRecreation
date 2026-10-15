package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobs;
import dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;

/**
 * Unstable Magma (wiki: Unstable_Magma): split off the Magma Boss in three sizes; it leaps at players and is
 * thrown back out if it falls into the arena's central lava pit.
 */
public class MobUnstableMagma extends CrimsonIsleMob {
    private static final int LEAP_INTERVAL = 60;
    private Pos pit = CrimsonMiniboss.MAGMA_BOSS.getSpawn();

    public MobUnstableMagma() {
        this(CrimsonMobs.UNSTABLE_MAGMA_40);
    }

    public MobUnstableMagma(CrimsonMobProfile profile) {
        super(profile);
    }

    public MobUnstableMagma pit(Pos pit) {
        this.pit = pit;
        return this;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(switch (getMobID()) {
            case "UNSTABLE_MAGMA_80" -> 4;
            case "UNSTABLE_MAGMA_60" -> 3;
            default -> 2;
        }));
    }

    @Override
    protected void onAiTick(int ticks) {
        if (getInstance() != null && getInstance().getBlock(getPosition()).name().contains("lava")) {
            Vec out = getPosition().sub(pit).asVec().withY(0);
            if (out.lengthSquared() < 0.01) out = new Vec(1, 0, 0);
            setVelocity(out.normalize().mul(12).withY(12));
            return;
        }
        if (ticks % LEAP_INTERVAL != 0) return;
        rangedTarget(16).ifPresent(player ->
                setVelocity(player.getPosition().sub(getPosition()).asVec().normalize().mul(10).withY(7)));
    }
}
