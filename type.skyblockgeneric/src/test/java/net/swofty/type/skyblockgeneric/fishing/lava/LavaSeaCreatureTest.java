package net.swofty.type.skyblockgeneric.fishing.lava;

import net.swofty.commons.skyblock.item.ItemType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LavaSeaCreatureTest {

    @Test
    void sixteenCrimsonLavaSeaCreatures() {
        assertEquals(16, LavaSeaCreature.values().length);
        assertEquals(100_000_000D, LavaSeaCreature.LORD_JAWBUS.getHealth());
        assertEquals(597, LavaSeaCreature.LORD_JAWBUS.getLevel());
    }

    @Test
    void everyDropIsARealItem() {
        for (LavaSeaCreature creature : LavaSeaCreature.values()) {
            for (LavaDrop drop : creature.getDrops()) {
                assertNotNull(ItemType.get(drop.itemId()), creature + " drops unknown " + drop.itemId());
            }
        }
    }

    @Test
    void everyCreatureCanDropCarmineAtItsRarityOdds() {
        assertTrue(Arrays.stream(LavaSeaCreature.values()).allMatch(creature ->
                creature.getDrops().stream().anyMatch(drop -> drop.itemId().equals("DYE_CARMINE"))));
        double jawbus = LavaSeaCreature.LORD_JAWBUS.getDrops().getLast().chancePercent();
        assertEquals(100D / 50_000, jawbus, 1e-12);
    }

    @Test
    void lordJawbusIsNeverCorrupted() {
        assertTrue(!LavaSeaCreature.LORD_JAWBUS.canBeCorrupted());
        assertTrue(LavaSeaCreature.THUNDER.canBeCorrupted());
    }
}
