package dev.daweapon.type.skyblockgeneric.levels;

import dev.daweapon.commons.skyblock.item.Rarity;
import dev.daweapon.type.skyblockgeneric.levels.causes.FairySoulExchangeLevelCause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pins the two XP formulas that were previously wrong: Accessory Bag paid a flat 1 per accessory
// instead of per Accessory Power, and Fairy Soul exchanges paid nothing at all.
class AccessoryAndFairySoulXPTest {

    @Test
    void accessoryPowerMatchesRealHypixelsRarityTable() {
        assertEquals(3, Rarity.COMMON.getAccessoryPower());
        assertEquals(5, Rarity.UNCOMMON.getAccessoryPower());
        assertEquals(8, Rarity.RARE.getAccessoryPower());
        assertEquals(12, Rarity.EPIC.getAccessoryPower());
        assertEquals(16, Rarity.LEGENDARY.getAccessoryPower());
        assertEquals(22, Rarity.MYTHIC.getAccessoryPower());
        assertEquals(28, Rarity.DIVINE.getAccessoryPower());
        assertEquals(3, Rarity.SPECIAL.getAccessoryPower());
        assertEquals(5, Rarity.VERY_SPECIAL.getAccessoryPower());
    }

    @Test
    void everyRarityReportsAnAccessoryPowerValue() {
        for (Rarity rarity : Rarity.values()) {
            assertTrue(rarity.getAccessoryPower() >= 0,
                    rarity + " has no Accessory Power value, so accessories of it would award negative XP");
        }
    }

    @Test
    void accessoryPowerRisesWithRarity() {
        Rarity[] ladder = {Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC,
                Rarity.LEGENDARY, Rarity.MYTHIC};
        for (int i = 1; i < ladder.length; i++) {
            assertTrue(ladder[i].getAccessoryPower() > ladder[i - 1].getAccessoryPower(),
                    ladder[i] + " must be worth more Accessory Power than " + ladder[i - 1]);
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
        // whole 19,822 category total on their own.
        assertTrue(SkyBlockXPSource.ACCESSORY_BAG.isUncapped());
        assertEquals(19822, SkyBlockXPSource.getTotalXPFor(SkyBlockXPSource.Group.CORE));
    }
}
