package dev.daweapon.type.generic.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

// Minestom BlockHandler for banner blocks; preserves the banner pattern NBT tag.
public final class BannerBlockHandler implements BlockHandler {
    public static final Key KEY = Key.key("minecraft:banner");

    @Override
    public @NotNull Key getKey() {
        return KEY;
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return Set.of(
            Tag.NBT("patterns"));
    }
}