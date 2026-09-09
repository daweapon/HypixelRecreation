package net.swofty.type.skyblockgeneric.item;

import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VanillaItemDisplayNameTest {
    @Test
    void vanillaItemNamesDoNotShowTheirNamespace() {
        String name = ItemLore.materialDisplayName(Material.JUNGLE_LEAVES);

        assertEquals("Jungle Leaves", name);
        assertFalse(name.contains("minecraft:"));
    }
}
