package dev.daweapon.type.skyblockgeneric.entity.mob.impl;

import lombok.Builder;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobRegistry;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.region.MobFreeZones;
import dev.daweapon.type.skyblockgeneric.region.RegionType;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegion;
import dev.daweapon.type.skyblockgeneric.runecrafting.RunicMobs;

import java.util.List;

public interface RegionPopulator {
    List<Populator> getPopulators();

    static void populateRegion(MobRegistry registry, Populator populator) {
        if (HypixelConst.getInstanceContainer() == null) return;
        SkyBlockRegion region = SkyBlockRegion.getRandomRegionOfType(populator.regionType());

        if (region == null) return;

        Pos randomPosition = region.getRandomPositionForEntity(HypixelConst.getInstanceContainer());

        if (randomPosition == null || MobFreeZones.isMobFreeAt(randomPosition)) return;

        if (!HypixelConst.getInstanceContainer().isChunkLoaded(randomPosition))
            HypixelConst.getInstanceContainer().loadChunk(randomPosition).join();

        SkyBlockMob mob = registry.asMob();
        RunicMobs.rollSpawn(mob);
        mob.setInstance(HypixelConst.getInstanceContainer(), randomPosition);
    }

    @Builder
    record Populator(RegionType regionType, int minimumAmountToPopulate) {}
}
