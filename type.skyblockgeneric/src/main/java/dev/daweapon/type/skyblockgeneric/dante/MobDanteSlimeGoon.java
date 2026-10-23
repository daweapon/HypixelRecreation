package dev.daweapon.type.skyblockgeneric.dante;

import net.minestom.server.entity.metadata.cube.SlimeMeta;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;

public class MobDanteSlimeGoon extends CrimsonIsleMob {
    private static final int SIZE = 3;
    private static final int HOP_INTERVAL = 25;
    private static final int CONTACT_INTERVAL = 20;
    private static final double AGGRO_RANGE = 80;
    private static final double HOP_SPEED = 9;
    private static final double HOP_LIFT = 6;

    public MobDanteSlimeGoon() {
        super(DanteProfiles.SLIME_GOON);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(SlimeMeta.class, meta -> meta.setSize(SIZE));
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
    protected void onAiTick(int ticks) {
        List<SkyBlockPlayer> fighters = playersWithin(AGGRO_RANGE);
        if (fighters.isEmpty()) return;
        SkyBlockPlayer target = fighters.stream()
                .min(Comparator.comparingDouble(player -> player.getPosition().distanceSquared(getPosition())))
                .orElseThrow();
        if (ticks % HOP_INTERVAL == 0 && isOnGround()) SlimeMovement.hop(this, target, HOP_SPEED, HOP_LIFT);
        if (ticks % CONTACT_INTERVAL == 0 && target.getPosition().distance(getPosition()) <= getBoundingBox().width() / 2 + 1.5) {
            strike(target, getProfile().damage());
        }
    }
}
