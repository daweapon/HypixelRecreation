package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.RoomColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

final class CatacombsPlaceholderGenerator implements Generator {
    private final CatacombsBlockPlan plan;

    CatacombsPlaceholderGenerator(CatacombsBlockPlan plan) {
        this.plan = plan;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();
        int minX = Math.max(start.blockX(), plan.minX());
        int maxX = Math.min(end.blockX() - 1, plan.maxX());
        int minZ = Math.max(start.blockZ(), plan.minZ());
        int maxZ = Math.min(end.blockZ() - 1, plan.maxZ());
        int minY = Math.max(start.blockY(), plan.minY());
        int maxY = Math.min(end.blockY() - 1, plan.maxY());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = block(plan.tileAt(x, y, z), plan.markerColourAt(x, z));
                    if (block != null) unit.modifier().setBlock(x, y, z, block);
                }
            }
        }
    }

    private static Block block(CatacombsBlockPlan.Tile tile, RoomColor colour) {
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
