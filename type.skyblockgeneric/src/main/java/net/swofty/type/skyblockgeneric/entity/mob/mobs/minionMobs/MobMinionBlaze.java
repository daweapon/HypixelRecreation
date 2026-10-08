package net.swofty.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionBlaze extends MinionSpawnedMob {
    public MobMinionBlaze() {
        super(EntityType.BLAZE);
    }

    @Override
    public String getDisplayName() {
        return "Blaze";
    }

    @Override
    protected double health() {
        return 160;
    }

    @Override
    protected boolean meleeAttacker() {
        return false;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.INFERNAL);
    }
}
