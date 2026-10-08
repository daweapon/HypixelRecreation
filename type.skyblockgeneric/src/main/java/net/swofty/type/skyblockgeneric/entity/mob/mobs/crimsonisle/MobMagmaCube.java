package net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle;

import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonIsleMob;
import net.swofty.type.skyblockgeneric.crimson.mob.CrimsonMobs;

public class MobMagmaCube extends CrimsonIsleMob {
    public MobMagmaCube() {
        super(CrimsonMobs.MAGMA_CUBE);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        editEntityMeta(MagmaCubeMeta.class, meta -> meta.setSize(3));
    }
}
