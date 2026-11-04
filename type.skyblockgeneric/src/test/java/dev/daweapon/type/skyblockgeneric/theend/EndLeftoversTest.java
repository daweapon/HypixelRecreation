package dev.daweapon.type.skyblockgeneric.theend;

import net.minestom.server.MinecraftServer;
import dev.daweapon.commons.skyblock.item.Rarity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndLeftoversTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void enderNodeTablesMatchTheWikiWeights() {
        assertEquals(1051, EnderNodeLoot.END_STONE.stream().mapToInt(EnderNodeLoot.Entry::weight).sum());
        assertEquals(1142, EnderNodeLoot.OBSIDIAN.stream().mapToInt(EnderNodeLoot.Entry::weight).sum());
    }

    @Test
    void stonkRemovesOnlyTheEndermiteOutcome() {
        for (EnderNodeType type : EnderNodeType.values()) {
            List<EnderNodeLoot.Entry> plain = EnderNodeLoot.table(type, false);
            List<EnderNodeLoot.Entry> stonk = EnderNodeLoot.table(type, true);
            assertTrue(plain.stream().anyMatch(EnderNodeLoot.Entry::endermites));
            assertFalse(stonk.stream().anyMatch(EnderNodeLoot.Entry::endermites));
            assertEquals(plain.size() - 1, stonk.size());
        }
    }

    @Test
    void protectorStagesFollowTheZealotCounter() {
        assertEquals(ProtectorProgress.Stage.RESTING, ProtectorProgress.stageFor(0));
        assertEquals(ProtectorProgress.Stage.DORMANT, ProtectorProgress.stageFor(1_000));
        assertEquals(ProtectorProgress.Stage.AWAKENING, ProtectorProgress.stageFor(4_999));
        assertEquals(ProtectorProgress.Stage.SUMMONED, ProtectorProgress.stageFor(5_000));
    }

    @Test
    void protectorQualityUsesPlacementZealotsAndDamageShare() {
        assertEquals(200 + 100 + 50, EndStoneProtectorLoot.quality(1, 1_000, 1_000, 250));
        assertEquals(175 + 10 + 25, EndStoneProtectorLoot.quality(2, 500, 1_000, 10));
        assertEquals(70, EndStoneProtectorLoot.quality(13, 0.5 + 0.6, 1_000_000, 0));
        assertEquals(10, EndStoneProtectorLoot.quality(13, 0.5, 1_000_000, 0));
    }

    @Test
    void placementGuaranteesFollowTheWikiTable() {
        assertEquals(10, EndStoneProtectorLoot.enchantedEndStone(1));
        assertEquals(7, EndStoneProtectorLoot.enchantedEndStone(2));
        assertEquals(2, EndStoneProtectorLoot.enchantedEndStone(8));
        assertEquals(1, EndStoneProtectorLoot.enchantedEndStone(9));
        assertEquals(2, EndStoneProtectorLoot.crystalFragments(1));
        assertEquals(1, EndStoneProtectorLoot.crystalFragments(5));
        assertEquals(0, EndStoneProtectorLoot.crystalFragments(6));
    }

    @Test
    void eachUniqueDropsOncePerBossAndCostsQuality() {
        Set<String> claimed = new HashSet<>();
        List<EndStoneProtectorLoot.Reward> first = EndStoneProtectorLoot.plan(1, 1_000, 1_000, 100, claimed,
                (item, chance) -> !item.equals("DYE_PEARLESCENT"));
        assertTrue(first.stream().anyMatch(reward -> reward.itemId().equals("PET_ITEM_TIER_BOOST_DROP")));
        assertFalse(first.stream().anyMatch(reward -> reward.itemId().equals("GOLEM_PET")));
        List<EndStoneProtectorLoot.Reward> second = EndStoneProtectorLoot.plan(2, 1_000, 1_000, 100, claimed,
                (item, chance) -> !item.equals("DYE_PEARLESCENT"));
        assertFalse(second.stream().anyMatch(reward -> reward.itemId().equals("PET_ITEM_TIER_BOOST_DROP")));
        assertTrue(second.stream().anyMatch(reward -> reward.itemId().equals("GOLEM_PET")
                && reward.rarity() == Rarity.LEGENDARY));
        assertTrue(second.stream().anyMatch(reward -> reward.itemId().equals("ENDSTONE_ROSE")));
    }
}
