package dev.daweapon.type.bedwarsgame.gui;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

// Placeholder tracker shop GUI shell with just a back button.
public class TrackerShopView extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((_, _) -> "Tracker Shop", InventoryType.CHEST_6_ROW);
    }


    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.back(layout, 49, ctx);
    }

}
