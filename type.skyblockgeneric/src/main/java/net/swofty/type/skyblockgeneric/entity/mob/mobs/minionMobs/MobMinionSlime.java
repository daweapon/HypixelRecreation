package net.swofty.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionSlime extends MinionSpawnedMob {
    public MobMinionSlime() {
        super(EntityType.SLIME);
    }

    @Override
    public String getDisplayName() {
        return "Slime";
    }

    @Override
    protected double health() {
        return 100;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.CUBIC);
    }
}
