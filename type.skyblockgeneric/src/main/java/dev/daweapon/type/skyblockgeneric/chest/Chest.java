package dev.daweapon.type.skyblockgeneric.chest;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;

import java.util.List;

// Common contract for a placeable, item-storing chest (single or double) in the world.
public interface Chest {

    String getName();

    Instance getInstance();

    Point[] getPosition();

    ChestType getType();

    void setItem(int slot, ItemStack stack);

    void removeItem(int slot);

    ItemStack getItem(int slot);

    List<ItemStack> getItems();

    InventoryType getSize();

    void update();

}
