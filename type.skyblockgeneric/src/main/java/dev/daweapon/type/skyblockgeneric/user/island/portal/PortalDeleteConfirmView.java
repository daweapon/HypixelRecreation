package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;

import java.util.function.Consumer;

public class PortalDeleteConfirmView extends StatelessView {
    private final PortalDestination destination;
    private final Consumer<SkyBlockPlayer> onConfirm;

    public PortalDeleteConfirmView(PortalDestination destination, Consumer<SkyBlockPlayer> onConfirm) {
        this.destination = destination;
        this.onConfirm = onConfirm;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Delete Portal?", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.LIME_TERRACOTTA, """
                        <a>Delete Portal
                        <7>Remove the portal to %s<7>.
                        <c>You will not get the portal back!

                        <e>Click to delete!""".formatted(destination.label())),
                (_, c) -> {
                    c.player().closeInventory();
                    onConfirm.accept((SkyBlockPlayer) c.player());
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"),
                (_, c) -> c.player().closeInventory());
    }
}
