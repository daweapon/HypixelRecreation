package dev.daweapon.type.skyblockgeneric.block;

import dev.daweapon.type.skyblockgeneric.block.blocks.BlockBrewingStand;
import dev.daweapon.type.skyblockgeneric.block.blocks.BlockChest;
import dev.daweapon.type.skyblockgeneric.block.blocks.BlockDecoration;
import dev.daweapon.type.skyblockgeneric.block.blocks.BlockIslandUtility;
import dev.daweapon.type.skyblockgeneric.block.blocks.BlockPortableCampfire;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;

// Registry of custom SkyBlock block kinds, each mapped to its CustomSkyBlockBlock implementation class.
public enum BlockType {
    CHEST(BlockChest.class),
    DECORATION(BlockDecoration.class),
    BREWING_STAND(BlockBrewingStand.class),
    ISLAND_UTILITY(BlockIslandUtility.class),
    MINION_STORAGE(dev.daweapon.type.skyblockgeneric.block.blocks.BlockMinionStorage.class),
    PORTABLE_CAMPFIRE(BlockPortableCampfire.class),
    EXPERIMENTATION_TABLE(dev.daweapon.type.skyblockgeneric.block.blocks.BlockExperimentationTable.class),
    ;

    public final Class<? extends CustomSkyBlockBlock> clazz;

    BlockType(Class<? extends CustomSkyBlockBlock> clazz) {
        this.clazz = clazz;
    }

    public static BlockType getFromName(String name) {
        for (BlockType blockType : values()) {
            if (blockType.name().equalsIgnoreCase(name)) {
                return blockType;
            }
        }
        return null;
    }
}
