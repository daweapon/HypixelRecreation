package dev.daweapon.type.skyblockgeneric.loottable;

import dev.daweapon.commons.skyblock.item.ItemType;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootRollTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private record Entry(ItemType item, int weight) {
    }

    @Test
    void aForcedItemWinsAZeroChanceRollOnce() {
        UUID player = UUID.randomUUID();
        ForcedDrops.force(player, ItemType.ENCHANTED_BOOK);
        assertTrue(LootRoll.roll(player, ItemType.ENCHANTED_BOOK, 0));
        assertFalse(LootRoll.roll(player, ItemType.ENCHANTED_BOOK, 0));
    }

    @Test
    void aForcedItemIsPickedFromAWeightedTable() {
        UUID player = UUID.randomUUID();
        ForcedDrops.force(player, ItemType.BRAIDED_GRIFFIN_FEATHER);
        List<Entry> table = List.of(new Entry(ItemType.GRIFFIN_FEATHER, 10_000), new Entry(ItemType.BRAIDED_GRIFFIN_FEATHER, 1));
        assertEquals(ItemType.BRAIDED_GRIFFIN_FEATHER, LootRoll.pick(player, table, Entry::item, Entry::weight, new Random(1)).item());
        assertTrue(ForcedDrops.pending(player).isEmpty());
    }

    @Test
    void anotherPlayersForcedDropIsUntouched() {
        UUID forced = UUID.randomUUID();
        ForcedDrops.force(forced, ItemType.ENCHANTED_BOOK);
        assertFalse(LootRoll.roll(UUID.randomUUID(), ItemType.ENCHANTED_BOOK, 0));
        assertEquals(1, ForcedDrops.pending(forced).size());
    }
}
