package net.swofty.type.skyblockgeneric.block.attribute;

import net.swofty.type.skyblockgeneric.block.BlockType;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.attribute.attributes.BlockAttributeType;

// Convenience wrapper exposing the BlockType of a given SkyBlockBlock via its type attribute.
public class BlockAttributeHandler {
    SkyBlockBlock block;

    public BlockAttributeHandler(SkyBlockBlock block) {
        this.block = block;
    }

    public BlockType getBlockType() {
        return BlockType.valueOf(((BlockAttributeType) block.getAttribute("block_type")).getValue());
    }

}
