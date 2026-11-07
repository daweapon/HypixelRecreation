package dev.daweapon.type.skyblockgeneric.item;

import dev.daweapon.commons.skyblock.item.attribute.ItemAttribute;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyBlockItemSimilarityTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
        ItemAttribute.registerItemAttributes();
    }

    @Test
    void itemsWhoseAttributesSaveAsNothingStillCompare() {
        assertTrue(new SkyBlockItem(Material.STONE).isSimilar(new SkyBlockItem(Material.STONE)));
        assertFalse(new SkyBlockItem(Material.STONE).isSimilar(new SkyBlockItem(Material.DIRT)));
    }
}
