package dev.daweapon.type.hub.gui.catacombs;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.hub.util.UnreleasedContent;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIDungeonEssenceShops;

public class GUIDungeonBlacksmith extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dungeon Blacksmith", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        UnreleasedContent.DUNGEONS.slot(layout, 20, Material.BEACON, """
                <a>Salvage Items
                <7>Salvage weapons and armor for
                <7>materials.""");
        UnreleasedContent.DUNGEONS.slot(layout, 22, Material.RED_DYE, """
                <a>Essence Crafting
                <7>Upgrade items or convert regular
                <7>items into Dungeon items using
                <d>Essence<7>!""");
        layout.slot(24, ItemStacks.item(Material.WITHER_SKELETON_SKULL, """
                <a>Essence Shop
                <7>Exchange <d>Undead Essence <7>and
                <d>Wither Essence <7>for items and
                <7>perks!

                <e>Click to view!"""), (_, c) -> c.push(new GUIDungeonEssenceShops()));
    }
}
