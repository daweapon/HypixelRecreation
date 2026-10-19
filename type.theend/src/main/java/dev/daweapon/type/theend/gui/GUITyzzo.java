package dev.daweapon.type.theend.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIEssenceShop;

public class GUITyzzo extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Tyzzo", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 22);
        layout.slot(11, ItemStacks.item(Material.DRAGON_HEAD, """
                <d>Dragon Essence Shop
                <7>Spend <d>Dragon Essence <7>on perks
                <7>that help in The End.

                <e>Click to open!"""), (_, c) -> c.push(new GUIEssenceShop(EssenceType.DRAGON)));
        layout.slot(15, ItemStacks.item(Material.CRYING_OBSIDIAN, """
                <5>Draconic Altar
                <7>Sacrifice dragon armor and Ender
                <7>gear for <d>Dragon Essence<7>.

                <e>Click to open!"""), (_, c) -> c.push(new GUIDraconicAltar()));
    }
}
