package net.swofty.type.skyblockgeneric.block;

import net.swofty.type.skyblockgeneric.block.blocks.BlockBrewingStand;
import net.swofty.type.skyblockgeneric.block.blocks.BlockChest;
import net.swofty.type.skyblockgeneric.block.blocks.BlockDecoration;
import net.swofty.type.skyblockgeneric.block.blocks.BlockIslandUtility;
import net.swofty.type.skyblockgeneric.block.blocks.BlockPortableCampfire;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;

// Registry of custom SkyBlock block kinds, each mapped to its CustomSkyBlockBlock implementation class.
public enum BlockType {
    CHEST(BlockChest.class),
    DECORATION(BlockDecoration.class),
    BREWING_STAND(BlockBrewingStand.class),
    ISLAND_UTILITY(BlockIslandUtility.class),
    PORTABLE_CAMPFIRE(BlockPortableCampfire.class),
    EXPERIMENTATION_TABLE(net.swofty.type.skyblockgeneric.block.blocks.BlockExperimentationTable.class),
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
