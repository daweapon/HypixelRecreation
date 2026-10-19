package dev.daweapon.type.theend.dragon;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DragonRulesTest {

    @Test
    void placementBaseFollowsTheWikiTable() {
        int[] expected = {300, 250, 200, 125, 110, 100, 100, 100, 90, 90, 80, 80, 70, 70};
        for (int i = 0; i < expected.length; i++) assertEquals(expected[i], LootQuality.placementBase(i + 1, 1000));
        assertEquals(70, LootQuality.placementBase(26, 2));
        assertEquals(10, LootQuality.placementBase(26, 0.5));
    }

    @Test
    void qualityAddsEyesAndDamageRatio() {
        assertEquals(300 + 800 + 100, LootQuality.quality(1, 5_000_000, 5_000_000, 8));
        assertEquals(250 + 400 + 50, LootQuality.quality(2, 2_500_000, 5_000_000, 4));
    }

    @Test
    void guaranteedPearlsFollowPlacement() {
        assertEquals(30, LootQuality.enderPearls(1, 10));
        assertEquals(7, LootQuality.enchantedEnderPearls(1));
        assertEquals(15, LootQuality.enderPearls(9, 10));
        assertEquals(12, LootQuality.enderPearls(15, 10));
        assertEquals(1, LootQuality.enchantedEnderPearls(15));
        assertEquals(10, LootQuality.enderPearls(22, 10));
        assertEquals(0, LootQuality.enchantedEnderPearls(22));
        assertEquals(8, LootQuality.enderPearls(40, 5));
        assertEquals(5, LootQuality.enderPearls(40, 0.2));
    }

    @Test
    void fragmentsAreCappedAndCostQuality() {
        assertEquals(0, LootQuality.fragments(21));
        assertEquals(3, LootQuality.fragments(70));
        assertEquals(32, LootQuality.fragments(5000));
    }

    @Test
    void superiorChanceRisesWithRecombobulatedEyes() {
        assertEquals(4, DragonVariant.superiorChance(0));
        assertEquals(12.5, DragonVariant.superiorChance(1));
        assertEquals(50, DragonVariant.superiorChance(4));
        assertEquals(100, DragonVariant.superiorChance(8));
    }

    @Test
    void eightRecombobulatedEyesAlwaysSummonSuperior() {
        Random random = new Random(1);
        for (int i = 0; i < 200; i++) assertEquals(DragonVariant.SUPERIOR, DragonVariant.roll(8, random));
    }

    @Test
    void withoutRecombobulatedEyesSuperiorIsRare() {
        Random random = new Random(7);
        int superior = 0;
        for (int i = 0; i < 20_000; i++) if (DragonVariant.roll(0, random).superior()) superior++;
        assertTrue(superior > 600 && superior < 1000, "superior count " + superior);
    }

    @Test
    void variantStatsMatchTheWikiTable() {
        assertEquals(15_000_000, DragonVariant.OLD.health());
        assertEquals(225, DragonVariant.YOUNG.speed());
        assertEquals(2200, DragonVariant.WISE.damage());
        assertEquals(12_000_000, DragonVariant.SUPERIOR.health());
        assertEquals(10, DragonVariant.SUPERIOR.essenceReward());
        assertEquals(5, DragonVariant.STRONG.essenceReward());
        assertEquals(50, DragonVariant.SUPERIOR.firstKillXp());
    }

    @Test
    void ledgerRequiresAllEightFramesAndOwnersWithdraw() {
        EyeLedger ledger = new EyeLedger();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        for (int i = 0; i < 7; i++) assertTrue(ledger.place(i, i < 3 ? a : b, i == 0));
        assertFalse(ledger.complete());
        assertFalse(ledger.place(0, b, false));
        assertTrue(ledger.place(7, a, true));
        assertTrue(ledger.complete());
        assertEquals(2, ledger.recombobulated());
        assertEquals(4, ledger.eyesByPlayer().get(a));
    }

    @Test
    void withdrawIsOwnerOnlyAndRateLimited() {
        EyeLedger ledger = new EyeLedger();
        UUID owner = UUID.randomUUID();
        ledger.place(0, owner, false);
        ledger.place(1, owner, false);
        assertTrue(ledger.withdraw(0, UUID.randomUUID(), 0).isEmpty());
        assertTrue(ledger.withdraw(0, owner, 1_000).isPresent());
        assertTrue(ledger.withdraw(1, owner, 2_000).isEmpty());
        assertTrue(ledger.withdraw(1, owner, 4_001).isPresent());
        assertEquals(0, ledger.count());
    }

    @Test
    void lootNeverGivesTwoMajorDropsAndSpendsQuality() {
        DragonLoot.Roller always = (item, chance) -> true;
        List<DragonLoot.Reward> rewards = DragonLoot.plan(DragonVariant.STRONG, 1200, 8, 1, 5_000_000, always);
        assertEquals(1, rewards.stream().filter(DragonLoot.Reward::major).filter(reward -> reward.item() != ItemType.get("DYE_PEARLESCENT")).count());
        assertTrue(rewards.stream().anyMatch(reward -> reward.item() == ItemType.ENDER_PEARL && reward.amount() == 30));
    }

    @Test
    void lowQualityCannotWinMajorDrops() {
        DragonLoot.Roller always = (item, chance) -> true;
        List<DragonLoot.Reward> rewards = DragonLoot.plan(DragonVariant.STRONG, 200, 0, 30, 5, always);
        assertTrue(rewards.stream().noneMatch(reward -> reward.major() && reward.item() != ItemType.get("DYE_PEARLESCENT")));
    }
}
