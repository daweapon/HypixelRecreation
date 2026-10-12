package net.swofty.type.skyblockgeneric.item.tool;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxeLogDropsTest {
    @Test
    void logsMapToTheirPlanks() {
        assertEquals(ItemType.OAK_PLANKS, AxeLogDrops.planksFor(Material.OAK_LOG).orElseThrow());
        assertEquals(ItemType.DARK_OAK_PLANKS, AxeLogDrops.planksFor(Material.DARK_OAK_LOG).orElseThrow());
        assertTrue(AxeLogDrops.planksFor(Material.STONE).isEmpty());
    }

    @Test
    void promisingAxeEarnsEfficiencyAtFiftyBlocks() {
        assertFalse(AxeLogDrops.promisingAxeEarnsEfficiency(49));
        assertTrue(AxeLogDrops.promisingAxeEarnsEfficiency(50));
    }
}
