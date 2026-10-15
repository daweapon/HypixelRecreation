package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;

// Developer/cheat GUI: general utilities on the first row, one button per category on the second, and loose buttons that belong to no category yet on the third
public final class GUICentauriToyBox implements View<GUICentauriToyBox.State> {
    private static final int FIRST_CATEGORY_INDEX = ToyBoxGrid.COLUMNS;
    private static final int FIRST_UNCATEGORISED_INDEX = ToyBoxGrid.COLUMNS * 2;

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Toy Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(4, (s, c) -> GUICentauri.item("<a>Toy Box", Material.CHEST, "<7>A variety of useful and cheaty", "<7>utilities to make your life easier!"));
        ToyBoxGrid.place(layout, ToyBoxButtons.general(), 0);
        ToyBoxCategory[] categories = ToyBoxCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ToyBoxCategory category = categories[i];
            layout.slot(ToyBoxGrid.slot(FIRST_CATEGORY_INDEX + i), (s, c) -> GUICentauri.item("<a>" + category.title(), category.icon(),
                            "<7>" + category.description(), " ", "<7>Buttons: <b>" + category.buttons().size(), " ", "<e>Click to view!"),
                    (click, c) -> c.push(new GUICentauriToyBoxCategory(), new GUICentauriToyBoxCategory.State(category)));
        }
        ToyBoxGrid.place(layout, ToyBoxButtons.uncategorised(), FIRST_UNCATEGORISED_INDEX);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    public record State() {}
}
