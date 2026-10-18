package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionTarantula extends MinionSpawnedMob {
    public MobMinionTarantula() {
        super(EntityType.SPIDER);
    }

    @Override
    public String getDisplayName() {
        return "Tarantula";
    }

    @Override
    protected double health() {
        return 100;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ARTHROPOD);
    }
}
