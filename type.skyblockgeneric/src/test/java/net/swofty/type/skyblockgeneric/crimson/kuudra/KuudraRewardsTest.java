package net.swofty.type.skyblockgeneric.crimson.kuudra;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraConfig;
import net.swofty.type.skyblockgeneric.crimson.kuudra.data.KuudraLootTable;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraReward;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraRewards;
import net.swofty.type.skyblockgeneric.crimson.kuudra.reward.KuudraRun;
import net.swofty.type.skyblockgeneric.loottable.ForcedDrops;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KuudraRewardsTest {
    private static final KuudraConfig CONFIG = KuudraConfigTest.CONFIG;
    private static final Set<String> NOT_YET_ITEMS = Set.of("AURORA_STAFF", "SHARD_WITHER_SPECTRE", "KUUDRA_SKULL_TRAVEL_SCROLL");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void everyLootItemExistsExceptTheKnownMissingOnes() {
        KuudraLootTable loot = CONFIG.loot();
        for (KuudraTier tier : KuudraTier.values()) {
            for (KuudraLootTable.Entry entry : concat(loot.primary(), loot.secondary())) {
                if (entry.weight(tier) <= 0 || entry.exists(tier)) continue;
                assertTrue(NOT_YET_ITEMS.contains(entry.itemId(tier)), "Unknown Kuudra loot item " + entry.itemId(tier));
            }
        }
        assertEquals(20 + 28, loot.primary().size());
    }

    @Test
    void armourDropsInTheRunTiersVariant() {
        KuudraLootTable.Entry helmet = CONFIG.loot().primary().getFirst();
        assertEquals("CRIMSON_HELMET", helmet.itemId(KuudraTier.BASIC));
        assertEquals("INFERNAL_CRIMSON_HELMET", helmet.itemId(KuudraTier.INFERNAL));
        for (KuudraTier tier : KuudraTier.values()) {
            CONFIG.loot().primary().stream().filter(KuudraLootTable.Entry::tieredArmor)
                    .forEach(entry -> assertTrue(entry.exists(tier), entry.itemId(tier)));
        }
    }

    @Test
    void tierOnlyRowsStayOutOfLowerTiers() {
        List<KuudraLootTable.Entry> basic = KuudraLootTable.candidates(CONFIG.loot().primary(), KuudraTier.BASIC, true);
        assertFalse(basic.stream().anyMatch(entry -> "TORMENTOR".equals(entry.itemId())));
        assertFalse(basic.stream().anyMatch(entry -> "SHARD_MAGMA_SLUG".equals(entry.itemId())));
        List<KuudraLootTable.Entry> infernal = KuudraLootTable.candidates(CONFIG.loot().secondary(), KuudraTier.INFERNAL, false);
        assertFalse(infernal.stream().anyMatch(entry -> "KUUDRA_MANDIBLE".equals(entry.itemId())));
        assertTrue(KuudraLootTable.candidates(CONFIG.loot().secondary(), KuudraTier.INFERNAL, true).stream()
                .anyMatch(entry -> "KUUDRA_MANDIBLE".equals(entry.itemId())));
    }

    @Test
    void bookLevelsFollowTheTier() {
        KuudraLootTable.Entry strong = CONFIG.loot().secondary().getFirst();
        assertEquals(KuudraReward.book("STRONG_MANA", 1), strong.reward(KuudraTier.BASIC));
        assertEquals(KuudraReward.book("STRONG_MANA", 5), strong.reward(KuudraTier.INFERNAL));
    }

    @Test
    void chestsHoldTheGuaranteedRewards() {
        SplittableRandom random = new SplittableRandom(7);
        for (KuudraTier tier : KuudraTier.values()) {
            for (int i = 0; i < 200; i++) {
                KuudraRun run = KuudraRewards.roll(UUID.randomUUID(), tier, true, CONFIG, random, 0);
                assertEquals(1, run.free().size());
                assertTrue(CONFIG.loot().freeEssence().get(tier).contains(run.free().getFirst().essence()));

                List<KuudraReward> paid = run.paid();
                assertEquals(5, paid.size(), tier + " " + paid);
                assertEquals(CONFIG.loot().guaranteed().get(tier).essence(), paid.getLast().essence());
                KuudraReward teeth = paid.get(2);
                assertEquals("KUUDRA_TEETH", teeth.itemId());
                KuudraLootTable.Guarantee guarantee = CONFIG.loot().guaranteed().get(tier);
                assertTrue(teeth.amount() >= guarantee.teethMin() && teeth.amount() <= guarantee.teethMax());
                KuudraReward kraken = paid.get(3);
                assertEquals("SHARD_KRAKEN", kraken.itemId());
                int floor = (int) Math.floor(CONFIG.tier(tier).krakenShards());
                assertTrue(kraken.amount() == floor || kraken.amount() == floor + 1);
            }
        }
    }

    @Test
    void forcedDropWinsThePrimaryRoll() {
        UUID player = UUID.randomUUID();
        ForcedDrops.force(player, ItemType.TORMENTOR);
        KuudraRun run = KuudraRewards.roll(player, KuudraTier.INFERNAL, false, CONFIG, new SplittableRandom(1), 0);
        assertEquals("TORMENTOR", run.paid().getFirst().itemId());
    }

    @Test
    void freeChestEssenceUsesTheNewerWikiValues() {
        assertEquals(List.of(80L, 100L, 120L), CONFIG.loot().freeEssence().get(KuudraTier.FIERY));
        assertEquals(List.of(200L), CONFIG.loot().freeEssence().get(KuudraTier.INFERNAL));
        assertEquals(2000, CONFIG.loot().guaranteed().get(KuudraTier.INFERNAL).essence());
    }

    private static List<KuudraLootTable.Entry> concat(List<KuudraLootTable.Entry> a, List<KuudraLootTable.Entry> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }
}
