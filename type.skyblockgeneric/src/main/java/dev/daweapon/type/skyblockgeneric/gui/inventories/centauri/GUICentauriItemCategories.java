package dev.daweapon.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import dev.daweapon.type.generic.gui.v2.Components;
import dev.daweapon.type.generic.gui.v2.View;
import dev.daweapon.type.generic.gui.v2.ViewConfiguration;
import dev.daweapon.type.generic.gui.v2.ViewLayout;
import dev.daweapon.type.generic.gui.v2.context.ViewContext;
import dev.daweapon.type.skyblockgeneric.gui.inventories.GUICreative;

public final class GUICentauriItemCategories implements View<GUICentauriItemCategories.State> {
    static final int[] CATEGORY_SLOTS = {11, 13, 15, 20, 22, 24};

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Item List", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        ItemCategory[] categories = ItemCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ItemCategory category = categories[i];
            layout.slot(CATEGORY_SLOTS[i], (s, c) -> GUICentauri.item("<a>" + category.getTitle(), category.getIcon(),
                            "<7>Browse " + category.getDescription() + ".", " ",
                            "<7>Items: <b>" + ItemCategory.items(category).size(), " ", "<e>Click to view!"),
                    (click, c) -> c.push(new GUICreative(), GUICreative.createInitialState(category, "", 0)));
        }
        Components.backOrClose(layout, 31, ctx);
    }

    public record State() {}
}
