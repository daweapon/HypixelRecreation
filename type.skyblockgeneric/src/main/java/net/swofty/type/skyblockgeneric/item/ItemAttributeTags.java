package net.swofty.type.skyblockgeneric.item;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Reads and writes an item's attribute strings straight from its custom-data compound.
 * Minestom's per-tag accessors rebuild a tag handler from the whole compound on every call,
 * which made loading or writing one item quadratic in its attribute count.
 */
public final class ItemAttributeTags {
    private ItemAttributeTags() {
    }

    public static CompoundBinaryTag of(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? CompoundBinaryTag.empty() : data.nbt();
    }

    public static @Nullable String string(CompoundBinaryTag nbt, String key) {
        return nbt.get(key) instanceof StringBinaryTag value ? value.value() : null;
    }

    public static boolean flag(CompoundBinaryTag nbt, String key) {
        BinaryTag tag = nbt.get(key);
        return tag instanceof ByteBinaryTag value && value.value() == 1;
    }

    public static CompoundBinaryTag compound(Collection<? extends ItemAttribute> attributes) {
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        for (ItemAttribute attribute : attributes) {
            String value = attribute.saveIntoString();
            if (value != null) builder.putString(attribute.getKey(), value);
        }
        return builder.build();
    }

    /** Replaces the builder's custom data with exactly these attributes. */
    public static ItemStack.Builder write(ItemStack.Builder builder, Collection<? extends ItemAttribute> attributes) {
        return builder.set(DataComponents.CUSTOM_DATA, new CustomData(compound(attributes)));
    }
}
