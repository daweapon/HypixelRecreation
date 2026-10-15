package dev.daweapon.type.hub.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.hub.util.UnreleasedContent;

public class GUIDimensionalInfusion extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Dimensional Infusion", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        UnreleasedContent.RIFT.slot(layout, 13, Material.LILAC, """
                <d>Dimensional Infusion
                <7>Infuse yourself with <b>magical protection
                <7>pioneered by <9>Barry <7>which lets you step into
                <7>the <d>wizard portal<7>.

                <a>Good to know
                <8> ⦾ <7>Items <c>don't <7>cross with you.
                <8> ⦾ <7>Items <a>do <7>save on the other side.
                <8> ⦾ <7>Your <a>Time <7>will be limited.
                <8> ⦾ <7>Earn the best <d>Necklace <7>in the world.

                <7>Cost
                <a><l>FREE! <a>(Every 4 hours)""");
        UnreleasedContent.RIFT.slot(layout, 30, Material.GRAY_DYE, """
                <a>Auto-Buy Infusion
                <7>Automatically purchase the
                <d>Dimensional Infusion <7>when
                <7>transferring to the rift, either for
                <7>free or using <b>bits<7>.""");
    }
}
