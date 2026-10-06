package net.swofty.type.skyblockgeneric.levels;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.levels.causes.FairySoulExchangeLevelCause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pins the two XP formulas that were previously wrong: Accessory Bag paid a flat 1 per accessory
// instead of per Magical Power, and Fairy Soul exchanges paid nothing at all.
class AccessoryAndFairySoulXPTest {

    @Test
    void magicalPowerMatchesRealHypixelsRarityTable() {
        assertEquals(3, Rarity.COMMON.getMagicalPower());
        assertEquals(5, Rarity.UNCOMMON.getMagicalPower());
        assertEquals(8, Rarity.RARE.getMagicalPower());
        assertEquals(12, Rarity.EPIC.getMagicalPower());
        assertEquals(16, Rarity.LEGENDARY.getMagicalPower());
        assertEquals(22, Rarity.MYTHIC.getMagicalPower());
        assertEquals(3, Rarity.SPECIAL.getMagicalPower());
        assertEquals(5, Rarity.VERY_SPECIAL.getMagicalPower());
    }

    @Test
    void everyRarityReportsAMagicalPowerValue() {
        for (Rarity rarity : Rarity.values()) {
            assertTrue(rarity.getMagicalPower() >= 0,
                    rarity + " has no Magical Power value, so accessories of it would award negative XP");
        }
    }

    @Test
    void magicalPowerRisesWithRarity() {
        Rarity[] ladder = {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC,
                Rarity.LEGENDARY, Rarity.MYTHIC};
        for (int i = 1; i < ladder.length; i++) {
            assertTrue(ladder[i].getMagicalPower() > ladder[i - 1].getMagicalPower(),
                    ladder[i] + " must be worth more Magical Power than " + ladder[i - 1]);
        }
    }

    @Test
    void fairySoulExchangesPayTenXPEach() {
        assertEquals(10, FairySoulExchangeLevelCause.XP_PER_EXCHANGE);
    }

    @Test
    void fairySoulSourceMaximumMatchesFiftySevenExchanges() {
        // Real Hypixel: +10 XP per 5-soul exchange, capped at 570 XP.
        assertEquals(570, SkyBlockXPSource.FAIRY_SOULS.getMaxXP());
        assertEquals(57, SkyBlockXPSource.FAIRY_SOULS.getMaxXP() / FairySoulExchangeLevelCause.XP_PER_EXCHANGE);
    }

    @Test
    void accessoryBagKeepsAccruingWithoutACeiling() {
        // Hypixel pays 1 XP per Accessory Power and sets no ceiling: the live Core tooltip shows
        // the task with a running total and no bar, and its seven capped siblings sum to the
        // whole 19,810 category total on their own.
        assertTrue(SkyBlockXPSource.ACCESSORY_BAG.isUncapped());
        assertEquals(19810, SkyBlockXPSource.getTotalXPFor(SkyBlockXPSource.Group.CORE));
    }
}
