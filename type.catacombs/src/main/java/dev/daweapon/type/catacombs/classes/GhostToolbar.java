package dev.daweapon.type.catacombs.classes;

import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A ghost's hotbar: Haunt in slot 1 and the class's two ghost abilities in slots 4 and 6, as on Hypixel. Whatever
 * sat in those slots is moved to a free inventory slot, never removed, so a save while the player is a ghost still
 * holds every item; it moves back on revive. A slot whose item has nowhere to go keeps it and gets no ghost item.
 */
public final class GhostToolbar {
    private static final int[] SLOTS = {ClassItems.HAUNT_SLOT, ClassItems.GHOST_FIRST_SLOT, ClassItems.GHOST_SECOND_SLOT};
    private static final int HOTBAR = 9;
    private static final int INVENTORY = 36;
    private static final int MENU_SLOT = 8;

    private final Map<UUID, Map<Integer, Integer>> moved = new ConcurrentHashMap<>();

    public void show(Player player, DungeonClassType type) {
        PlayerInventory inventory = player.getInventory();
        Map<Integer, Integer> displaced = new HashMap<>();
        for (int slot : SLOTS) {
            ItemStack current = inventory.getItemStack(slot);
            if (!current.isAir() && !isToolbarItem(current)) {
                int free = freeSlot(inventory);
                if (free < 0) continue;
                inventory.setItemStack(free, current);
                displaced.put(slot, free);
            }
            inventory.setItemStack(slot, itemFor(slot, type));
        }
        moved.put(player.getUuid(), displaced);
    }

    public void hide(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int slot : SLOTS) {
            if (isToolbarItem(inventory.getItemStack(slot))) inventory.setItemStack(slot, ItemStack.AIR);
        }
        Map<Integer, Integer> displaced = moved.remove(player.getUuid());
        if (displaced == null) return;
        displaced.forEach((home, now) -> {
            ItemStack item = inventory.getItemStack(now);
            if (item.isAir() || isToolbarItem(item) || !inventory.getItemStack(home).isAir()) return;
            inventory.setItemStack(home, item);
            inventory.setItemStack(now, ItemStack.AIR);
        });
    }

    public void forget(UUID player) {
        moved.remove(player);
    }

    private static ItemStack itemFor(int slot, DungeonClassType type) {
        if (slot == ClassItems.HAUNT_SLOT) return ClassItems.haunt();
        return ClassItems.ghost(type, slot == ClassItems.GHOST_FIRST_SLOT ? 0 : 1);
    }

    private static int freeSlot(PlayerInventory inventory) {
        for (int slot = HOTBAR; slot < INVENTORY; slot++) {
            if (inventory.getItemStack(slot).isAir()) return slot;
        }
        for (int slot = 0; slot < HOTBAR; slot++) {
            if (slot == MENU_SLOT || isToolbarSlot(slot)) continue;
            if (inventory.getItemStack(slot).isAir()) return slot;
        }
        return -1;
    }

    private static boolean isToolbarItem(ItemStack stack) {
        String kind = ClassItems.kind(stack);
        return ClassItems.HAUNT.equals(kind) || ClassItems.GHOST_FIRST.equals(kind) || ClassItems.GHOST_SECOND.equals(kind);
    }

    private static boolean isToolbarSlot(int slot) {
        for (int each : SLOTS) if (each == slot) return true;
        return false;
    }
}
