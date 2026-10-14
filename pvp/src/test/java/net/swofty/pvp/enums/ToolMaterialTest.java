package net.swofty.pvp.enums;

import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolMaterialTest {
    @Test
    void everyToolOfATierMapsBackToIt() {
        assertEquals(ToolMaterial.IRON, ToolMaterial.fromMaterial(Material.IRON_SWORD));
        assertEquals(ToolMaterial.IRON, ToolMaterial.fromMaterial(Material.IRON_HOE));
        assertEquals(ToolMaterial.NETHERITE, ToolMaterial.fromMaterial(Material.NETHERITE_PICKAXE));
        assertEquals(ToolMaterial.GOLD, ToolMaterial.fromMaterial(Material.GOLDEN_AXE));
        assertEquals(ToolMaterial.WOOD, ToolMaterial.fromMaterial(Material.WOODEN_SHOVEL));
    }

    @Test
    void nonToolsHaveNoMaterial() {
        assertNull(ToolMaterial.fromMaterial(Material.DIRT));
        assertNull(ToolMaterial.fromMaterial(Material.BOW));
    }

    @Test
    void attackDamageIncreasesWithTierAndGoldMatchesWood() {
        assertEquals(0.0F, ToolMaterial.WOOD.getAttackDamage());
        assertEquals(ToolMaterial.WOOD.getAttackDamage(), ToolMaterial.GOLD.getAttackDamage());
        assertTrue(ToolMaterial.STONE.getAttackDamage() < ToolMaterial.IRON.getAttackDamage());
        assertTrue(ToolMaterial.IRON.getAttackDamage() < ToolMaterial.DIAMOND.getAttackDamage());
        assertTrue(ToolMaterial.DIAMOND.getAttackDamage() < ToolMaterial.NETHERITE.getAttackDamage());
    }

    @Test
    void goldMinesFastestButAtWoodLevel() {
        for (ToolMaterial material : ToolMaterial.values()) {
            assertTrue(material.getMiningSpeed() <= ToolMaterial.GOLD.getMiningSpeed());
        }
        assertEquals(ToolMaterial.WOOD.getMiningLevel(), ToolMaterial.GOLD.getMiningLevel());
        assertEquals(4, ToolMaterial.NETHERITE.getMiningLevel());
    }
}
