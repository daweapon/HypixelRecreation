package dev.daweapon.type.skyblockgeneric.block.attribute.attributes;

import dev.daweapon.commons.skyblock.item.UnderstandableSkyBlockItem;
import dev.daweapon.type.skyblockgeneric.block.attribute.BlockAttribute;
import dev.daweapon.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Block attribute storing a chest's item contents as a comma-delimited serialized list.
public class BlockAttributeChestData extends BlockAttribute<List<SkyBlockItem>> {
    @Override
    public String getKey() {
        return "chest";
    }

    @Override
    public List<SkyBlockItem> getDefaultValue(@Nullable Class<? extends CustomSkyBlockBlock> blockClass) {
        return new ArrayList<>();
    }

    @Override
    public List<SkyBlockItem> loadFromString(String string) {
        List<SkyBlockItem> items = new ArrayList<>();

        if (string.isEmpty()) {
            return items;
        }

        String[] split = string.split(",");
        for (String item : split) {
            items.add(new SkyBlockItem(UnderstandableSkyBlockItem.deserialize(item)));
        }

        return items;
    }

    @Override
    public String saveIntoString() {
        List<SkyBlockItem> items = this.value;
        List<String> serializedItems = new ArrayList<>();

        items.forEach(item -> {
            serializedItems.add(item.toUnderstandable().serialize());
        });

        return String.join(",", serializedItems);
    }
}
