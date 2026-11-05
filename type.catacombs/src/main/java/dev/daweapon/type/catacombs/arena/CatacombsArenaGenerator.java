package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.RoomColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

/** Fills each chunk of a run's dungeon as Minestom generates it, so the rooms are built off the tick thread. */
final class CatacombsArenaGenerator implements Generator {
    private final CatacombsArenaBlocks blocks;

    CatacombsArenaGenerator(CatacombsArenaBlocks blocks) {
        this.blocks = blocks;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();
        int minX = Math.max(start.blockX(), blocks.minX());
        int maxX = Math.min(end.blockX() - 1, blocks.maxX());
        int minZ = Math.max(start.blockZ(), blocks.minZ());
        int maxZ = Math.min(end.blockZ() - 1, blocks.maxZ());
        int unitMinY = start.blockY();
        int unitMaxY = end.blockY() - 1;
        CatacombsBlockPlan plan = blocks.plan();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                RealRoom owner = blocks.ownerAt(x, z);
                int minY = Math.max(unitMinY, owner != null ? owner.minY() : plan.minY());
                int maxY = Math.min(unitMaxY, owner != null ? owner.maxY() : plan.maxY());
                for (int y = minY; y <= maxY; y++) {
                    Block block = owner != null ? blocks.blockIn(owner, x, y, z) : blocks.planBlock(x, y, z);
                    if (block != null) unit.modifier().setBlock(x, y, z, block);
                }
            }
        }
    }

    static Block block(CatacombsBlockPlan.Tile tile, RoomColor colour) {
        return switch (tile) {
            case AIR -> null;
            case BEDROCK -> Block.BEDROCK;
            case FLOOR -> Block.STONE_BRICKS;
            case WALL -> Block.DEEPSLATE_BRICKS;
            case CEILING -> Block.BARRIER;
            case WITHER_DOOR -> Block.COAL_BLOCK;
            case BLOOD_DOOR -> Block.RED_WOOL;
            case MARKER_FLOOR -> concrete(colour);
            case MARKER_PILLAR -> glass(colour);
        };
    }

    static Block concrete(RoomColor colour) {
        return switch (colour) {
            case GREEN -> Block.GREEN_CONCRETE;
            case BROWN -> Block.BROWN_CONCRETE;
            case PURPLE -> Block.PURPLE_CONCRETE;
            case YELLOW -> Block.YELLOW_CONCRETE;
            case ORANGE -> Block.ORANGE_CONCRETE;
            case PINK -> Block.PINK_CONCRETE;
            case RED -> Block.RED_CONCRETE;
        };
    }

    private static Block glass(RoomColor colour) {
        return switch (colour) {
            case GREEN -> Block.GREEN_STAINED_GLASS;
            case BROWN -> Block.BROWN_STAINED_GLASS;
            case PURPLE -> Block.PURPLE_STAINED_GLASS;
            case YELLOW -> Block.YELLOW_STAINED_GLASS;
            case ORANGE -> Block.ORANGE_STAINED_GLASS;
            case PINK -> Block.PINK_STAINED_GLASS;
            case RED -> Block.RED_STAINED_GLASS;
        };
    }
}
