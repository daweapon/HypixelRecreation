package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

public final class GUICentauriToyBoxCategory implements View<GUICentauriToyBoxCategory.State> {
    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Toy Box - " + state.category().title(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        ToyBoxCategory category = state.category();
        layout.slot(4, (s, c) -> GUICentauri.item("<a>" + category.title(), category.icon(), "<7>" + category.description()));
        ToyBoxGrid.place(layout, category.buttons(), 0);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    public record State(ToyBoxCategory category) {}
}
