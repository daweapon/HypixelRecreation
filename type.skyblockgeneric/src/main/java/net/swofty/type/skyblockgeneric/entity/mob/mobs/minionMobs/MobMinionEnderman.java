package net.swofty.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionEnderman extends MinionSpawnedMob {
    public MobMinionEnderman() {
        super(EntityType.ENDERMAN);
    }

    @Override
    public String getDisplayName() {
        return "Enderman";
    }

    @Override
    protected double health() {
        return 100;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.ENDER);
    }
}
