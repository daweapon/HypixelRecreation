package net.swofty.type.skyblockgeneric.entity.mob.mobs.minionMobs;

import net.minestom.server.entity.EntityType;
import net.swofty.type.skyblockgeneric.entity.mob.MinionSpawnedMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;

import java.util.List;

public class MobMinionGhast extends MinionSpawnedMob {
    public MobMinionGhast() {
        super(EntityType.GHAST);
    }

    @Override
    public String getDisplayName() {
        return "Ghast";
    }

    @Override
    protected double health() {
        return 150;
    }

    @Override
    protected boolean meleeAttacker() {
        return false;
    }

    @Override
    public List<MobType> getMobTypes() {
        return List.of(MobType.CUBIC, MobType.AIRBORNE);
    }
}
