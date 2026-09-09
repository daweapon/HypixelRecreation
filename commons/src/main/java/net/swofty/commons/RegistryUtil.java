package net.swofty.commons;

import lombok.experimental.UtilityClass;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@UtilityClass
// Helper for checking whether a Minestom Material belongs to a given material registry tag.
public class RegistryUtil {
    final Registry<Material> MATERIAL = Material.staticRegistry();

    public boolean inMaterial(final @NotNull TagKey<Material> materialRegistryTag, final @NotNull Material material) {
        return MATERIAL.getTag(materialRegistryTag).contains(Objects.requireNonNull(MATERIAL.getKey(material)));
    }
}
