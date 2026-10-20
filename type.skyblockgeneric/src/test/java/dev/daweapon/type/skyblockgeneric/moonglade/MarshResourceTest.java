package dev.daweapon.type.skyblockgeneric.moonglade;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.hotf.HotfRegistry;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynResource;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarshResourceTest {
    @Test
    void bushesYieldOneLushlilacAndPickleYieldsOneToFourSeaLumies() {
        assertEquals(ItemType.LUSHLILAC, MarshResource.LUSHLILAC.getItem());
        assertEquals(1, MarshResource.LUSHLILAC.getMinimum());
        assertEquals(1, MarshResource.LUSHLILAC.getMaximum());
        assertEquals(25, MarshResource.LUSHLILAC.getForagingXpPerItem(), 1e-9);
        assertEquals(100, MarshResource.LUSHLILAC.getForestWhispers());
        assertEquals(StarlynResource.LUSHLILAC, MarshResource.LUSHLILAC.getContestResource());
        assertEquals(ItemType.SEA_LUMIES, MarshResource.SEA_LUMIES.getItem());
        assertEquals(1, MarshResource.SEA_LUMIES.getMinimum());
        assertEquals(4, MarshResource.SEA_LUMIES.getMaximum());
        assertEquals(3.75, MarshResource.SEA_LUMIES.getForagingXpPerItem(), 1e-9);
        assertEquals(15, MarshResource.SEA_LUMIES.getForagingXpPerItem() * MarshResource.SEA_LUMIES.getMaximum(), 1e-9);
        for (int i = 0; i < 200; i++) {
            int amount = MarshResource.SEA_LUMIES.rollAmount();
            assertTrue(amount >= 1 && amount <= 4);
        }
    }

    @Test
    void blocksMapToTheirResourceAndDepletedStates() {
        assertSame(MarshResource.LUSHLILAC, MarshResource.ofBlock(Block.FLOWERING_AZALEA));
        assertSame(MarshResource.SEA_LUMIES, MarshResource.ofBlock(Block.SEA_PICKLE.withProperty("pickles", "3")));
        assertNull(MarshResource.ofBlock(Block.AZALEA));
        assertEquals(Block.AZALEA, MarshResource.LUSHLILAC.depletedBlock(Block.FLOWERING_AZALEA));
        assertEquals(Block.WATER, MarshResource.SEA_LUMIES.depletedBlock(Block.SEA_PICKLE.withProperty("waterlogged", "true")));
        assertEquals(Block.AIR, MarshResource.SEA_LUMIES.depletedBlock(Block.SEA_PICKLE.withProperty("waterlogged", "false")));
    }

    @Test
    void collectorDoublesUpToOneHundredPercentAndYummyIsInTheCatalogue() throws Exception {
        HotfRegistry registry = HotfRegistry.load(Path.of("../configuration/skyblock/hotf"));
        assertTrue(registry.perk(MarshGathering.COLLECTOR_PERK).available());
        assertEquals(2, registry.perk(MarshGathering.COLLECTOR_PERK).value(1), 1e-9);
        assertEquals(100, registry.perk(MarshGathering.COLLECTOR_PERK).value(50), 1e-9);
        String shards = Files.readString(Path.of("../configuration/skyblock/shards.yml"));
        assertTrue(shards.contains("\"id\": \"" + MarshGathering.YUMMY_ATTRIBUTE + "\""));
    }

    @Test
    void helixFortuneIsAForagingStatistic() {
        assertEquals("Helix Fortune", ItemStatistic.HELIX_FORTUNE.getDisplayName());
    }
}
