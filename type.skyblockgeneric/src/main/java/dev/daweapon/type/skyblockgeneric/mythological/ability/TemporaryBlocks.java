package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Blocks a creature places into the Hub for a while, remembered so the original world comes back
 * when they expire or the creature leaves.
 */
public final class TemporaryBlocks {
    private final Map<BlockVec, Block> originals = new LinkedHashMap<>();
    private Instance instance;

    public boolean place(Instance instance, Point position, Block block, boolean replaceSolid) {
        BlockVec key = new BlockVec(position);
        Block current = instance.getBlock(key);
        if (current.handler() != null || current.hasNbt()) return false;
        if (!replaceSolid && current.isSolid()) return false;
        this.instance = instance;
        originals.putIfAbsent(key, current);
        instance.setBlock(key, block);
        return true;
    }

    public boolean contains(Point position) {
        return originals.containsKey(new BlockVec(position));
    }

    public Set<BlockVec> positions() {
        return originals.keySet();
    }

    public void restoreAll() {
        if (instance == null) return;
        originals.forEach((position, block) -> instance.setBlock(position, block));
        originals.clear();
    }
}
