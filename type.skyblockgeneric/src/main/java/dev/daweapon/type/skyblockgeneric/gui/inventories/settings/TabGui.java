package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

final class TabGui {
    static final int[] GRID = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private TabGui() {
    }

    static String status(boolean on) {
        return on ? "<a>ENABLED" : "<c>DISABLED";
    }

    static ItemStack.Builder toggle(String name, String description, boolean on) {
        return ItemStacks.item(on ? Material.LIME_DYE : Material.GRAY_DYE,
                (on ? "<a>" : "<c>") + name + "\n" + description
                        + "\n\n<7>Currently: " + status(on)
                        + "\n<e>Click to " + (on ? "disable" : "enable") + "!");
    }
}
