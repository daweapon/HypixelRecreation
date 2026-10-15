package dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.inventory.ItemStacks;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.DefaultState;
import dev.daweapon.type.generic.gui.v2.StatelessView;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Consumer;

/** Drakuu's Golden Ball trade. The wiki names the button; the layout is an estimate. */
public class GUIConfirmDrakuuTrade extends StatelessView {
    private final Consumer<SkyBlockPlayer> onConfirm;

    public GUIConfirmDrakuuTrade(Consumer<SkyBlockPlayer> onConfirm) {
        this.onConfirm = onConfirm;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Drakuu", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.LIME_TERRACOTTA, """
                        <a>Confirm Drakuu Trade
                        <7>Give Drakuu your <6>Golden Ball<7>.

                        <e>Click to confirm!"""),
                (_, c) -> {
                    c.player().closeInventory();
                    onConfirm.accept((SkyBlockPlayer) c.player());
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"),
                (_, c) -> c.player().closeInventory());
    }
}
