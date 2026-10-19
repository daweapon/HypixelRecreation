package dev.daweapon.type.theend.altar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DraconicAltarRulesTest {

    @Test
    void dragonArmorEssenceByPiece() {
        assertEquals(25, DraconicAltarRules.of("STRONG_DRAGON_HELMET").orElseThrow().essence());
        assertEquals(40, DraconicAltarRules.of("OLD_DRAGON_CHESTPLATE").orElseThrow().essence());
        assertEquals(35, DraconicAltarRules.of("WISE_DRAGON_LEGGINGS").orElseThrow().essence());
        assertEquals(20, DraconicAltarRules.of("YOUNG_DRAGON_BOOTS").orElseThrow().essence());
    }

    @Test
    void superiorArmorPaysDouble() {
        assertEquals(50, DraconicAltarRules.of("SUPERIOR_DRAGON_HELMET").orElseThrow().essence());
        assertEquals(80, DraconicAltarRules.of("SUPERIOR_DRAGON_CHESTPLATE").orElseThrow().essence());
    }

    @Test
    void enderGearAndBowPayThree() {
        for (String id : new String[]{"END_HELMET", "ENDER_CLOAK", "END_STONE_BOW", "OBSIDIAN_CHESTPLATE"}) {
            assertEquals(3, DraconicAltarRules.of(id).orElseThrow().essence(), id);
        }
        assertEquals(60, DraconicAltarRules.of("ASPECT_OF_THE_DRAGON").orElseThrow().essence());
    }

    @Test
    void bonusEntriesSumToOneHundredWithTheWikiBonusChance() {
        for (String id : new String[]{"STRONG_DRAGON_HELMET", "ASPECT_OF_THE_DRAGON", "ENDER_BELT", "END_STONE_BOW", "OBSIDIAN_CHESTPLATE"}) {
            var entries = DraconicAltarRules.of(id).orElseThrow().withNothing();
            assertEquals(100, entries.stream().mapToDouble(DraconicAltarRules.Bonus::weight).sum(), 1e-9, id);
            assertEquals(55, entries.getLast().weight(), 1e-9);
        }
    }

    @Test
    void unrelatedItemsCannotBeSacrificed() {
        assertTrue(DraconicAltarRules.of("DIAMOND_SWORD").isEmpty());
    }
}
