package net.swofty.type.skyblockgeneric.item.tool;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;

import java.util.Map;
import java.util.Optional;

public final class AxeLogDrops {
    public static final int EFFICIENT_AXE_PLANKS = 5;
    public static final double SWEET_AXE_APPLE_CHANCE = 20;
    public static final int PROMISING_AXE_BLOCKS_FOR_EFFICIENCY = 50;

    private static final Map<String, ItemType> PLANKS_BY_WOOD = Map.of(
            "oak", ItemType.OAK_PLANKS,
            "spruce", ItemType.SPRUCE_PLANKS,
            "birch", ItemType.BIRCH_PLANKS,
            "jungle", ItemType.JUNGLE_PLANKS,
            "acacia", ItemType.ACACIA_PLANKS,
            "dark_oak", ItemType.DARK_OAK_PLANKS);

    private AxeLogDrops() {
    }

    public static boolean isLog(Material material) {
        String name = material.key().value();
        return name.endsWith("_log") && !name.startsWith("stripped_");
    }

    public static Optional<ItemType> planksFor(Material log) {
        String name = log.key().value();
        if (!name.endsWith("_log")) return Optional.empty();
        String wood = name.substring(0, name.length() - "_log".length()).replace("stripped_", "");
        return Optional.ofNullable(PLANKS_BY_WOOD.get(wood));
    }

    public static boolean promisingAxeEarnsEfficiency(int blocksBroken) {
        return blocksBroken >= PROMISING_AXE_BLOCKS_FOR_EFFICIENCY;
    }
}
