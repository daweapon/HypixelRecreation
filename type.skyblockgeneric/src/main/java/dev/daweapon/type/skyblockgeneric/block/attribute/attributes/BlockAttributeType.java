package dev.daweapon.type.skyblockgeneric.block.attribute.attributes;

import dev.daweapon.type.skyblockgeneric.block.attribute.BlockAttribute;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import org.jetbrains.annotations.Nullable;

// Block attribute storing the block's BlockType name as a plain string.
public class BlockAttributeType extends BlockAttribute<String> {
    @Override
    public String getKey() {
        return "block_type";
    }

    @Override
    public String getDefaultValue(@Nullable Class<? extends CustomSkyBlockBlock> itemClass) {
        return "N/A";
    }

    @Override
    public String loadFromString(String string) {
        return string;
    }

    @Override
    public String saveIntoString() {
        return this.value;
    }
}
