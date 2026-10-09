package net.swofty.type.kuudra.arena;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

final class KuudraPlaceholderGenerator implements Generator {
    private final KuudraPlaceholderLayout layout;

    KuudraPlaceholderGenerator(KuudraPlaceholderLayout layout) {
        this.layout = layout;
    }

    @Override
    public void generate(@NotNull GenerationUnit unit) {
        Point start = unit.absoluteStart();
        Point end = unit.absoluteEnd();
        int minY = Math.max(start.blockY(), layout.minY());
        int maxY = Math.min(end.blockY() - 1, layout.maxY());
        for (int x = start.blockX(); x < end.blockX(); x++) {
            for (int z = start.blockZ(); z < end.blockZ(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block block = block(layout.tileAt(x, y, z));
                    if (block != null) unit.modifier().setBlock(x, y, z, block);
                }
            }
        }
    }

    private static Block block(KuudraPlaceholderLayout.Tile tile) {
        return switch (tile) {
            case AIR -> null;
            case BEDROCK -> Block.BEDROCK;
            case LAVA -> Block.LAVA;
            case FOUNDATION -> Block.BASALT;
            case FLOOR -> Block.POLISHED_BLACKSTONE;
            case BRIDGE -> Block.NETHER_BRICKS;
            case PILE -> Block.GOLD_BLOCK;
            case SUPPLY_SPOT -> Block.MAGMA_BLOCK;
            case BALLISTA -> Block.IRON_BLOCK;
        };
    }
}
