package dev.daweapon.type.skyblockgeneric.mythological.ability;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * The cross of Iron Blocks the Gaia Construct and Minos Champion raise under a player; standing on
 * it calls down lightning.
 */
final class IronCross {
    private static final int ARM_LENGTH = 2;

    private IronCross() {
    }

    static List<BlockVec> raise(Instance instance, Point feet, TemporaryBlocks blocks) {
        int y = (int) Math.floor(feet.y() - 0.01) - 1;
        List<BlockVec> placed = new ArrayList<>();
        for (int offset = -ARM_LENGTH; offset <= ARM_LENGTH; offset++) {
            tryPlace(instance, new BlockVec(feet.blockX() + offset, y, feet.blockZ()), blocks, placed);
            if (offset != 0) tryPlace(instance, new BlockVec(feet.blockX(), y, feet.blockZ() + offset), blocks, placed);
        }
        return placed;
    }

    static boolean isStandingOn(Point feet, List<BlockVec> cross) {
        BlockVec below = new BlockVec(feet.blockX(), (int) Math.floor(feet.y() - 0.01) - 1, feet.blockZ());
        return cross.contains(below);
    }

    private static void tryPlace(Instance instance, BlockVec position, TemporaryBlocks blocks, List<BlockVec> placed) {
        if (!instance.getBlock(position).isSolid()) return;
        if (blocks.place(instance, position, Block.IRON_BLOCK, true)) placed.add(position);
    }
}
