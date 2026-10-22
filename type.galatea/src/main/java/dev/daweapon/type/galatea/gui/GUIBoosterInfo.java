package dev.daweapon.type.galatea.gui;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.foraging.booster.BoosterSlot;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public class GUIBoosterInfo extends StatelessView {
    private static final int[] GRID = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Booster Info", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        layout.slot(4, ItemStacks.item(Material.ANVIL, """
                <a>Boosters
                <7>There are <a>5 <7>different <9>Boosters<7>, all
                <7>of which can be applied
                <7>simultaneously to any <2>Foraging Gear
                <7>item.

                <7>This menu displays all items that can
                <7>have a <9>Booster <7>applied to them."""));
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<String> ids = new ArrayList<>();
        for (BoosterSlot slot : BoosterSlot.values()) ids.addAll(slot.items().stream().sorted().toList());
        int index = 0;
        for (String id : ids) {
            ItemType type = ItemType.get(id);
            if (type == null || index >= GRID.length) continue;
            layout.slot(GRID[index++], PlayerItemUpdater.playerUpdate(player, new SkyBlockItem(type).getItemStack()));
        }
    }
}
