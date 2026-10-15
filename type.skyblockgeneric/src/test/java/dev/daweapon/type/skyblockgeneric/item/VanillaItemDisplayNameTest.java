package dev.daweapon.type.skyblockgeneric.item;

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

    @Test
    void unnamedSkyBlockItemsUseTheirIdNotTheirMaterial() {
        assertEquals("Fire Veil Wand", ItemLore.fallbackDisplayName("FIRE_VEIL_WAND", null, Material.BLAZE_ROD));
        assertEquals("Ragnarock Axe", ItemLore.fallbackDisplayName("RAGNAROCK_AXE", null, Material.GOLDEN_SWORD));
    }

    @Test
    void configuredNamesWinAndVanillaFallsBackToTheMaterial() {
        assertEquals("Kindlebane Dagger", ItemLore.fallbackDisplayName("BURSTFIRE_DAGGER", "Kindlebane Dagger", Material.STONE_SWORD));
        assertEquals("Jungle Leaves", ItemLore.fallbackDisplayName(null, null, Material.JUNGLE_LEAVES));
    }
}
