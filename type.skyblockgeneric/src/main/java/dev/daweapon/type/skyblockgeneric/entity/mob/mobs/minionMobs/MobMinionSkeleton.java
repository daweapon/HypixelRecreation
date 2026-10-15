package dev.daweapon.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import dev.daweapon.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionSkeleton extends MinionSpawnedMob {
    public MobMinionSkeleton() {
        super(EntityType.SKELETON);
    }

    @Override
    public String getDisplayName() {
        return "Skeleton";
    }

    @Override
    protected double health() {
        return 100;
    }

    @Override
    protected boolean meleeAttacker() {
        return false;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.SKELETAL, MobType.UNDEAD);
    }
}
