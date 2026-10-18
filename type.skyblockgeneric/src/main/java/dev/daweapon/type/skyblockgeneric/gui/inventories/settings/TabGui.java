package dev.daweapon.type.skyblockgeneric.gui.inventories.settings;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.skyblockgeneric.tab.SkyBlockTablistManager;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabSettings;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

final class TabGui {
    static final int[] GRID = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };
    static final int[] WIDGET_GRID = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    static final int[] PREVIEW_SLOTS = {3, 4, 5};
    static final int THIRD_COLUMN_SLOT = 50;
    static final int BACK_SLOT = 48;
    static final int CLOSE_SLOT = 49;
    static final int RESET_SLOT = 53;

    private TabGui() {
    }

    static String status(boolean on) {
        return on ? "<a>ENABLED" : "<c>DISABLED";
    }

    static ItemStack.Builder toggle(String name, String description, boolean on) {
        return ItemStacks.item(on ? Material.LIME_DYE : Material.GRAY_DYE,
                (on ? "<a>\u2714 " : "<c>\u2716 ") + name + "\n" + description
                        + "\n\n<e>Click to " + (on ? "disable" : "enable") + "!");
    }

    static ItemStack.Builder preview(SkyBlockPlayer player, TabLocation location) {
        List<Text> lines = SkyBlockTablistManager.previewLines(player, location, TabSettings.of(player));
        return ItemStacks.item(Material.PLAYER_HEAD, Text.of("<a>{} Widgets Preview", location.getDisplayName()), lines);
    }
}
