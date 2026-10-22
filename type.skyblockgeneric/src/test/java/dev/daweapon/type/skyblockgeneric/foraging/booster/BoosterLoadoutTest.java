package dev.daweapon.type.skyblockgeneric.foraging.booster;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoosterLoadoutTest {
    @Test
    void roundTripsThroughItsSerializedForm() {
        BoosterLoadout loadout = BoosterLoadout.EMPTY.with(BoosterKind.SWEEP, 1).with(BoosterKind.FORAGING_FORTUNE, 2);
        BoosterLoadout parsed = BoosterLoadout.parse(loadout.serialize());
        assertEquals(1, parsed.level(BoosterKind.SWEEP));
        assertEquals(2, parsed.level(BoosterKind.FORAGING_FORTUNE));
        assertEquals(0, parsed.level(BoosterKind.FIGHTING));
        assertEquals(2, parsed.count());
    }

    @Test
    void ignoresGarbage() {
        assertEquals(0, BoosterLoadout.parse("").count());
        assertEquals(0, BoosterLoadout.parse(null).count());
        assertEquals(1, BoosterLoadout.parse("NOPE:1;SWEEP:2;SWEEP").count());
    }

    @Test
    void oneBoosterPerKindAndUncommonNeedsCommon() {
        BoosterLoadout empty = BoosterLoadout.EMPTY;
        assertTrue(empty.accepts(BoosterKind.SWEEP, BoosterKind.COMMON));
        assertFalse(empty.accepts(BoosterKind.SWEEP, BoosterKind.UNCOMMON));
        BoosterLoadout common = empty.with(BoosterKind.SWEEP, BoosterKind.COMMON);
        assertFalse(common.accepts(BoosterKind.SWEEP, BoosterKind.COMMON));
        assertTrue(common.accepts(BoosterKind.SWEEP, BoosterKind.UNCOMMON));
        assertFalse(common.with(BoosterKind.SWEEP, BoosterKind.UNCOMMON).accepts(BoosterKind.SWEEP, BoosterKind.UNCOMMON));
    }

    @Test
    void slotsComeFromTheBoosterItemList() {
        assertEquals(BoosterSlot.AXE, BoosterSlot.of("FIG_AXE"));
        assertEquals(BoosterSlot.ARMOR, BoosterSlot.of("HELIX_ARMOR_BOOTS"));
        assertEquals(BoosterSlot.EQUIPMENT, BoosterSlot.of("TORRHUS_BELT"));
        assertNull(BoosterSlot.of("DIAMOND_AXE"));
        assertNull(BoosterSlot.of(null));
        int total = 0;
        for (BoosterSlot slot : BoosterSlot.values()) total += slot.items().size();
        assertEquals(28, total);
    }

    @Test
    void fullUncommonSetMatchesTheWikiTrivia() {
        double sweep = 0;
        double fortune = 0;
        double foragingWisdom = 0;
        double huntingWisdom = 0;
        double damage = 0;
        int[] counts = {1, 4, 4};
        for (BoosterSlot slot : BoosterSlot.values()) {
            sweep += counts[slot.ordinal()] * BoosterKind.SWEEP.amount(BoosterKind.UNCOMMON, slot);
            fortune += counts[slot.ordinal()] * BoosterKind.FORAGING_FORTUNE.amount(BoosterKind.UNCOMMON, slot);
            foragingWisdom += counts[slot.ordinal()] * BoosterKind.FORAGING_WISDOM.amount(BoosterKind.UNCOMMON, slot);
            huntingWisdom += counts[slot.ordinal()] * BoosterKind.HUNTING_WISDOM.amount(BoosterKind.UNCOMMON, slot);
            damage += counts[slot.ordinal()] * BoosterKind.FIGHTING.amount(BoosterKind.UNCOMMON, slot);
        }
        assertEquals(38, sweep);
        assertEquals(80, fortune);
        assertEquals(15, foragingWisdom);
        assertEquals(15, huntingWisdom);
        assertEquals(38, damage);
    }

    @Test
    void commonAmountsPerSlot() {
        assertEquals(10, BoosterKind.FORAGING_FORTUNE.amount(BoosterKind.COMMON, BoosterSlot.AXE));
        assertEquals(5, BoosterKind.FORAGING_FORTUNE.amount(BoosterKind.COMMON, BoosterSlot.ARMOR));
        assertEquals(2, BoosterKind.FORAGING_FORTUNE.amount(BoosterKind.COMMON, BoosterSlot.EQUIPMENT));
        assertEquals(0.5, BoosterKind.FORAGING_WISDOM.amount(BoosterKind.COMMON, BoosterSlot.EQUIPMENT));
        assertEquals(0, BoosterKind.SWEEP.amount(0, BoosterSlot.AXE));
    }

    @Test
    void itemIdsMapBackToKindAndRarity() {
        assertEquals(BoosterKind.HUNTING_WISDOM, BoosterKind.ofItem("HUNTING_WISDOM_BOOSTER_COMMON"));
        assertEquals(BoosterKind.UNCOMMON, BoosterKind.levelOfItem("SWEEP_BOOSTER_UNCOMMON"));
        assertEquals(BoosterKind.COMMON, BoosterKind.levelOfItem("SWEEP_BOOSTER"));
        assertEquals(0, BoosterKind.levelOfItem("STONE"));
        assertEquals("FORAGING_FORTUNE_BOOSTER_UNCOMMON", BoosterKind.FORAGING_FORTUNE.itemId(BoosterKind.UNCOMMON));
    }
}
