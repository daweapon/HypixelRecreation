package dev.daweapon.type.catacombs.run;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatacombsItemsTest {
    private static final ItemStack SWORD = ItemStack.of(Material.DIAMOND_SWORD);

    @BeforeAll
    static void initializeServer() {
        MinecraftServer.init();
    }

    @Test
    void readyDyeNeverReplacesThePlayersOwnItem() {
        PlayerInventory inventory = new PlayerInventory();
        inventory.setItemStack(CatacombsItems.READY_SLOT, SWORD);

        CatacombsItems.showReady(inventory, false);
        CatacombsItems.showReady(inventory, true);

        assertEquals(SWORD, inventory.getItemStack(CatacombsItems.READY_SLOT));
        assertEquals(1, readyItems(inventory));

        CatacombsItems.remove(inventory);
        assertEquals(SWORD, inventory.getItemStack(CatacombsItems.READY_SLOT));
        assertEquals(0, readyItems(inventory));
    }

    @Test
    void readyDyeUsesItsSlotWhenFree() {
        PlayerInventory inventory = new PlayerInventory();
        CatacombsItems.showReady(inventory, false);
        assertTrue(CatacombsItems.isReadyItem(inventory.getItemStack(CatacombsItems.READY_SLOT)));
    }

    @Test
    void fullInventoryGetsNoDyeAndLosesNothing() {
        PlayerInventory inventory = new PlayerInventory();
        for (int slot = 0; slot < 36; slot++) inventory.setItemStack(slot, SWORD);

        CatacombsItems.showReady(inventory, false);

        assertEquals(0, readyItems(inventory));
        for (int slot = 0; slot < 36; slot++) assertFalse(inventory.getItemStack(slot).isAir());
    }

    private static int readyItems(PlayerInventory inventory) {
        int count = 0;
        for (int slot = 0; slot < 36; slot++) {
            if (CatacombsItems.isReadyItem(inventory.getItemStack(slot))) count++;
        }
        return count;
    }
}
