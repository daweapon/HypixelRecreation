package dev.daweapon.type.skyblockgeneric.gui.inventories.mineshaft;

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

public class GUIMineshaftPortal extends StatelessView {
    public static final int ENTER_SLOT = 13;
    public static final int CLOSE_SLOT = 31;

    private final Consumer<SkyBlockPlayer> enter;

    public GUIMineshaftPortal(Consumer<SkyBlockPlayer> enter) {
        this.enter = enter;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Glacite Mineshaft", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(ENTER_SLOT, ItemStacks.item(Material.IRON_PICKAXE, """
                <a>Enter Mineshaft
                <7>Adventure through the depths of
                <7>the <b>Glacite Mineshafts<7>.

                <e>Click to enter!"""), (_, c) -> {
            c.player().closeInventory();
            enter.accept((SkyBlockPlayer) c.player());
        });
        layout.slot(CLOSE_SLOT, ItemStacks.item(Material.BARRIER, "<c>Close"), (_, c) -> c.player().closeInventory());
    }
}
