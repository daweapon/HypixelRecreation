package dev.daweapon.type.skyblockgeneric.miningevent;

import dev.daweapon.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiningEventConfigTest {
    private static final MiningEventConfig CONFIG = MiningEventConfig.load(new File("../configuration/skyblock/mining_events.yml"));

    @Test
    void everyEventHasAWeightAndDuration() {
        for (MiningEventType type : MiningEventType.values()) {
            assertTrue(CONFIG.weight(type) > 0, type.name());
            assertTrue(CONFIG.durationMillis(type) > 0, type.name());
        }
        assertEquals(20 * 60_000L, CONFIG.durationMillis(MiningEventType.BETTER_TOGETHER));
        assertEquals(5 * 60_000L, CONFIG.durationMillis(MiningEventType.GOBLIN_RAID));
    }

    @Test
    void passiveEventsRunInBothZonesAndActiveOnesOnlyInTheMines() {
        for (MiningEventType type : MiningEventType.values()) {
            assertTrue(type.runsIn(MiningEventZone.DWARVEN_MINES), type.name());
            assertEquals(type.kind() == MiningEventKind.PASSIVE, type.runsIn(MiningEventZone.CRYSTAL_HOLLOWS), type.name());
        }
    }

    @Test
    void rewardTablesMatchTheWiki() {
        MiningEventConfig.RewardTier raid = CONFIG.tier("rewards", "goblin_raid", "high_success");
        assertEquals(30_000, raid.miningXp());
        assertEquals(20_000, raid.mithrilPowder());
        assertEquals(2, raid.items());
        assertEquals(0, CONFIG.tier("rewards", "goblin_raid", "low_fail").miningXp());
        assertEquals(30_000, CONFIG.tier("rewards", "raffle", "winner").mithrilPowder());
        assertEquals(25, CONFIG.tier("rewards", "raffle", "participant").perKillPowder());
        assertEquals(9_500, CONFIG.tier("rewards", "mithril_gourmand", "high_fail").mithrilPowder());
    }

    @Test
    void miningItemPoolResolvesToRealItems() {
        assertEquals(5, CONFIG.strings("mining_items").size());
        for (String id : CONFIG.strings("mining_items")) {
            if (id.equals("FLOWSTATE_BOOK")) continue;
            assertNotNull(ItemType.get(id), id);
        }
    }

    @Test
    void everySiteIsDefined() {
        assertFalse(CONFIG.sites("mithril_gourmand", "sites").isEmpty());
        assertEquals(3, CONFIG.sites("raffle", "sites").size());
        assertEquals(7, CONFIG.sites("random", "fallen_star", "sites").size());
        assertEquals(4, CONFIG.sites("random", "powder_ghast", "sites").size());
        assertEquals("The Forge", CONFIG.singleSite("goblin_raid", "site").name());
    }
}
