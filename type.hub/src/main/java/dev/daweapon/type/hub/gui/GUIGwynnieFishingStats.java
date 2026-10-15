package dev.daweapon.type.hub.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

public class GUIGwynnieFishingStats extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fishing Stats", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);

        layout.slot(19, ItemStacks.item(Material.FISHING_ROD, """
                <b>☂ Fishing Speed
                <7>Fishing Speed reduces the quantity
                <7>of temporal units that may generally
                <7>occur before the event of 'catching
                <7>a fish'.

                <7>Stat Cap: <b>300☂

                <7>The cap increases to <b>350☂ <7>on the
                <c>Crimson Isle<7>!"""));
        layout.slot(21, ItemStacks.item(Material.PRISMARINE_CRYSTALS, """
                <3>α Sea Creature Chance
                <7>Sea Creature Chance is your chance
                <7>to catch Sea Creatures while fishing."""));
        layout.slot(23, ItemStacks.item(Material.CHEST, """
                <6>⛃ Treasure Chance
                <7>Increases your chance to catch
                <6>Treasure <7>while fishing.

                <7>Stat Cap: <6>100%⛃"""));
        layout.slot(25, ItemStacks.item(Material.COOKED_COD, """
                <9>⚓ Double Hook Chance
                <7>Chance to catch two Sea Creatures
                <7>at once.

                <7>Stat Cap: <9>100%⚓"""));
    }
}
