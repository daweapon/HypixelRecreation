package dev.daweapon.type.generic.entity.npc.walk;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link WalkGrid} read from a live instance. Unloaded chunks and liquids are never entered, so a
 * search never loads chunks; doors and fence gates count as open, since NPCs walk through them.
 */
public final class InstanceWalkGrid implements WalkGrid {
    private final Instance instance;
    private final Map<Long, Double> tops = new HashMap<>();

    public InstanceWalkGrid(Instance instance) {
        this.instance = instance;
    }

    @Override
    public double top(int x, int y, int z) {
        return tops.computeIfAbsent(NPCPathfinder.key(x, y, z), _ -> read(x, y, z));
    }

    private double read(int x, int y, int z) {
        if (!instance.isChunkLoaded(x >> 4, z >> 4)) return BLOCKED;
        Block block;
        try {
            block = instance.getBlock(x, y, z, Block.Getter.Condition.TYPE);
        } catch (RuntimeException exception) {
            return BLOCKED;
        }
        if (block.liquid()) return BLOCKED;
        if (!block.solid() || isDoorway(block)) return 0;
        return Math.max(0.01, block.collisionShape().relativeEnd().y());
    }

    private static boolean isDoorway(Block block) {
        String name = block.key().value();
        return name.endsWith("_door") || name.endsWith("_fence_gate");
    }
}
