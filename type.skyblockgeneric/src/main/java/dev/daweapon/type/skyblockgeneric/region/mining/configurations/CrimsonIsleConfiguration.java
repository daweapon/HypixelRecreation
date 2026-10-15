package dev.daweapon.type.skyblockgeneric.region.mining.configurations;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import dev.daweapon.type.skyblockgeneric.region.SkyBlockRegenConfiguration;

import java.util.List;

/**
 * Crimson Isle gathering. Mycelium, Red Sand, Glowstone, Nether Quartz Ore and Netherrack grow back
 * through bedrock; Dead Bushes and Brown Mushrooms (Red Thornleaf and Mushroom Spore) regrow in
 * place; Sulphur Ore (sponge) from volcano eruptions is gone once mined. The wiki gives no
 * regeneration time, so it uses 6 seconds (estimate).
 */
public class CrimsonIsleConfiguration extends SkyBlockRegenConfiguration {
    private static final List<Material> MINEABLE = List.of(Material.MYCELIUM, Material.RED_SAND, Material.GLOWSTONE,
            Material.NETHER_QUARTZ_ORE, Material.NETHERRACK, Material.SPONGE, Material.DEAD_BUSH, Material.BROWN_MUSHROOM);
    private static final List<Block> PLANTS = List.of(Block.DEAD_BUSH, Block.BROWN_MUSHROOM);

    @Override
    public MiningTask handleStageOne(MiningTask task, Pos brokenBlock) {
        if (task.getInitialMinedBlock().compare(Block.SPONGE)) {
            task.setIntermediaryBlock(Block.AIR);
            task.setReviveBlock(Block.AIR);
            return task;
        }
        boolean plant = PLANTS.stream().anyMatch(task.getInitialMinedBlock()::compare);
        task.setIntermediaryBlock(plant ? Block.AIR : Block.BEDROCK);
        task.setReviveBlock(task.getInitialMinedBlock());
        return task;
    }

    @Override
    public MiningTask handleStageTwo(MiningTask task, Pos brokenBlock) {
        return task;
    }

    @Override
    public List<Material> getMineableBlocks(Instance instance, Point point) {
        return MINEABLE;
    }

    @Override
    public long getRegenerationTime() {
        return 6_000;
    }
}
