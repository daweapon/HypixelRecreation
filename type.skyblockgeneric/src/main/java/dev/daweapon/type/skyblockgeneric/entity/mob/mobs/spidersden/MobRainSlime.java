package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.entity.metadata.cube.SlimeMeta;
import dev.daweapon.type.skyblockgeneric.crimson.mob.CrimsonMobProfile;
import dev.daweapon.type.skyblockgeneric.spidersden.SpiderDenMob;

public class MobRainSlime extends SpiderDenMob {
    private static final int SIZE = 3;
    private static final int TOXIC_SIZE = 4;

    private final boolean toxic;

    public MobRainSlime(CrimsonMobProfile profile, boolean toxic) {
        super(profile);
        this.toxic = toxic;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(SlimeMeta.class, meta -> meta.setSize(toxic ? TOXIC_SIZE : SIZE));
    }
}
