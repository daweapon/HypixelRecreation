package dev.daweapon.type.skyblockgeneric.moonglade.flames;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueFlamesTrialsTest {
    @Test
    void damageTableMatchesTheWiki() {
        assertEquals(1, BlueFlamesTrials.damagePerSecond(1));
        assertEquals(10, BlueFlamesTrials.totalDamage(1));
        assertEquals(160, BlueFlamesTrials.totalDamage(3));
        assertEquals(7_460, BlueFlamesTrials.totalDamage(10));
        assertEquals(104_460, BlueFlamesTrials.totalDamage(22));
        assertEquals(304_460, BlueFlamesTrials.totalDamage(30));
    }

    @Test
    void firstBadgeComesFromTheSecondTrial() {
        assertEquals(0, BlueFlamesTrials.badgeLevelAfter(0));
        assertEquals(0, BlueFlamesTrials.badgeLevelAfter(1));
        assertEquals(1, BlueFlamesTrials.badgeLevelAfter(2));
        assertEquals(29, BlueFlamesTrials.badgeLevelAfter(30));
        assertEquals("SOUL_CAMPFIRE_TALISMAN_7", BlueFlamesTrials.badgeId(7));
        assertEquals(7, BlueFlamesTrials.levelOfBadge("SOUL_CAMPFIRE_TALISMAN_7"));
        assertEquals(0, BlueFlamesTrials.levelOfBadge("DIAMOND"));
    }

    @Test
    void rarityBoundariesNeedMaterials() {
        assertNull(BlueFlamesTrials.upgradeInto(3));
        assertEquals(new BlueFlamesTrials.Upgrade("ENCHANTED_FIG_LOG", 64), BlueFlamesTrials.upgradeInto(4));
        assertEquals(new BlueFlamesTrials.Upgrade("ENCHANTED_FIG_LOG", 256), BlueFlamesTrials.upgradeInto(8));
        assertEquals(new BlueFlamesTrials.Upgrade("ENCHANTED_MANGROVE_LOG", 256), BlueFlamesTrials.upgradeInto(13));
        assertEquals(new BlueFlamesTrials.Upgrade("ENCHANTED_MANGROVE_LOG", 512), BlueFlamesTrials.upgradeInto(21));
    }

    @Test
    void raritiesFollowTheBadgeLevels() {
        assertEquals("Initiate", BlueFlamesTrials.rarityOf(1));
        assertEquals("Initiate", BlueFlamesTrials.rarityOf(3));
        assertEquals("Adept", BlueFlamesTrials.rarityOf(4));
        assertEquals("Cultist", BlueFlamesTrials.rarityOf(8));
        assertEquals("Scion", BlueFlamesTrials.rarityOf(20));
        assertEquals("God", BlueFlamesTrials.rarityOf(21));
        assertEquals("God", BlueFlamesTrials.rarityOf(29));
    }

    @Test
    void countsTrialsUntilTheNextRarity() {
        assertEquals(3, BlueFlamesTrials.trialsUntilRarityUp(2));
        assertEquals(1, BlueFlamesTrials.trialsUntilRarityUp(4));
        assertEquals(1, BlueFlamesTrials.trialsUntilRarityUp(8));
        assertEquals(0, BlueFlamesTrials.trialsUntilRarityUp(30));
    }

    @Test
    void defenseReducesTheBurnAndHugeBurnsFailInstantly() {
        assertEquals(50, BlueFlamesTrials.damageAfterDefense(100, 100), 1e-9);
        assertEquals(100, BlueFlamesTrials.damageAfterDefense(100, 0), 1e-9);
        assertTrue(BlueFlamesTrials.instantFail(500, 400));
        assertFalse(BlueFlamesTrials.instantFail(400, 400));
    }
}
