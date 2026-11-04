package dev.daweapon.type.skyblockgeneric.technoblade;

import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.utility.MobAttackService;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

public class MobChargedCreeper extends CrimsonIsleMob {
    private static final double CHASE_RANGE = 16;
    private static final double FUSE_RANGE = 3;
    private static final double BLAST_RANGE = 4;
    private static final int FUSE_TICKS = 30;
    private static final int REPATH_TICKS = 10;

    private int fuse = -1;

    public MobChargedCreeper() {
        super(ThunderProfiles.CHARGED_CREEPER);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(CreeperMeta.class, meta -> meta.setCharged(true));
    }

    @Override
    protected boolean melee() {
        return false;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected void onAiTick(int ticks) {
        if (fuse < 0) {
            if (!playersWithin(FUSE_RANGE).isEmpty()) {
                fuse = FUSE_TICKS;
                editEntityMeta(CreeperMeta.class, meta -> meta.setIgnited(true));
                return;
            }
            if (ticks % REPATH_TICKS == 0) {
                nearestPlayer(CHASE_RANGE, player -> true)
                        .ifPresent(target -> getNavigator().setPathTo(target.getPosition()));
            }
            return;
        }
        if (--fuse > 0) return;
        explode();
    }

    private void explode() {
        getInstance().sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION, getPosition(), Vec.ZERO, 0, 1));
        for (SkyBlockPlayer player : playersWithin(BLAST_RANGE)) {
            MobAttackService.strike(this, player, getProfile().damage());
        }
        remove();
    }
}
