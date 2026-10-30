package dev.daweapon.type.skyblockgeneric.glacite.mineshaft;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

final class MineshaftPlaceholderGenerator implements Generator {
    private static final Block LADDER = Block.LADDER.withProperty("facing", "west");

    private final Block primaryVein;

    MineshaftPlaceholderGenerator(ShaftLayout layout) {
        Material vein = layout.type().vein();
        this.primaryVein = vein.block() == null ? Block.GOLD_BLOCK : vein.block();
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();
        int minX = Math.max(start.blockX(), MineshaftPlaceholderLayout.MIN_X);
        int maxX = Math.min(end.blockX() - 1, MineshaftPlaceholderLayout.MAX_X);
        int minZ = Math.max(start.blockZ(), MineshaftPlaceholderLayout.MIN_Z);
        int maxZ = Math.min(end.blockZ() - 1, MineshaftPlaceholderLayout.MAX_Z);
        int minY = Math.max(start.blockY(), MineshaftPlaceholderLayout.MIN_Y);
        int maxY = Math.min(end.blockY() - 1, MineshaftPlaceholderLayout.MAX_Y);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = block(MineshaftPlaceholderLayout.tileAt(x, y, z));
                    if (block != null) unit.modifier().setBlock(x, y, z, block);
                }
            }
        }
    }

    private Block block(MineshaftPlaceholderLayout.Tile tile) {
        return switch (tile) {
            case AIR -> null;
            case WALL -> Block.STONE_BRICKS;
            case FILL -> Block.DEEPSLATE_BRICKS;
            case PLATFORM -> Block.COBBLESTONE;
            case PILLAR -> Block.STONE_BRICKS;
            case LADDER -> LADDER;
            case VEIN_PRIMARY -> primaryVein;
            case VEIN_GLACITE -> Block.PACKED_ICE;
            case VEIN_UMBER -> Block.BROWN_TERRACOTTA;
            case VEIN_TUNGSTEN -> Block.CLAY;
            case VEIN_TITANIUM -> Block.POLISHED_DIORITE;
        };
    }
}
