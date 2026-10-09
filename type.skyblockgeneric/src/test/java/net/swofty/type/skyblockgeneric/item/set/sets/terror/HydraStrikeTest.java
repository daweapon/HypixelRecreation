package net.swofty.type.skyblockgeneric.item.set.sets.terror;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.handlers.bow.ShortBowVolley;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HydraStrikeTest {
    private static final long FOUR_PIECE_DECAY = TerrorTier.decayMillis(4);

    @Test
    void stacksComeAtMostOncePerFifthOfASecond() {
        HydraStrikeStacks stacks = new HydraStrikeStacks();
        assertTrue(stacks.gain(0, FOUR_PIECE_DECAY));
        assertFalse(stacks.gain(0, FOUR_PIECE_DECAY));
        assertFalse(stacks.gain(199, FOUR_PIECE_DECAY));
        assertEquals(1, stacks.stacks(199, FOUR_PIECE_DECAY));
        assertTrue(stacks.gain(200, FOUR_PIECE_DECAY));
        assertEquals(2, stacks.stacks(200, FOUR_PIECE_DECAY));
    }

    @Test
    void stacksCapAtTen() {
        HydraStrikeStacks stacks = new HydraStrikeStacks();
        for (int i = 0; i < 15; i++) stacks.gain(i * 200L, FOUR_PIECE_DECAY);
        assertEquals(HydraStrikeStacks.MAX_STACKS, stacks.stacks(15 * 200L, FOUR_PIECE_DECAY));
    }

    @Test
    void oneStackFallsOffPerDecayWindowWithoutAGain() {
        long decay = TerrorTier.decayMillis(2);
        assertEquals(4000, decay);
        HydraStrikeStacks stacks = new HydraStrikeStacks();
        for (int i = 0; i < 10; i++) stacks.gain(i * 200L, decay);
        long last = 9 * 200L;
        assertEquals(10, stacks.stacks(last + decay - 1, decay));
        assertEquals(9, stacks.stacks(last + decay, decay));
        assertEquals(8, stacks.stacks(last + 2 * decay, decay));
        assertTrue(stacks.gain(last + 2 * decay + 100, decay));
        assertEquals(9, stacks.stacks(last + 3 * decay, decay));
        assertEquals(0, stacks.stacks(last + 100 * decay, decay));
    }

    @Test
    void decayWindowGrowsWithPieces() {
        assertEquals(4000, TerrorTier.decayMillis(2));
        assertEquals(7000, TerrorTier.decayMillis(3));
        assertEquals(10000, TerrorTier.decayMillis(4));
    }

    @Test
    void twoPiecesOfAnyTierActivateItAtTheLowestTier() {
        assertNull(HydraStrike.wear(List.of(ItemType.INFERNAL_TERROR_HELMET)));
        assertNull(HydraStrike.wear(Arrays.asList(ItemType.INFERNAL_TERROR_HELMET, null, ItemType.CRIMSON_BOOTS)));
        HydraStrike.Wear mixed = HydraStrike.wear(List.of(ItemType.INFERNAL_TERROR_HELMET, ItemType.HOT_TERROR_BOOTS, ItemType.FIERY_TERROR_LEGGINGS));
        assertEquals(TerrorTier.HOT, mixed.tier());
        assertEquals(3, mixed.pieces());
    }

    @Test
    void maxedInfernalTerrorNumbers() {
        HydraStrike.Wear infernal = HydraStrike.wear(TerrorTier.INFERNAL.pieces());
        assertEquals(4, infernal.pieces());
        assertEquals(2.5, infernal.damageMultiplier(10), 1e-9);
        assertEquals(1.3, infernal.arrowSpeedMultiplier(10), 1e-9);
        assertEquals(0.6, infernal.extraArrowFactor(10), 1e-9);
        assertEquals(0, infernal.extraArrowFactor(9), 1e-9);
        assertEquals(1, infernal.damageMultiplier(0), 1e-9);
    }

    @Test
    void perStackDamageScalesWithPiecesAndTier() {
        assertEquals(2, TerrorTier.BASIC.damagePerStack(2));
        assertEquals(6, TerrorTier.BASIC.damagePerStack(4));
        assertEquals(8.25, TerrorTier.HOT.damagePerStack(4));
        assertEquals(10.5, TerrorTier.BURNING.damagePerStack(4));
        assertEquals(8.5, TerrorTier.FIERY.damagePerStack(3));
        assertEquals(5, TerrorTier.INFERNAL.damagePerStack(2));
        assertEquals(0.2, TerrorTier.BASIC.extraArrowDamage() / 100, 1e-9);
    }

    @Test
    void tenStacksAddTwoArrows() {
        assertEquals(HydraStrike.EXTRA_ARROWS, ShortBowVolley.hydraOffsets(HydraStrike.EXTRA_ARROWS).size());
    }

    @Test
    void everyTerrorTierIsARegisteredTieredSetWithLiveLore() {
        for (TerrorTier tier : TerrorTier.values()) {
            for (ItemType piece : tier.pieces()) {
                ArmorSetRegistry registry = ArmorSetRegistry.getArmorSet(piece);
                assertTrue(registry != null && HydraStrikeArmorSet.class.isAssignableFrom(registry.getClazz()), piece.name());
            }
        }
        List<String> lore = new HydraStrikeArmorSet.Infernal().getDescription(4);
        assertTrue(lore.stream().anyMatch(line -> line.contains("+15% Damage")));
        assertTrue(lore.stream().anyMatch(line -> line.contains("10s of not gaining")));
        assertTrue(lore.stream().anyMatch(line -> line.contains("60%")));
        assertTrue(new HydraStrikeArmorSet.Hot().getDescription(2).stream().anyMatch(line -> line.contains("+2.75% Damage")));
    }
}
