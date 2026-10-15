package dev.daweapon.type.bedwarslobby.gui.cosmetics;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

// Hub GUI for the Slumber Hotel feature: guide, quest log, and inventory entry points.
public class GUITheSlumberHotel extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("The Slumber Hotel", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.backOrClose(layout, 31, ctx);

        layout.slot(10, ItemStacks.item(Material.SAND, """
                <3>Hotel Guide

                <7>The <3>Slumber Hotel <7>exists in a unique
                <7>dimension between the waking world
                <7>and the dream world.

                <e>Click to learn more!"""), (_, c) -> c.push(new GUIHotelGuide()));
        layout.slot(12, ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Quest Log

                <7>Keep track of what quests you've
                <7>started, completed, or haven't even
                <7>found yet!

                <e>Click to open!"""));
        layout.slot(14, ItemStacks.item(Material.CHEST, """
                <a>Slumber Inventory

                <7>Collect <e>Slumber Tickets <7>and various
                <a>Slumber Items <7>by playing Bed Wars!

                <e>Click to open!"""));
        layout.slot(16, ItemStacks.item(Material.RED_STAINED_GLASS, """
                <c>The Dreamfeast
                <8>You'll need to talk to the Hotel Owner
                <8>first..."""));
    }
}
