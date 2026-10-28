package dev.daweapon.type.skyblockgeneric.gui.inventories.hunting;

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

public class GUIMassSyphonConfirm extends StatelessView {
    private final int shards;
    private final int attributes;
    private final Consumer<SkyBlockPlayer> onConfirm;

    public GUIMassSyphonConfirm(int shards, int attributes, Consumer<SkyBlockPlayer> onConfirm) {
        this.shards = shards;
        this.attributes = attributes;
        this.onConfirm = onConfirm;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Confirm Mass Syphon", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(11, (_, _) -> ItemStacks.item(Material.LIME_TERRACOTTA, """
                        <a>Confirm
                        <7>Syphon <c>%d Shards <7>from <a>%d <7>Attributes.
                        <c>This cannot be undone!

                        <e>Click to confirm!""".formatted(shards, attributes)),
                (_, c) -> {
                    c.player().closeInventory();
                    onConfirm.accept((SkyBlockPlayer) c.player());
                });
        layout.slot(15, (_, _) -> ItemStacks.item(Material.RED_TERRACOTTA, "<c>Cancel"),
                (_, c) -> c.player().closeInventory());
    }
}
