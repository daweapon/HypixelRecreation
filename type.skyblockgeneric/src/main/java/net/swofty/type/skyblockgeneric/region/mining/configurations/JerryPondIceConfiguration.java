package net.swofty.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegenConfiguration;
import net.swofty.type.skyblockgeneric.region.mining.PondIce;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class JerryPondIceConfiguration extends SkyBlockRegenConfiguration {
    private static final double OCCUPANT_SEARCH_RADIUS = 3;
    private static final double BOBBER_HEADROOM = 0.25;

    protected long minRegrowMillis() {
        return 120_000;
    }

    protected long maxRegrowMillis() {
        return 300_000;
    }

    @Override
    public MiningTask handleStageOne(MiningTask task, Pos brokenBlock) {
        task.setIntermediaryBlock(Block.WATER);
        task.setReviveBlock(Block.ICE);
        task.setRegenerationTime(System.currentTimeMillis()
                + PondIce.regrowDelayMillis(minRegrowMillis(), maxRegrowMillis(), ThreadLocalRandom.current()));
        return task;
    }

    @Override
    public MiningTask handleStageTwo(MiningTask task, Pos brokenBlock) {
        return task;
    }

    @Override
    public List<Material> getMineableBlocks(Instance instance, Point point) {
        if (PondIce.isBreakable(instance.getBlock(point), instance.getBlock(point.sub(0, 1, 0)))) {
            return List.of(Material.ICE);
        }
        return List.of();
    }

    @Override
    public long getRegenerationTime() {
        return minRegrowMillis();
    }

    @Override
    public boolean canRegenerate(MiningTask task) {
        Point cell = task.getPosition();
        Point center = cell.add(0.5, 0.5, 0.5);
        for (Entity entity : task.getInstance().getNearbyEntities(center, OCCUPANT_SEARCH_RADIUS)) {
            Pos position = entity.getPosition();
            if (PondIce.occupies(position.add(entity.getBoundingBox().relativeStart()),
                    position.add(entity.getBoundingBox().relativeEnd()), cell, BOBBER_HEADROOM)) {
                return false;
            }
        }
        return true;
    }
}
