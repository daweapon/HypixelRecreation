package dev.daweapon.type.bedwarsgame.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

// Hub GUI linking to the Quick Communications and enemy tracker purchase menus.
public class GUITrackerAndCommunication extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Tracker & Communication", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.slot(11, ItemStacks.item(Material.EMERALD, """
            <a>Quick Communications
            <7>Send highlighted chat messages to
            <7>your teammates!

            <e>Click to open!"""), (_, context) -> context.push(new GUIQuickCommunications()));
        layout.slot(15, ItemStacks.item(Material.COMPASS, """
            <a>Tracker Shop
            <7>Purchase tracking upgrade for your
            <7>compass which will track each player
            <7>on a specific team until you die.

            <e>Click to open!"""), (_, context) -> context.push(new GUIPurchaseEnemyTracker()));
    }
}
