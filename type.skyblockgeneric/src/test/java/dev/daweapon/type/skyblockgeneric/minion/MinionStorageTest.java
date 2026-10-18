package dev.daweapon.type.skyblockgeneric.minion;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorageLinks;
import dev.daweapon.type.skyblockgeneric.minion.storage.MinionStorageTier;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinionStorageTest {
    @Test
    void tiersAddThreeNineFifteenTwentyOneAndTwentySevenSlots() {
        assertEquals(List.of(3, 9, 15, 21, 27),
                List.of(MinionStorageTier.values()).stream().map(MinionStorageTier::slots).toList());
        assertEquals(15 * 64, MinionStorageTier.LARGE.capacityItems());
    }

    @Test
    void chestItemsMapToTheirTier() {
        assertEquals(Optional.of(MinionStorageTier.XXLARGE), MinionStorageTier.fromItem(ItemType.XXLARGE_ENCHANTED_CHEST));
        assertTrue(MinionStorageTier.fromItem(ItemType.COBBLESTONE_MINION).isEmpty());
    }

    @Test
    void linkOrderIsSouthWestNorthEast() {
        List<Point> order = MinionStorageLinks.linkOrder(new Vec(10, 64, 10));
        assertEquals(new Vec(10, 64, 11), order.get(0));
        assertEquals(new Vec(9, 64, 10), order.get(1));
        assertEquals(new Vec(10, 64, 9), order.get(2));
        assertEquals(new Vec(11, 64, 10), order.get(3));
    }

    @Test
    void firstAvailableSkipsMinionsThatAlreadyHaveAChest() {
        Point chest = new Vec(0, 70, 0);
        Optional<Point> linked = MinionStorageLinks.firstAvailable(chest,
                candidate -> candidate.blockZ() != 1 && candidate.blockX() != -1);
        assertEquals(Optional.of(new Vec(0, 70, -1)), linked);
        assertTrue(MinionStorageLinks.firstAvailable(chest, candidate -> false).isEmpty());
    }

    @Test
    void chestFacesItsMinion() {
        Point chest = new Vec(0, 70, 0);
        assertEquals("south", MinionStorageLinks.facingToward(chest, new Vec(0, 70, 1)));
        assertEquals("west", MinionStorageLinks.facingToward(chest, new Vec(-1, 70, 0)));
        assertEquals("north", MinionStorageLinks.facingToward(chest, new Vec(0, 70, -1)));
        assertEquals("east", MinionStorageLinks.facingToward(chest, new Vec(1, 70, 0)));
    }
}
