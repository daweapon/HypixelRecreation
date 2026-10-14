package net.swofty.pvp.utils;

import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatUtilsTest {
    @Test
    void potionFlagsCombineBits() {
        assertEquals(0, PotionFlags.create(false, false, false));
        assertEquals(1, PotionFlags.create(true, false, false));
        assertEquals(2, PotionFlags.create(false, true, false));
        assertEquals(4, PotionFlags.create(false, false, true));
        assertEquals(7, PotionFlags.create(true, true, true));
    }

    @Test
    void defaultPotionFlagsShowParticlesAndIconButNotAmbient() {
        assertEquals(6, PotionFlags.defaultFlags());
    }

    @Test
    void combatVersionResolvesFromLegacyFlag() {
        assertSame(CombatVersion.LEGACY, CombatVersion.fromLegacy(true));
        assertSame(CombatVersion.MODERN, CombatVersion.fromLegacy(false));
        assertTrue(CombatVersion.LEGACY.legacy());
        assertFalse(CombatVersion.LEGACY.modern());
        assertTrue(CombatVersion.MODERN.modern());
    }

    @Test
    void fluidLevelWithoutPropertyIsFull() {
        assertEquals(8, FluidUtil.getLevel(Block.STONE));
        assertEquals(1.0, FluidUtil.getHeight(Block.STONE));
    }

    @Test
    void fluidLevelInvertsWaterLevelProperty() {
        assertEquals(8, FluidUtil.getLevel(Block.WATER.withProperty("level", "0")));
        assertEquals(1, FluidUtil.getLevel(Block.WATER.withProperty("level", "7")));
        assertEquals(8, FluidUtil.getLevel(Block.WATER.withProperty("level", "9")));
    }

    @Test
    void fluidHeightFollowsLevel() {
        assertEquals(0.25, FluidUtil.getHeight(Block.WATER.withProperty("level", "7")));
        assertEquals(0.5, FluidUtil.getHeight(Block.WATER.withProperty("level", "5")));
        assertEquals(0.75, FluidUtil.getHeight(Block.WATER.withProperty("level", "3")));
        assertEquals(1.0, FluidUtil.getHeight(Block.WATER.withProperty("level", "0")));
    }

    @Test
    void armorModifierKeysCoverFourSlots() {
        assertEquals(4, ModifierId.ARMOR_MODIFIERS.length);
        assertEquals("minecraft:armor.boots", ModifierId.ARMOR_MODIFIERS[0].asString());
        assertEquals("minecraft:armor.helmet", ModifierId.ARMOR_MODIFIERS[3].asString());
    }
}
