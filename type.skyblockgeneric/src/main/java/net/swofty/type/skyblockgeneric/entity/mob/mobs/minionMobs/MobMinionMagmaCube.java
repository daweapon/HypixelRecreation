package net.swofty.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionMagmaCube extends MinionSpawnedMob {
    public MobMinionMagmaCube() {
        super(EntityType.MAGMA_CUBE);
    }

    @Override
    public String getDisplayName() {
        return "Magma Cube";
    }

    @Override
    protected double health() {
        return 25;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.INFERNAL, MobType.CUBIC);
    }
}
