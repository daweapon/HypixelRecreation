package dev.daweapon.type.skyblockgeneric.region.mining;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class CropBlocks {
    private static final Map<String, Material> CROP_ITEMS = Map.of(
            "minecraft:potatoes", Material.POTATO,
            "minecraft:carrots", Material.CARROT,
            "minecraft:beetroots", Material.BEETROOT,
            "minecraft:cocoa", Material.COCOA_BEANS);
    private static final Map<String, String> MAX_AGE = Map.of(
            "minecraft:wheat", "7",
            "minecraft:potatoes", "7",
            "minecraft:carrots", "7",
            "minecraft:beetroots", "3",
            "minecraft:nether_wart", "3",
            "minecraft:cocoa", "2");

    private CropBlocks() {
    }

    public static @Nullable Material materialOf(Block block) {
        Material crop = CROP_ITEMS.get(block.key().asString());
        return crop != null ? crop : Material.fromKey(block.key());
    }

    public static Block fullyGrown(Block block) {
        String age = MAX_AGE.get(block.key().asString());
        return age == null ? block : block.withProperty("age", age);
    }
}
